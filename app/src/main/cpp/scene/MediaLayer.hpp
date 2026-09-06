#pragma once

#include "Layer.hpp"

namespace AuraMotion {

class MediaLayer : public Layer {
public:
    std::string mediaPath;
    float mediaDuration = 0.0f;
    float mediaOffset = 0.0f;
    float speedMultiplier = 1.0f;
    bool isLooping = false;
    int nativeWidth = 1920;
    int nativeHeight = 1080;
    uint32_t glTextureId = 0;

    MediaLayer(const std::string& layerId, const std::string& layerName, LayerType mediaType = LayerType::Image)
        : Layer(layerId, layerName, mediaType) {}
};

class AdjustmentLayer : public Layer {
public:
    AdjustmentLayer(const std::string& layerId = "adj_layer", const std::string& layerName = "Adjustment Layer")
        : Layer(layerId, layerName, LayerType::Adjustment) {}
};

class NullLayer : public Layer {
public:
    NullLayer(const std::string& layerId = "null_layer", const std::string& layerName = "Null Controller")
        : Layer(layerId, layerName, LayerType::Null) {}
};

} // namespace AuraMotion
