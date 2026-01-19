package engine.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 3D object consisting of a collection of triangles.
 * <p>
 * A Mesh is simply a container for triangles. In more complex engines, 
 * this would also hold Position, Rotation, and Scale for the entire object.
 * </p>
 */
public class Mesh {
    public List<Triangle> triangles;

    public Mesh() {
        this.triangles = new ArrayList<>();
    }

    /**
     * Translates (moves) the entire mesh by the specified offset.
     * @param x Offset X
     * @param y Offset Y
     * @param z Offset Z
     */
    public void translate(double x, double y, double z) {
        for (Triangle tri : triangles) {
            for (Vector3D v : tri.v) {
                v.x += x;
                v.y += y;
                v.z += z;
            }
        }
    }
}
