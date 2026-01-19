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
        // "Epic Scale" settings: Lower frequency = Wider hills. Higher amplitude = Taller mountains.
        private static final double NOISE_FREQUENCY = 0.02;
        private static final double HEIGHT_AMPLITUDE = 30.0;
        
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
         * Samples the fractal noise at a given grid coordinate.
         * This combines multiple "octaves" of noise to create realistic terrain.
         */
        private double sampleHeight(double x, double z) {
            double total = 0;
            double frequency = NOISE_FREQUENCY;
            double amplitude = HEIGHT_AMPLITUDE;
            double persistence = 0.45; // Slightly lower persistence so small details don't overpower the shape
            double lacunarity = 2.1;   // How much frequency increases per octave
            
            // Layer 1: Big Mountains
            total += noise.noise(x * frequency, 0, z * frequency) * amplitude;
            
            // Layer 2: Smaller Hills
            frequency *= lacunarity;
            amplitude *= persistence;
            total += noise.noise(x * frequency, 0, z * frequency) * amplitude;
            
            // Layer 3: Surface Roughness/Rocks
            frequency *= lacunarity;
            amplitude *= persistence;
            total += noise.noise(x * frequency, 0, z * frequency) * amplitude;
            
            // Layer 4: Fine Detail (Extra grit)
            frequency *= lacunarity;
            amplitude *= persistence;
            total += noise.noise(x * frequency, 0, z * frequency) * amplitude;
            
            return total;
        }
    /**
     * Calculates the terrain height at a specific world coordinate.
     */
    public double getHeight(double worldX, double worldZ) {
        double xGrid = (worldX / scale) + (width / 2.0);
        double zGrid = (worldZ / scale) + (depth / 2.0);
        return sampleHeight(xGrid, zGrid);
    }

    /**
     * Calculates the surface normal at a specific grid coordinate.
     * Updated to handle multi-layered noise accurately.
     */
    public Vector3D getNormal(int x, int z) {
        double hL = sampleHeight(x - 0.1, z);
        double hR = sampleHeight(x + 0.1, z);
        double hD = sampleHeight(x, z - 0.1);
        double hU = sampleHeight(x, z + 0.1);
        
        // We use a small epsilon (0.1) for sampling to get a smooth local gradient
        return new Vector3D(hL - hR, 0.2, hD - hU).normalize();
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
                        
                        // Heights (using Fractal Noise)
                        double y00 = sampleHeight(x, z);
                        double y01 = sampleHeight(x, z + 1);
                        double y10 = sampleHeight(x + 1, z);
                        double y11 = sampleHeight(x + 1, z + 1);
                        
                        // Normals
                        Vector3D n00 = getNormal(x, z);
                        Vector3D n01 = getNormal(x, z + 1);
                        Vector3D n10 = getNormal(x + 1, z);
                        Vector3D n11 = getNormal(x + 1, z + 1);

                        // Improved Dynamic Coloring
                        int color;
                        if (y00 < -2.0) color = 0x2E8B57; // Deep Sea
                        else if (y00 < 0.0) color = 0x3CB371; // Shallow Water
                        else if (y00 < 1.0) color = 0xC2B280; // Sand/Beach
                        else if (y00 < 8.0) color = 0x8B4513; // Dirt/Hills
                        else color = 0xFFFFFF; // Snow Peaks

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
