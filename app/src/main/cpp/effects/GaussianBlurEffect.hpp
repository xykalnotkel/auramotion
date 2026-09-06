#pragma once

#include "Effect.hpp"

namespace AuraMotion {

class GaussianBlurEffect : public Effect {
public:
    AnimatableProperty<float> radius{"Radius", 10.0f};

    float currentRadius = 10.0f;
    float currentMix = 1.0f;

    GaussianBlurEffect(const std::string& effectId = "fx_blur")
        : Effect(effectId, "Gaussian Blur", EffectType::GaussianBlur) {}

    void update(float time) override {
        currentRadius = radius.evaluate(time);
        currentMix = mix.evaluate(time);
    }

    std::string getFragmentShader() const override {
        return R"(#version 300 es
precision highp float;
in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform vec2 uResolution;
uniform vec2 uDirection;
uniform float uRadius;
uniform float uMix;

void main() {
    vec4 baseColor = texture(uTexture, vTexCoord);
    if (uRadius <= 0.01) {
        fragColor = baseColor;
        return;
    }

    vec2 texelSize = 1.0 / uResolution;
    vec4 sum = vec4(0.0);
    float totalWeight = 0.0;
    
    int sampleCount = int(clamp(uRadius, 2.0, 24.0));
    float sigma = uRadius * 0.5;

    for (int i = -sampleCount; i <= sampleCount; ++i) {
        float fi = float(i);
        float weight = exp(-0.5 * (fi * fi) / (sigma * sigma));
        vec2 offset = uDirection * (fi * texelSize * (uRadius / float(sampleCount)));
        sum += texture(uTexture, vTexCoord + offset) * weight;
        totalWeight += weight;
    }

    vec4 blurred = sum / totalWeight;
    fragColor = mix(baseColor, blurred, uMix);
}
)";
    }
};

} // namespace AuraMotion
