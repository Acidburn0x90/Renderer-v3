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
 * Responsible for transforming 3D Meshes into 2D pixels on the Screen.
 */
public class Renderer {
    private final Screen screen;
    private Matrix4x4 projectionMatrix;

    public Renderer(Screen screen) {
        this.screen = screen;
        // Default projection, can be updated later
        this.projectionMatrix = Matrix4x4.makeProjection(90.0, (double)screen.getHeight() / screen.getWidth(), 0.1, 1000.0);
    }

    /**
     * Updates the projection matrix (e.g. when window resizes).
     */
    public void updateProjection(int width, int height) {
        this.projectionMatrix = Matrix4x4.makeProjection(90.0, (double)height / width, 0.1, 1000.0);
    }

    /**
     * Renders a mesh relative to a camera.
     */
    public void renderMesh(Mesh mesh, Camera camera) {
        // List to store triangles that should be drawn this frame
        List<Triangle> trianglesToRaster = new ArrayList<>();

        // Create Rotation Matrices for the Camera (Inverse rotation of the world)
        Matrix4x4 matRotY = Matrix4x4.rotationY(-camera.yaw);
        Matrix4x4 matRotX = Matrix4x4.rotationX(-camera.pitch);

        // Process every triangle in the mesh
        for (Triangle tri : mesh.triangles) {
            Triangle triTranslated = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
            Triangle triRotatedYaw = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
            Triangle triView = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));

            // 1. TRANSLATION: World Space -> Camera-Relative Space
            for (int i = 0; i < 3; i++) {
                triTranslated.v[i] = new Vector3D(
                    tri.v[i].x - camera.position.x,
                    tri.v[i].y - camera.position.y,
                    tri.v[i].z - camera.position.z + 3.0 // Offset cube forward
                );
            }

            // 2. ROTATION: Camera-Relative Space -> View Space
            for (int i = 0; i < 3; i++) {
                // Apply Yaw first, then Pitch
                triRotatedYaw.v[i] = matRotY.multiplyVector(triTranslated.v[i]);
                triView.v[i] = matRotX.multiplyVector(triRotatedYaw.v[i]);
            }

            // 3. CLIP: Near Plane Clipping (Basic)
            if (triView.v[0].z < 0.1 || triView.v[1].z < 0.1 || triView.v[2].z < 0.1) {
                continue;
            }

            // 4. CULL: Backface Culling
            Vector3D line1 = triView.v[1].subtract(triView.v[0]);
            Vector3D line2 = triView.v[2].subtract(triView.v[0]);
            Vector3D normal = line1.crossProduct(line2).normalize();
            
            Vector3D cameraRay = triView.v[0].subtract(new Vector3D(0,0,0));

            if (normal.dotProduct(cameraRay) < 0.0f) {
                // Add to list for sorting/rasterizing
                trianglesToRaster.add(triView);
            }
        }

        // 5. SORT: Painter's Algorithm (Sort by average Z, Far -> Near)
        Collections.sort(trianglesToRaster, new Comparator<Triangle>() {
            @Override
            public int compare(Triangle t1, Triangle t2) {
                double z1 = (t1.v[0].z + t1.v[1].z + t1.v[2].z) / 3.0;
                double z2 = (t2.v[0].z + t2.v[1].z + t2.v[2].z) / 3.0;
                return Double.compare(z2, z1); // Descending order
            }
        });

        // 6. PROJECT & RASTERIZE
        Vector3D lightDirection = new Vector3D(0, 0, -1); // Light coming from the camera direction
        lightDirection = lightDirection.normalize();

        for (Triangle triView : trianglesToRaster) {
            Triangle triProjected = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));

            // Lighting Calculation
            // We need the normal again. (Optimization: Store it in a wrapper class with the triangle)
            Vector3D line1 = triView.v[1].subtract(triView.v[0]);
            Vector3D line2 = triView.v[2].subtract(triView.v[0]);
            Vector3D normal = line1.crossProduct(line2).normalize();

            // Dot Product for Diffuse Lighting
            // How much is the face aligned with the light?
            double dp = normal.dotProduct(lightDirection);
            
            // Clamp brightness (Ambient light 0.1 to Max 1.0)
            // Note: Since light is (0,0,-1) and normal points back at us (0,0,-1), dot product is positive 1.
            // If normal points away, it's culled anyway.
            double brightness = Math.max(0.1, dp);
            
            // Apply brightness to white (255, 255, 255)
            int colVal = (int)(255 * brightness);
            // Clamp to 0-255 just in case
            colVal = Math.min(255, Math.max(0, colVal));
            
            // Construct hex color 0xRRGGBB
            int color = (colVal << 16) | (colVal << 8) | colVal;

            // Project
            triProjected.v[0] = projectionMatrix.multiplyVector(triView.v[0]);
            triProjected.v[1] = projectionMatrix.multiplyVector(triView.v[1]);
            triProjected.v[2] = projectionMatrix.multiplyVector(triView.v[2]);

            // Scale to Screen
            for (int i = 0; i < 3; i++) {
                triProjected.v[i].x = (triProjected.v[i].x + 1.0) * 0.5 * screen.getWidth();
                triProjected.v[i].y = (triProjected.v[i].y + 1.0) * 0.5 * screen.getHeight();
            }

            // Draw Solid
            screen.fillTriangle(
                (int)triProjected.v[0].x, (int)triProjected.v[0].y,
                (int)triProjected.v[1].x, (int)triProjected.v[1].y,
                (int)triProjected.v[2].x, (int)triProjected.v[2].y,
                color
            );
        }
    }
}
