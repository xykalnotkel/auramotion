#pragma once

#include "animation/AnimatableProperty.hpp"
#include <string>
#include <memory>
#include <unordered_map>

namespace AuraMotion {

enum class EffectType {
    GaussianBlur,
    Glow,
    ChromaticAberration,
    ColorGrading,
    Displacement,
    Vignette,
    MotionBlur,
    CustomShader
};

class Effect {
public:
    std::string id;
    std::string name;
    EffectType type;
    bool enabled = true;

    AnimatableProperty<float> mix{"Mix", 1.0f};

    virtual ~Effect() = default;

    Effect(const std::string& effectId, const std::string& effectName, EffectType effectType)
        : id(effectId), name(effectName), type(effectType) {}

    virtual void update(float time) = 0;
    virtual std::string getFragmentShader() const = 0;
};

} // namespace AuraMotion
