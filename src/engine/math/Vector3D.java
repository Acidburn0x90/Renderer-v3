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
     * Result = This - V
     * @return A new Vector3D result.
     */
    public Vector3D subtract(Vector3D v) {
        return new Vector3D(x - v.x, y - v.y, z - v.z);
    }

    /**
     * Adds another vector to this one.
     * Result = This + V
     * @return A new Vector3D result.
     */
    public Vector3D add(Vector3D v) {
        return new Vector3D(x + v.x, y + v.y, z + v.z);
    }

    /**
     * Multiplies the vector by a scalar value.
     * Result = This * scalar
     * @return A new Vector3D result.
     */
    public Vector3D multiply(double scalar) {
        return new Vector3D(x * scalar, y * scalar, z * scalar);
    }

    /**
     * Calculates the Dot Product (Scalar product).
     * <p>
     * The Dot Product tells us how much two vectors are pointing in the same direction.
     * <ul>
     * <li>> 0: Facing generally same direction</li>
     * <li>0: Perpendicular (90 degrees)</li>
     * <li>< 0: Facing generally opposite directions</li>
     * </ul>
     * Used heavily for lighting (Normal vs LightDir) and Backface Culling.
     * </p>
     */
    public double dotProduct(Vector3D v) {
        return x * v.x + y * v.y + z * v.z;
    }

    /**
     * Calculates the Cross Product.
     * <p>
     * The Cross Product returns a vector that is PERPENDICULAR to both input vectors.
     * If you cross product two edges of a triangle, you get the Surface Normal (the direction the face is pointing).
     * </p>
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
     * Normalizes the vector.
     * <p>
     * This scales the vector so its length is exactly 1.0, while keeping the same direction.
     * Essential for lighting calculations where only direction matters, not magnitude.
     * </p>
     * @return A new normalized Vector3D, or a zero vector if length is 0.
     */
    public Vector3D normalize() {
        double l = length();
        if (l == 0) return new Vector3D(0, 0, 0);
        return new Vector3D(x / l, y / l, z / l);
    }
}