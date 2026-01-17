package engine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * The Screen class represents the raw pixel data of our engine.
 * <p>
 * It acts as a framebuffer (Video Memory) that we can manipulate directly before rendering.
 * To achieve high performance in Java, we bypass standard AWT drawing for individual pixels
 * and manipulate the integer array backing the BufferedImage directly.
 * </p>
 */
public class Screen {
    private int width;
    private int height;
    private BufferedImage image;
    private int[] pixels;
    private Graphics2D g; // High-performance graphics context for polygon filling

    /**
     * Initializes the Screen with a specific width and height.
     * It creates a BufferedImage and extracts its underlying integer array for direct manipulation.
     *
     * @param width  The width of the screen in pixels.
     * @param height The height of the screen in pixels.
     */
    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        
        // Create a new image with integer precision (TYPE_INT_RGB).
        // We avoid Alpha channel (ARGB) for speed, as we don't need transparency for the final buffer.
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Link our 'pixels' array directly to the image's internal memory.
        // This allows us to write 'pixels[i] = color' and have it update the image instantly
        // without function call overhead for every pixel.
        this.pixels = ((DataBufferInt) this.image.getRaster().getDataBuffer()).getData();
        
        // Initialize Graphics2D for advanced drawing operations like fillPolygon
        // which are hardware accelerated or highly optimized in the JVM.
        this.g = this.image.createGraphics();
    }

    /**
     * Getter for the underlying BufferedImage.
     * Used by the Window to draw the final result to the real screen.
     * @return The rendered image.
     */
    public BufferedImage getImage() {
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Helper method to parse hex strings (e.g., "#FF0000", "0xFF0000", "FF0000") into integers.
     * Uses UnsignedInt parsing to handle the full 32-bit color range correctly.
     * @param hexString should be in the following form e.g. "#RRGGBB" or "0xRRGGBB"
     */
    public static int hexStringToInt(String hexString) {
        hexString = hexString.toUpperCase();
        if (hexString.startsWith("#")) {
            hexString = hexString.substring(1);
        } else if (hexString.startsWith("0X")) {
            hexString = hexString.substring(2);
        } else {
            throw new IllegalArgumentException("Invalid hex string: " + hexString);
        }
        return Integer.parseUnsignedInt(hexString, 16);
    }

    /**
     * Clears the entire screen to black (0).
     */
    public void clearPixels() {
        // Using Graphics2D is often faster than iterating the array in Java
        // because it uses native code/hardware accel under the hood.
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
    }

    /**
     * Clears the entire screen to a specific hex color.
     */
    public void clearPixels(String hexColor) {
        g.setColor(new Color(hexStringToInt(hexColor)));
        g.fillRect(0, 0, width, height);
    }

    /**
     * Fills a solid triangle using Hardware Acceleration (Graphics2D).
     * This is significantly faster than writing a software scanline algorithm in pure Java.
     */
    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        g.setColor(new Color(color));
        g.fillPolygon(new int[]{x1, x2, x3}, new int[]{y1, y2, y3}, 3);
    }

    /**
     * Sets a single pixel at (x, y) to a specific integer color.
     * Includes clipping: if the pixel is off-screen, it is simply ignored.
     */
    public void setPixel(int x, int y, int color) {
        // Bounds check (Clipping)
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        // Direct array access for maximum performance
        pixels[x + y * width] = color;
    }

    /**
     * Calculates the linear array index for a 2D coordinate (x, y).
     * Note: This method still throws an exception if used incorrectly elsewhere.
     */
    public int index(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) throw new IndexOutOfBoundsException();
        return y * width + x;
    }

    /**
     * Draws a line between two points using Bresenham's Line Algorithm.
     * This algorithm uses only integer arithmetic (add/subtract/bit-shift) for high performance.
     * It avoids floating point math entirely.
     *
     * @param x0    Start X coordinate
     * @param y0    Start Y coordinate
     * @param x1    End X coordinate
     * @param y1    End Y coordinate
     * @param color The color of the line
     */
    public void drawLine(int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            setPixel(x0, y0, color);
            
            // If we have reached the end point, break the loop
            if (x0 == x1 && y0 == y1) break;
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }
}
