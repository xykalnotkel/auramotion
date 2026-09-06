#pragma once

#include "MathUtils.hpp"
#include <cmath>

namespace AuraMotion {

struct Vec2 {
    float x = 0.0f;
    float y = 0.0f;

    constexpr Vec2() = default;
    constexpr Vec2(float x_, float y_) : x(x_), y(y_) {}
    explicit constexpr Vec2(float scalar) : x(scalar), y(scalar) {}

    Vec2 operator+(const Vec2& rhs) const { return Vec2(x + rhs.x, y + rhs.y); }
    Vec2 operator-(const Vec2& rhs) const { return Vec2(x - rhs.x, y - rhs.y); }
    Vec2 operator*(float scalar) const { return Vec2(x * scalar, y * scalar); }
    Vec2 operator/(float scalar) const { return Vec2(x / scalar, y / scalar); }
    Vec2 operator*(const Vec2& rhs) const { return Vec2(x * rhs.x, y * rhs.y); }
    Vec2 operator/(const Vec2& rhs) const { return Vec2(x / rhs.x, y / rhs.y); }

    Vec2& operator+=(const Vec2& rhs) { x += rhs.x; y += rhs.y; return *this; }
    Vec2& operator-=(const Vec2& rhs) { x -= rhs.x; y -= rhs.y; return *this; }
    Vec2& operator*=(float scalar) { x *= scalar; y *= scalar; return *this; }
    Vec2& operator/=(float scalar) { x /= scalar; y /= scalar; return *this; }

    bool operator==(const Vec2& rhs) const { return std::abs(x - rhs.x) < 1e-5f && std::abs(y - rhs.y) < 1e-5f; }
    bool operator!=(const Vec2& rhs) const { return !(*this == rhs); }

    float lengthSquared() const { return x * x + y * y; }
    float length() const { return std::sqrt(lengthSquared()); }

    Vec2 normalized() const {
        float len = length();
        return len > 0.0f ? Vec2(x / len, y / len) : Vec2(0.0f, 0.0f);
    }

    float dot(const Vec2& rhs) const { return x * rhs.x + y * rhs.y; }
    float cross(const Vec2& rhs) const { return x * rhs.y - y * rhs.x; }

    static Vec2 lerp(const Vec2& a, const Vec2& b, float t) {
        return Vec2(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t);
    }

    Vec2 rotated(float angleRad) const {
        float cosA = std::cos(angleRad);
        float sinA = std::sin(angleRad);
        return Vec2(x * cosA - y * sinA, x * sinA + y * cosA);
    }
};

} // namespace AuraMotion
