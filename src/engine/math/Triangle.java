package engine.math;

/**
 * Represents a polygon in 3D space defined by 3 vertices.
 */
public class Triangle {
    public Vector3D[] v;

    public Triangle(Vector3D v1, Vector3D v2, Vector3D v3) {
        this.v = new Vector3D[]{v1, v2, v3};
    }
}
