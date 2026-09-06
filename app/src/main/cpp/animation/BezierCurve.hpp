#pragma once

#include "math/MathUtils.hpp"
#include <cmath>
#include <algorithm>

namespace AuraMotion {

class BezierCurve {
public:
    float x1 = 0.25f;
    float y1 = 0.1f;
    float x2 = 0.25f;
    float y2 = 1.0f;

    constexpr BezierCurve() = default;
    constexpr BezierCurve(float p1x, float p1y, float p2x, float p2y)
        : x1(clamp(p1x, 0.0f, 1.0f)), y1(p1y), x2(clamp(p2x, 0.0f, 1.0f)), y2(p2y) {}

    // Preset Easing Curves
    static BezierCurve Linear() { return BezierCurve(0.0f, 0.0f, 1.0f, 1.0f); }
    static BezierCurve EaseIn() { return BezierCurve(0.42f, 0.0f, 1.0f, 1.0f); }
    static BezierCurve EaseOut() { return BezierCurve(0.0f, 0.0f, 0.58f, 1.0f); }
    static BezierCurve EaseInOut() { return BezierCurve(0.42f, 0.0f, 0.58f, 1.0f); }
    static BezierCurve FastInSlowOut() { return BezierCurve(0.12f, 0.0f, 0.39f, 0.0f); }
    static BezierCurve SlowInFastOut() { return BezierCurve(0.61f, 1.0f, 0.88f, 1.0f); }
    static BezierCurve Overshoot() { return BezierCurve(0.34f, 1.56f, 0.64f, 1.0f); }
    static BezierCurve Anticipate() { return BezierCurve(0.36f, 0.0f, 0.66f, -0.56f); }

    // Solve for y given x (time progress 0..1)
    float solve(float x) const {
        if (x <= 0.0f) return 0.0f;
        if (x >= 1.0f) return 1.0f;
        if (std::abs(x1 - y1) < 1e-4f && std::abs(x2 - y2) < 1e-4f && std::abs(x1 - 0.0f) < 1e-4f) {
            return x; // Linear shortcut
        }

        // Newton-Raphson to solve for curve parameter u where curveX(u) == x
        float u = x; // initial guess
        for (int i = 0; i < 8; ++i) {
            float currentX = sampleCurveX(u) - x;
            if (std::abs(currentX) < 1e-5f) {
                return sampleCurveY(u);
            }
            float dXdu = sampleCurveDerivativeX(u);
            if (std::abs(dXdu) < 1e-5f) {
                break;
            }
            u -= currentX / dXdu;
        }

        // Fallback: Bisection search if Newton-Raphson diverges
        float uMin = 0.0f;
        float uMax = 1.0f;
        u = x;
        for (int i = 0; i < 12; ++i) {
            float currentX = sampleCurveX(u);
            if (std::abs(currentX - x) < 1e-5f) {
                return sampleCurveY(u);
            }
            if (x > currentX) {
                uMin = u;
            } else {
                uMax = u;
            }
            u = (uMin + uMax) * 0.5f;
        }

        return sampleCurveY(u);
    }

private:
    float sampleCurveX(float t) const {
        // Cubic bezier: (1-t)^3 * 0 + 3(1-t)^2 * t * x1 + 3(1-t) * t^2 * x2 + t^3 * 1
        float oneMinusT = 1.0f - t;
        return 3.0f * oneMinusT * oneMinusT * t * x1 + 3.0f * oneMinusT * t * t * x2 + t * t * t;
    }

    float sampleCurveY(float t) const {
        float oneMinusT = 1.0f - t;
        return 3.0f * oneMinusT * oneMinusT * t * y1 + 3.0f * oneMinusT * t * t * y2 + t * t * t;
    }

    float sampleCurveDerivativeX(float t) const {
        float oneMinusT = 1.0f - t;
        return 3.0f * oneMinusT * oneMinusT * x1 + 6.0f * oneMinusT * t * (x2 - x1) + 3.0f * t * t * (1.0f - x2);
    }
};

} // namespace AuraMotion
