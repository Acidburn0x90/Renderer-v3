import engine.core.Camera;
import engine.core.Engine;
import engine.math.Mesh;
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
    
    // We will render a LIST of meshes now
    private List<Mesh> meshes;
    private Camera camera;

    public Main() {
        // CHANGE RENDER SCALE TO GET A RETRO LOOK
        // A scale of 1 is full resolution
        // A scale of 0.5 renderes at half resolution and upscales to fit original screen
        super(1920, 1080, "Renderer v3 - Stress Test", (double) 1/3);
    }

    public static void main(String[] args) {
        new Main().start();
    }

    @Override
    public void init() {
        camera = new Camera();
        camera.position.z = 5.0; 
        camera.position.y = -2.0; // Start slightly high up
        
        meshes = new ArrayList<>();

        // STRESS TEST: Create a grid of 100 cubes (10x10)
        int size = 10;
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                // Space them out by 2 units
                double offsetX = (x - size / 2.0) * 2.0;
                double offsetZ = z * 2.0;
                
                meshes.add(createCube(offsetX, 0, offsetZ));
            }
        }
    }

    /**
     * Helper to generate a cube mesh at a specific position.
     */
    private Mesh createCube(double x, double y, double z) {
        Mesh mesh = new Mesh();
        // Add 12 triangles for a cube at offset (x, y, z)
        // Note: This is verbose, but efficient for this engine structure
        
        // South Face
        mesh.triangles.add(new Triangle(new Vector3D(x+0, y+0, z+0), new Vector3D(x+0, y+1, z+0), new Vector3D(x+1, y+1, z+0)));
        mesh.triangles.add(new Triangle(new Vector3D(x+0, y+0, z+0), new Vector3D(x+1, y+1, z+0), new Vector3D(x+1, y+0, z+0)));
        
        // East Face
        mesh.triangles.add(new Triangle(new Vector3D(x+1, y+0, z+0), new Vector3D(x+1, y+1, z+0), new Vector3D(x+1, y+1, z+1)));
        mesh.triangles.add(new Triangle(new Vector3D(x+1, y+0, z+0), new Vector3D(x+1, y+1, z+1), new Vector3D(x+1, y+0, z+1)));
        
        // North Face
        mesh.triangles.add(new Triangle(new Vector3D(x+1, y+0, z+1), new Vector3D(x+1, y+1, z+1), new Vector3D(x+0, y+1, z+1)));
        mesh.triangles.add(new Triangle(new Vector3D(x+1, y+0, z+1), new Vector3D(x+0, y+1, z+1), new Vector3D(x+0, y+0, z+1)));
        
        // West Face
        mesh.triangles.add(new Triangle(new Vector3D(x+0, y+0, z+1), new Vector3D(x+0, y+1, z+1), new Vector3D(x+0, y+1, z+0)));
        mesh.triangles.add(new Triangle(new Vector3D(x+0, y+0, z+1), new Vector3D(x+0, y+1, z+0), new Vector3D(x+0, y+0, z+0)));
        
        // Top Face
        mesh.triangles.add(new Triangle(new Vector3D(x+0, y+1, z+0), new Vector3D(x+0, y+1, z+1), new Vector3D(x+1, y+1, z+1)));
        mesh.triangles.add(new Triangle(new Vector3D(x+0, y+1, z+0), new Vector3D(x+1, y+1, z+1), new Vector3D(x+1, y+1, z+0)));
        
        // Bottom Face
        mesh.triangles.add(new Triangle(new Vector3D(x+1, y+0, z+1), new Vector3D(x+0, y+0, z+1), new Vector3D(x+0, y+0, z+0)));
        mesh.triangles.add(new Triangle(new Vector3D(x+1, y+0, z+1), new Vector3D(x+0, y+0, z+0), new Vector3D(x+1, y+0, z+0)));
        
        return mesh;
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
        renderer.endFrame();
    }
}