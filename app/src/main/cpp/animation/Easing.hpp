#pragma once

#include "math/MathUtils.hpp"
#include <cmath>

namespace AuraMotion {

enum class EasingType {
    Linear,
    EaseInQuad,
    EaseOutQuad,
    EaseInOutQuad,
    EaseInCubic,
    EaseOutCubic,
    EaseInOutCubic,
    EaseInSine,
    EaseOutSine,
    EaseInOutSine,
    EaseInBack,
    EaseOutBack,
    EaseInOutBack,
    EaseInElastic,
    EaseOutElastic,
    EaseInOutElastic,
    EaseInBounce,
    EaseOutBounce,
    EaseInOutBounce,
    Hold,
    CustomBezier
};

class Easing {
public:
    static float evaluate(EasingType type, float t) {
        t = clamp(t, 0.0f, 1.0f);
        switch (type) {
            case EasingType::Linear:
                return t;
            case EasingType::EaseInQuad:
                return t * t;
            case EasingType::EaseOutQuad:
                return t * (2.0f - t);
            case EasingType::EaseInOutQuad:
                return t < 0.5f ? 2.0f * t * t : -1.0f + (4.0f - 2.0f * t) * t;
            case EasingType::EaseInCubic:
                return t * t * t;
            case EasingType::EaseOutCubic: {
                float f = t - 1.0f;
                return f * f * f + 1.0f;
            }
            case EasingType::EaseInOutCubic:
                return t < 0.5f ? 4.0f * t * t * t : (t - 1.0f) * (2.0f * t - 2.0f) * (2.0f * t - 2.0f) + 1.0f;
            case EasingType::EaseInSine:
                return 1.0f - std::cos(t * (PI * 0.5f));
            case EasingType::EaseOutSine:
                return std::sin(t * (PI * 0.5f));
            case EasingType::EaseInOutSine:
                return -0.5f * (std::cos(PI * t) - 1.0f);
            case EasingType::EaseOutBack: {
                constexpr float c1 = 1.70158f;
                constexpr float c3 = c1 + 1.0f;
                float f = t - 1.0f;
                return 1.0f + c3 * f * f * f + c1 * f * f;
            }
            case EasingType::EaseOutBounce:
                return easeOutBounce(t);
            case EasingType::EaseInBounce:
                return 1.0f - easeOutBounce(1.0f - t);
            case EasingType::EaseInOutBounce:
                return t < 0.5f ? (1.0f - easeOutBounce(1.0f - 2.0f * t)) * 0.5f
                                : (1.0f + easeOutBounce(2.0f * t - 1.0f)) * 0.5f;
            case EasingType::EaseOutElastic: {
                if (t == 0.0f) return 0.0f;
                if (t == 1.0f) return 1.0f;
                return std::pow(2.0f, -10.0f * t) * std::sin((t * 10.0f - 0.75f) * (TWO_PI / 3.0f)) + 1.0f;
            }
            case EasingType::Hold:
                return t < 1.0f ? 0.0f : 1.0f;
            default:
                return t;
        }
    }

private:
    static float easeOutBounce(float t) {
        constexpr float n1 = 7.5625f;
        constexpr float d1 = 2.75f;
        if (t < 1.0f / d1) {
            return n1 * t * t;
        } else if (t < 2.0f / d1) {
            t -= 1.5f / d1;
            return n1 * t * t + 0.75f;
        } else if (t < 2.5f / d1) {
            t -= 2.25f / d1;
            return n1 * t * t + 0.9375f;
        } else {
            t -= 2.625f / d1;
            return n1 * t * t + 0.984375f;
        }
    }
};

} // namespace AuraMotion
