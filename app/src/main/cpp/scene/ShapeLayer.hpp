#pragma once

#include "Layer.hpp"

namespace AuraMotion {

enum class ShapeType {
    Rectangle,
    RoundedRectangle,
    Circle,
    Star,
    Polygon,
    Heart,
    CustomPath
};

class ShapeLayer : public Layer {
public:
    ShapeType shapeType = ShapeType::RoundedRectangle;

    AnimatableProperty<Vec2> size{"Size", Vec2(200.0f, 200.0f)};
    AnimatableProperty<float> cornerRadius{"CornerRadius", 24.0f};
    AnimatableProperty<int> pointsCount{"PointsCount", 5};
    AnimatableProperty<float> innerRadiusRatio{"InnerRadiusRatio", 0.5f};

    // Fill & Stroke
    bool hasFill = true;
    AnimatableProperty<Color> fillColor{"FillColor", Color(0.0f, 0.9f, 1.0f, 1.0f)};
    bool hasGradient = false;
    AnimatableProperty<Color> gradientColorEnd{"GradientEnd", Color(0.48f, 0.30f, 1.0f, 1.0f)};

    bool hasStroke = true;
    AnimatableProperty<Color> strokeColor{"StrokeColor", Color(1.0f, 1.0f, 1.0f, 1.0f)};
    AnimatableProperty<float> strokeWidth{"StrokeWidth", 4.0f};

    ShapeLayer(const std::string& layerId = "shape_layer", const std::string& layerName = "Shape Layer")
        : Layer(layerId, layerName, LayerType::Shape) {}
};

} // namespace AuraMotion
