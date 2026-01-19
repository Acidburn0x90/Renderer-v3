# Java Software Renderer v3

A professional-grade 3D engine built from scratch in Java without external graphics libraries (OpenGL/Vulkan). This project demonstrates the fundamental mathematics and architecture of 3D computer graphics by implementing them manually.

## 🌟 Key Features

*   **Software Rasterization**: Implements a pure Java rasterizer using `Graphics2D` and direct pixel array manipulation for high-speed polygon filling.
*   **3D Pipeline**: Full Vertex Transformation pipeline (Model -> View -> Clip -> Projection -> Screen).
*   **Global Sorting**: Implements **Painter's Algorithm** with global triangle sorting to handle depth correctly across multiple objects.
*   **Procedural Terrain**: Uses **Perlin Noise** to generate infinite rolling hills.
    *   **Physics Support**: Includes a height-map query system (`Terrain.getHeight`) for collision detection and walking.
*   **OBJ Model Loading**: Supports loading standard `.obj` 3D models (vertices and faces) from disk.
*   **Lighting**: Fixed Directional Lighting (Sun) calculated in World Space, rotated into View Space for correct shading relative to the camera. Includes height-based terrain coloring.
*   **FPS Camera**: Free-look camera with Mouse Locking (infinite rotation) and WASD movement using proper trigonometry.
    *   **Walking Mode**: Gravity simulation that snaps the camera to the terrain surface.
*   **Performance**: Configurable Render Scale (e.g., render at 12.5% resolution and upscale) to achieve high FPS even on 4K screens.

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
*   **`io/ObjLoader.java`**: A utility to parse `.obj` 3D model files into `Mesh` objects.
*   **`core/Camera.java`**: Represents the observer. Handles Position (x,y,z) and Rotation (Pitch, Yaw).
*   **`graphics/Screen.java`**: A wrapper around `BufferedImage`. It accesses the underlying `int[]` pixel array directly for maximum write performance.
*   **`display/Window.java`**: Manages the OS Window (JFrame) and buffering. It implements **Triple Buffering** via `BufferStrategy` to eliminate screen tearing and flickering.
*   **`math/*`**: A robust math library built from scratch (`Matrix4x4`, `Vector3D`, `PerlinNoise`).

### `src/game` (The Content)
The game package uses the engine to create a specific experience.

*   **`DemoGame.java`**: The main game implementation. It loads the scene, handles inputs, and toggles walking modes.
*   **`Terrain.java`**: Encapsulates the procedural generation logic and provides physics height queries for the player.

### `src/Main.java`
*   The entry point. It simply instantiates `DemoGame` and calls `start()`.

---

## 🎮 Controls

*   **Mouse**: Look Around (Infinite Mouse Lock).
*   **W / S**: Move Forward / Backward (Directional).
*   **A / D**: Strafe Left / Right.
*   **Space / Ctrl**: Fly Up / Down (Y-axis).
*   **G**: **Toggle Walking Mode** (Gravity on/off).
*   **Shift**: Sprint.
*   **ESC**: Quit.

## 🛠️ Usage

To run the engine:
1.  Open the project in IntelliJ or any Java IDE.
2.  Run `src/Main.java`.
3.  Press **'G'** to drop to the ground and walk around!

---
*Created as a learning project to understand the internals of 3D Graphics Engines.*