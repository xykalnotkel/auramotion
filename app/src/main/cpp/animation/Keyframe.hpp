#pragma once

#include "BezierCurve.hpp"
#include "Easing.hpp"
#include <string>

namespace AuraMotion {

template <typename T>
struct Keyframe {
    float time = 0.0f; // timestamp in seconds
    T value{};
    EasingType easing = EasingType::CustomBezier;
    BezierCurve curve = BezierCurve::EaseInOut();

    Keyframe() = default;
    Keyframe(float t, const T& val, const BezierCurve& c = BezierCurve::EaseInOut())
        : time(t), value(val), easing(EasingType::CustomBezier), curve(c) {}
    Keyframe(float t, const T& val, EasingType ease)
        : time(t), value(val), easing(ease), curve(BezierCurve::Linear()) {}

    bool operator<(const Keyframe<T>& other) const {
        return time < other.time;
    }
};

} // namespace AuraMotion
