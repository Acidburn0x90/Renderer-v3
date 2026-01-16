package engine.core;

import engine.math.Vector3D;

/**
 * Represents a Camera in 3D space.
 * Handles position and basic movement logic.
 */
public class Camera {
    public Vector3D position;
    public Vector3D rotation; // Pitch, Yaw, Roll

    public Camera() {
        this.position = new Vector3D(0, 0, 0);
        this.rotation = new Vector3D(0, 0, 0);
    }

    public void moveForward(double speed) {
        position.z += speed;
    }

    public void moveBackward(double speed) {
        position.z -= speed;
    }

    public void moveLeft(double speed) {
        position.x -= speed;
    }

    public void moveRight(double speed) {
        position.x += speed;
    }

    public void moveUp(double speed) {
        position.y -= speed; // Y is often inverted in screen space, keep mind of coordinate systems
    }

    public void moveDown(double speed) {
        position.y += speed;
    }
}
