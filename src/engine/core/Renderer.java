package engine.core;

import engine.graphics.Screen;
import engine.math.Matrix4x4;
import engine.math.Mesh;
import engine.math.Triangle;
import engine.math.Vector3D;

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
        // Process every triangle in the mesh
        for (Triangle tri : mesh.triangles) {
            // We use temp triangles for pipeline stages to keep the original mesh data safe
            Triangle triTransformed = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));
            Triangle triProjected = new Triangle(new Vector3D(0,0,0), new Vector3D(0,0,0), new Vector3D(0,0,0));

            // 1. TRANSFORM: Model Space -> View Space
            // Currently just Translation: Vertex - Camera + WorldOffset
            for (int i = 0; i < 3; i++) {
                triTransformed.v[i] = new Vector3D(
                    tri.v[i].x - camera.position.x,
                    tri.v[i].y - camera.position.y,
                    tri.v[i].z - camera.position.z + 3.0 // Temporary offset to place world in front
                );
            }

            // 2. CLIP: Near Plane Clipping (Basic)
            if (triTransformed.v[0].z < 0.1 || triTransformed.v[1].z < 0.1 || triTransformed.v[2].z < 0.1) {
                continue;
            }

            // 3. CULL: Backface Culling
            Vector3D line1 = triTransformed.v[1].subtract(triTransformed.v[0]);
            Vector3D line2 = triTransformed.v[2].subtract(triTransformed.v[0]);
            Vector3D normal = line1.crossProduct(line2).normalize();
            
            // Camera ray from 0,0,0 to the triangle vertex (since we already translated the triangle relative to 0,0,0)
            Vector3D cameraRay = triTransformed.v[0].subtract(new Vector3D(0,0,0));

            if (normal.dotProduct(cameraRay) < 0.0f) {
                
                // 4. PROJECT: View Space -> Screen Space (NDC)
                triProjected.v[0] = projectionMatrix.multiplyVector(triTransformed.v[0]);
                triProjected.v[1] = projectionMatrix.multiplyVector(triTransformed.v[1]);
                triProjected.v[2] = projectionMatrix.multiplyVector(triTransformed.v[2]);

                // 5. SCALE: NDC (-1 to 1) -> Pixel Coordinates
                for (int i = 0; i < 3; i++) {
                    triProjected.v[i].x = (triProjected.v[i].x + 1.0) * 0.5 * screen.getWidth();
                    triProjected.v[i].y = (triProjected.v[i].y + 1.0) * 0.5 * screen.getHeight();
                }

                // 6. RASTERIZE: Draw the lines
                screen.drawLine((int)triProjected.v[0].x, (int)triProjected.v[0].y, (int)triProjected.v[1].x, (int)triProjected.v[1].y, 0xFFFFFF);
                screen.drawLine((int)triProjected.v[1].x, (int)triProjected.v[1].y, (int)triProjected.v[2].x, (int)triProjected.v[2].y, 0xFFFFFF);
                screen.drawLine((int)triProjected.v[2].x, (int)triProjected.v[2].y, (int)triProjected.v[0].x, (int)triProjected.v[0].y, 0xFFFFFF);
            }
        }
    }
}
