package game;

import engine.math.Mesh;
import engine.math.PerlinNoise;
import engine.math.Triangle;
import engine.math.Vector3D;

/**
 * Static helper class to generate procedural meshes.
 */
public class WorldGenerator {

    /**
     * Generates a "Terrain" mesh using Perlin Noise.
     * <p>
     * It creates a grid of squares, where each vertex's Y-height is determined by 
     * the Perlin Noise function. Each square is then split into two triangles.
     * </p>
     * 
     * @param width Width of the grid (number of vertices).
     * @param depth Depth of the grid (number of vertices).
     * @param scale Distance between vertices.
     * @param seed Random seed for the noise generator.
     * @return A Mesh containing the generated terrain.
     */
    public static Mesh generateTerrain(int width, int depth, double scale, long seed) {
        Mesh mesh = new Mesh();
        PerlinNoise noise = new PerlinNoise(seed);

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                // Calculate position of the current square's corners
                double x0 = (x - width/2.0) * scale;
                double z0 = (z - depth/2.0) * scale;
                
                double x1 = (x + 1 - width/2.0) * scale;
                double z1 = (z + 1 - depth/2.0) * scale;
                
                // Get Heights from Perlin Noise (Scaled)
                // We sample smaller steps (x*0.1) to get smooth hills.
                // Multiplying by 10.0 gives the hills vertical height.
                double y00 = noise.noise(x * 0.1, 0, z * 0.1) * 10.0;
                double y01 = noise.noise(x * 0.1, 0, (z+1) * 0.1) * 10.0;
                double y10 = noise.noise((x+1) * 0.1, 0, z * 0.1) * 10.0;
                double y11 = noise.noise((x+1) * 0.1, 0, (z+1) * 0.1) * 10.0;
                
                // Procedural Coloring based on height
                // Sea Level -> Ground -> Snow Peaks
                int color;
                if (y00 < 0.0) {
                    color = 0x2E8B57; // Sea Green (Valley)
                } else if (y00 < 4.0) {
                    color = 0x8B4513; // Saddle Brown (Slopes)
                } else {
                    color = 0xFFFFFF; // Snow (Peaks)
                }

                // Create 2 Triangles to form a square (Quad)
                
                // Triangle 1 (Top-Left)
                mesh.triangles.add(new Triangle(
                    new Vector3D(x0, y00, z0),
                    new Vector3D(x1, y11, z1),
                    new Vector3D(x0, y01, z1),
                    color
                ));
                
                // Triangle 2 (Bottom-Right)
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