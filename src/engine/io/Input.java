package engine.io;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Handles keyboard input by storing the state of keys.
 * Used for smooth polling in the game loop.
 */
public class Input implements KeyListener {
    
    // Array to store the state of every possible key code.
    // 65536 covers standard key codes.
    private final boolean[] keys = new boolean[65536];

    /**
     * Checks if a specific key is currently held down.
     * @param keyCode The KeyEvent constant (e.g., KeyEvent.VK_W).
     * @return true if pressed, false otherwise.
     */
    public boolean isKey(int keyCode) {
        return keys[keyCode];
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not needed for movement, mostly for typing text
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) {
            keys[code] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) {
            keys[code] = false;
        }
    }
}
