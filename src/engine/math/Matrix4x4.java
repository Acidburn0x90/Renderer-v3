package engine.math;

/**
 * A 4x4 Matrix used for 3D transformations.
 * <p>
 * This class handles the math required to:
 * <ul>
 *     <li>Rotate points in 3D space.</li>
 *     <li>Translate (move) points.</li>
 *     <li>Scale points.</li>
 *     <li>Project 3D points onto a 2D plane (Perspective).</li>
 * </ul>
 * It uses a standard row-major or column-major logic (implementation dependent, here tailored for the engine's vector multiply).
 * </p>
 */
public class Matrix4x4 {
    public double[][] m = new double[4][4];

    /**
     * Multiplies a Vector3D by this matrix.
     * <p>
     * This performs the full transformation pipeline including Perspective Division.
     * effectively: Output = Matrix * InputVector
     * </p>
     * 
     * @param i The input vector.
     * @return A new Vector3D representing the transformed point.
     */
    public Vector3D multiplyVector(Vector3D i) {
        Vector3D out = new Vector3D(0,0,0);
        multiplyVector(i, out);
        return out;
    }

    /**
     * Zero-Allocation Matrix Multiplication.
     * Transforms 'in' and stores the result in 'out'.
     * @param in Input Vector
     * @param out Output Vector (Modified in-place)
     */
    public void multiplyVector(Vector3D in, Vector3D out) {
        double x = in.x * m[0][0] + in.y * m[1][0] + in.z * m[2][0] + m[3][0];
        double y = in.x * m[0][1] + in.y * m[1][1] + in.z * m[2][1] + m[3][1];
        double z = in.x * m[0][2] + in.y * m[1][2] + in.z * m[2][2] + m[3][2];
        double w = in.x * m[0][3] + in.y * m[1][3] + in.z * m[2][3] + m[3][3];

        if (w != 0.0f) {
            out.x = x / w;
            out.y = y / w;
            out.z = z / w;
        } else {
            out.x = x;
            out.y = y;
            out.z = z;
        }
    }

    /**
     * Creates an Identity Matrix.
     * <p>
     * An identity matrix is the "neutral" matrix. Multiplying by it changes nothing.
     * [1 0 0 0]
     * [0 1 0 0]
     * [0 0 1 0]
     * [0 0 0 1]
     * </p>
     */
    public static Matrix4x4 makeIdentity() {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.m[0][0] = 1.0;
        matrix.m[1][1] = 1.0;
        matrix.m[2][2] = 1.0;
        matrix.m[3][3] = 1.0;
        return matrix;
    }

    /**
     * Creates a Rotation Matrix for the X-axis.
     * @param angleRad The angle in radians.
     */
    public static Matrix4x4 rotationX(double angleRad) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.m[0][0] = 1.0;
        matrix.m[1][1] = Math.cos(angleRad);
        matrix.m[1][2] = Math.sin(angleRad);
        matrix.m[2][1] = -Math.sin(angleRad);
        matrix.m[2][2] = Math.cos(angleRad);
        matrix.m[3][3] = 1.0;
        return matrix;
    }

    /**
     * Creates a Rotation Matrix for the Y-axis.
     * @param angleRad The angle in radians.
     */
    public static Matrix4x4 rotationY(double angleRad) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.m[0][0] = Math.cos(angleRad);
        matrix.m[0][2] = Math.sin(angleRad);
        matrix.m[1][1] = 1.0;
        matrix.m[2][0] = -Math.sin(angleRad);
        matrix.m[2][2] = Math.cos(angleRad);
        matrix.m[3][3] = 1.0;
        return matrix;
    }

    /**
     * Creates a Rotation Matrix for the Z-axis.
     * @param angleRad The angle in radians.
     */
    public static Matrix4x4 rotationZ(double angleRad) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.m[0][0] = Math.cos(angleRad);
        matrix.m[0][1] = Math.sin(angleRad);
        matrix.m[1][0] = -Math.sin(angleRad);
        matrix.m[1][1] = Math.cos(angleRad);
        matrix.m[2][2] = 1.0;
        matrix.m[3][3] = 1.0;
        return matrix;
    }

    /**
     * Creates a Perspective Projection Matrix.
     * <p>
     * This magic matrix squishes the 3D frustum (pyramid shape view) into a 2D cube (Normalized Device Coordinates).
     * It handles Field of View and Aspect Ratio.
     * </p>
     * @param fovDeg Field of View in degrees.
     * @param aspectRatio Screen Width / Screen Height.
     * @param near Near clipping plane (closest visible distance).
     * @param far Far clipping plane (furthest visible distance).
     */
    public static Matrix4x4 makeProjection(double fovDeg, double aspectRatio, double near, double far) {
        Matrix4x4 matrix = new Matrix4x4();
        double fovRad = 1.0 / Math.tan(fovDeg * 0.5 / 180.0 * Math.PI);
        matrix.m[0][0] = aspectRatio * fovRad;
        matrix.m[1][1] = fovRad;
        matrix.m[2][2] = far / (far - near);
        matrix.m[3][2] = (-far * near) / (far - near);
        matrix.m[2][3] = 1.0;
        matrix.m[3][3] = 0.0;
        return matrix;
    }
}