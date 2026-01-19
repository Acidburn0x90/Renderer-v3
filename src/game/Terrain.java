package game;

import engine.math.Mesh;
import engine.math.PerlinNoise;
import engine.math.Triangle;
import engine.math.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the game world terrain.
 * Encapsulates mesh generation and height queries for physics.
 */
public class Terrain {
    // We now split the world into multiple smaller meshes ("Chunks").
    // This allows the renderer to skip drawing chunks that are behind the player.
    public List<Mesh> chunks = new ArrayList<>();
    
    private final PerlinNoise noise;
    private final double scale;
    private final int width;
    private final int depth;

    // Config for terrain shape
    private static final double NOISE_FREQUENCY = 0.1;
    private static final double HEIGHT_AMPLITUDE = 10.0;
    
    // Chunk size (e.g., 10x10 squares per chunk)
    private static final int CHUNK_SIZE = 10;

    public Terrain(int width, int depth, double scale, long seed) {
        this.width = width;
        this.depth = depth;
        this.scale = scale;
        this.noise = new PerlinNoise(seed);
        
        generateChunks();
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

    /**
     * Calculates the surface normal at a specific grid coordinate.
     * Used for smooth (Gouraud) shading.
     */
    public Vector3D getNormal(int x, int z) {
        // We approximate the normal by looking at the heights of neighbors.
        // n = (-dy/dx, 1, -dy/dz)
        double hL = noise.noise((x - 1) * NOISE_FREQUENCY, 0, z * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
        double hR = noise.noise((x + 1) * NOISE_FREQUENCY, 0, z * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
        double hD = noise.noise(x * NOISE_FREQUENCY, 0, (z - 1) * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
        double hU = noise.noise(x * NOISE_FREQUENCY, 0, (z + 1) * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
        
        return new Vector3D(hL - hR, 2.0, hD - hU).normalize();
    }

    private void generateChunks() {
        // Loop through the grid in steps of CHUNK_SIZE
        for (int startX = 0; startX < width; startX += CHUNK_SIZE) {
            for (int startZ = 0; startZ < depth; startZ += CHUNK_SIZE) {
                
                Mesh chunk = new Mesh();
                
                int endX = Math.min(startX + CHUNK_SIZE, width);
                int endZ = Math.min(startZ + CHUNK_SIZE, depth);
                
                for (int x = startX; x < endX; x++) {
                    for (int z = startZ; z < endZ; z++) {
                        // Coordinates
                        double x0 = (x - width/2.0) * scale;
                        double z0 = (z - depth/2.0) * scale;
                        double x1 = (x + 1 - width/2.0) * scale;
                        double z1 = (z + 1 - depth/2.0) * scale;
                        
                        // Heights
                        double y00 = noise.noise(x * NOISE_FREQUENCY, 0, z * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
                        double y01 = noise.noise(x * NOISE_FREQUENCY, 0, (z+1) * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
                        double y10 = noise.noise((x+1) * NOISE_FREQUENCY, 0, z * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
                        double y11 = noise.noise((x+1) * NOISE_FREQUENCY, 0, (z+1) * NOISE_FREQUENCY) * HEIGHT_AMPLITUDE;
                        
                        // Normals
                        Vector3D n00 = getNormal(x, z);
                        Vector3D n01 = getNormal(x, z + 1);
                        Vector3D n10 = getNormal(x + 1, z);
                        Vector3D n11 = getNormal(x + 1, z + 1);

                        // Coloring
                        int color = (y00 < 0.0) ? 0x2E8B57 : (y00 < 4.0) ? 0x8B4513 : 0xFFFFFF;

                        // Triangle 1
                        Triangle t1 = new Triangle(
                            new Vector3D(x0, y00, z0),
                            new Vector3D(x1, y11, z1),
                            new Vector3D(x0, y01, z1),
                            color
                        );
                        t1.n[0] = n00; t1.n[1] = n11; t1.n[2] = n01;
                        chunk.triangles.add(t1);
                        
                        // Triangle 2
                        Triangle t2 = new Triangle(
                            new Vector3D(x0, y00, z0),
                            new Vector3D(x1, y10, z0),
                            new Vector3D(x1, y11, z1),
                            color
                        );
                        t2.n[0] = n00; t2.n[1] = n10; t2.n[2] = n11;
                        chunk.triangles.add(t2);
                    }
                }
                
                chunk.recalculateBounds();
                chunks.add(chunk);
            }
        }
    }
}
