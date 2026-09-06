#pragma once

#include "Layer.hpp"
#include "Timeline.hpp"
#include <vector>
#include <memory>
#include <string>
#include <algorithm>

namespace AuraMotion {

class Scene {
public:
    std::string id = "project_01";
    std::string title = "New Project";
    
    int width = 1080;
    int height = 1920; // Default 9:16 vertical motion canvas (TikTok/Reels/Shorts)
    Color backgroundColor = Color(0.05f, 0.06f, 0.08f, 1.0f); // Dark Slate

    Timeline timeline;
    std::vector<std::shared_ptr<Layer>> layers;

    Scene() = default;

    void addLayer(std::shared_ptr<Layer> layer) {
        layers.push_back(layer);
    }

    void insertLayer(size_t index, std::shared_ptr<Layer> layer) {
        if (index <= layers.size()) {
            layers.insert(layers.begin() + index, layer);
        } else {
            layers.push_back(layer);
        }
    }

    bool removeLayer(const std::string& layerId) {
        auto it = std::remove_if(layers.begin(), layers.end(), [&](const std::shared_ptr<Layer>& l) {
            return l->id == layerId;
        });
        if (it != layers.end()) {
            layers.erase(it, layers.end());
            return true;
        }
        return false;
    }

    std::shared_ptr<Layer> findLayer(const std::string& layerId) const {
        for (const auto& l : layers) {
            if (l->id == layerId) return l;
        }
        return nullptr;
    }

    void moveLayer(size_t fromIndex, size_t toIndex) {
        if (fromIndex < layers.size() && toIndex < layers.size() && fromIndex != toIndex) {
            auto layer = layers[fromIndex];
            layers.erase(layers.begin() + fromIndex);
            layers.insert(layers.begin() + toIndex, layer);
        }
    }

    void update(float time) {
        for (auto& layer : layers) {
            layer->update(time);
        }
    }
};

} // namespace AuraMotion
