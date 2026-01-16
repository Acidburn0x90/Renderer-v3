# Java Software Renderer v3

A professional-grade 3D engine built from scratch in Java without external graphics libraries (OpenGL/Vulkan). This project demonstrates the fundamental mathematics and architecture of 3D computer graphics.

## 📂 Project Structure

The project is organized into logical modules to separate concerns:

### `src/engine/core`
The brain of the operation.
*   **`Engine.java`**: The abstract base class handling the Game Loop (`start`, `run`, `update`, `render`), Window management, and Input timing. It provides the skeleton that `Main` extends.
*   **`Renderer.java`**: The graphics pipeline. It accepts a `Mesh` and a `Camera`, performs all 3D math (transformations, clipping, projection, sorting), and rasterizes the result to the `Screen`.
*   **`Camera.java`**: Represents the observer. Stores Position (x, y, z) and Rotation (Pitch, Yaw). Handles trigonometric movement logic (FPS style).

### `src/engine/graphics`
The raw pixel manipulation layer.
*   **`Screen.java`**: Wraps a `BufferedImage` and provides hardware-accelerated drawing methods (`fillTriangle`, `clearPixels`). Uses Java 2D `Graphics` for performance.

### `src/engine/display`
The operating system integration.
*   **`Window.java`**: Manages the `JFrame` and `Canvas`. Handles Double Buffering (BufferStrategy), Fullscreen mode, and Input Listeners.

### `src/engine/math`
The mathematical foundation.
*   **`Vector3D.java`**: A 3-component vector (x, y, z) with operations for Addition, Subtraction, Dot Product, Cross Product, and Normalization.
*   **`Matrix4x4.java`**: A 4x4 Matrix used for complex transformations. Includes factory methods for Projection Matrices and Rotation Matrices.
*   **`Mesh.java`**: A collection of Triangles representing a 3D object.
*   **`Triangle.java`**: A simple container for 3 `Vector3D` vertices.

### `src/engine/io`
Input handling.
*   **`Input.java`**: A unified listener for Keyboard and Mouse. Tracks key states and calculates Mouse Deltas for looking around.

---

## 🚀 The Rendering Pipeline

Every frame, the `Renderer` performs the following steps for every Triangle in a Mesh:

### 1. Model & View Transformation
*   **Concept**: Converting local coordinates to World Space, then to View Space (relative to Camera).
*   **Operation**: `Vertex_View = (Vertex_World - Camera_Pos) * Camera_RotationMatrix`.

### 2. Near Plane Clipping
*   **Concept**: Preventing the "Division by Zero" crash. We cannot project points that are exactly at or behind the camera's eye.
*   **Operation**: If any vertex has a `Z < 0.1`, the triangle is discarded (skipped).

### 3. Backface Culling
*   **Concept**: Optimization. We shouldn't draw the inside faces of a solid object.
*   **Operation**: Calculate `DotProduct(Triangle_Normal, Camera_Ray)`. If positive, cull.

### 4. Z-Sorting (Painter's Algorithm)
*   **Concept**: Ensuring close objects appear in front of far objects.
*   **Operation**: Collect all visible triangles, calculate their average depth, and sort them Far-to-Near before drawing.

### 5. Lighting (Flat Shading)
*   **Concept**: Giving depth to the object.
*   **Operation**: `Brightness = DotProduct(Triangle_Normal, Light_Direction)`. This value scales the triangle's color.

### 6. Projection & Rasterization
*   **Concept**: Converting 3D coordinates to 2D pixels.
*   **Operation**: `x = x / z`, `y = y / z`. Then call `g.fillPolygon()` to draw the solid shape.

---

## 🎮 Controls

*   **Mouse**: Look Around (Infinite Mouse Lock).
*   **W / S**: Move Forward / Backward (Directional).
*   **A / D**: Strafe Left / Right.
*   **Space / Ctrl**: Fly Up / Down (Y-axis).
*   **Shift**: Sprint.
*   **ESC**: Quit.

## 🧮 Math Cheat Sheet

*   **Dot Product**: Returns a single number describing how "aligned" two vectors are. Used for Lighting and Culling.
*   **Cross Product**: Returns a new Vector perpendicular to two others. Used to find the "Normal" of a surface.
*   **Projection Matrix**: Encodes Aspect Ratio, Field of View (FOV), and Z-scaling logic.