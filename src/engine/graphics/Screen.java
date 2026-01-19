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
     * 
     * @param l1, l2, l3 Lighting intensity at each vertex (0.0 to 1.0)
     */
    public void fillTriangle(int x1, int y1, double z1, double l1,
                             int x2, int y2, double z2, double l2,
                             int x3, int y3, double z3, double l3,
                             int color) {
        // Default to full screen
        fillTriangle(x1, y1, z1, l1, x2, y2, z2, l2, x3, y3, z3, l3, color, 0, width, 0, height);
    }

    /**
     * Multi-Threaded Rasterizer variant (Tile-Based).
     * Only draws the part of the triangle that falls within [minX, maxX) and [minY, maxY).
     */
    public void fillTriangle(int x1, int y1, double z1, double l1,
                             int x2, int y2, double z2, double l2,
                             int x3, int y3, double z3, double l3,
                             int color,
                             int minX, int maxX, int minY, int maxY) {
        // 1. Sort Vertices by Y (Top to Bottom) using a simple swap bubble-sort
        // We want v1 to be top (min Y), v3 to be bottom (max Y)
        if (y1 > y2) {
            int ti = x1; x1 = x2; x2 = ti;
            int ty = y1; y1 = y2; y2 = ty;
            double tz = z1; z1 = z2; z2 = tz;
            double tl = l1; l1 = l2; l2 = tl;
        }
        if (y1 > y3) {
            int ti = x1; x1 = x3; x3 = ti;
            int ty = y1; y1 = y3; y3 = ty;
            double tz = z1; z1 = z3; z3 = tz;
            double tl = l1; l1 = l3; l3 = tl;
        }
        if (y2 > y3) {
            int ti = x2; x2 = x3; x3 = ti;
            int ty = y2; y2 = y3; y3 = ty;
            double tz = z2; z2 = z3; z3 = tz;
            double tl = l2; l2 = l3; l3 = tl;
        }

        // Optimization: Bounding Box Check (X and Y)
        // If the triangle is completely outside this tile, skip it immediately.
        // Y-check:
        if (y3 < minY || y1 >= maxY) return;
        
        // X-check: find min/max X of the triangle
        int triMinX = Math.min(x1, Math.min(x2, x3));
        int triMaxX = Math.max(x1, Math.max(x2, x3));
        if (triMaxX < minX || triMinX >= maxX) return;

        // 2. Rasterize
        // Triangle is split into two parts: Top-Flat and Bottom-Flat by the middle vertex (v2).
        
        // Slopes (Change in X, Z, and Lighting per Y)
        double dX13 = 0, dX12 = 0, dX23 = 0;
        double dZ13 = 0, dZ12 = 0, dZ23 = 0;
        double dL13 = 0, dL12 = 0, dL23 = 0;

        if (y3 != y1) {
            dX13 = (double)(x3 - x1) / (y3 - y1);
            dZ13 = (z3 - z1) / (y3 - y1);
            dL13 = (l3 - l1) / (y3 - y1);
        }
        if (y2 != y1) {
            dX12 = (double)(x2 - x1) / (y2 - y1);
            dZ12 = (z2 - z1) / (y2 - y1);
            dL12 = (l2 - l1) / (y2 - y1);
        }
        if (y3 != y2) {
            dX23 = (double)(x3 - x2) / (y3 - y2);
            dZ23 = (z3 - z2) / (y3 - y2);
            dL23 = (l3 - l2) / (y3 - y2);
        }

        // Iterate Scanlines
        
        // --- Top Half (v1 to v2) ---
        double curX_A = x1;
        double curZ_A = z1;
        double curL_A = l1;
        double curX_B = x1;
        double curZ_B = z1;
        double curL_B = l1;

        // We only loop through lines that are actually inside our slice [minY, maxY)
        // However, we MUST calculate the start values (curX, curZ) correctly if we skip lines at the top.
        
        // Calculate start/end Y for the top half
        int startY = y1;
        int endY = y2;
        
        // Pre-stepping: If the triangle starts above our slice, we must fast-forward the math.
        if (startY < minY) {
            int skip = minY - startY;
            curX_A += dX13 * skip; curZ_A += dZ13 * skip; curL_A += dL13 * skip;
            curX_B += dX12 * skip; curZ_B += dZ12 * skip; curL_B += dL12 * skip;
            startY = minY;
        }
        if (endY > maxY) endY = maxY; // Clip bottom

        for (int y = startY; y < endY; y++) {
            drawScanline(y, (int)curX_A, curZ_A, curL_A, (int)curX_B, curZ_B, curL_B, color, minX, maxX);
            curX_A += dX13; curZ_A += dZ13; curL_A += dL13;
            curX_B += dX12; curZ_B += dZ12; curL_B += dL12;
        }

        // --- Bottom Half (v2 to v3) ---
        // We need to re-calculate B start values because they originate from v2, not v1.
        // A continues from v1.
        
        // If we completely skipped the top half, we need to advance A to y2
        if (y1 < y2 && y2 < minY) {
             // We are starting LATE in the bottom half.
             // A needs to catch up from y1 to y2... then from y2 to minY.
             // Actually, A is continuous v1->v3.
             // So if we start loop at `minY`, A just needs to advance (minY - y1) steps.
             // BUT, curX_A variable above was already advanced to `endY` (which might be y2 or maxY).
             
             // Simplest approach: Reset A to v1 and advance full distance to startY of this loop.
             curX_A = x1 + dX13 * (Math.max(y2, minY) - y1);
             curZ_A = z1 + dZ13 * (Math.max(y2, minY) - y1);
             curL_A = l1 + dL13 * (Math.max(y2, minY) - y1);
        }
        
        // B starts fresh at v2
        curX_B = x2;
        curZ_B = z2;
        curL_B = l2;
        
        startY = y2;
        endY = y3;
        
        if (startY < minY) {
             int skip = minY - startY;
             // A has already been advanced by the previous loop logic or the catch-up logic
             if (y2 >= minY) { 
                 // If we didn't skip the top half, A is already at y2.
                 // We just need to skip inside this loop.
                 curX_A += dX13 * skip; curZ_A += dZ13 * skip; curL_A += dL13 * skip;
             }
             
             curX_B += dX23 * skip; curZ_B += dZ23 * skip; curL_B += dL23 * skip;
             startY = minY;
        }
        if (endY > maxY) endY = maxY;

        for (int y = startY; y <= endY; y++) {
            drawScanline(y, (int)curX_A, curZ_A, curL_A, (int)curX_B, curZ_B, curL_B, color, minX, maxX);
            curX_A += dX13; curZ_A += dZ13; curL_A += dL13;
            curX_B += dX23; curZ_B += dZ23; curL_B += dL23;
        }
    }
    
    /**
     * Draws a single horizontal line, interpolating Z and Lighting, and checking the Z-Buffer.
     */
    private void drawScanline(int y, int xStart, double zStart, double lStart, 
                                     int xEnd, double zEnd, double lEnd, int color,
                                     int minX, int maxX) {
        // Ensure Left-to-Right
        if (xStart > xEnd) {
            int ti = xStart; xStart = xEnd; xEnd = ti;
            double tz = zStart; zStart = zEnd; zEnd = tz;
            double tl = lStart; lStart = lEnd; lEnd = tl;
        }

        // Clipping (Y-axis) - redundant if called from fillTriangle with correct bounds, but safe.
        if (y < 0 || y >= height) return;

        // Calculate Steps for interpolation
        double width = (double)(xEnd - xStart);
        if (width <= 0) width = 1;

        double zStep = (zEnd - zStart) / width;
        double lStep = (lEnd - lStart) / width;

        double curZ = zStart;
        double curL = lStart;

        // X-axis Clipping bounds
        int realXStart = Math.max(minX, xStart);
        int realXEnd = Math.min(maxX - 1, xEnd);
        
        // Adjust start values if we clipped the start
        double offset = (realXStart - xStart);
        curZ += zStep * offset;
        curL += lStep * offset;

        int bufferRowOffset = y * this.width;
        
        // Base color components
        int rBase = (color >> 16) & 0xFF;
        int gBase = (color >> 8) & 0xFF;
        int bBase = color & 0xFF;

        for (int x = realXStart; x <= realXEnd; x++) {
            int idx = bufferRowOffset + x;
            
            // --- Z-BUFFER TEST ---
            if (curZ < zBuffer[idx]) {
                zBuffer[idx] = curZ; 
                
                // Apply Lighting to Base Color
                int r = (int)(rBase * curL);
                int g = (int)(gBase * curL);
                int b = (int)(bBase * curL);
                
                pixels[idx] = (r << 16) | (g << 8) | b;
            }
            
            curZ += zStep;
            curL += lStep;
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
