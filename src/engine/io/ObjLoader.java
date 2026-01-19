package engine.io;

import engine.math.Mesh;
import engine.math.Triangle;
import engine.math.Vector3D;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple OBJ file loader.
 * Supports vertices (v) and faces (f).
 * Does not support normals (vn) or texture coords (vt) yet.
 * Assumes faces are triangles.
 */
public class ObjLoader {
    
    /**
     * Loads an OBJ file from disk into a Mesh.
     * @param filePath Path to the .obj file.
     * @return The constructed Mesh.
     */
    public static Mesh load(String filePath) {
        Mesh mesh = new Mesh();
        List<Vector3D> vertices = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Clean line: remove whitespace and ignore comments (#)
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                // Split by spaces to get tokens
                // e.g. "v 1.0 2.0 3.0" -> ["v", "1.0", "2.0", "3.0"]
                String[] tokens = line.split("\\s+");
                
                if (tokens[0].equals("v")) {
                    // --- Vertex Definition ---
                    // Format: v x y z
                    double x = Double.parseDouble(tokens[1]);
                    double y = Double.parseDouble(tokens[2]);
                    double z = Double.parseDouble(tokens[3]);
                    vertices.add(new Vector3D(x, y, z));
                    
                } else if (tokens[0].equals("f")) {
                    // --- Face Definition ---
                    // Format: f v1/vt1/vn1 v2/... v3/...
                    // An OBJ face references vertices by their INDEX in the file (1-based).
                    
                    if (tokens.length < 4) continue; // Skip lines that aren't at least triangles
                    
                    // Parse the first 3 vertices to form a triangle
                    int[] vIndices = new int[3];
                    for (int i = 0; i < 3; i++) {
                        String vToken = tokens[i + 1];
                        // Faces can look like "1/2/3" (v/vt/vn) or "1//3" (v//vn) or just "1" (v).
                        // We split by "/" and only take the first number (Vertex Index).
                        String[] subTokens = vToken.split("/");
                        
                        // IMPORTANT: OBJ files use 1-based indexing (starts at 1).
                        // Java Lists use 0-based indexing. So we subtract 1.
                        vIndices[i] = Integer.parseInt(subTokens[0]) - 1; 
                    }

                    Vector3D p1 = vertices.get(vIndices[0]);
                    Vector3D p2 = vertices.get(vIndices[1]);
                    Vector3D p3 = vertices.get(vIndices[2]);
                    
                    // Add the triangle to the mesh (Default to White color)
                    mesh.triangles.add(new Triangle(p1, p2, p3, 0xFFFFFF));
                    
                    // --- Triangulation (Quad Support) ---
                    // If the face has 4 vertices (Quad), we split it into TWO triangles.
                    // Triangle 1: v1, v2, v3 (Already added above)
                    // Triangle 2: v1, v3, v4
                    if (tokens.length == 5) {
                         String vToken = tokens[4];
                         String[] subTokens = vToken.split("/");
                         int v4Index = Integer.parseInt(subTokens[0]) - 1;
                         Vector3D p4 = vertices.get(v4Index);
                         
                         mesh.triangles.add(new Triangle(p1, p3, p4, 0xFFFFFF));
                    }
                }
            }
            System.out.println("Loaded Model: " + filePath + " (" + mesh.triangles.size() + " triangles)");
        } catch (IOException e) {
            System.err.println("Failed to load model: " + filePath);
            e.printStackTrace();
        }
        return mesh;
    }
}
