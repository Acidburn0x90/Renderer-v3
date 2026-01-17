package engine.core;

import engine.math.Vector3D;

/**
 * Represents a Camera in 3D space (The Player's Eye).
 * <p>
 * Handles position (x,y,z) and orientation (pitch, yaw).
 * Implements First-Person Shooter (FPS) style movement logic where movement
 * is relative to the camera's current yaw (rotation around the Y-axis).
 * </p>2
 */
public class Camera {
    /** World space position of the camera */
    public Vector3D position;
    
    /** Rotation up/down (in radians). clamped to prevent flipping. */
    public double pitch = 0; 
    
    /** Rotation left/right (in radians). */
    public double yaw = 0;   

    public Camera() {
        this.position = new Vector3D(0, 0, 0);
    }

    /**
     * Rotates the camera based on mouse input.
     * @param dPitch Change in pitch (Y-axis mouse movement)
     * @param dYaw Change in yaw (X-axis mouse movement)
     */
    public void rotate(double dPitch, double dYaw) {
        this.pitch += dPitch;
        this.yaw += dYaw;

        // Clamp pitch to prevent flipping (standard FPS behavior).
        // 1.5 radians is approximately 86 degrees.
        if (pitch > 1.5) pitch = 1.5;
        if (pitch < -1.5) pitch = -1.5;
    }

    /**
     * Moves the camera forward in the direction it is facing (ignoring vertical pitch).
     * @param speed Distance to move.
     */
    public void moveForward(double speed) {
        // We use trigonometry to calculate the X and Z components of the forward vector.
        // Math.sin(yaw) gives us the X component.
        // Math.cos(yaw) gives us the Z component.
        // Note: The signs (-/+) depend on the coordinate system orientation.
        position.x -= Math.sin(yaw) * speed;
        position.z += Math.cos(yaw) * speed;
    }

    /**
     * Moves the camera backward.
     * @param speed Distance to move.
     */
    public void moveBackward(double speed) {
        position.x += Math.sin(yaw) * speed;
        position.z -= Math.cos(yaw) * speed;
    }

    /**
     * Strafes the camera to the left (perpendicular to direction).
     * @param speed Distance to move.
     */
    public void moveLeft(double speed) {
        // To strafe left, we rotate the forward vector 90 degrees.
        // This corresponds to using Cos for X and Sin for Z.
        position.x -= Math.cos(yaw) * speed;
        position.z -= Math.sin(yaw) * speed;
    }

    /**
     * Strafes the camera to the right.
     * @param speed Distance to move.
     */
    public void moveRight(double speed) {
        position.x += Math.cos(yaw) * speed;
        position.z += Math.sin(yaw) * speed;
    }

    /**
     * Moves the camera vertically up (Global Y axis).
     * @param speed Distance to move.
     */
    public void moveUp(double speed) {
        position.y -= speed; 
    }

    /**
     * Moves the camera vertically down (Global Y axis).
     * @param speed Distance to move.
     */
    public void moveDown(double speed) {
        position.y += speed;
    }
}