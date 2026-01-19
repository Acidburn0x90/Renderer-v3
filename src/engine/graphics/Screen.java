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
    private double[] zBuffer; // Depth Buffer
    private Graphics2D g;

    /**
     * Initializes the Screen with a specific width and height.
     */
    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.pixels = ((DataBufferInt) this.image.getRaster().getDataBuffer()).getData();
        this.zBuffer = new double[width * height]; // Allocate Z-Buffer
        this.g = this.image.createGraphics();
    }

    /**
     * Getter for the underlying BufferedImage.
     */
    public BufferedImage getImage() {
        return image;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    /**
     * Clears the screen color.
     */
    public void clearPixels(String hexColor) {
        g.setColor(new Color(hexStringToInt(hexColor)));
        g.fillRect(0, 0, width, height);
    }
    
    /**
     * Resets the Z-Buffer to Infinity. 
     * Must be called at the start of every frame.
     */
    public void clearZBuffer() {
        Arrays.fill(zBuffer, Double.MAX_VALUE);
    }

    /**
     * Standard Rasterizer: Fills a triangle using Scanline conversion + Z-Buffering.
     * Takes 3 vertices which MUST be in Screen Space (Pixels) but preserve their Z depth.
     */
    public void fillTriangle(int x1, int y1, double z1, int x2, int y2, double z2, int x3, int y3, double z3, int color) {
        // 1. Sort Vertices by Y (Top to Bottom) using a simple swap bubble-sort
        // We want v1 to be top (min Y), v3 to be bottom (max Y)
        if (y1 > y2) {
            int ti = x1; x1 = x2; x2 = ti;
            int ty = y1; y1 = y2; y2 = ty;
            double tz = z1; z1 = z2; z2 = tz;
        }
        if (y1 > y3) {
            int ti = x1; x1 = x3; x3 = ti;
            int ty = y1; y1 = y3; y3 = ty;
            double tz = z1; z1 = z3; z3 = tz;
        }
        if (y2 > y3) {
            int ti = x2; x2 = x3; x3 = ti;
            int ty = y2; y2 = y3; y3 = ty;
            double tz = z2; z2 = z3; z3 = tz;
        }

        // 2. Rasterize
        // Triangle is split into two parts: Top-Flat and Bottom-Flat by the middle vertex (v2).
        
        // Slopes (Change in X per Y, Change in Z per Y)
        // Inverse slopes: dX/dY
        double dX13 = 0, dX12 = 0, dX23 = 0;
        double dZ13 = 0, dZ12 = 0, dZ23 = 0;

        if (y3 != y1) {
            dX13 = (double)(x3 - x1) / (y3 - y1);
            dZ13 = (z3 - z1) / (y3 - y1);
        }
        if (y2 != y1) {
            dX12 = (double)(x2 - x1) / (y2 - y1);
            dZ12 = (z2 - z1) / (y2 - y1);
        }
        if (y3 != y2) {
            dX23 = (double)(x3 - x2) / (y3 - y2);
            dZ23 = (z3 - z2) / (y3 - y2);
        }

        // Iterate Scanlines
        
        // --- Top Half (v1 to v2) ---
        // We walk down edges v1->v3 (Long) and v1->v2 (Short)
        double curX_A = x1;
        double curZ_A = z1;
        double curX_B = x1;
        double curZ_B = z1;

        for (int y = y1; y < y2; y++) {
            drawScanline(y, (int)curX_A, curZ_A, (int)curX_B, curZ_B, color);
            curX_A += dX13; curZ_A += dZ13;
            curX_B += dX12; curZ_B += dZ12;
        }

        // --- Bottom Half (v2 to v3) ---
        // We continue walking v1->v3 (Long) but switch short edge to v2->v3
        // Note: curX_A/Z_A are already correct from the previous loop (tracking v1->v3)
        // We just reset B to start at v2
        curX_B = x2;
        curZ_B = z2;
        
        for (int y = y2; y <= y3; y++) {
            drawScanline(y, (int)curX_A, curZ_A, (int)curX_B, curZ_B, color);
            curX_A += dX13; curZ_A += dZ13;
            curX_B += dX23; curZ_B += dZ23;
        }
    }
    
    /**
     * Draws a single horizontal line, interpolating Z and checking the Z-Buffer.
     */
    private void drawScanline(int y, int xStart, double zStart, int xEnd, double zEnd, int color) {
        // Ensure Left-to-Right
        if (xStart > xEnd) {
            int ti = xStart; xStart = xEnd; xEnd = ti;
            double tz = zStart; zStart = zEnd; zEnd = tz;
        }

        // Clipping (Y-axis)
        if (y < 0 || y >= height) return;

        // Calculate Step for Z interpolation
        double zStep = (zEnd - zStart) / (double)(xEnd - xStart);
        double curZ = zStart;

        // X-axis Clipping bounds
        int realXStart = Math.max(0, xStart);
        int realXEnd = Math.min(width - 1, xEnd);
        
        // Adjust Z if we clipped the start
        curZ += zStep * (realXStart - xStart);

        int bufferRowOffset = y * width;
        
        for (int x = realXStart; x <= realXEnd; x++) {
            int idx = bufferRowOffset + x;
            
            // --- Z-BUFFER TEST ---
            // Only draw if this pixel is closer (smaller Z) than what's already there
            if (curZ < zBuffer[idx]) {
                zBuffer[idx] = curZ; // Update Depth
                pixels[idx] = color; // Draw Pixel
            }
            
            curZ += zStep;
        }
    }

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
     * Resets all pixels in the color buffer to black (0x000000).
     */
    public void clearPixels() {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0;
        }
    }

    /**
     * Draws a vertical sky gradient. 
     * Usually called at the start of a frame instead of clearPixels().
     * 
     * @param topColor Hex color for the top of the screen.
     * @param bottomColor Hex color for the horizon/bottom.
     */
    public void drawSky(int topColor, int bottomColor) {
        int r1 = (topColor >> 16) & 0xFF;
        int g1 = (topColor >> 8) & 0xFF;
        int b1 = topColor & 0xFF;

        int r2 = (bottomColor >> 16) & 0xFF;
        int g2 = (bottomColor >> 8) & 0xFF;
        int b2 = bottomColor & 0xFF;

        for (int y = 0; y < height; y++) {
            double alpha = (double) y / height;
            int r = (int) (r1 + (r2 - r1) * alpha);
            int g = (int) (g1 + (g2 - g1) * alpha);
            int b = (int) (b1 + (b2 - b1) * alpha);
            int color = (r << 16) | (g << 8) | b;

            for (int x = 0; x < width; x++) {
                pixels[x + y * width] = color;
            }
        }
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
     * Draws text onto the screen using Java's standard Graphics2D.
     * Useful for UI, FPS counters, and debug info.
     * 
     * @param text The string to draw.
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param color The text color (hex integer, e.g. 0xFFFFFF).
     */
    public void drawText(String text, int x, int y, int color) {
        g.setColor(new Color(color));
        g.drawString(text, x, y);
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
