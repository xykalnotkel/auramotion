#pragma once

#include "Keyframe.hpp"
#include "math/Vec2.hpp"
#include "math/Vec3.hpp"
#include "math/Vec4.hpp"
#include <vector>
#include <algorithm>
#include <string>

namespace AuraMotion {

// Generic Lerp helper
template <typename T>
inline T interpolateValue(const T& a, const T& b, float t) {
    return a + (b - a) * t;
}

template <>
inline Vec2 interpolateValue<Vec2>(const Vec2& a, const Vec2& b, float t) {
    return Vec2::lerp(a, b, t);
}

template <>
inline Vec3 interpolateValue<Vec3>(const Vec3& a, const Vec3& b, float t) {
    return Vec3::lerp(a, b, t);
}

template <>
inline Vec4 interpolateValue<Vec4>(const Vec4& a, const Vec4& b, float t) {
    return Vec4::lerp(a, b, t);
}

template <typename T>
class AnimatableProperty {
public:
    std::string name;
    T defaultValue{};
    std::vector<Keyframe<T>> keyframes;

    AnimatableProperty() = default;
    explicit AnimatableProperty(const std::string& propName, const T& initialValue = T{})
        : name(propName), defaultValue(initialValue) {}

    bool hasKeyframes() const {
        return !keyframes.empty();
    }

    void addKeyframe(float time, const T& value, const BezierCurve& curve = BezierCurve::EaseInOut()) {
        removeKeyframe(time, 0.001f);
        keyframes.emplace_back(time, value, curve);
        std::sort(keyframes.begin(), keyframes.end());
    }

    void addKeyframe(float time, const T& value, EasingType easing) {
        removeKeyframe(time, 0.001f);
        keyframes.emplace_back(time, value, easing);
        std::sort(keyframes.begin(), keyframes.end());
    }

    bool removeKeyframe(float time, float tolerance = 0.001f) {
        auto it = std::remove_if(keyframes.begin(), keyframes.end(), [time, tolerance](const Keyframe<T>& kf) {
            return std::abs(kf.time - time) <= tolerance;
        });
        if (it != keyframes.end()) {
            keyframes.erase(it, keyframes.end());
            return true;
        }
        return false;
    }

    void clearKeyframes() {
        keyframes.clear();
    }

    void setCurveAt(size_t index, const BezierCurve& curve) {
        if (index < keyframes.size()) {
            keyframes[index].curve = curve;
            keyframes[index].easing = EasingType::CustomBezier;
        }
    }

    T evaluate(float time) const {
        if (keyframes.empty()) {
            return defaultValue;
        }

        if (keyframes.size() == 1 || time <= keyframes.front().time) {
            return keyframes.front().value;
        }

        if (time >= keyframes.back().time) {
            return keyframes.back().value;
        }

        // Find surrounding keyframes [k0, k1]
        for (size_t i = 0; i < keyframes.size() - 1; ++i) {
            const auto& k0 = keyframes[i];
            const auto& k1 = keyframes[i + 1];

            if (time >= k0.time && time <= k1.time) {
                float duration = k1.time - k0.time;
                if (duration <= 1e-6f) {
                    return k1.value;
                }

                float linearT = (time - k0.time) / duration;
                float easedT = linearT;

                if (k0.easing == EasingType::CustomBezier) {
                    easedT = k0.curve.solve(linearT);
                } else {
                    easedT = Easing::evaluate(k0.easing, linearT);
                }

                return interpolateValue<T>(k0.value, k1.value, easedT);
            }
        }

        return keyframes.back().value;
    }
};

} // namespace AuraMotion
