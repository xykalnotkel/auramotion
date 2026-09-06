#pragma once

#include "math/Vec2.hpp"
#include "math/Vec3.hpp"
#include "math/Vec4.hpp"
#include "math/Mat4.hpp"
#include "animation/AnimatableProperty.hpp"
#include "effects/Effect.hpp"
#include <string>
#include <vector>
#include <memory>
#include <cmath>

namespace AuraMotion {

enum class LayerType {
    Shape,
    Text,
    Image,
    Video,
    Audio,
    Adjustment,
    Null
};

enum class BlendMode {
    Normal,
    Add,
    Multiply,
    Screen,
    Overlay,
    Darken,
    Lighten,
    ColorDodge,
    ColorBurn,
    Difference,
    Exclusion
};

enum class MatteMode {
    None,
    AlphaMatte,
    AlphaInverted,
    LumaMatte,
    LumaInverted
};

class Layer {
public:
    std::string id;
    std::string name;
    LayerType type;
    std::string parentId;

    float startTime = 0.0f;
    float endTime = 5.0f;

    bool visible = true;
    bool locked = false;
    bool solo = false;
    bool is3D = false;

    BlendMode blendMode = BlendMode::Normal;
    MatteMode matteMode = MatteMode::None;

    // Animatable Transform Properties
    AnimatableProperty<Vec3> position{"Position", Vec3(0.0f, 0.0f, 0.0f)};
    AnimatableProperty<Vec2> scale{"Scale", Vec2(1.0f, 1.0f)};
    AnimatableProperty<Vec3> rotation{"Rotation", Vec3(0.0f, 0.0f, 0.0f)}; // X, Y, Z in degrees
    AnimatableProperty<Vec2> anchorPoint{"AnchorPoint", Vec2(0.5f, 0.5f)};
    AnimatableProperty<Vec2> skew{"Skew", Vec2(0.0f, 0.0f)};
    AnimatableProperty<float> opacity{"Opacity", 1.0f};

    std::vector<std::shared_ptr<Effect>> effects;

    virtual ~Layer() = default;

    Layer(const std::string& layerId, const std::string& layerName, LayerType layerType)
        : id(layerId), name(layerName), type(layerType) {}

    bool isActiveAt(float time) const {
        return visible && time >= startTime && time <= endTime;
    }

    void addEffect(std::shared_ptr<Effect> effect) {
        effects.push_back(effect);
    }

    void removeEffect(const std::string& effectId) {
        effects.erase(std::remove_if(effects.begin(), effects.end(), [&](const std::shared_ptr<Effect>& e) {
            return e->id == effectId;
        }), effects.end());
    }

    virtual void update(float time) {
        for (auto& fx : effects) {
            if (fx->enabled) {
                fx->update(time);
            }
        }
    }

    Mat4 computeLocalTransform(float time, float layerWidth = 100.0f, float layerHeight = 100.0f) const {
        Vec3 pos = position.evaluate(time);
        Vec2 scl = scale.evaluate(time);
        Vec3 rot = rotation.evaluate(time);
        Vec2 anc = anchorPoint.evaluate(time);
        Vec2 skw = skew.evaluate(time);

        // 1. Translate to final position
        Mat4 tMat = Mat4::translation(pos.x, pos.y, pos.z);

        // 2. Rotate around Z, Y, X
        Mat4 rz = Mat4::rotationZ(degToRad(rot.z));
        Mat4 ry = Mat4::rotationY(degToRad(rot.y));
        Mat4 rx = Mat4::rotationX(degToRad(rot.x));
        Mat4 rMat = rz * ry * rx;

        // 3. Skew
        Mat4 skMat = Mat4::skew(degToRad(skw.x), degToRad(skw.y));

        // 4. Scale
        Mat4 sMat = Mat4::scaling(scl.x, scl.y, 1.0f);

        // 5. Anchor point offset
        Mat4 aMat = Mat4::translation(-anc.x * layerWidth, -anc.y * layerHeight, 0.0f);

        return tMat * rMat * skMat * sMat * aMat;
    }
};

} // namespace AuraMotion
