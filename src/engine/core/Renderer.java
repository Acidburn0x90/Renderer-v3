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
    
    // Global list of triangles to render this frame (for global sorting)
    private final List<Triangle> trianglesToRaster = new ArrayList<>();

    public Renderer(Screen screen) {
        this.screen = screen;
        this.projectionMatrix = Matrix4x4.makeProjection(90.0, (double)screen.getHeight() / screen.getWidth(), 0.1, 1000.0);
    }

    public void updateProjection(int width, int height) {
        this.projectionMatrix = Matrix4x4.makeProjection(90.0, (double)height / width, 0.1, 1000.0);
    }

    /**
     * Clears the screen and resets the triangle buffer. Call this at the start of render().
     */
    public void beginFrame() {
        screen.clearPixels("#000000");
        trianglesToRaster.clear();
    }

    /**
     * Processes a mesh and adds its visible triangles to the buffer.
     */
    public void renderMesh(Mesh mesh, Camera camera) {
        Matrix4x4 matRotY = Matrix4x4.rotationY(-camera.yaw);
        Matrix4x4 matRotX = Matrix4x4.rotationX(-camera.pitch);

        for (Triangle tri : mesh.triangles) {
            Triangle triTranslated = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
            // Preserve color
            triTranslated.color = tri.color;
            
            Triangle triRotatedYaw = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
            triRotatedYaw.color = tri.color;
            
            Triangle triView = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
            triView.color = tri.color;

            // 1. TRANSLATION
            for (int i = 0; i < 3; i++) {
                triTranslated.v[i] = new Vector3D(
                    tri.v[i].x - camera.position.x,
                    tri.v[i].y - camera.position.y,
                    tri.v[i].z - camera.position.z
                );
            }

            // 2. ROTATION
            for (int i = 0; i < 3; i++) {
                triRotatedYaw.v[i] = matRotY.multiplyVector(triTranslated.v[i]);
                triView.v[i] = matRotX.multiplyVector(triRotatedYaw.v[i]);
            }

            // 3. CLIP
            if (triView.v[0].z < 0.1 || triView.v[1].z < 0.1 || triView.v[2].z < 0.1) {
                continue;
            }

            // 4. CULL
            Vector3D line1 = triView.v[1].subtract(triView.v[0]);
            Vector3D line2 = triView.v[2].subtract(triView.v[0]);
            Vector3D normal = line1.crossProduct(line2).normalize();
            Vector3D cameraRay = triView.v[0].subtract(new Vector3D(0,0,0));

            if (normal.dotProduct(cameraRay) < 0.0f) {
                // Add to list for sorting later. 
                trianglesToRaster.add(triView);
            }
        }
    }

    /**
     * Sorts the accumulated triangles and rasterizes them to the screen. Call this at the end of render().
     */
    public void endFrame(Camera camera) {
        // 5. SORT: Painter's Algorithm (Sort by average Z, Far -> Near)
        Collections.sort(trianglesToRaster, new Comparator<Triangle>() {
            @Override
            public int compare(Triangle t1, Triangle t2) {
                double z1 = (t1.v[0].z + t1.v[1].z + t1.v[2].z) / 3.0;
                double z2 = (t2.v[0].z + t2.v[1].z + t2.v[2].z) / 3.0;
                return Double.compare(z2, z1); 
            }
        });

        // FIXED SUN LIGHTING
        // We define the sun in WORLD SPACE
        // Slightly higher sun for better overall visibility
        Vector3D worldLightDir = new Vector3D(0.2, 0.8, -0.5); 
        worldLightDir = worldLightDir.normalize();
        
        // We must rotate the Light Direction into VIEW SPACE to match the rotated triangles.
        // If the camera rotates left, the "Sun" vector relative to the camera must rotate right.
        Matrix4x4 matRotY = Matrix4x4.rotationY(-camera.yaw);
        Matrix4x4 matRotX = Matrix4x4.rotationX(-camera.pitch);
        
        Vector3D viewLightDir = matRotY.multiplyVector(worldLightDir);
        viewLightDir = matRotX.multiplyVector(viewLightDir);

        // 6. PROJECT & RASTERIZE
        for (Triangle triView : trianglesToRaster) {
            Triangle triProjected = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));

            // Lighting Calculation (View Space)
            Vector3D line1 = triView.v[1].subtract(triView.v[0]);
            Vector3D line2 = triView.v[2].subtract(triView.v[0]);
            Vector3D normal = line1.crossProduct(line2).normalize();
            
            // Use the Rotated Light Vector
            double dp = normal.dotProduct(viewLightDir);
            
            double brightness = Math.max(0.2, dp); // Ambient 0.2
            
            // Apply lighting to the Triangle's own color
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