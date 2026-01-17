package engine.math;

/**
 * Represents a polygon in 3D space defined by 3 vertices.
 * The fundamental building block of 3D objects.
 */
public class Triangle {
    public Vector3D[] v;
    public int color;

    /**
     * Creates a triangle with a specific color.
     * @param v1 Vertex 1
     * @param v2 Vertex 2
     * @param v3 Vertex 3
     * @param color Color integer (0xRRGGBB)
     */
    public Triangle(Vector3D v1, Vector3D v2, Vector3D v3, int color) {
        this.v = new Vector3D[]{v1, v2, v3};
        this.color = color;
    }
    
    // Helper for backward compatibility (defaults to white)
    public Triangle(Vector3D v1, Vector3D v2, Vector3D v3) {
        this(v1, v2, v3, 0xFFFFFF);
    }
}