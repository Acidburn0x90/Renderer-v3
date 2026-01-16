package engine.core;

import engine.display.Window;
import engine.graphics.Screen;
import engine.io.Input;

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
