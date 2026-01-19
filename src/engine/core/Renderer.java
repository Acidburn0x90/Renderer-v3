package engine.core;

import engine.graphics.Screen;
import engine.math.Matrix4x4;
import engine.math.Mesh;
import engine.math.Triangle;
import engine.math.Vector3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Handles the 3D rendering pipeline.
 * <p>
 * This class is responsible for the full "Software Rasterization" pipeline.
 * It takes 3D Meshes and converts them into 2D colored pixels on the Screen.
 * </p>
 * 
 * <h3>The Pipeline Steps:</h3>
 * <ol>
 *     <li><b>Transform</b>: Convert Model Space -> World Space -> View Space (Camera relative).</li>
 *     <li><b>Clip</b>: Remove triangles that are behind the camera (Near Plane Clipping).</li>
 *     <li><b>Cull</b>: Ignore triangles facing away from the camera (Backface Culling).</li>
 *     <li><b>Project</b>: Convert 3D View Space -> 2D Screen Space (Perspective Projection).</li>
 *     <li><b>Sort</b>: Sort triangles by depth (Painter's Algorithm) to draw far objects first.</li>
 *     <li><b>Rasterize</b>: Fill the 2D triangles with color.</li>
 * </ol>
 */
public class Renderer {
    private final Screen screen;
    private Matrix4x4 projectionMatrix;
    
    // --- CACHE (Object Pool) ---
    // Reusable objects to prevent Garbage Collection spikes.
    private final Triangle triTranslated = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
    private final Triangle triRotatedYaw = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
    private final Triangle triView = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
    private final Triangle triProjected = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
    
    // Clipping Pool
    private final List<Triangle> clippedTrianglesOut = new ArrayList<>(); // Reusable list
    private final Triangle triClipped1 = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
    private final Triangle triClipped2 = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
    
    // Helper vectors for math
    private final Vector3D vLine1 = new Vector3D(0,0,0);
    private final Vector3D vLine2 = new Vector3D(0,0,0);
    private final Vector3D vNormal = new Vector3D(0,0,0);
    private final Vector3D vCameraRay = new Vector3D(0,0,0);
    
    // Clipping planes are static/constant for now
    private final Vector3D planePoint = new Vector3D(0, 0, 0.1);
    private final Vector3D planeNormal = new Vector3D(0, 0, 1);
    
    // Frustum Culling
    private final ViewFrustum frustum = new ViewFrustum();

    public Renderer(Screen screen) {
        this.screen = screen;
        // Initialize Projection Matrix (90 FOV, Aspect Ratio, Near 0.1, Far 1000.0)
        this.projectionMatrix = Matrix4x4.makeProjection(90.0, (double)screen.getHeight() / screen.getWidth(), 0.1, 1000.0);
    }

    public void updateProjection(int width, int height) {
        this.projectionMatrix = Matrix4x4.makeProjection(90.0, (double)height / width, 0.1, 1000.0);
    }

    /**
     * Clears the screen with a sky gradient and resets the Z-Buffer. 
     * Call this at the start of render().
     */
    public void beginFrame() {
        // Deep Sky Blue to Horizon Light Blue
        screen.drawSky(0x000033, 0x87CEEB); 
        screen.clearZBuffer(); // Reset Depth
    }

    /**
     * Processes a mesh: Transforms, Clips, Lights, Projects, and Draws directly to Screen.
     */
    public void renderMesh(Mesh mesh, Camera camera) {
        // --- PREPARE MATRICES ---
        // 1. Build View Matrix
        // To move the world relative to the camera, we do:
        // Translate(-CamPos) * RotateY(-CamYaw) * RotateX(-CamPitch)
        // Note: We build this manually here for the Frustum Update.
        // We still use the manual optimized path for vertices below to save allocations.
        
        Matrix4x4 matTrans = Matrix4x4.translation(-camera.position.x, -camera.position.y, -camera.position.z);
        Matrix4x4 matRotY = Matrix4x4.rotationY(-camera.yaw);
        Matrix4x4 matRotX = Matrix4x4.rotationX(-camera.pitch);
        
        // View = Trans * RotY * RotX (Row Vector Convention: v * M)
        // This matches the manual loop: v.sub(pos) -> rotY -> rotX
        Matrix4x4 matView = matTrans.multiply(matRotY).multiply(matRotX);
        
        // 2. Build View-Projection Matrix for Culling
        Matrix4x4 matViewProj = matView.multiply(projectionMatrix);
        
        // 3. Update Frustum
        frustum.update(matViewProj);

        // --- 0. FRUSTUM CULLING ---
        // Check if the mesh is completely outside the visible frustum
        if (frustum.isSphereOutside(mesh.center, mesh.radius)) {
            return; // Skip this mesh entirely
        }

        // FIXED SUN LIGHTING SETUP (World Space)
        Vector3D worldLightDir = new Vector3D(0.5, 1.0, -0.2).normalize();
        
        // Rotate Sun into View Space
        Vector3D viewLightDir = matRotY.multiplyVector(worldLightDir);
        viewLightDir = matRotX.multiplyVector(viewLightDir);

        for (Triangle tri : mesh.triangles) {
            
            // 1. TRANSLATION (View Space)
            // Move vertices relative to camera
            for (int i = 0; i < 3; i++) {
                triTranslated.v[i].set(tri.v[i]);
                triTranslated.v[i].subtractInPlace(camera.position);
            }

            // 2. ROTATION (View Space)
            // Apply Camera Rotations
            for (int i = 0; i < 3; i++) {
                matRotY.multiplyVector(triTranslated.v[i], triRotatedYaw.v[i]);
                matRotX.multiplyVector(triRotatedYaw.v[i], triView.v[i]);
            }
            triView.color = tri.color;

            // 3. CLIP (Near Plane)
            // This fills 'clippedTrianglesOut' with 0, 1, or 2 triangles from our pool.
            clipTriangleAgainstPlane(planePoint, planeNormal, triView);

            for (Triangle clipped : clippedTrianglesOut) {
                // 4. CULL (Backface Culling)
                // Calculate Normal: (v1-v0) x (v2-v0)
                vLine1.set(clipped.v[1]); vLine1.subtractInPlace(clipped.v[0]);
                vLine2.set(clipped.v[2]); vLine2.subtractInPlace(clipped.v[0]);
                
                // Cross Product
                vNormal.set(
                    vLine1.y * vLine2.z - vLine1.z * vLine2.y,
                    vLine1.z * vLine2.x - vLine1.x * vLine2.z,
                    vLine1.x * vLine2.y - vLine1.y * vLine2.x
                );
                
                double len = vNormal.length();
                if (len == 0) continue;
                vNormal.multiplyInPlace(1.0 / len); // Normalize

                // Vector from Camera to Triangle (Approximate as v0 since cam is at 0,0,0)
                // v0 - 0 = v0
                vCameraRay.set(clipped.v[0]);

                if (vNormal.dotProduct(vCameraRay) < 0.0f) {
                    
                    // 5. LIGHTING (Improved Contrast)
                    double dp = vNormal.dotProduct(viewLightDir);
                    
                    // Ambient + Diffuse (Wrapped lighting for softer shadows)
                    double ambient = 0.2;
                    double diffuse = Math.max(0, dp);
                    double brightness = ambient + (1.0 - ambient) * diffuse;
                    
                    // Boost contrast slightly
                    brightness = Math.pow(brightness, 1.2); 
                    
                    int baseColor = clipped.color;
                    int r = (int)(((baseColor >> 16) & 0xFF) * brightness);
                    int g = (int)(((baseColor >> 8) & 0xFF) * brightness);
                    int b = (int)((baseColor & 0xFF) * brightness);
                    r = Math.min(255, Math.max(0, r));
                    g = Math.min(255, Math.max(0, g));
                    b = Math.min(255, Math.max(0, b));
                    int finalColor = (r << 16) | (g << 8) | b;

                    // 6. PROJECT & DRAW
                    for (int i = 0; i < 3; i++) {
                        projectionMatrix.multiplyVector(clipped.v[i], triProjected.v[i]);
                        
                        // Scale to Screen
                        triProjected.v[i].x = (triProjected.v[i].x + 1.0) * 0.5 * screen.getWidth();
                        triProjected.v[i].y = (triProjected.v[i].y + 1.0) * 0.5 * screen.getHeight();
                    }

                    // 7. RASTERIZE
                    screen.fillTriangle(
                        (int)triProjected.v[0].x, (int)triProjected.v[0].y, triProjected.v[0].z,
                        (int)triProjected.v[1].x, (int)triProjected.v[1].y, triProjected.v[1].z,
                        (int)triProjected.v[2].x, (int)triProjected.v[2].y, triProjected.v[2].z,
                        finalColor
                    );
                }
            }
        }
    }
    
    // Note: endFrame() is removed as it was only for sorting.

    /**
     * Clips a triangle against a plane using reusable objects.
     * populates 'clippedTrianglesOut' with 0, 1, or 2 triangles.
     */
    private void clipTriangleAgainstPlane(Vector3D planePoint, Vector3D planeNormal, Triangle inTri) {
        clippedTrianglesOut.clear(); // Reset the list
        planeNormal = planeNormal.normalize();
        
        // 1. Calculate distance of each point from the plane
        // dist = (point - planePoint) . normal
        double[] dist = new double[3];
        int insideCount = 0;
        int outsideCount = 0;
        
        for (int i = 0; i < 3; i++) {
            dist[i] = inTri.v[i].subtract(planePoint).dotProduct(planeNormal);
            if (dist[i] >= 0) insideCount++;
            else outsideCount++;
        }
        
        if (insideCount == 0) return; // All outside, return empty
        if (insideCount == 3) {
            clippedTrianglesOut.add(inTri); // All inside, return original
            return;
        }
        
        // 2. Classify and Reorder Vertices
        Vector3D[] v = new Vector3D[]{ inTri.v[0], inTri.v[1], inTri.v[2] };
        double[] d = new double[]{ dist[0], dist[1], dist[2] };
        
        if (insideCount == 1) {
            // Cycle until v[0] is the inside point
            while (d[0] < 0) {
                Vector3D tv = v[0]; v[0] = v[1]; v[1] = v[2]; v[2] = tv;
                double td = d[0]; d[0] = d[1]; d[1] = d[2]; d[2] = td;
            }
            
            // Re-use triClipped1
            // Vertices: Inside, Intersect(In->Out1), Intersect(In->Out2)
            triClipped1.v[0].set(v[0]);
            intersectPlane(planePoint, planeNormal, v[0], v[1], triClipped1.v[1]);
            intersectPlane(planePoint, planeNormal, v[0], v[2], triClipped1.v[2]);
            triClipped1.color = inTri.color;
            
            clippedTrianglesOut.add(triClipped1);
            
        } else if (insideCount == 2) {
            // Cycle until v[2] is the outside point
            while (d[2] >= 0) {
                Vector3D tv = v[0]; v[0] = v[1]; v[1] = v[2]; v[2] = tv;
                double td = d[0]; d[0] = d[1]; d[1] = d[2]; d[2] = td;
            }
            
            // Quad formed by 2 Triangles. Re-use triClipped1 and triClipped2.
            
            // Tri 1: In1, In2, Intersect(In2->Out)
            triClipped1.v[0].set(v[0]);
            triClipped1.v[1].set(v[1]);
            intersectPlane(planePoint, planeNormal, v[1], v[2], triClipped1.v[2]);
            triClipped1.color = inTri.color;
            
            // Tri 2: In1, Intersect(In2->Out), Intersect(In1->Out)
            triClipped2.v[0].set(v[0]);
            triClipped2.v[1].set(triClipped1.v[2]); // Reuse the point calculated above
            intersectPlane(planePoint, planeNormal, v[0], v[2], triClipped2.v[2]);
            triClipped2.color = inTri.color;
            
            clippedTrianglesOut.add(triClipped1);
            clippedTrianglesOut.add(triClipped2);
        }
    }
    
    /**
     * Calculates intersection and stores it in 'out'.
     */
    private void intersectPlane(Vector3D planeP, Vector3D planeN, Vector3D lineStart, Vector3D lineEnd, Vector3D out) {
        planeN = planeN.normalize();
        double planeD = -planeN.dotProduct(planeP);
        double ad = lineStart.dotProduct(planeN);
        double bd = lineEnd.dotProduct(planeN);
        double t = (-planeD - ad) / (bd - ad);
        Vector3D lineStartToEnd = lineEnd.subtract(lineStart);
        Vector3D lineToIntersect = lineStartToEnd.multiply(t);
        
        out.set(lineStart);
        out.addInPlace(lineToIntersect);
    }}
