#pragma once

#include "Compositor.hpp"
#include "scene/Scene.hpp"
#include "math/Vec2.hpp"
#include <memory>
#include <string>

namespace AuraMotion {

class Renderer {
public:
    std::shared_ptr<Scene> scene;
    Compositor compositor;

    int viewportWidth = 1080;
    int viewportHeight = 1920;
    float zoom = 1.0f;
    Vec2 panOffset{0.0f, 0.0f};

    std::string selectedLayerId;

    Renderer() {
        scene = std::make_shared<Scene>();
    }

    void init(int width, int height) {
        viewportWidth = width;
        viewportHeight = height;
        compositor.init(width, height);
    }

    void resize(int width, int height) {
        viewportWidth = width;
        viewportHeight = height;
        compositor.resize(width, height);
    }

    void tick(float deltaTime) {
        if (!scene) return;
        scene->timeline.advance(deltaTime);
        scene->update(scene->timeline.currentTime);
    }

    void seek(float time) {
        if (!scene) return;
        scene->timeline.seek(time);
        scene->update(scene->timeline.currentTime);
    }

    void render() {
        if (!scene) return;
        compositor.renderScene(*scene, scene->timeline.currentTime);
    }

    void selectLayer(const std::string& layerId) {
        selectedLayerId = layerId;
    }

    std::shared_ptr<Layer> getSelectedLayer() const {
        if (!scene || selectedLayerId.empty()) return nullptr;
        return scene->findLayer(selectedLayerId);
    }
};

} // namespace AuraMotion
