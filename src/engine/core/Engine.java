package engine.core;

import engine.display.Window;
import engine.graphics.Screen;
import engine.io.Input;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

/**
 * The core Engine class.
 * <p>
 * This abstract class handles the "Game Loop", which is the heart of any real-time simulation.
 * It manages:
 * <ul>
 *     <li>Timing (Fixed time-step updates for consistent physics/logic).</li>
 *     <li>Window creation and management.</li>
 *     <li>Input initialization.</li>
 *     <li>Frame rendering coordination.</li>
 * </ul>
 * Games should extend this class and implement {@link #init()}, {@link #update()}, and {@link #render()}.
 * </p>
 */
public abstract class Engine implements Runnable {
    // Core Subsystems
    protected Window window;
    protected Screen screen;
    protected Input input;
    protected Renderer renderer;
    protected Robot robot; // Used for mouse locking (centering mouse)
    
    // Loop State
    protected boolean running = false;
    
    // Configuration
    protected double renderScale = 0.5;
    private final double TICKS_PER_SECOND = 60.0;
    
    private int currentWidth, currentHeight;

    /**
     * Configures the engine and initializes subsystems.
     * @param width Window width
     * @param height Window height
     * @param title Window title
     * @param renderScale Internal resolution scale (e.g. 0.5 renders at half size and stretches)
     */
    public Engine(int width, int height, String title, double renderScale) {
        // Enable OpenGL pipeline for Java2D to ensure high-performance image drawing
        System.setProperty("sun.java2d.opengl", "true");
        
        this.currentWidth = width;
        this.currentHeight = height;
        this.renderScale = renderScale;
        
        // Initialize Core Components
        this.window = new Window(width, height, title);
        // Screen is the pixel buffer. It can be smaller than the window for performance/retro-style.
        this.screen = new Screen((int)(width * renderScale), (int)(height * renderScale));
        this.input = new Input();
        this.renderer = new Renderer(screen);
        
        window.addInputListener(input);
        
        try {
            // Robot is used to programmatically move the mouse (for infinite mouse look)
            this.robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the game loop in a separate thread.
     */
    public void start() {
        if (running) return;
        running = true;
        new Thread(this).start();
    }

    /**
     * The Main Game Loop.
     * <p>
     * Uses a "Fixed Time-Step" approach:
     * <ul>
     *     <li><b>Update</b>: Runs exactly 60 times per second (physics/logic).</li>
     *     <li><b>Render</b>: Runs as fast as possible (unlimited FPS).</li>
     * </ul>
     * This decouples game logic speed from framerate.
     * </p>
     */
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double ns = 1_000_000_000 / TICKS_PER_SECOND; // Nanoseconds per Tick
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;

        // One-time init for the game implementation
        init();

        while (running) {
            // Check if window resized and rebuild buffers if necessary
            handleResize();

            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            // Catch up on updates if we are lagging
            while (delta >= 1) {
                // --- MOUSE LOCKING LOGIC ---
                if (window.isShowing()) {
                    int centerX = window.getCenterX();
                    int centerY = window.getCenterY();
                    
                    // 1. Get current mouse position (absolute on screen component)
                    double currentMouseX = input.getMouseX();
                    double currentMouseY = input.getMouseY();
                    
                    // 2. Calculate center position of the window
                    double centerComponentX = window.getWidth() / 2.0;
                    double centerComponentY = window.getHeight() / 2.0;
                    
                    // 3. Calculate how far the mouse moved from the center
                    double dx = currentMouseX - centerComponentX;
                    double dy = currentMouseY - centerComponentY;
                    
                    // 4. Pass this delta to the Input system for the game to use
                    input.setExplitDeltas(dx, dy);
                    
                    // 5. Physically reset the mouse to the center of the window
                    // This allows infinite scrolling without hitting screen edges.
                    if (robot != null) {
                        robot.mouseMove(centerX, centerY);
                    }
                }

                // Game Logic Update
                update();
                inputUpdate(); // System keys (e.g. Escape)
                delta--;
            }

            // Render Frame
            render(); // Draw 3D scene to 'screen' buffer
            window.render(screen.getImage()); // Blit 'screen' buffer to Window
            frames++;

            // Simple FPS Counter to Console
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames);
                frames = 0;
            }
        }
    }
    
    /**
     * Handles window resizing. 
     * If the window size changes, we must recreate the 'Screen' buffer 
     * to match the new aspect ratio and size (scaled by renderScale).
     */
    private void handleResize() {
        if (window.getWidth() != currentWidth || window.getHeight() != currentHeight) {
            currentWidth = window.getWidth();
            currentHeight = window.getHeight();
            if (currentWidth > 0 && currentHeight > 0) {
                int scaledWidth = (int)(currentWidth * renderScale);
                int scaledHeight = (int)(currentHeight * renderScale);
                if (scaledWidth < 1) scaledWidth = 1;
                if (scaledHeight < 1) scaledHeight = 1;
                
                // Re-init screen and renderer
                screen = new Screen(scaledWidth, scaledHeight);
                renderer = new Renderer(screen); // This will reset projection matrix
            }
        }
    }
    
    /**
     * Default Input handling (e.g. Escape to quit).
     */
    private void inputUpdate() {
        if (input.isKey(KeyEvent.VK_ESCAPE)) {
            System.exit(0);
        }
    }

    // Abstract methods for the specific game to implement
    
    /** Initialize game resources/scenes. */
    public abstract void init();
    
    /** Update game logic (movement, physics). Runs 60 times/sec. */
    public abstract void update();
    
    /** Draw the scene. Runs as fast as possible. */
    public abstract void render();
}