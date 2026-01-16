package engine.math;


/**
 * Literally like all of this is just from the internet cuz its kinda standard
 * I had no idea that 4x4 can be used for translation
 * the 3x3 logic is standard but all the FOV stuff and near and far plane junk is off of the internet.
 * */


/**
 * A 4x4 Matrix used for 3D transformations (Rotation, Translation, Scaling, Projection).
 */
public class Matrix4x4 {
    public double[][] m = new double[4][4];

    /**
     * Multiplies a Vector3D by this matrix.
     * This performs the full transformation pipeline including Perspective Division.
     * 
     * @param i The input vector.
     * @return A new Vector3D representing the transformed point.
     */
    public Vector3D multiplyVector(Vector3D i) {
        double x = i.x * m[0][0] + i.y * m[1][0] + i.z * m[2][0] + m[3][0];
        double y = i.x * m[0][1] + i.y * m[1][1] + i.z * m[2][1] + m[3][1];
        double z = i.x * m[0][2] + i.y * m[1][2] + i.z * m[2][2] + m[3][2];
        double w = i.x * m[0][3] + i.y * m[1][3] + i.z * m[2][3] + m[3][3];

        // Perspective Division (Normalizing to Cartesian coordinates)
        if (w != 0.0f) {
            x /= w;
            y /= w;
            z /= w;
        }

        return new Vector3D(x, y, z);
    }

    /**
     * Creates an Identity Matrix (diagonals are 1, rest are 0).
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
     * @param fovDeg Field of View in degrees.
     * @param aspectRatio Screen Width / Screen Height.
     * @param near Near clipping plane.
     * @param far Far clipping plane.
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
