package engine.math;

import engine.graphics.Screen;

/**
 * Represents a straight line between two points in 2D space.
 */
public class Line extends Shape {

    private Point2D start;
    private Point2D end;

    /**
     * Creates a new Line.
     * @param start The starting point.
     * @param end   The ending point.
     * @param color The color of the line (hex string).
     */
    public Line(Point2D start, Point2D end, String color) {
        super(color);
        this.start = start;
        this.end = end;
    }

    @Override
    public void render(Screen screen) {
        screen.drawLine(start.x, start.y, end.x, end.y, this.color);
    }
}
