package engine.math;

/**
 * Represents a point or vector in 3D space.
 * Fields are public for performance and ease of access during math operations.
 */
public class Vector3D {
    public double x;
    public double y;
    public double z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Subtracts another vector from this one.
     * @return A new Vector3D result.
     */
    public Vector3D subtract(Vector3D v) {
        return new Vector3D(x - v.x, y - v.y, z - v.z);
    }

    /**
     * Calculates the Dot Product (Scalar product).
     * Used for lighting calculations and angles.
     */
    public double dotProduct(Vector3D v) {
        return x * v.x + y * v.y + z * v.z;
    }

    /**
     * Calculates the Cross Product.
     * Returns a vector perpendicular to both this vector and v.
     * Used for finding surface normals.
     */
    public Vector3D crossProduct(Vector3D v) {
        return new Vector3D(
            y * v.z - z * v.y,
            z * v.x - x * v.z,
            x * v.y - y * v.x
        );
    }

    /**
     * Calculates the length (magnitude) of the vector.
     */
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Normalizes the vector (scales it to length 1).
     * @return A new normalized Vector3D, or a zero vector if length is 0.
     */
    public Vector3D normalize() {
        double l = length();
        if (l == 0) return new Vector3D(0, 0, 0);
        return new Vector3D(x / l, y / l, z / l);
    }
}
