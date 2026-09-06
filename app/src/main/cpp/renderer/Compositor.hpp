#pragma once

#include "Framebuffer.hpp"
#include "scene/Scene.hpp"
#include "scene/ShapeLayer.hpp"
#include "math/Mat4.hpp"
#include <memory>
#include <vector>

namespace AuraMotion {

class Compositor {
public:
    int renderWidth = 1080;
    int renderHeight = 1920;

    Framebuffer primaryFbo;
    Framebuffer pingFbo;
    Framebuffer pongFbo;

    Compositor() = default;

    void init(int width, int height) {
        renderWidth = width;
        renderHeight = height;
        primaryFbo.init(width, height);
        pingFbo.init(width, height);
        pongFbo.init(width, height);
    }

    void resize(int width, int height) {
        renderWidth = width;
        renderHeight = height;
        primaryFbo.resize(width, height);
        pingFbo.resize(width, height);
        pongFbo.resize(width, height);
    }

    void renderScene(const Scene& scene, float time) {
        // 1. Prepare projection matrix (Ortho: -width/2..width/2, -height/2..height/2)
        float halfW = scene.width * 0.5f;
        float halfH = scene.height * 0.5f;
        Mat4 projection = Mat4::ortho(-halfW, halfW, -halfH, halfH, -1000.0f, 1000.0f);

        // 2. Clear background with scene.backgroundColor
        // glClearColor(scene.backgroundColor.r, scene.backgroundColor.g, scene.backgroundColor.b, scene.backgroundColor.a);
        // glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        // 3. Render active layers from bottom to top
        for (const auto& layer : scene.layers) {
            if (!layer->isActiveAt(time)) continue;

            renderLayer(layer, projection, time);
        }
    }

    void renderLayer(const std::shared_ptr<Layer>& layer, const Mat4& projection, float time) {
        Mat4 localTransform = layer->computeLocalTransform(time, 200.0f, 200.0f);
        Mat4 mvp = projection * localTransform;

        // Apply shader effects stack if any
        for (const auto& fx : layer->effects) {
            if (fx->enabled) {
                // Multi-pass shader execution with Ping-Pong FBO
            }
        }
    }
};

} // namespace AuraMotion
