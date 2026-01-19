package engine.math;

/**
 * Represents a geometric plane in 3D space.
 * Defined by a Normal vector and a Distance from the origin (Constant).
 * Equation: Ax + By + Cz + D = 0
 */
public class Plane {
    public Vector3D normal = new Vector3D(0, 1, 0);
    public double d = 0;

    public Plane() {}

    /**
     * Normalizes the plane equation so that the normal vector has length 1.
     * Essential for distance calculations to be in correct units.
     */
    public void normalize() {
        double len = normal.length();
        if (len > 0) {
            normal.multiplyInPlace(1.0 / len);
            d /= len;
        }
    }

    /**
     * Calculates the signed distance from a point to this plane.
     * @param p The point to check.
     * @return Positive if in front (normal direction), Negative if behind.
     */
    public double distanceToPoint(Vector3D p) {
        return normal.dotProduct(p) + d;
    }
}
