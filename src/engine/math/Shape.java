package engine.math;

import engine.graphics.Screen;

/**
 * The abstract base class for all geometric shapes in the engine.
 * It holds common properties like color and enforces the implementation of a render method.
 */
public abstract class Shape {
    /** The color of the shape, stored as a 32-bit integer (0xRRGGBB or 0xAARRGGBB). */
    protected int color;

    /**
     * Constructs a Shape with a specific color.
     * @param hexColor The color string (e.g., "#FF0000").
     */
    public Shape(String hexColor) {
        this.color = Screen.hexStringToInt(hexColor);
    }

    /**
     * Draws the shape onto the provided screen buffer.
     * This must be implemented by all specific shape classes (Line, Rectangle, etc.).
     *
     * @param screen The screen buffer to draw pixels onto.
     */
    public abstract void render(Screen screen);
}
