import engine.core.Camera;
import engine.core.Engine;
import engine.math.Mesh;
import engine.math.PerlinNoise;
import engine.math.Triangle;
import engine.math.Vector3D;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The actual Game Logic.
 * Extends the generic 'Engine' to provide specific behavior.
 */
public class Main extends Engine {
    
    // We will render a LIST of meshes nowys
    private List<Mesh> meshes;
    private Camera camera;

    public Main() {
        // CHANGE RENDER SCALE TO GET A RETRO LOOK
        // A scale of 1 is full resolution
        // A scale of 0.5 renders at half resolution and upscales to fit original screen
        super(1920, 1080, "Renderer v3 - Terrain Demo", (double) 1/4);
    }

    public static void main(String[] args) {
        new Main().start();
    }

    @Override
    public void init() {
        camera = new Camera();
        camera.position.z = 0.0; 
        camera.position.y = -15.0; // Fly high above the terrain
        
        meshes = new ArrayList<>();
        
        PerlinNoise noise = new PerlinNoise(System.currentTimeMillis());

        // TERRAIN GENERATION
        // Generate a single mesh for the terrain (efficient)
        Mesh terrainMesh = new Mesh();
        int width = 40;
        int depth = 40;
        double scale = 2.0; // Distance between points
        
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                // Calculate position and height
                double x0 = (x - width/2.0) * scale;
                double z0 = (z - depth/2.0) * scale;
                
                double x1 = (x + 1 - width/2.0) * scale;
                double z1 = (z + 1 - depth/2.0) * scale;
                
                // Get Heights from Perlin Noise (Scaled)
                // We sample smaller steps (x*0.1) to get smooth hills
                double y00 = noise.noise(x * 0.1, 0, z * 0.1) * 10.0;
                double y01 = noise.noise(x * 0.1, 0, (z+1) * 0.1) * 10.0;
                double y10 = noise.noise((x+1) * 0.1, 0, z * 0.1) * 10.0;
                double y11 = noise.noise((x+1) * 0.1, 0, (z+1) * 0.1) * 10.0;
                
                // Create 2 Triangles for this square
                
                // Color based on height (widened ranges for visibility)
                int color;
                if (y00 < 0.0) {
                    color = 0x2E8B57; // Sea Green (Valley)
                } else if (y00 < 4.0) {
                    color = 0x8B4513; // Saddle Brown (Slopes)
                } else {
                    color = 0xFFFFFF; // Snow (Peaks)
                }

                // Triangle 1 (Top-Left)
                terrainMesh.triangles.add(new Triangle(
                    new Vector3D(x0, y00, z0),
                    new Vector3D(x1, y11, z1),
                    new Vector3D(x0, y01, z1),
                    color
                ));
                
                // Triangle 2 (Bottom-Right)
                terrainMesh.triangles.add(new Triangle(
                    new Vector3D(x0, y00, z0),
                    new Vector3D(x1, y10, z0),
                    new Vector3D(x1, y11, z1),
                    color
                ));
            }
        }
        meshes.add(terrainMesh);
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
        // Start the frame (clears screen and triangle buffer)
        renderer.beginFrame();
        
        // Add all meshes to the render queue
        for (Mesh mesh : meshes) {
            renderer.renderMesh(mesh, camera);
        }
        
        // Sort and draw everything
        renderer.endFrame(camera);
    }
}