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
    
    // Bounding Sphere for Culling
    public Vector3D center = new Vector3D(0,0,0);
    public double radius = 0;

    public Mesh() {
        this.triangles = new ArrayList<>();
    }
    
    /**
     * Translates (moves) the entire mesh by the specified offset.
     * @param x Offset X
     * @param y Offset Y
     * @param z Offset Z
     */
    public void translate(double x, double y, double z) {
        for (Triangle tri : triangles) {
            for (Vector3D v : tri.v) {
                v.x += x;
                v.y += y;
                v.z += z;
            }
        }
        recalculateBounds(); // Update bounds after moving
    }
    
    /**
     * Calculates the Bounding Sphere (Center and Radius) of the mesh.
     * This is used for Frustum Culling (checking if the object is visible).
     */
    public void recalculateBounds() {
        if (triangles.isEmpty()) return;
        
        // 1. Calculate Average Center (Centroid)
        double sumX = 0, sumY = 0, sumZ = 0;
        int count = 0;
        
        for (Triangle tri : triangles) {
            for (Vector3D v : tri.v) {
                sumX += v.x;
                sumY += v.y;
                sumZ += v.z;
                count++;
            }
        }
        
        center.x = sumX / count;
        center.y = sumY / count;
        center.z = sumZ / count;
        
        // 2. Calculate Radius (Distance to furthest point)
        double maxDistSq = 0;
        
        for (Triangle tri : triangles) {
            for (Vector3D v : tri.v) {
                double dx = v.x - center.x;
                double dy = v.y - center.y;
                double dz = v.z - center.z;
                double distSq = dx*dx + dy*dy + dz*dz;
                if (distSq > maxDistSq) {
                    maxDistSq = distSq;
                }
            }
        }
        
        radius = Math.sqrt(maxDistSq);
    }
}
