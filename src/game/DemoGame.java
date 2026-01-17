package game;

import engine.core.Camera;
import engine.core.Engine;
import engine.math.Mesh;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The specific game implementation.
 * Defines the initial scene and update logic.
 */
public class DemoGame extends Engine {
    
    private List<Mesh> meshes;
    private Camera camera;

    public DemoGame() {
        // Initialize Engine with specific window settings
        // Render scale 0.33... gives a retro look and high performance
        super(1920, 1080, "Renderer v3 - Terrain Demo", (double) 1/3);
    }

    @Override
    public void init() {
        camera = new Camera();
        camera.position.z = 0.0; 
        camera.position.y = -15.0; // Fly high above the terrain
        
        meshes = new ArrayList<>();
        
        // Use the generator to create content
        meshes.add(WorldGenerator.generateTerrain(40, 40, 2.0, System.currentTimeMillis()));
    }

    @Override
    public void update() {
        // --- Keyboard Movement ---
        double speed = 0.1;
        if (input.isKey(KeyEvent.VK_SHIFT)) speed = 0.3;

        if (input.isKey(KeyEvent.VK_W)) camera.moveForward(speed);
        if (input.isKey(KeyEvent.VK_S)) camera.moveBackward(speed);
        if (input.isKey(KeyEvent.VK_A)) camera.moveLeft(speed);
        if (input.isKey(KeyEvent.VK_D)) camera.moveRight(speed);
        if (input.isKey(KeyEvent.VK_SPACE)) camera.moveUp(speed);
        if (input.isKey(KeyEvent.VK_CONTROL)) camera.moveDown(speed);

        // --- Mouse Look ---
        double sensitivity = 0.005;
        double dx = input.getDeltaX();
        double dy = input.getDeltaY();

        if (dx != 0 || dy != 0) {
            camera.rotate(-dy * sensitivity, -dx * sensitivity);
        }
    }

    @Override
    public void render() {
        renderer.beginFrame();
        
        for (Mesh mesh : meshes) {
            renderer.renderMesh(mesh, camera);
        }
        
        renderer.endFrame(camera);
    }
}
