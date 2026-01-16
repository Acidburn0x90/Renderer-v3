package engine.core;

import engine.display.Window;
import engine.graphics.Screen;
import engine.io.Input;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

/**
 * The core Engine class.
 * Handles the game loop, timing, input initialization, and window management.
 * Games should extend this class.
 */
public abstract class Engine implements Runnable {
    protected Window window;
    protected Screen screen;
    protected Input input;
    protected Renderer renderer;
    protected boolean running = false;
    protected Robot robot;
    
    // Configuration
    private final double RENDER_SCALE = 0.5;
    private final double TICKS_PER_SECOND = 60.0;
    
    private int currentWidth, currentHeight;

    public Engine(int width, int height, String title) {
        // System flags for Linux/Performance
        System.setProperty("sun.java2d.opengl", "true");
        
        this.currentWidth = width;
        this.currentHeight = height;
        
        // Initialize Core Components
        this.window = new Window(width, height, title);
        this.screen = new Screen((int)(width * RENDER_SCALE), (int)(height * RENDER_SCALE));
        this.input = new Input();
        this.renderer = new Renderer(screen);
        
        window.addInputListener(input);
        
        try {
            this.robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (running) return;
        running = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double ns = 1_000_000_000 / TICKS_PER_SECOND;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;

        // One-time init for the game
        init();

        while (running) {
            // Resize Handling
            handleResize();

            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                // Input Update (Mouse Locking)
                if (window.isShowing()) {
                    int centerX = window.getCenterX();
                    int centerY = window.getCenterY();
                    
                    // The mouse position relative to screen coordinates
                    // We rely on Input's mouseMoved/Dragged to get absolute screen coords from MouseEvent if possible
                    // Or simpler: just let Input track relative movement, but since we reset every frame:
                    // Delta is (CurrentMouse - Center)
                    // Note: Java MouseEvents are component-relative. 
                    // To do this robustly with Robot, we rely on the fact that we center the mouse every frame.
                    
                    // However, a simpler way without fighting AWT coords:
                    // Just read input.getMouseX() vs Center? No, input gives component relative.
                    // Let's use the Input class's deltas but we must Reset the mouse.
                    
                    // Correct FPS Logic:
                    // 1. Read input state (which has current mouse pos).
                    // 2. Calculate delta = InputPos - CenterPos (relative to component).
                    // 3. Reset mouse to Center using Robot.
                    
                    // Since Input.java tracks absolute mouse position on component:
                    double currentMouseX = input.getMouseX();
                    double currentMouseY = input.getMouseY();
                    double centerComponentX = window.getWidth() / 2.0;
                    double centerComponentY = window.getHeight() / 2.0;
                    
                    double dx = currentMouseX - centerComponentX;
                    double dy = currentMouseY - centerComponentY;
                    
                    input.setExplitDeltas(dx, dy);
                    
                    // Recenter mouse physically
                    if (robot != null) {
                        robot.mouseMove(centerX, centerY);
                    }
                }

                // Game Logic Update
                update();
                inputUpdate();
                delta--;
            }

            // Render
            render();
            window.render(screen.getImage()); // Swap buffers
            frames++;

            // FPS Counter
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames);
                frames = 0;
            }
        }
    }
    
    private void handleResize() {
        if (window.getWidth() != currentWidth || window.getHeight() != currentHeight) {
            currentWidth = window.getWidth();
            currentHeight = window.getHeight();
            if (currentWidth > 0 && currentHeight > 0) {
                int scaledWidth = (int)(currentWidth * RENDER_SCALE);
                int scaledHeight = (int)(currentHeight * RENDER_SCALE);
                if (scaledWidth < 1) scaledWidth = 1;
                if (scaledHeight < 1) scaledHeight = 1;
                
                // Re-init screen and renderer
                screen = new Screen(scaledWidth, scaledHeight);
                renderer = new Renderer(screen); // This will reset projection
            }
        }
    }
    
    // Default Input handling (Escape to quit)
    private void inputUpdate() {
        if (input.isKey(KeyEvent.VK_ESCAPE)) {
            System.exit(0);
        }
    }

    // Abstract methods for the specific game to implement
    public abstract void init();
    public abstract void update();
    public abstract void render();
}
