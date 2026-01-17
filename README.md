# Java Software Renderer v3

A professional-grade 3D engine built from scratch in Java without external graphics libraries (OpenGL/Vulkan). This project demonstrates the fundamental mathematics and architecture of 3D computer graphics.

## 🌟 Key Features

*   **Software Rasterization**: Implements a pure Java rasterizer using `Graphics2D` for high-speed polygon filling.
*   **3D Pipeline**: Full Vertex Transformation pipeline (Model -> View -> Clip -> Projection -> Screen).
*   **Global Sorting**: Implements **Painter's Algorithm** with global triangle sorting to handle depth correctly across multiple objects.
*   **Procedural Terrain**: Uses **Perlin Noise** to generate infinite (40x40 chunk) rolling hills.
*   **Lighting**: Fixed Directional Lighting (Sun) calculated in World Space, rotated into View Space for correct shading relative to the camera. Includes height-based terrain coloring.
*   **FPS Camera**: Free-look camera with Mouse Locking (infinite rotation) and WASD movement using proper trigonometry.
*   **Performance**: Configurable Render Scale (e.g., render at 33% resolution and upscale) to achieve high FPS even on 4K screens.

## 📂 Project Structure

### `src/engine` (The Technology)
*   **`core/Engine.java`**: Abstract base class for the Game Loop, Window, and Input.
*   **`core/Renderer.java`**: The graphics pipeline (Transform, Clip, Cull, Sort, Light, Draw).
*   **`core/Camera.java`**: Handles Position (x,y,z) and Rotation (Pitch, Yaw).
*   **`graphics/Screen.java`**: The pixel buffer and drawing routines.
*   **`display/Window.java`**: The OS Window (JFrame) and Input Listeners.
*   **`math/*`**: Vector3D, Matrix4x4, Mesh, Triangle, PerlinNoise.
*   **`io/Input.java`**: Keyboard and Mouse handling.

### `src/game` (The Content)
*   **`DemoGame.java`**: The specific implementation of the engine (initializes scene, handles updates).
*   **`WorldGenerator.java`**: Helper to generate the procedural terrain mesh.

### `src/Main.java`
*   The entry point. Minimal launcher.

---

## 🚀 The Rendering Pipeline

Every frame, the `Renderer` performs the following steps:

1.  **Transformation**: Converts all 3D models into View Space (relative to camera).
2.  **Clipping**: Discards triangles behind the camera (Near Plane Clipping) to prevent crashes.
3.  **Culling**: Calculates the Dot Product of the face normal and camera ray. If the face looks away, it is ignored (Backface Culling).
4.  **Collection**: Visible triangles are added to a **Global Render List**.
5.  **Sorting**: The global list is sorted by average Z-depth (Far to Near).
6.  **Lighting**: 
    *   Sun Vector is defined in World Space (e.g. Horizon).
    *   Rotated into View Space by the Camera Matrix.
    *   `Brightness = DotProduct(Normal, ViewSunVector)`.
7.  **Rasterization**: Triangles are projected to 2D and drawn to the screen buffer using `g.fillPolygon`.

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