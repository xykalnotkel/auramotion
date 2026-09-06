#pragma once

#include "Effect.hpp"

namespace AuraMotion {

class GlowEffect : public Effect {
public:
    AnimatableProperty<float> radius{"Radius", 15.0f};
    AnimatableProperty<float> intensity{"Intensity", 1.8f};
    AnimatableProperty<float> threshold{"Threshold", 0.4f};
    AnimatableProperty<Color> color{"Color", Color(0.0f, 0.9f, 1.0f, 1.0f)}; // Neon Cyan Glow

    // Current evaluated values
    float currentRadius = 15.0f;
    float currentIntensity = 1.8f;
    float currentThreshold = 0.4f;
    Color currentColor = Color(0.0f, 0.9f, 1.0f, 1.0f);
    float currentMix = 1.0f;

    GlowEffect(const std::string& effectId = "fx_glow")
        : Effect(effectId, "Glow / Bloom", EffectType::Glow) {}

    void update(float time) override {
        currentRadius = radius.evaluate(time);
        currentIntensity = intensity.evaluate(time);
        currentThreshold = threshold.evaluate(time);
        currentColor = color.evaluate(time);
        currentMix = mix.evaluate(time);
    }

    std::string getFragmentShader() const override {
        return R"(#version 300 es
precision highp float;
in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform vec2 uResolution;
uniform float uRadius;
uniform float uIntensity;
uniform float uThreshold;
uniform vec4 uGlowColor;
uniform float uMix;

void main() {
    vec4 baseColor = texture(uTexture, vTexCoord);
    vec2 texelSize = 1.0 / uResolution;
    
    vec4 bloom = vec4(0.0);
    float totalWeight = 0.0;
    
    int samples = int(clamp(uRadius, 2.0, 16.0));
    for (int x = -samples; x <= samples; ++x) {
        for (int y = -samples; y <= samples; ++y) {
            vec2 offset = vec2(float(x), float(y)) * texelSize * (uRadius / float(samples));
            vec4 c = texture(uTexture, vTexCoord + offset);
            float brightness = max(c.r, max(c.g, c.b));
            if (brightness > uThreshold) {
                float dist = length(vec2(x, y));
                float weight = exp(-0.5 * (dist * dist) / (float(samples) * 0.5));
                bloom += (c - uThreshold) / (1.0 - uThreshold) * weight;
                totalWeight += weight;
            }
        }
    }
    
    if (totalWeight > 0.0) {
        bloom /= totalWeight;
    }
    
    vec4 finalGlow = bloom * uIntensity * uGlowColor;
    vec4 blended = baseColor + finalGlow;
    fragColor = mix(baseColor, blended, uMix);
}
)";
    }
};

} // namespace AuraMotion
