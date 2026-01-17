# Java Software Renderer v3

A professional-grade 3D engine built from scratch in Java without external graphics libraries (OpenGL/Vulkan). This project demonstrates the fundamental mathematics and architecture of 3D computer graphics.

## 🌟 Key Features

*   **Software Rasterization**: Implements a pure Java rasterizer using `Graphics2D` for high-speed polygon filling.
*   **3D Pipeline**: Full Vertex Transformation pipeline (Model -> View -> Clip -> Projection -> Screen).
*   **Global Sorting**: Implements **Painter's Algorithm** with global triangle sorting to handle depth correctly across multiple objects.
*   **Lighting**: Basic flat shading using Dot Product lighting calculations.
*   **FPS Camera**: Free-look camera with Mouse Locking (infinite rotation) and WASD movement.
*   **Performance**: Configurable Render Scale (e.g., render at 50% resolution and upscale) to achieve high FPS even on 4K screens.

## 📂 Project Structure

### `src/engine/core`
The brain of the operation.
*   **`Engine.java`**: The abstract base class handling the Game Loop (`start`, `run`, `update`, `render`), Window management, Input timing, and **Mouse Locking** logic.
*   **`Renderer.java`**: The graphics pipeline. Collects triangles from all meshes, transforms them, clips them, performs **Backface Culling**, sorts them by depth, calculates lighting, and draws them.
*   **`Camera.java`**: Represents the observer. Stores Position (x, y, z) and Rotation (Pitch, Yaw). Handles trigonometric movement logic.

### `src/engine/graphics`
*   **`Screen.java`**: Wraps a `BufferedImage` and provides hardware-accelerated drawing methods (`fillTriangle`, `clearPixels`).

### `src/engine/display`
*   **`Window.java`**: Manages the `JFrame` and `Canvas`. Handles Double Buffering and transparent Cursor hiding.

### `src/engine/math`
*   **`Vector3D.java`**: Vector arithmetic (Dot, Cross, Normalize).
*   **`Matrix4x4.java`**: Projection and Rotation Matrices.
*   **`Mesh.java`**: A collection of Triangles.

### `src/engine/io`
*   **`Input.java`**: A unified listener for Keyboard and Mouse. Tracks key states and supports explicit mouse delta injection from the Engine's locking mechanism.

---

## 🚀 The Rendering Pipeline

Every frame, the `Renderer` performs the following steps:

1.  **Transformation**: Converts all 3D models into View Space (relative to camera).
2.  **Clipping**: Discards triangles behind the camera (Near Plane Clipping) to prevent crashes.
3.  **Culling**: Calculates the Dot Product of the face normal and camera ray. If the face looks away, it is ignored (Backface Culling).
4.  **Collection**: Visible triangles are added to a **Global Render List**.
5.  **Sorting**: The global list is sorted by average Z-depth (Far to Near).
6.  **Lighting**: Brightness is calculated: `dot(Normal, LightDir)`.
7.  **Rasterization**: Triangles are projected to 2D and drawn to the screen buffer.

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
3.  Enjoy the Stress Test (100 Cubes)!

---
*Created as a learning project to understand the internals of 3D Graphics Engines.*
