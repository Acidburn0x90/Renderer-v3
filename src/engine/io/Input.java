package engine.io;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Handles keyboard and mouse input by storing states and deltas.
 * Used for smooth polling in the game loop.
 */
public class Input implements KeyListener, MouseListener, MouseMotionListener {
    
    // Keyboard State
    private final boolean[] keys = new boolean[65536];

    // Mouse State
    private double mouseX, mouseY;
    private double lastMouseX, lastMouseY;
    private double deltaX, deltaY;

    /**
     * Sets explicit mouse deltas. 
     * Used by the Engine to inject calculated movement when Mouse Locking is active.
     * @param dx The X difference.
     * @param dy The Y difference.
     */
    public void setExplitDeltas(double dx, double dy) {
        this.deltaX = dx;
        this.deltaY = dy;
    }

    public boolean isKey(int keyCode) {
        return keys[keyCode];
    }

    public double getDeltaX() { return deltaX; }
    public double getDeltaY() { return deltaY; }
    
    /**
     * Gets the absolute X position of the mouse on the component.
     */
    public double getMouseX() { return mouseX; }
    
    /**
     * Gets the absolute Y position of the mouse on the component.
     */
    public double getMouseY() { return mouseY; }

    // --- KeyListener Implementation ---

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) keys[code] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) keys[code] = false;
    }

    // --- MouseListener & MouseMotionListener Implementation ---

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
