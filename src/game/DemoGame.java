package game;

import engine.core.Camera;
import engine.core.Engine;
import engine.io.ObjLoader;
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
    private Terrain terrain;
    
        private boolean walkingMode = false;
    
        
    
        // FPS Counting
    
        private int fps = 0;
    
        private int frames = 0;
    
        private long timer = System.currentTimeMillis();
    
    
    
        public DemoGame() {
    
            // Initialize Engine with specific window settings
    
            // Render scale 0.25 (1/4) means for a 2560x1440 window, we render at 640x360.
    
            // This gives a cool "Retro" pixelated look and ensures very high FPS (hundreds/thousands).
    
            super(1920, 1080, "Renderer v3 - Terrain Demo", (double) 1/4);
    
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
    
            
    
                    // --- 1. Generate Terrain ---
    
            
    
                    // We create a Terrain object which serves two purposes:
    
            
    
                    // A) It generates the visual Mesh (triangles) for the renderer.
    
            
    
                    // B) It holds the math (Perlin Noise) allows us to query ground height at any (x,z) for physics.
    
            
    
                    // Parameters: 100x100 grid (Stress Test), Scale 2.0.
    
            
    
                    terrain = new Terrain(100, 100, 2.0, System.currentTimeMillis());
    
            
    
                    
    
            
    
                    // Add all the chunks to our render list
    
            
    
                    meshes.addAll(terrain.chunks);
    
            
    
                }
    
            
    
            
    
            
    
                /**
    
            
    
                 * Called 60 times per second.
    
            
    
                 * Handles keyboard and mouse input to move the camera.
    
            
    
                 */
    
            
    
                @Override
    
            
    
                public void update() {
    
            
    
                    // --- Toggles ---
    
            
    
                    // Toggle 'Walking Mode' when G is pressed.
    
            
    
                    if (input.isKey(KeyEvent.VK_G)) {
    
            
    
                         walkingMode = !walkingMode;
    
            
    
                         // Small debounce hack: normally you'd handle "OnPress" events separately
    
            
    
                         try { Thread.sleep(200); } catch (Exception e) {}
    
            
    
                    }
    
            
    
                
    
            
    
                    // --- Keyboard Movement ---
    
            
    
                    double speed = 0.1;
    
            
    
                    if (input.isKey(KeyEvent.VK_SHIFT)) speed = 0.3; // Sprint
    
            
    
            
    
            
    
                    // WASD Movement (Relative to Camera Rotation)
    
            
    
                    if (input.isKey(KeyEvent.VK_W)) camera.moveForward(speed);
    
            
    
                    if (input.isKey(KeyEvent.VK_S)) camera.moveBackward(speed);
    
            
    
                    if (input.isKey(KeyEvent.VK_A)) camera.moveLeft(speed);
    
            
    
                    if (input.isKey(KeyEvent.VK_D)) camera.moveRight(speed);
    
            
    
                    
    
            
    
                    // Vertical Movement (Global Axis)
    
            
    
                    // Only allow flying if we are NOT in walking mode.
    
            
    
                    if (!walkingMode) {
    
            
    
                        if (input.isKey(KeyEvent.VK_SPACE)) camera.moveUp(speed);
    
            
    
                        if (input.isKey(KeyEvent.VK_CONTROL)) camera.moveDown(speed);
    
            
    
                    }
    
            
    
            
    
            
    
                    // --- Mouse Look ---
    
            
    
                    // We get the raw delta (change) in mouse position from the Input system.
    
            
    
                    // This is enabled by the "Mouse Lock" logic in the Engine class.
    
            
    
                    double sensitivity = 0.005;
    
            
    
                    double dx = input.getDeltaX();
    
            
    
                    double dy = input.getDeltaY();
    
            
    
            
    
            
    
                    if (dx != 0 || dy != 0) {
    
            
    
                        camera.rotate(-dy * sensitivity, -dx * sensitivity);
    
            
    
                    }
    
            
    
                    
    
            
    
                    // --- Physics / Walking Logic ---
    
            
    
                    if (walkingMode) {
    
            
    
                        // 1. Query the Terrain for the exact ground height at our current (x, z) position.
    
            
    
                        double terrainHeight = terrain.getHeight(camera.position.x, camera.position.z);
    
            
    
                        
    
            
    
                        // 2. Snap the camera to that height.
    
            
    
                        // Note on Coordinates: In this engine, Y is inverted (like 2D screen coordinates).
    
            
    
                        // Negative Y is UP, Positive Y is DOWN.
    
            
    
                        // If the ground is at Y=10, standing "on top" of it means being at Y=8 (2 units 'up').
    
            
    
                        camera.position.y = terrainHeight - 2.0;
    
            
    
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
    
            
    
                    // endFrame() is no longer needed as triangles are drawn immediately
    
            
    
                    
    
            
    
                    // --- FPS Counter ---
    
            
    
                    frames++;
    
            
    
                    if (System.currentTimeMillis() - timer > 1000) {
    
            
    
                        timer += 1000;
    
            
    
                        fps = frames;
    
            
    
                        frames = 0;
    
            
    
                        System.out.println("FPS: " + fps); // Keep console log as backup
    
            
    
                    }
    
            
    
                    screen.drawText("FPS: " + fps, 10, 20, 0xFFFF00); // Yellow Text
    
            
    
                    
    
            
    
                    // Count total triangles in the scene (Static count)
    
            
    
                    int totalTris = 0;
    
            
    
                    for (Mesh m : meshes) totalTris += m.triangles.size();
    
            
    
                    
    
            
    
                    screen.drawText("Triangles: " + totalTris, 10, 40, 0xFFFFFF);
    
            
    
                }
    
            
    
            }