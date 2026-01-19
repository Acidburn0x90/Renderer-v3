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
    
    // Global list of triangles to render this frame.
    // We collect ALL valid triangles from ALL meshes here so we can sort them globally.
    private final List<Triangle> trianglesToRaster = new ArrayList<>();

    public Renderer(Screen screen) {
        this.screen = screen;
        // Initialize Projection Matrix (90 FOV, Aspect Ratio, Near 0.1, Far 1000.0)
        this.projectionMatrix = Matrix4x4.makeProjection(90.0, (double)screen.getHeight() / screen.getWidth(), 0.1, 1000.0);
    }

    public void updateProjection(int width, int height) {
        this.projectionMatrix = Matrix4x4.makeProjection(90.0, (double)height / width, 0.1, 1000.0);
    }

    /**
     * Clears the screen and resets the triangle buffer. Call this at the start of render().
     */
    public void beginFrame() {
        screen.clearPixels("#000000"); // Clear to Black
        trianglesToRaster.clear();
    }

    /**
     * Processes a mesh: Transforms, Clips, Culls, and adds visible triangles to the buffer.
     * Does NOT draw them yet.
     */
    public void renderMesh(Mesh mesh, Camera camera) {
        // Create Rotation Matrices based on Camera Orientation
        // We rotate the world OPPOSITE to the camera rotation to simulate looking around.
        Matrix4x4 matRotY = Matrix4x4.rotationY(-camera.yaw);
        Matrix4x4 matRotX = Matrix4x4.rotationX(-camera.pitch);

        for (Triangle tri : mesh.triangles) {
            // Temporary triangles for pipeline stages
            Triangle triTranslated = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
            triTranslated.color = tri.color;
            
            Triangle triRotatedYaw = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
            triRotatedYaw.color = tri.color;
            
            Triangle triView = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
            triView.color = tri.color;

            // 1. TRANSLATION (View Space)
            // Move the world so the camera is at (0,0,0).
            for (int i = 0; i < 3; i++) {
                triTranslated.v[i] = new Vector3D(
                    tri.v[i].x - camera.position.x,
                    tri.v[i].y - camera.position.y,
                    tri.v[i].z - camera.position.z
                );
            }

            // 2. ROTATION (View Space)
            // Rotate vertices based on Camera Yaw and Pitch.
            for (int i = 0; i < 3; i++) {
                triRotatedYaw.v[i] = matRotY.multiplyVector(triTranslated.v[i]);
                triView.v[i] = matRotX.multiplyVector(triRotatedYaw.v[i]);
            }

            // 3. CLIP (Near Plane)
            // Use the Sutherland-Hodgman algorithm to slice triangles against the Near Plane (Z=0.1).
            // This prevents the "disappearing ground" bug when walking.
            List<Triangle> clippedTriangles = clipTriangleAgainstPlane(
                new Vector3D(0, 0, 0.1), // Plane Point
                new Vector3D(0, 0, 1),   // Plane Normal
                triView
            );

            for (Triangle clipped : clippedTriangles) {
                // 4. CULL (Backface Culling)
                // Determine if the triangle is facing the camera.
                // Calculate Surface Normal.
                Vector3D line1 = clipped.v[1].subtract(clipped.v[0]);
                Vector3D line2 = clipped.v[2].subtract(clipped.v[0]);
                Vector3D normal = line1.crossProduct(line2).normalize();
                
                // Vector from Camera (0,0,0) to Triangle.
                Vector3D cameraRay = clipped.v[0].subtract(new Vector3D(0,0,0));

                // Dot Product < 0 means the normal is pointing towards the camera.
                if (normal.dotProduct(cameraRay) < 0.0f) {
                    // Triangle is visible, add to list for sorting later.
                    trianglesToRaster.add(clipped);
                }
            }
        }
    }

    /**
     * Sorts the accumulated triangles and rasterizes them to the screen. Call this at the end of render().
     */
    public void endFrame(Camera camera) {
        // 5. SORT: Painter's Algorithm
        // Sort triangles from Farthest to Nearest (Z-depth) so near triangles draw on top of far ones.
        Collections.sort(trianglesToRaster, new Comparator<Triangle>() {
            @Override
            public int compare(Triangle t1, Triangle t2) {
                double z1 = (t1.v[0].z + t1.v[1].z + t1.v[2].z) / 3.0;
                double z2 = (t2.v[0].z + t2.v[1].z + t2.v[2].z) / 3.0;
                return Double.compare(z2, z1); 
            }
        });

        // FIXED SUN LIGHTING SETUP
        // We define the sun direction in WORLD SPACE.
        Vector3D worldLightDir = new Vector3D(0.2, 1.5, -0.5); 
        worldLightDir = worldLightDir.normalize();
        
        // We must rotate the Light Direction into VIEW SPACE to match the rotated triangles.
        // e.g. If the camera rotates Left, the Sun (relative to camera) must rotate Right.
        Matrix4x4 matRotY = Matrix4x4.rotationY(-camera.yaw);
        Matrix4x4 matRotX = Matrix4x4.rotationX(-camera.pitch);
        
        Vector3D viewLightDir = matRotY.multiplyVector(worldLightDir);
        viewLightDir = matRotX.multiplyVector(viewLightDir);

        // 6. PROJECT & RASTERIZE
        for (Triangle triView : trianglesToRaster) {
            Triangle triProjected = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));

            // --- LIGHTING CALCULATION (View Space) ---
            Vector3D line1 = triView.v[1].subtract(triView.v[0]);
            Vector3D line2 = triView.v[2].subtract(triView.v[0]);
            Vector3D normal = line1.crossProduct(line2).normalize();
            
            // Dot Product of Surface Normal and Light Direction.
            // 1.0 = Facing Light directly. 0.0 = Perpendicular. -1.0 = Facing away.
            double dp = normal.dotProduct(viewLightDir);
            
            // Clamp brightness (Ambient light 0.4 ensures nothing is pitch black).
            double brightness = Math.max(0.4, dp); 
            
            // Apply lighting to the Triangle's hex color
            int baseColor = triView.color;
            int r = (baseColor >> 16) & 0xFF;
            int g = (baseColor >> 8) & 0xFF;
            int b = baseColor & 0xFF;
            
            r = (int)(r * brightness);
            g = (int)(g * brightness);
            b = (int)(b * brightness);
            
            r = Math.min(255, Math.max(0, r));
            g = Math.min(255, Math.max(0, g));
            b = Math.min(255, Math.max(0, b));
            
            int color = (r << 16) | (g << 8) | b;

            // --- PROJECTION ---
            // Matrix Multiply: View Space (3D) -> Projection Space (Homogeneous Clip Space)
            triProjected.v[0] = projectionMatrix.multiplyVector(triView.v[0]);
            triProjected.v[1] = projectionMatrix.multiplyVector(triView.v[1]);
            triProjected.v[2] = projectionMatrix.multiplyVector(triView.v[2]);

            // --- SCALE TO SCREEN ---
            // Convert normalized coordinates (-1 to +1) to pixel coordinates (0 to Width/Height).
            for (int i = 0; i < 3; i++) {
                triProjected.v[i].x = (triProjected.v[i].x + 1.0) * 0.5 * screen.getWidth();
                triProjected.v[i].y = (triProjected.v[i].y + 1.0) * 0.5 * screen.getHeight();
            }

            // --- DRAW ---
            screen.fillTriangle(
                (int)triProjected.v[0].x, (int)triProjected.v[0].y,
                (int)triProjected.v[1].x, (int)triProjected.v[1].y,
                (int)triProjected.v[2].x, (int)triProjected.v[2].y,
                color
            );
        }
    }

    /**
     * Clips a triangle against a plane.
     * Returns 0, 1, or 2 triangles.
     */
    private List<Triangle> clipTriangleAgainstPlane(Vector3D planePoint, Vector3D planeNormal, Triangle inTri) {
        List<Triangle> outTriangles = new ArrayList<>();
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
        
        if (insideCount == 0) return outTriangles; // All outside
        if (insideCount == 3) {
            outTriangles.add(inTri); // All inside
            return outTriangles;
        }
        
        // 2. Classify and Reorder Vertices
        // We rotate the vertices so that:
        // - If 1 point is inside, it is at index 0.
        // - If 2 points are inside, the ONE outside point is at index 2.
        
        Vector3D[] v = new Vector3D[]{ inTri.v[0], inTri.v[1], inTri.v[2] };
        double[] d = new double[]{ dist[0], dist[1], dist[2] };
        
        if (insideCount == 1) {
            // Cycle until v[0] is the inside point
            while (d[0] < 0) {
                Vector3D tv = v[0]; v[0] = v[1]; v[1] = v[2]; v[2] = tv;
                double td = d[0]; d[0] = d[1]; d[1] = d[2]; d[2] = td;
            }
            
            // New Triangle: Inside, Intersect(In->Out1), Intersect(In->Out2)
            Vector3D p1 = v[0];
            Vector3D p2 = intersectPlane(planePoint, planeNormal, v[0], v[1]);
            Vector3D p3 = intersectPlane(planePoint, planeNormal, v[0], v[2]);
            
            outTriangles.add(new Triangle(p1, p2, p3, inTri.color));
            
        } else if (insideCount == 2) {
            // Cycle until v[2] is the outside point
            while (d[2] >= 0) {
                Vector3D tv = v[0]; v[0] = v[1]; v[1] = v[2]; v[2] = tv;
                double td = d[0]; d[0] = d[1]; d[1] = d[2]; d[2] = td;
            }
            
            // Quad formed by 2 Triangles
            // Tri 1: In1, In2, Intersect(In2->Out)
            // Tri 2: In1, Intersect(In2->Out), Intersect(In1->Out)
            
            Vector3D p1 = v[0];
            Vector3D p2 = v[1];
            Vector3D p3 = intersectPlane(planePoint, planeNormal, v[1], v[2]);
            Vector3D p4 = intersectPlane(planePoint, planeNormal, v[0], v[2]);
            
            outTriangles.add(new Triangle(p1, p2, p3, inTri.color));
            outTriangles.add(new Triangle(p1, p3, p4, inTri.color));
        }
        
        return outTriangles;
    }
    
    /**
     * Calculates the intersection point of a line segment and a plane.
     */
    private Vector3D intersectPlane(Vector3D planeP, Vector3D planeN, Vector3D lineStart, Vector3D lineEnd) {
        planeN = planeN.normalize();
        double planeD = -planeN.dotProduct(planeP);
        double ad = lineStart.dotProduct(planeN);
        double bd = lineEnd.dotProduct(planeN);
        double t = (-planeD - ad) / (bd - ad);
        Vector3D lineStartToEnd = lineEnd.subtract(lineStart);
        Vector3D lineToIntersect = lineStartToEnd.multiply(t);
        return lineStart.add(lineToIntersect);
    }}
