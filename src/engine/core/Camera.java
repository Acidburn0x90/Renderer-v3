package engine.core;

import engine.math.Vector3D;

/**
 * Represents a Camera in 3D space.
 * Handles position and basic movement logic.
 */
public class Camera {
    public Vector3D position;
    public double pitch = 0; // Up/Down rotation
    public double yaw = 0;   // Left/Right rotation

    public Camera() {
        this.position = new Vector3D(0, 0, 0);
    }

    public void rotate(double dPitch, double dYaw) {
        this.pitch += dPitch;
        this.yaw += dYaw;

        // Clamp pitch to prevent flipping (standard FPS behavior)
        if (pitch > 1.5) pitch = 1.5;
        if (pitch < -1.5) pitch = -1.5;
    }

    public void moveForward(double speed) {
        // Corrected Trigonometry for FPS movement
        position.x -= Math.sin(yaw) * speed;
        position.z += Math.cos(yaw) * speed;
    }

    public void moveBackward(double speed) {
        position.x += Math.sin(yaw) * speed;
        position.z -= Math.cos(yaw) * speed;
    }

    public void moveLeft(double speed) {
        // Strafe Left
        position.x -= Math.cos(yaw) * speed;
        position.z -= Math.sin(yaw) * speed;
    }

    public void moveRight(double speed) {
        // Strafe Right
        position.x += Math.cos(yaw) * speed;
        position.z += Math.sin(yaw) * speed;
    }

    public void moveUp(double speed) {
        position.y -= speed; 
    }

    public void moveDown(double speed) {
        position.y += speed;
    }
}
