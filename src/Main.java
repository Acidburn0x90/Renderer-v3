import engine.core.Camera;
import engine.core.Engine;
import engine.math.Mesh;
import engine.math.Triangle;
import engine.math.Vector3D;

import java.awt.event.KeyEvent;

/**
 * The actual Game Logic.
 * Extends the generic 'Engine' to provide specific behavior.
 */
public class Main extends Engine {
    
    private Mesh meshCube;
    private Camera camera;

    public Main() {
        super(800, 600, "Renderer v3 - Refactored");
    }

    public static void main(String[] args) {
        new Main().start();
    }

    @Override
    public void init() {
        camera = new Camera();
        
        // Define the 3D Cube
        meshCube = new Mesh();
        // South
        meshCube.triangles.add(new Triangle(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0), new Vector3D(1, 1, 0)));
        meshCube.triangles.add(new Triangle(new Vector3D(0, 0, 0), new Vector3D(1, 1, 0), new Vector3D(1, 0, 0)));
        // East
        meshCube.triangles.add(new Triangle(new Vector3D(1, 0, 0), new Vector3D(1, 1, 0), new Vector3D(1, 1, 1)));
        meshCube.triangles.add(new Triangle(new Vector3D(1, 0, 0), new Vector3D(1, 1, 1), new Vector3D(1, 0, 1)));
        // North
        meshCube.triangles.add(new Triangle(new Vector3D(1, 0, 1), new Vector3D(1, 1, 1), new Vector3D(0, 1, 1)));
        meshCube.triangles.add(new Triangle(new Vector3D(1, 0, 1), new Vector3D(0, 1, 1), new Vector3D(0, 0, 1)));
        // West
        meshCube.triangles.add(new Triangle(new Vector3D(0, 0, 1), new Vector3D(0, 1, 1), new Vector3D(0, 1, 0)));
        meshCube.triangles.add(new Triangle(new Vector3D(0, 0, 1), new Vector3D(0, 1, 0), new Vector3D(0, 0, 0)));
        // Top
        meshCube.triangles.add(new Triangle(new Vector3D(0, 1, 0), new Vector3D(0, 1, 1), new Vector3D(1, 1, 1)));
        meshCube.triangles.add(new Triangle(new Vector3D(0, 1, 0), new Vector3D(1, 1, 1), new Vector3D(1, 1, 0)));
        // Bottom
        meshCube.triangles.add(new Triangle(new Vector3D(1, 0, 1), new Vector3D(0, 0, 1), new Vector3D(0, 0, 0)));
        meshCube.triangles.add(new Triangle(new Vector3D(1, 0, 1), new Vector3D(0, 0, 0), new Vector3D(1, 0, 0)));
    }

    @Override
    public void update() {
        double speed = 0.1;
        if (input.isKey(KeyEvent.VK_SHIFT)) speed = 0.3;

        if (input.isKey(KeyEvent.VK_W)) camera.moveForward(speed);
        if (input.isKey(KeyEvent.VK_S)) camera.moveBackward(speed);
        if (input.isKey(KeyEvent.VK_A)) camera.moveLeft(speed);
        if (input.isKey(KeyEvent.VK_D)) camera.moveRight(speed);
        if (input.isKey(KeyEvent.VK_SPACE)) camera.moveUp(speed);
        if (input.isKey(KeyEvent.VK_CONTROL)) camera.moveDown(speed);
    }

    @Override
    public void render() {
        screen.clearPixels("#000000");
        renderer.renderMesh(meshCube, camera);
    }
}
