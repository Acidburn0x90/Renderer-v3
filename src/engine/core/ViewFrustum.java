package engine.core;

import engine.math.Matrix4x4;
import engine.math.Plane;
import engine.math.Vector3D;
import engine.math.Mesh;

/**
 * Represents the Camera's Field of View as 6 planes.
 * Used to quickly discard (cull) objects that are outside the screen.
 */
public class ViewFrustum {
    private final Plane[] planes = new Plane[6];

    public ViewFrustum() {
        for (int i = 0; i < 6; i++) {
            planes[i] = new Plane();
        }
    }

    /**
     * Updates the 6 frustum planes based on the combined View-Projection Matrix.
     * This extracts the planes directly from the transformation mathematics.
     * 
     * @param m The View * Projection Matrix.
     */
    public void update(Matrix4x4 m) {
        // The View-Projection matrix transforms World Space -> Clip Space.
        // In Clip Space, the visible volume is a cube defined by -w <= x,y,z <= w.
        // We can extract the plane equations by combining rows/columns of the matrix.
        
        // Note: Our Matrix implementation uses a specific memory layout.
        // We access elements as m.m[row][col] or m.m[col][row] depending on the convention.
        // Based on multiplyVector logic: x = x*m00 + y*m10 + z*m20 + m30
        // This implies the columns are the basis vectors.
        
        double[][] mat = m.m;

        // Left Plane:   w + x > 0
        planes[0].normal.x = mat[0][3] + mat[0][0];
        planes[0].normal.y = mat[1][3] + mat[1][0];
        planes[0].normal.z = mat[2][3] + mat[2][0];
        planes[0].d        = mat[3][3] + mat[3][0];

        // Right Plane:  w - x > 0
        planes[1].normal.x = mat[0][3] - mat[0][0];
        planes[1].normal.y = mat[1][3] - mat[1][0];
        planes[1].normal.z = mat[2][3] - mat[2][0];
        planes[1].d        = mat[3][3] - mat[3][0];

        // Bottom Plane: w + y > 0
        planes[2].normal.x = mat[0][3] + mat[0][1];
        planes[2].normal.y = mat[1][3] + mat[1][1];
        planes[2].normal.z = mat[2][3] + mat[2][1];
        planes[2].d        = mat[3][3] + mat[3][1];

        // Top Plane:    w - y > 0
        planes[3].normal.x = mat[0][3] - mat[0][1];
        planes[3].normal.y = mat[1][3] - mat[1][1];
        planes[3].normal.z = mat[2][3] - mat[2][1];
        planes[3].d        = mat[3][3] - mat[3][1];

        // Near Plane:   w + z > 0 (or z > 0 depending on depth range)
        planes[4].normal.x = mat[0][3] + mat[0][2];
        planes[4].normal.y = mat[1][3] + mat[1][2];
        planes[4].normal.z = mat[2][3] + mat[2][2];
        planes[4].d        = mat[3][3] + mat[3][2];

        // Far Plane:    w - z > 0
        planes[5].normal.x = mat[0][3] - mat[0][2];
        planes[5].normal.y = mat[1][3] - mat[1][2];
        planes[5].normal.z = mat[2][3] - mat[2][2];
        planes[5].d        = mat[3][3] - mat[3][2];

        // Normalize all planes so distance calculations are correct
        for (Plane p : planes) {
            p.normalize();
        }
    }

    /**
     * Checks if a Sphere is strictly OUTSIDE the frustum.
     * @param center The center of the sphere.
     * @param radius The radius of the sphere.
     * @return true if the sphere is completely invisible (can be culled).
     */
    public boolean isSphereOutside(Vector3D center, double radius) {
        for (Plane p : planes) {
            // Distance from point to plane.
            // If distance is < -radius, the entire sphere is behind the plane.
            if (p.distanceToPoint(center) < -radius) {
                return true;
            }
        }
        return false;
    }
}
