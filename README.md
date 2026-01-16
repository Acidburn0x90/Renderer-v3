# Java Software Renderer v3

A professional-grade 3D engine built from scratch in Java without external graphics libraries (OpenGL/Vulkan). This project demonstrates the fundamental mathematics and architecture of 3D computer graphics.

## 📂 Project Structure

The project is organized into logical modules to separate concerns:

### `src/engine/core`
The brain of the operation.
*   **`Engine.java`**: The abstract base class handling the Game Loop (`start`, `run`, `update`, `render`), Window management, and Input timing. It provides the skeleton that `Main` extends.
*   **`Renderer.java`**: The graphics pipeline. It accepts a `Mesh` and a `Camera`, performs all 3D math (transformations, clipping, projection), and draws the result to the `Screen`.
*   **`Camera.java`**: Represents the observer. Stores Position (x, y, z) and Rotation (Pitch, Yaw). Handles movement logic.

### `src/engine/graphics`
The raw pixel manipulation layer.
*   **`Screen.java`**: Wraps a `BufferedImage` and its underlying `int[]` pixel buffer. Provides fast, direct access to pixels and implements rasterization algorithms (like Bresenham's Line Algorithm) with bounds checking (clipping).

### `src/engine/display`
The operating system integration.
*   **`Window.java`**: Manages the `JFrame` and `Canvas`. Handles Double Buffering (BufferStrategy) to prevent screen flickering during rendering.

### `src/engine/math`
The mathematical foundation.
*   **`Vector3D.java`**: A 3-component vector (x, y, z) with operations for Addition, Subtraction, Dot Product, Cross Product, and Normalization.
*   **`Matrix4x4.java`**: A 4x4 Matrix used for complex transformations. Includes factory methods for Projection Matrices and Rotation Matrices.
*   **`Mesh.java`**: A collection of Triangles representing a 3D object.
*   **`Triangle.java`**: A simple container for 3 `Vector3D` vertices.

### `src/engine/io`
Input handling.
*   **`Input.java`**: A key listener that maintains the state of the keyboard (pressed/released) for smooth polling in the game loop.

---

## 🚀 The Rendering Pipeline

Every frame, the `Renderer` performs the following steps for every Triangle in a Mesh:

### 1. Model Transformation
*   **Concept**: Converting local coordinates (relative to the cube's center) to world coordinates.
*   **Operation**: Currently, we create a **Deep Copy** of the vertices to prevent modifying the original mesh.

### 2. View Transformation (Camera)
*   **Concept**: Moving the world relative to the camera. If the camera moves Forward (+Z), the world moves Backward (-Z).
*   **Operation**: `Vertex_View = Vertex_World - Camera_Position`.

### 3. Near Plane Clipping
*   **Concept**: Preventing the "Division by Zero" crash. We cannot project points that are exactly at or behind the camera's eye.
*   **Operation**: If any vertex has a `Z < 0.1`, the triangle is discarded (skipped) for safety. *Future Improvement: Split the triangle into two smaller ones instead of skipping.*

### 4. Backface Culling
*   **Concept**: Optimization. We shouldn't draw the inside faces of a solid object because the front faces block them.
*   **Math**:
    1.  Calculate the **Normal** (N) of the triangle surface using the **Cross Product** of two edges.
    2.  Calculate the **Camera Ray** (V) from the eye to the triangle.
    3.  Calculate **Dot Product** (N · V).
    4.  If the result is `< 0`, the face is looking at us. **Draw it.**
    5.  If `> 0`, it's looking away. **Cull it.**

### 5. Projection
*   **Concept**: Converting 3D coordinates (X, Y, Z) into 2D Normalized Device Coordinates (NDC) between -1 and 1.
*   **Math**: `Vertex_NDC = Vertex_View * ProjectionMatrix`.
*   **Effect**: Objects get smaller as they get further away (`Z` division).

### 6. Screen Scaling
*   **Concept**: Converting NDC (-1 to 1) to actual pixel coordinates (0 to 1920).
*   **Operation**:
    *   `x = (x + 1) * 0.5 * Width`
    *   `y = (y + 1) * 0.5 * Height`

### 7. Rasterization
*   **Concept**: Turning mathematical points into actual colored pixels.
*   **Operation**: Calls `Screen.drawLine()` to connect the vertices (Wireframe rendering).

---

## 🎮 Controls

*   **W / S**: Move Camera Forward / Backward (Z-axis).
*   **A / D**: Strafe Camera Left / Right (X-axis).
*   **Space / Ctrl**: Fly Up / Down (Y-axis).
*   **Shift**: Sprint (Move faster).

## 🧮 Math Cheat Sheet

*   **Dot Product**: Returns a single number describing how "aligned" two vectors are.
    *   `1.0` = Same direction.
    *   `0.0` = Perpendicular (90 degrees).
    *   `-1.0` = Opposite direction.
*   **Cross Product**: Returns a new Vector perpendicular to two others. Used to find the "Normal" (facing direction) of a surface.
*   **Projection Matrix**: A 4x4 grid of numbers that encodes Aspect Ratio, Field of View (FOV), and Z-scaling logic to squish a 3D frustum into a 2D square.
