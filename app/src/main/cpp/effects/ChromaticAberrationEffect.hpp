#pragma once

#include "Effect.hpp"

namespace AuraMotion {

class ChromaticAberrationEffect : public Effect {
public:
    AnimatableProperty<float> distance{"Distance", 12.0f};
    AnimatableProperty<float> angle{"Angle", 0.0f};

    float currentDistance = 12.0f;
    float currentAngle = 0.0f;
    float currentMix = 1.0f;

    ChromaticAberrationEffect(const std::string& effectId = "fx_chromatic")
        : Effect(effectId, "RGB Split / Chromatic Aberration", EffectType::ChromaticAberration) {}

    void update(float time) override {
        currentDistance = distance.evaluate(time);
        currentAngle = angle.evaluate(time);
        currentMix = mix.evaluate(time);
    }

    std::string getFragmentShader() const override {
        return R"(#version 300 es
precision highp float;
in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform vec2 uResolution;
uniform float uDistance;
uniform float uAngle;
uniform float uMix;

void main() {
    vec4 original = texture(uTexture, vTexCoord);
    if (uDistance <= 0.01) {
        fragColor = original;
        return;
    }

    float rad = radians(uAngle);
    vec2 dir = vec2(cos(rad), sin(rad)) / uResolution * uDistance;

    float r = texture(uTexture, vTexCoord + dir).r;
    float g = texture(uTexture, vTexCoord).g;
    float b = texture(uTexture, vTexCoord - dir).b;
    float a = original.a;

    vec4 separated = vec4(r, g, b, a);
    fragColor = mix(original, separated, uMix);
}
)";
    }
};

} // namespace AuraMotion
