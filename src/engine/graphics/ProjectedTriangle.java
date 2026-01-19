package engine.graphics;

/**
 * A simple data structure to hold a triangle that is ready to be drawn.
 * Contains Screen-Space coordinates (Integers), Depth (Doubles), and Lighting info.
 * Used for buffering the render queue.
 */
public class ProjectedTriangle {
    public int x1, y1, x2, y2, x3, y3;
    public double z1, z2, z3;
    public double l1, l2, l3;
    public int color;
}
