package engine.core;

import engine.math.Vector3D;

/**
 * Represents a Camera in 3D space (The Player's Eye).
 * <p>
 * Handles position (x,y,z) and orientation (pitch, yaw).
 * Implements First-Person Shooter (FPS) style movement logic where movement
 * is relative to the camera's current yaw (rotation around the Y-axis).
 * </p>
 */
public class Camera {
    /** World space position of the camera */
    public Vector3D position;
    
    /** Rotation up/down (in radians). clamped to prevent flipping. */
    public double pitch = 0; 
    
    /** Rotation left/right (in radians). */
    public double yaw = 0;   

    // --- Physics ---
    public double vy = 0; // Vertical Velocity
    private boolean isGrounded = false;
    
    // Physics Constants
    private static final double GRAVITY = 0.015;
    private static final double JUMP_FORCE = 0.4;
    public double eyeHeight = 2.0; // Dynamic eye level (allows bobbing/crouching)

    public Camera() {
        this.position = new Vector3D(0, 0, 0);
    }
    
    /**
     * Updates physics (Gravity, Jumping, Collision).
     * @param terrain The terrain to collide with.
     * @param jumpRequest True if the player pressed the Jump key this frame.
     */
    public void updatePhysics(game.Terrain terrain, boolean jumpRequest) {
        // 1. Apply Gravity (Remember: +Y is Down in this engine)
        vy += GRAVITY;
        
        // 2. Jump Input
        if (isGrounded && jumpRequest) {
            vy = -JUMP_FORCE; // Negative Y is Up
            isGrounded = false;
        }
        
        // 3. Apply Velocity
        position.y += vy;
        
        // 4. Terrain Collision
        double groundHeight = terrain.getHeight(position.x, position.z);
        double feetY = position.y + eyeHeight; // The bottom of the player
        
        // If feet are below ground
        if (feetY > groundHeight) {
            // Snap to ground
            position.y = groundHeight - eyeHeight;
            vy = 0;
            isGrounded = true;
        } else {
            isGrounded = false;
        }
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
    
    /**
     * Calculates the vector pointing "Forward" from the camera.
     * Used for culling (checking what is in front of the camera).
     * @return Normalized direction vector.
     */
    public Vector3D getForward() {
        // Based on the WASD movement math:
        // x = -sin(yaw)
        // z = cos(yaw)
        // Note: This is a 2D forward (on the ground plane), which is sufficient for simple terrain culling.
        // For full 3D culling (including looking up/down), we would include pitch.
        return new Vector3D(-Math.sin(yaw), 0, Math.cos(yaw)).normalize();
    }
}