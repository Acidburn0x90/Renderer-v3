package game;

import engine.math.Mesh;
import engine.math.PerlinNoise;
import engine.math.Triangle;
import engine.math.Vector3D;

/**
 * Represents the game world terrain.
 * Encapsulates mesh generation and height queries for physics.
 */
public class Terrain {
    public Mesh mesh;
    private final PerlinNoise noise;
    private final double scale;
    private final int width;
    private final int depth;

    // Config for terrain shape
    private static final double NOISE_FREQUENCY = 0.1;
    private static final double HEIGHT_AMPLITUDE = 10.0;

    public Terrain(int width, int depth, double scale, long seed) {
        this.width = width;
        this.depth = depth;
        this.scale = scale;
        this.noise = new PerlinNoise(seed);
        this.mesh = generateMesh();
    }

    /**
     * Calculates the terrain height at a specific world coordinate.
     * <p>
     * This method allows us to query the "Physics Ground" at any point, independent of where the vertices are.
     * It works by reversing the math used to generate the mesh vertices.
     * </p>
     * 
     * @param worldX X coordinate in world space.
     * @param worldZ Z coordinate in world space.
     * @return The Y coordinate of the terrain at this point.
     */
    public double getHeight(double worldX, double worldZ) {
        // 1. Convert World Coords -> Grid Coords (Inverse of generation logic)
        // In generation: xWorld = (xGrid - width/2.0) * scale
        // Therefore: xGrid = (xWorld / scale) + width/2.0
        double xGrid = (worldX / scale) + (width / 2.0);
        double zGrid = (worldZ / scale) + (depth / 2.0);
        
        // 2. Sample the Perlin Noise
        // Note: In generation we iterated over integer grid points (0, 1, 2...).
        // Here, xGrid and zGrid are doubles (e.g., 10.5).
        // This is fine because Perlin Noise is a continuous function—it returns smooth values
        // even between the integer grid points.
        double y = noise.noise(xGrid * NOISE_FREQUENCY, 0, zGrid * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
        
        return y;
    }

    private Mesh generateMesh() {
        Mesh mesh = new Mesh();
        
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                // Calculate position of the current square's corners
                double x0 = (x - width/2.0) * scale;
                double z0 = (z - depth/2.0) * scale;
                
                double x1 = (x + 1 - width/2.0) * scale;
                double z1 = (z + 1 - depth/2.0) * scale;
                
                // Get Heights
                double y00 = noise.noise(x * NOISE_FREQUENCY, 0, z * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
                double y01 = noise.noise(x * NOISE_FREQUENCY, 0, (z+1) * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
                double y10 = noise.noise((x+1) * NOISE_FREQUENCY, 0, z * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
                double y11 = noise.noise((x+1) * NOISE_FREQUENCY, 0, (z+1) * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
                
                // Coloring Logic
                int color;
                if (y00 < 0.0) {
                    color = 0x2E8B57; // Sea Green
                } else if (y00 < 4.0) {
                    color = 0x8B4513; // Saddle Brown
                } else {
                    color = 0xFFFFFF; // Snow
                }

                // Triangle 1
                mesh.triangles.add(new Triangle(
                    new Vector3D(x0, y00, z0),
                    new Vector3D(x1, y11, z1),
                    new Vector3D(x0, y01, z1),
                    color
                ));
                
                // Triangle 2
                mesh.triangles.add(new Triangle(
                    new Vector3D(x0, y00, z0),
                    new Vector3D(x1, y10, z0),
                    new Vector3D(x1, y11, z1),
                    color
                ));
            }
        }
        return mesh;
    }
}
