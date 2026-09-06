#pragma once

#include "MathUtils.hpp"
#include <cmath>
#include <cstdint>
#include <string>

namespace AuraMotion {

struct Vec4 {
    float r = 0.0f;
    float g = 0.0f;
    float b = 0.0f;
    float a = 1.0f;

    constexpr Vec4() = default;
    constexpr Vec4(float r_, float g_, float b_, float a_ = 1.0f) : r(r_), g(g_), b(b_), a(a_) {}
    explicit constexpr Vec4(float scalar) : r(scalar), g(scalar), b(scalar), a(scalar) {}

    // Convenience aliases
    float& x() { return r; }
    float& y() { return g; }
    float& z() { return b; }
    float& w() { return a; }

    float x() const { return r; }
    float y() const { return g; }
    float z() const { return b; }
    float w() const { return a; }

    Vec4 operator+(const Vec4& rhs) const { return Vec4(r + rhs.r, g + rhs.g, b + rhs.b, a + rhs.a); }
    Vec4 operator-(const Vec4& rhs) const { return Vec4(r - rhs.r, g - rhs.g, b - rhs.b, a - rhs.a); }
    Vec4 operator*(float scalar) const { return Vec4(r * scalar, g * scalar, b * scalar, a * scalar); }
    Vec4 operator*(const Vec4& rhs) const { return Vec4(r * rhs.r, g * rhs.g, b * rhs.b, a * rhs.a); }

    static Vec4 fromHex(uint32_t argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        return Vec4(r, g, b, a);
    }

    uint32_t toHex() const {
        uint32_t ia = static_cast<uint32_t>(clamp(a * 255.0f, 0.0f, 255.0f));
        uint32_t ir = static_cast<uint32_t>(clamp(r * 255.0f, 0.0f, 255.0f));
        uint32_t ig = static_cast<uint32_t>(clamp(g * 255.0f, 0.0f, 255.0f));
        uint32_t ib = static_cast<uint32_t>(clamp(b * 255.0f, 0.0f, 255.0f));
        return (ia << 24) | (ir << 16) | (ig << 8) | ib;
    }

    static Vec4 lerp(const Vec4& start, const Vec4& end, float t) {
        return Vec4(
            start.r + (end.r - start.r) * t,
            start.g + (end.g - start.g) * t,
            start.b + (end.b - start.b) * t,
            start.a + (end.a - start.a) * t
        );
    }
};

using Color = Vec4;

} // namespace AuraMotion
