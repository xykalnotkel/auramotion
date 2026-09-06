# ⚡ AuraMotion — Professional Mobile Motion Graphics & VFX Studio

> **An Alight Motion-grade Motion Graphics, Keyframe Animation & Visual Effects Engine for Android and Cross-Platform.**

---

## 🎯 Architecture Decision: Native Kotlin vs C++ Native

Untuk aplikasi motion editing kelas profesional seperti **Alight Motion**, **After Effects Mobile**, atau **CapCut**, pendekatan terbaik adalah **Arsitektur Hibrida (Hybrid Architecture)**:

```
┌────────────────────────────────────────────────────────┐
│   📱 UI & Interaction Layer (Android Native Kotlin)    │
│   • Jetpack Compose & Material 3                       │
│   • Multi-Track Timeline & Touch Scrubbing             │
│   • Interactive Bézier Curve Graph Editor              │
│   • Viewport Gestures & Layer Inspector                │
└──────────────────────────┬─────────────────────────────┘
                           │ JNI (Java Native Interface)
┌──────────────────────────▼─────────────────────────────┐
│   ⚡ Rendering & Core Engine (C++20 Native NDK)        │
│   • OpenGL ES 3.0 / Vulkan Multi-pass Shaders         │
│   • 60 FPS Cubic Bézier Keyframe Evaluator             │
│   • Separable Gaussian Blur, Bloom, RGB Split          │
│   • Ping-Pong Framebuffer Compositor                   │
│   • Hardware Video Encoder (MediaCodec / FFmpeg)       │
└────────────────────────────────────────────────────────┘
```

### Mengapa Bukan Full Kotlin atau Full C++?

| Kriteria | Full Kotlin (JVM) | Full C++ (NDK UI) | **Hybrid (Kotlin UI + C++ Core)** 🏆 |
| :--- | :--- | :--- | :--- |
| **GPU Compositing (60fps)** | ⚠️ GC pause overhead saat multi-layer | ⚡ Sangat Cepat & Efisien | ⚡ **Sangat Cepat & Bebas GC Pause** |
| **Shader & Multi-Pass FBO** | ⚠️ JNI overhead per GL call | ⚡ Direct OpenGL/Vulkan access | ⚡ **Direct Native GL Pipeline** |
| **Bézier Keyframing Math** | 🟡 Lumayan cepat tapi boxing overhead | ⚡ SIMD Vectorized & Newton-Raphson | ⚡ **Performa Frame-Perfect C++20** |
| **UI, Touch & Gestures** | ⚡ Sangat mudah & modern (Compose) | ❌ Sangat rumit dibuat dari nol | ⚡ **Sangat Responsif & Halus (Compose)** |
| **Portabilitas (iOS / Web)** | ❌ Terikat JVM Android | ⚡ Shared Core ke iOS (Metal/Swift) | ⚡ **Shared C++ Engine ke iOS & WebAssembly** |

---

## 🚀 Fitur Utama AuraMotion

1. **C++20 Motion Graphics Engine**:
   - `Mat4`, `Vec2`, `Vec3`, `Vec4` high-performance 3D/2D affine matrix transformations.
   - Exact Cubic Bézier Root Solver (Newton-Raphson + Bisection fallback) untuk evaluasi kurva animasi selevel Alight Motion.
   - Animatable properties: Position $(X, Y, Z)$, Scale, Rotation $(X, Y, Z)$, Skew, Opacity, Anchor Point.

2. **Real-time GLSL Shader Effects**:
   - **Bloom & Glow**: Thresholded multi-sample neon diffusion.
   - **RGB Split / Chromatic Aberration**: Dispersi gelombang warna terpisah.
   - **Gaussian Blur**: 2-pass separable convolution filter.
   - **Wave Warp / Displacement**: Gelombang distorsi dinamis.
   - **Color Grading & LUT**: Hue, Saturation, Brightness, Contrast & Temperature.

