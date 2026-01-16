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
 * It extends Canvas to provide a raw surface for pixel manipulation and uses
 * a JFrame to provide the window borders and controls.
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
        frame.setResizable(false); // Keep size fixed for simplicity for now
        
        frame.add(this); // Add this Canvas to the JFrame
        frame.pack(); // Resize the frame to fit the preferred size of its components (our Canvas)
        
        // Manual Rendering Optimization: Tell the OS to ignore standard repaint events
        setIgnoreRepaint(true);
        frame.setIgnoreRepaint(true);

        frame.setLocationRelativeTo(null); // Center on screen
//        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        frame.setVisible(true);

        requestFocus(); // Ensure window is focused for input/rendering
        hideCursor();
    }

    /**
     * Creates a transparent cursor to hide the mouse during gameplay.
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
     * Draws the provided image to the window with Double or Triple Buffering
     * @param image The fully rendered frame from the Screen Class
     * */
    public void render(BufferedImage image) {
        // Get current BufferStrategy eg. getBufferStrategy() from extending Canvas
        BufferStrategy bs = this.getBufferStrategy();

        // Create the BufferStrategy
        if (bs == null) {
            this.createBufferStrategy(3); // 3 is Triple Buffering, 2 is Double Buffering
            return;
        }

        // Get the hidden buffer
        Graphics g = bs.getDrawGraphics();

        // Draw the image
        // 0, 0 is top left
        // null is ImageObserver

        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);

        // Cleanup

        g.dispose();

        // Show the hidden buffer
        bs.show();
    }
}
