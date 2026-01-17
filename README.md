# Java Software Renderer v3

A professional-grade 3D engine built from scratch in Java without external graphics libraries (OpenGL/Vulkan). This project demonstrates the fundamental mathematics and architecture of 3D computer graphics by implementing them manually.

## 🌟 Key Features

*   **Software Rasterization**: Implements a pure Java rasterizer using `Graphics2D` and direct pixel array manipulation for high-speed polygon filling.
*   **3D Pipeline**: Full Vertex Transformation pipeline (Model -> View -> Clip -> Projection -> Screen).
*   **Global Sorting**: Implements **Painter's Algorithm** with global triangle sorting to handle depth correctly across multiple objects.
*   **Procedural Terrain**: Uses **Perlin Noise** to generate infinite (40x40 chunk) rolling hills.
*   **Lighting**: Fixed Directional Lighting (Sun) calculated in World Space, rotated into View Space for correct shading relative to the camera. Includes height-based terrain coloring.
*   **FPS Camera**: Free-look camera with Mouse Locking (infinite rotation) and WASD movement using proper trigonometry.
*   **Performance**: Configurable Render Scale (e.g., render at 25% resolution and upscale) to achieve high FPS even on 4K screens.

## 📂 Project Structure

### `src/engine` (The Technology)
The engine package contains the reusable core technology, completely decoupled from any specific game logic.

*   **`core/Engine.java`**: The abstract base class that manages the **Game Loop** (Fixed Time-Step), Window creation, and Input polling. It uses `Thread.sleep` or high-resolution timers to maintain 60 Updates/Second while rendering as fast as possible.
*   **`core/Renderer.java`**: The heart of the graphics engine. It handles the entire pipeline:
    1.  **Transform**: Rotating/Translating vertices.
    2.  **Clip**: Near-plane clipping to prevent division-by-zero errors behind the camera.
    3.  **Cull**: Back-face culling using dot products to skip hidden triangles.
    4.  **Sort**: Sorting visible triangles by depth (Painter's Algorithm).
    5.  **Light**: Directional lighting calculation.
    6.  **Rasterize**: Drawing the final triangles to the screen.
*   **`core/Camera.java`**: Represents the observer. Handles Position (x,y,z) and Rotation (Pitch, Yaw). It implements **First Person Shooter** movement logic, converting WASD inputs into movement vectors relative to the current Yaw angle.
*   **`graphics/Screen.java`**: A wrapper around `BufferedImage`. It accesses the underlying `int[]` pixel array directly for maximum write performance, bypassing standard, slower `setRGB` methods.
*   **`display/Window.java`**: Manages the OS Window (JFrame) and buffering. It implements **Triple Buffering** via `BufferStrategy` to eliminate screen tearing and flickering.
*   **`math/*`**: A robust math library built from scratch:
    *   `Matrix4x4`: Handles 3D transformations (Rotation, Projection, scaling).
    *   `Vector3D`: Handles vector math (Dot Product, Cross Product, Normalization).
    *   `PerlinNoise`: Implementation of gradient noise for procedural generation.
*   **`io/Input.java`**: Bridges the gap between Java's Event-Driven input (listeners) and the Engine's Polling-based input (checking state every frame).

### `src/game` (The Content)
The game package uses the engine to create a specific experience.

*   **`DemoGame.java`**: Extends `Engine`. It sets up the specific scene (Terrain), configures the camera start position, and defines the control scheme (WASD + Mouse Look).
*   **`WorldGenerator.java`**: A helper class that uses `PerlinNoise` to generate a mesh grid of triangles, manipulating their Y-coordinates to create hills and valleys.

### `src/Main.java`
*   The entry point. It simply instantiates `DemoGame` and calls `start()`.

---

## 🚀 The Rendering Pipeline (Detailed)

Every frame, the `Renderer` performs the following steps:

1.  **Transformation**: Converts all 3D models into **View Space**. This means "moving the world" so the camera is at (0,0,0) facing forward.
2.  **Clipping**: Discards triangles that are mathematically "behind" the camera (Z < 0.1) to prevent graphical glitches and crashes.
3.  **Culling**: Calculates the **Surface Normal** of every triangle. If the normal points away from the camera (Dot Product > 0), the triangle is skipped.
4.  **Collection**: Visible triangles are added to a **Global Render List**.
5.  **Sorting**: The global list is sorted by average Z-depth (Far to Near). This ensures distant mountains are drawn *before* nearby ground.
6.  **Lighting**: 
    *   Sun Vector is defined in World Space (e.g. Horizon).
    *   It is rotated into View Space by the Camera Matrix so it matches the rotated geometry.
    *   `Brightness = DotProduct(Normal, ViewSunVector)`.
7.  **Projection**: Converts 3D View coordinates (X, Y, Z) into 2D Normalized Device Coordinates (-1 to 1) using a Projection Matrix.
8.  **Rasterization**: Maps 2D coordinates to Screen Pixels (Width, Height) and draws them using `Graphics2D` hardware acceleration.

---

## 🎮 Controls

*   **Mouse**: Look Around (Infinite Mouse Lock).
*   **W / S**: Move Forward / Backward (Directional).
*   **A / D**: Strafe Left / Right.
*   **Space / Ctrl**: Fly Up / Down (Y-axis).
*   **Shift**: Sprint.
*   **ESC**: Quit.

## 🛠️ Usage

To run the engine:
1.  Open the project in IntelliJ or any Java IDE.
2.  Run `src/Main.java`.
3.  Enjoy the Perlin Noise Terrain!

---
*Created as a learning project to understand the internals of 3D Graphics Engines.*
