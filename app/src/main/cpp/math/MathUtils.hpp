#pragma once

#include <cmath>
#include <algorithm>
#include <string>
#include <sstream>
#include <iomanip>

namespace AuraMotion {

constexpr float PI = 3.14159265358979323846f;
constexpr float TWO_PI = 6.28318530717958647692f;
constexpr float DEG_TO_RAD = PI / 180.0f;
constexpr float RAD_TO_DEG = 180.0f / PI;

constexpr inline float clamp(float value, float minVal, float maxVal) {
    return value < minVal ? minVal : (value > maxVal ? maxVal : value);
}

constexpr inline float lerp(float a, float b, float t) {
    return a + t * (b - a);
}

inline float smoothstep(float edge0, float edge1, float x) {
    float t = clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
    return t * t * (3.0f - 2.0f * t);
}

constexpr inline float degToRad(float deg) {
    return deg * DEG_TO_RAD;
}

constexpr inline float radToDeg(float rad) {
    return rad * RAD_TO_DEG;
}

} // namespace AuraMotion
