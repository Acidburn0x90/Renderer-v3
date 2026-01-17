package engine.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 3D object consisting of a collection of triangles.
 * <p>
 * A Mesh is simply a container for triangles. In more complex engines, 
 * this would also hold Position, Rotation, and Scale for the entire object.
 * </p>
 */
public class Mesh {
    public List<Triangle> triangles;

    public Mesh() {
        this.triangles = new ArrayList<>();
    }
}
