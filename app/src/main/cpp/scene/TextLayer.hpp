#pragma once

#include "Layer.hpp"

namespace AuraMotion {

class TextLayer : public Layer {
public:
    std::string text = "AuraMotion";
    std::string fontFamily = "Roboto-Bold";

    AnimatableProperty<float> fontSize{"FontSize", 64.0f};
    AnimatableProperty<float> letterSpacing{"LetterSpacing", 2.0f};
    AnimatableProperty<float> lineSpacing{"LineSpacing", 1.2f};
    AnimatableProperty<Color> textColor{"TextColor", Color(1.0f, 1.0f, 1.0f, 1.0f)};
    
    // Stroke
    bool hasStroke = false;
    AnimatableProperty<Color> strokeColor{"StrokeColor", Color(0.0f, 0.0f, 0.0f, 1.0f)};
    AnimatableProperty<float> strokeWidth{"StrokeWidth", 2.0f};

    TextLayer(const std::string& layerId = "text_layer", const std::string& layerName = "Text Layer")
        : Layer(layerId, layerName, LayerType::Text) {}
};

} // namespace AuraMotion
