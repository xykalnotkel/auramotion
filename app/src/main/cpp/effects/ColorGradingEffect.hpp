#pragma once

#include "Effect.hpp"

namespace AuraMotion {

class ColorGradingEffect : public Effect {
public:
    AnimatableProperty<float> brightness{"Brightness", 0.0f};
    AnimatableProperty<float> contrast{"Contrast", 1.0f};
    AnimatableProperty<float> saturation{"Saturation", 1.0f};
    AnimatableProperty<float> hue{"Hue", 0.0f};
    AnimatableProperty<float> temperature{"Temperature", 0.0f};

    float currentBrightness = 0.0f;
    float currentContrast = 1.0f;
    float currentSaturation = 1.0f;
    float currentHue = 0.0f;
    float currentTemperature = 0.0f;
    float currentMix = 1.0f;

    ColorGradingEffect(const std::string& effectId = "fx_color_grade")
        : Effect(effectId, "Color Grading & LUT", EffectType::ColorGrading) {}

    void update(float time) override {
        currentBrightness = brightness.evaluate(time);
        currentContrast = contrast.evaluate(time);
        currentSaturation = saturation.evaluate(time);
        currentHue = hue.evaluate(time);
        currentTemperature = temperature.evaluate(time);
        currentMix = mix.evaluate(time);
    }

    std::string getFragmentShader() const override {
        return R"(#version 300 es
precision highp float;
in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform float uBrightness;
uniform float uContrast;
uniform float uSaturation;
uniform float uHue;
uniform float uTemperature;
uniform float uMix;

vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec4 original = texture(uTexture, vTexCoord);
    vec3 color = original.rgb;

    color = (color - 0.5) * uContrast + 0.5 + uBrightness;

    if (uTemperature > 0.0) {
        color.r += uTemperature * 0.1;
        color.b -= uTemperature * 0.1;
    } else {
        color.r += uTemperature * 0.1;
        color.b -= uTemperature * 0.1;
    }

    vec3 hsv = rgb2hsv(clamp(color, 0.0, 1.0));
    hsv.x = fract(hsv.x + uHue / 360.0);
    hsv.y = clamp(hsv.y * uSaturation, 0.0, 1.0);
    color = hsv2rgb(hsv);

    vec4 finalColor = vec4(clamp(color, 0.0, 1.0), original.a);
    fragColor = mix(original, finalColor, uMix);
}
)";
    }
};

class DisplacementEffect : public Effect {
public:
    AnimatableProperty<float> amplitude{"Amplitude", 20.0f};
    AnimatableProperty<float> frequency{"Frequency", 5.0f};
    AnimatableProperty<float> speed{"Speed", 2.0f};

    float currentAmplitude = 20.0f;
    float currentFrequency = 5.0f;
    float currentSpeed = 2.0f;
    float currentMix = 1.0f;
    float currentTime = 0.0f;

    DisplacementEffect(const std::string& effectId = "fx_warp")
        : Effect(effectId, "Wave Warp / Displacement", EffectType::Displacement) {}

    void update(float time) override {
        currentTime = time;
        currentAmplitude = amplitude.evaluate(time);
        currentFrequency = frequency.evaluate(time);
        currentSpeed = speed.evaluate(time);
        currentMix = mix.evaluate(time);
    }

    std::string getFragmentShader() const override {
        return R"(#version 300 es
precision highp float;
in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform vec2 uResolution;
uniform float uTime;
uniform float uAmplitude;
uniform float uFrequency;
uniform float uSpeed;
uniform float uMix;

void main() {
    vec4 original = texture(uTexture, vTexCoord);
    
    float waveX = sin(vTexCoord.y * uFrequency + uTime * uSpeed) * (uAmplitude / uResolution.x);
    float waveY = cos(vTexCoord.x * uFrequency + uTime * uSpeed) * (uAmplitude / uResolution.y);
    
    vec2 distortedCoord = vTexCoord + vec2(waveX, waveY);
    vec4 warped = texture(uTexture, distortedCoord);
    
    fragColor = mix(original, warped, uMix);
}
)";
    }
};

} // namespace AuraMotion