3. **Multi-Track Timeline & Easing Graph**:
   - Frame-accurate scrubber with sub-second timecode.
   - Keyframe diamond markers on layer tracks.
   - Draggable Cubic Bézier tangent handles ($P_1, P_2$) dengan preset: *Linear, Ease In, Ease Out, Ease In-Out, Fast In, Overshoot*.

4. **CI/CD Build Pipeline (GitHub Actions)**:
   - Full automated workflow: NDK r26d, Clang 17, CMake, Gradle caching, unit tests, APK & AAB signing, release artifacts packaging.

---

## 🛠️ Struktur Repositori

```
auramotion/
├── .github/
│   └── workflows/
│       └── build-android.yml       # GitHub Actions CI/CD Build Workflow
├── app/
│   ├── build.gradle.kts            # Android App Gradle with NDK CMake setup
│   ├── CMakeLists.txt              # NDK C++20 Compilation Script
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── cpp/                    # Native C++20 Core Engine
│       │   ├── math/               # Mat4, Vec2, Vec3, Vec4, MathUtils
│       │   ├── animation/          # BezierCurve, Easing, Keyframe, AnimatableProperty
│       │   ├── scene/              # Layer, ShapeLayer, TextLayer, MediaLayer, Scene, Timeline
│       │   ├── effects/            # GlowEffect, GaussianBlur, ChromaticAberration, etc.
│       │   ├── renderer/           # GLShader, Framebuffer, Compositor, Renderer
│       │   ├── export/             # VideoExporter (MediaCodec / FFmpeg offline rendering)
│       │   └── jni/                # EngineBridge JNI Methods
│       └── java/com/auramotion/editor/
│           ├── MainActivity.kt
│           ├── engine/NativeEngine.kt
│           ├── model/Models.kt
│           ├── viewmodel/EditorViewModel.kt
│           └── ui/
│               ├── theme/          # Dark Neon Slate Theme
│               ├── components/     # TimelineView, CurveGraphEditor, CanvasViewport, LayerInspector, etc.
│               └── screens/        # EditorScreen, ProjectBrowserScreen
├── tests/
│   └── cpp/test_engine.cpp         # C++ Native Test Suite (100% Passing)
├── web/                            # Interactive Live Studio Preview
│   ├── index.html
│   ├── css/style.css
│   └── js/app.js
├── CMakeLists.txt                  # Root CMake for Standalone Native Builds
├── build.gradle.kts
└── settings.gradle.kts
```

---

## ⚡ Cara Build APK Menggunakan GitHub Actions

1. **Push kode ini ke repositori GitHub Anda:**
   ```bash
   git init
   git add .
   git commit -m "feat: Initial AuraMotion release with C++ NDK Engine & Compose UI"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/auramotion.git
   git push -u origin main
   ```

2. **GitHub Actions Workflow** (`.github/workflows/build-android.yml`) akan otomatis berjalan:
   - Menjalankan **C++ Engine Unit Tests** dengan Clang & CMake.
   - Mengatur **Android SDK & NDK r26d**.
   - Mem-build **Debug APK**, **Release APK**, dan **AAB (Android App Bundle)**.
   - Meng-upload file APK langsung ke tab **Actions > Artifacts** atau membuat **GitHub Release** otomatis saat tag `v*` di-push!

---

## 🧪 Menjalankan Native Unit Tests Lokal

```bash
cd auramotion
cmake -B build-test -S . -DCMAKE_BUILD_TYPE=Release -DBUILD_TESTS=ON
cmake --build build-test --config Release
./build-test/test_engine
```

**Hasil:**
```
=== AuraMotion Native C++ Engine Test Suite ===
[Test] Math & Matrix Operations...
  ✓ Math tests passed!
[Test] Cubic Bezier & Keyframe Interpolation...
  ✓ Keyframing tests passed!
[Test] Scene, Layers & Effects Pipeline...
  ✓ Scene & Effects tests passed!
[Test] Offline Video Export Simulation...
  ✓ Export tests passed (30/30 frames rendered)!
=== All Tests Passed Successfully! ===
```

---

## 🌐 Menjalankan Live Studio Preview di Web Browser

```bash
node server.js
# Buka http://localhost:3000 pada browser Anda
```
