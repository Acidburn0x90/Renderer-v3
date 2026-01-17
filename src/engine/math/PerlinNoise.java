package engine.math;

import java.util.Random;

/**
 * A standard implementation of Perlin Noise (Simplex/Gradient noise).
 * <p>
 * Perlin Noise generates smooth, pseudo-random values. Unlike pure random noise (TV static),
 * Perlin noise transitions smoothly between values, making it perfect for generating
 * natural-looking terrain, clouds, and textures.
 * </p>
 */
public class PerlinNoise {
    
    // Permutation table (Randomly ordered 0-255)
    // Used to pseudo-randomly hash coordinates into gradient vectors.
    private final int[] p = new int[512]; 
    private final int[] permutation = new int[256];

    public PerlinNoise(long seed) {
        Random rand = new Random(seed);
        
        // Initialize permutation table
        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }

        // Shuffle
        for (int i = 0; i < 256; i++) {
            int swapIndex = rand.nextInt(256);
            int temp = permutation[i];
            permutation[i] = permutation[swapIndex];
            permutation[swapIndex] = temp;
        }

        // Duplicate for overflow handling (avoids % 256 in the loop)
        for (int i = 0; i < 512; i++) {
            p[i] = permutation[i % 256];
        }
    }

    /**
     * Generates a noise value between roughly -1.0 and 1.0.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return noise value
     */
    public double noise(double x, double y, double z) {
        // Find unit cube that contains point
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;

        // Find relative x,y,z of point in cube
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);

        // Compute fade curves for each of x,y,z
        double u = fade(x);
        double v = fade(y);
        double w = fade(z);

        // Hash coordinates of the 8 cube corners
        int A = p[X] + Y;
        int AA = p[A] + Z;
        int AB = p[A + 1] + Z;
        int B = p[X + 1] + Y;
        int BA = p[B] + Z;
        int BB = p[B + 1] + Z;

        // Add blended results from 8 corners of the cube
        return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z),
                        grad(p[BA], x - 1, y, z)),
                lerp(u, grad(p[AB], x, y - 1, z),
                        grad(p[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(p[AA + 1], x, y, z - 1),
                        grad(p[BA + 1], x - 1, y, z - 1)),
                lerp(u, grad(p[AB + 1], x, y - 1, z - 1),
                        grad(p[BB + 1], x - 1, y - 1, z - 1))));
    }

    // Fade function (smootherstep) to ease coordinate values
    // 6t^5 - 15t^4 + 10t^3
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    // Linear Interpolation
    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    // Gradient calculation
    // Calculates the dot product of a randomly selected gradient vector and the distance vector.
    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}