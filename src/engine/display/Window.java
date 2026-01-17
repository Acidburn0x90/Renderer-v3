package engine.display;

import engine.io.Input;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

/**
 * The Window class handles the operating system window and the drawing surface.
 * <p>
 * It extends {@link Canvas} to provide a raw surface for pixel manipulation and uses
 * a {@link JFrame} to provide the window borders and OS controls.
 * It implements Double/Triple Buffering to prevent screen tearing.
 * </p>
 */
public class Window extends Canvas {
    // Composition: The Window 'has a' JFrame.
    private final JFrame frame;

    /**
     * Constructor to initialize the window with a specific size and title.
     * Sets up the JFrame and the Canvas, and prepares the window for rendering.
     *
     * @param width  The width of the content area.
     * @param height The height of the content area.
     * @param title  The title displayed on the window bar.
     */
    public Window(int width, int height, String title) {
        // "this" refers to the Canvas since we extend it.
        // We set its size so the JFrame knows how big to be when we pack it.
        setPreferredSize(new Dimension(width, height));

        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Stop program when 'X' is clicked
        frame.setResizable(false); // Keep size fixed for simplicity
        
        frame.add(this); // Add this Canvas to the JFrame
        frame.pack(); // Resize the frame to fit the preferred size of its components (our Canvas)
        
        // Manual Rendering Optimization: 
        // We tell the OS to ignore standard repaint events because we will handle 
        // drawing manually in the Game Loop.
        setIgnoreRepaint(true);
        frame.setIgnoreRepaint(true);

        frame.setLocationRelativeTo(null); // Center on screen
//        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        frame.setVisible(true);

        requestFocus(); // Ensure window is focused so it can receive Input
        hideCursor();
    }

    /**
     * Creates a transparent cursor to hide the mouse during gameplay (FPS style).
     */
    private void hideCursor() {
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            cursorImg, new Point(0, 0), "blank cursor");
        frame.getContentPane().setCursor(blankCursor);
    }

    public int getCenterX() {
        return frame.getX() + frame.getWidth() / 2;
    }

    public int getCenterY() {
        return frame.getY() + frame.getHeight() / 2;
    }

    /**
     * Attaches an input listener (Keyboard and Mouse) to the window.
     * @param listener The Input class implementing KeyListener, MouseListener, and MouseMotionListener.
     */
    public void addInputListener(Input listener) {
        this.addKeyListener(listener);
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);
        frame.addKeyListener(listener);
        this.requestFocus();
    }

    /**
     * Draws the provided image to the window with Double or Triple Buffering.
     * <p>
     * buffering strategy uses 2 or 3 memory buffers:
     * 1. The one currently on the screen.
     * 2. The one we are drawing to.
     * 3. (Optional) An intermediate one.
     * We draw to the back buffer, then "flip" it to the front. This eliminates flickering.
     * </p>
     * @param image The fully rendered frame from the Screen Class
     * */
    public void render(BufferedImage image) {
        // Get current BufferStrategy
        BufferStrategy bs = this.getBufferStrategy();

        // Create the BufferStrategy if it doesn't exist yet
        if (bs == null) {
            this.createBufferStrategy(3); // 3 is Triple Buffering (Smoother)
            return;
        }

        // Get the graphics context of the "Hidden" back buffer
        Graphics g = bs.getDrawGraphics();

        // Draw our rendered image onto the back buffer
        // 0, 0 is top left
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);

        // Cleanup
        g.dispose();

        // Show the hidden buffer (Swap Buffers)
        bs.show();
    }
}