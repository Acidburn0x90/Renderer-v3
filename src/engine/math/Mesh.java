package engine.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 3D object consisting of a collection of triangles.
 */
public class Mesh {
    public List<Triangle> triangles;

    public Mesh() {
        this.triangles = new ArrayList<>();
    }
}