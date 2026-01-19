# Java Software Renderer v3 (Multi-Threaded Release v1.1)

A professional-grade 3D engine built from scratch in Java without external graphics libraries (OpenGL/Vulkan). This project demonstrates the fundamental mathematics and architecture of 3D computer graphics by implementing them manually.

**Current State**: This version features a **Dynamic Tile-Based Multi-Threaded Rasterizer**, allowing it to scale effortlessly to high core counts (e.g., 22 cores) for high-performance software rendering.

## 🌟 Key Features

*   **Multi-Threaded Rendering**: Implements a parallel rasterizer that splits the screen into small tiles (64x64) and uses a **Work-Stealing** thread pool to keep all CPU cores at 100% utilization.
*   **Software Rasterization**: Pure Java Z-Buffer Scanline Algorithm.
*   **Gouraud Shading**: Smooth lighting interpolation across triangle surfaces.
*   **Fractal Terrain**: "Epic Scale" procedural world using 4-layered Perlin Noise (FBM) with dynamic biomes (Water, Sand, Grass, Snow).
*   **Physics System**:
    *   Gravity & Velocity integration.
    *   **Jumping** and Ground Snapping.
    *   **Head Bobbing** for realistic movement.
    *   Terrain Collision detection.
*   **Optimized Architecture**:
    *   **Zero-Allocation**: Uses Object Pooling to eliminate GC pressure.
    *   **Frustum Culling**: 6-Plane geometric culling.
    *   **Terrain Chunking**: Splits the infinite world into managed sections.
*   **Atmosphere**: Sky gradients and improved lighting contrast.

## 📂 Project Structure

### `src/engine` (The Technology)
The engine package contains the reusable core technology, completely decoupled from any specific game logic.

*   **`core/Renderer.java`**: The heart of the graphics engine. Manages the Thread Pool and Tile dispatching.
*   **`core/ViewFrustum.java`**: Handles geometric culling.
*   **`graphics/Screen.java`**: The framebuffer and clipped rasterizer.
*   **`math/*`**: Zero-allocation math library (`Matrix4x4`, `Vector3D`, `Plane`, `PerlinNoise`).

### `src/game` (The Content)
*   **`DemoGame.java`**: The main game loop and physics logic.
*   **`Terrain.java`**: The Fractal Brownian Motion (FBM) terrain generator.

---

## 🎮 Controls

*   **Mouse**: Look Around (Infinite Mouse Lock).
*   **W / S**: Move Forward / Backward.
*   **A / D**: Strafe Left / Right.
*   **Space**: **Jump** (when in Walking Mode).
*   **G**: **Toggle Walking Mode** (Physics On/Off).
*   **Shift**: Sprint.
*   **ESC**: Quit.

---
*Created as a learning project to understand the internals of 3D Graphics Engines.*

