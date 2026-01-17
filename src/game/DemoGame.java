package game;

import engine.core.Camera;
import engine.core.Engine;
import engine.math.Mesh;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The specific game implementation.
 * <p>
 * This class extends the generic {@link Engine} to create a specific experience.
 * It sets up the "World" (a procedural terrain), creates the Camera,
 * and handles the user input to fly around the world.
 * </p>
 */
public class DemoGame extends Engine {
    
    private List<Mesh> meshes;
    private Camera camera;

    public DemoGame() {
        // Initialize Engine with specific window settings
        // Render scale 0.25 (1/4) means for a 2560x1440 window, we render at 640x360.
        // This gives a cool "Retro" pixelated look and ensures very high FPS (hundreds/thousands).
        super(2560, 1440, "Renderer v3 - Terrain Demo", (double) 1/4);
    }

    /**
     * Called once when the game starts.
     * Sets up the camera, and generates the terrain.
     */
    @Override
    public void init() {
        camera = new Camera();
        camera.position.z = 0.0; 
        camera.position.y = -15.0; // Fly high above the terrain (negative Y is up in this coordinate system)
        
        meshes = new ArrayList<>();
        
        // Use the generator to create content
        // Generate a 200x200 terrain with scale 2.0 (400x400 units wide)
        meshes.add(WorldGenerator.generateTerrain(200, 200, 2.0, System.currentTimeMillis()));
    }

    /**
     * Called 60 times per second.
     * Handles keyboard and mouse input to move the camera.
     */
    @Override
    public void update() {
        // --- Keyboard Movement ---
        double speed = 0.1;
        if (input.isKey(KeyEvent.VK_SHIFT)) speed = 0.3; // Sprint

        // WASD Movement (Relative to Camera Rotation)
        if (input.isKey(KeyEvent.VK_W)) camera.moveForward(speed);
        if (input.isKey(KeyEvent.VK_S)) camera.moveBackward(speed);
        if (input.isKey(KeyEvent.VK_A)) camera.moveLeft(speed);
        if (input.isKey(KeyEvent.VK_D)) camera.moveRight(speed);
        
        // Vertical Movement (Global Axis)
        if (input.isKey(KeyEvent.VK_SPACE)) camera.moveUp(speed);
        if (input.isKey(KeyEvent.VK_CONTROL)) camera.moveDown(speed);

        // --- Mouse Look ---
        // We get the raw delta (change) in mouse position from the Input system.
        // This is enabled by the "Mouse Lock" logic in the Engine class.
        double sensitivity = 0.005;
        double dx = input.getDeltaX();
        double dy = input.getDeltaY();

        if (dx != 0 || dy != 0) {
            camera.rotate(-dy * sensitivity, -dx * sensitivity);
        }
    }

    /**
     * Called as fast as possible.
     * Sends the meshes to the renderer.
     */
    @Override
    public void render() {
        renderer.beginFrame();
        
        for (Mesh mesh : meshes) {
            renderer.renderMesh(mesh, camera);
        }
        
        renderer.endFrame(camera);
    }
}