#pragma once

#include "Vec2.hpp"
#include "Vec3.hpp"
#include "Vec4.hpp"
#include "MathUtils.hpp"
#include <array>
#include <cmath>
#include <cstring>

namespace AuraMotion {

struct Mat4 {
    // Column-major array of 16 elements (OpenGL ES standard)
    std::array<float, 16> m{
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    };

    static Mat4 identity() {
        return Mat4();
    }

    float& operator()(size_t row, size_t col) {
        return m[col * 4 + row];
    }

    float operator()(size_t row, size_t col) const {
        return m[col * 4 + row];
    }

    const float* data() const { return m.data(); }
    float* data() { return m.data(); }

    Mat4 operator*(const Mat4& rhs) const {
        Mat4 res;
        for (int row = 0; row < 4; ++row) {
            for (int col = 0; col < 4; ++col) {
                float sum = 0.0f;
                for (int k = 0; k < 4; ++k) {
                    sum += (*this)(row, k) * rhs(k, col);
                }
                res(row, col) = sum;
            }
        }
        return res;
    }

    Vec4 operator*(const Vec4& v) const {
        return Vec4(
            (*this)(0,0)*v.x() + (*this)(0,1)*v.y() + (*this)(0,2)*v.z() + (*this)(0,3)*v.w(),
            (*this)(1,0)*v.x() + (*this)(1,1)*v.y() + (*this)(1,2)*v.z() + (*this)(1,3)*v.w(),
            (*this)(2,0)*v.x() + (*this)(2,1)*v.y() + (*this)(2,2)*v.z() + (*this)(2,3)*v.w(),
            (*this)(3,0)*v.x() + (*this)(3,1)*v.y() + (*this)(3,2)*v.z() + (*this)(3,3)*v.w()
        );
    }

    Vec2 transformPoint(const Vec2& p) const {
        Vec4 v4(p.x, p.y, 0.0f, 1.0f);
        Vec4 res = (*this) * v4;
        if (std::abs(res.w()) > 1e-6f) {
            return Vec2(res.x() / res.w(), res.y() / res.w());
        }
        return Vec2(res.x(), res.y());
    }

    static Mat4 translation(float tx, float ty, float tz = 0.0f) {
        Mat4 res = identity();
        res(0, 3) = tx;
        res(1, 3) = ty;
        res(2, 3) = tz;
        return res;
    }

    static Mat4 translation(const Vec2& t) {
        return translation(t.x, t.y, 0.0f);
    }

    static Mat4 scaling(float sx, float sy, float sz = 1.0f) {
        Mat4 res = identity();
        res(0, 0) = sx;
        res(1, 1) = sy;
        res(2, 2) = sz;
        return res;
    }

    static Mat4 scaling(const Vec2& s) {
        return scaling(s.x, s.y, 1.0f);
    }

    static Mat4 rotationZ(float angleRad) {
        Mat4 res = identity();
        float c = std::cos(angleRad);
        float s = std::sin(angleRad);
        res(0, 0) = c;  res(0, 1) = -s;
        res(1, 0) = s;  res(1, 1) = c;
        return res;
    }

    static Mat4 rotationX(float angleRad) {
        Mat4 res = identity();
        float c = std::cos(angleRad);
        float s = std::sin(angleRad);
        res(1, 1) = c;  res(1, 2) = -s;
        res(2, 1) = s;  res(2, 2) = c;
        return res;
    }

    static Mat4 rotationY(float angleRad) {
        Mat4 res = identity();
        float c = std::cos(angleRad);
        float s = std::sin(angleRad);
        res(0, 0) = c;  res(0, 2) = s;
        res(2, 0) = -s; res(2, 2) = c;
        return res;
    }

    static Mat4 skew(float skewXRad, float skewYRad) {
        Mat4 res = identity();
        res(0, 1) = std::tan(skewXRad);
        res(1, 0) = std::tan(skewYRad);
        return res;
    }

    static Mat4 ortho(float left, float right, float bottom, float top, float nearVal = -1.0f, float farVal = 1.0f) {
        Mat4 res = identity();
        res(0, 0) = 2.0f / (right - left);
        res(1, 1) = 2.0f / (top - bottom);
        res(2, 2) = -2.0f / (farVal - nearVal);
        res(0, 3) = -(right + left) / (right - left);
        res(1, 3) = -(top + bottom) / (top - bottom);
        res(2, 3) = -(farVal + nearVal) / (farVal - nearVal);
        return res;
    }

    static Mat4 perspective(float fovRad, float aspect, float nearVal, float farVal) {
        Mat4 res;
        float tanHalfFov = std::tan(fovRad / 2.0f);
        res(0, 0) = 1.0f / (aspect * tanHalfFov);
        res(1, 1) = 1.0f / tanHalfFov;
        res(2, 2) = -(farVal + nearVal) / (farVal - nearVal);
        res(2, 3) = -(2.0f * farVal * nearVal) / (farVal - nearVal);
        res(3, 2) = -1.0f;
        res(3, 3) = 0.0f;
        return res;
    }

    Mat4 inverted() const {
        // Analytic inverse for 4x4 matrix
        float inv[16], det;
        const float* m_ = m.data();

        inv[0] = m_[5]  * m_[10] * m_[15] - 
                 m_[5]  * m_[11] * m_[14] - 
                 m_[9]  * m_[6]  * m_[15] + 
                 m_[9]  * m_[7]  * m_[14] +
                 m_[13] * m_[6]  * m_[11] - 
                 m_[13] * m_[7]  * m_[10];

        inv[4] = -m_[4]  * m_[10] * m_[15] + 
                  m_[4]  * m_[11] * m_[14] + 
                  m_[8]  * m_[6]  * m_[15] - 
                  m_[8]  * m_[7]  * m_[14] - 
                  m_[12] * m_[6]  * m_[11] + 
                  m_[12] * m_[7]  * m_[10];

        inv[8] = m_[4]  * m_[9] * m_[15] - 
                 m_[4]  * m_[11] * m_[13] - 
                 m_[8]  * m_[5] * m_[15] + 
                 m_[8]  * m_[7] * m_[13] + 
                 m_[12] * m_[5] * m_[11] - 
                 m_[12] * m_[7] * m_[9];

        inv[12] = -m_[4]  * m_[9] * m_[14] + 
                   m_[4]  * m_[10] * m_[13] +
                   m_[8]  * m_[5] * m_[14] - 
                   m_[8]  * m_[6] * m_[13] - 
                   m_[12] * m_[5] * m_[10] + 
                   m_[12] * m_[6] * m_[9];

        inv[1] = -m_[1]  * m_[10] * m_[15] + 
                  m_[1]  * m_[11] * m_[14] + 
                  m_[9]  * m_[2] * m_[15] - 
                  m_[9]  * m_[3] * m_[14] - 
                  m_[13] * m_[2] * m_[11] + 
                  m_[13] * m_[3] * m_[10];

        inv[5] = m_[0]  * m_[10] * m_[15] - 
                 m_[0]  * m_[11] * m_[14] - 
                 m_[8]  * m_[2] * m_[15] + 
                 m_[8]  * m_[3] * m_[14] + 
                 m_[12] * m_[2] * m_[11] - 
                 m_[12] * m_[3] * m_[10];

        inv[9] = -m_[0]  * m_[9] * m_[15] + 
                  m_[0]  * m_[11] * m_[13] + 
                  m_[8]  * m_[1] * m_[15] - 
                  m_[8]  * m_[3] * m_[13] - 
                  m_[12] * m_[1] * m_[11] + 
                  m_[12] * m_[3] * m_[9];

        inv[13] = m_[0]  * m_[9] * m_[14] - 
                  m_[0]  * m_[10] * m_[13] - 
                  m_[8]  * m_[1] * m_[14] + 
                  m_[8]  * m_[2] * m_[13] + 
                  m_[12] * m_[1] * m_[10] - 
                  m_[12] * m_[2] * m_[9];

        inv[2] = m_[1]  * m_[6] * m_[15] - 
                 m_[1]  * m_[7] * m_[14] - 
                 m_[5]  * m_[2] * m_[15] + 
                 m_[5]  * m_[3] * m_[14] + 
                 m_[13] * m_[2] * m_[7] - 
                 m_[13] * m_[3] * m_[6];

        inv[6] = -m_[0]  * m_[6] * m_[15] + 
                  m_[0]  * m_[7] * m_[14] + 
                  m_[4]  * m_[2] * m_[15] - 
                  m_[4]  * m_[3] * m_[14] - 
                  m_[12] * m_[2] * m_[7] + 
                  m_[12] * m_[3] * m_[6];

        inv[10] = m_[0]  * m_[5] * m_[15] - 
                  m_[0]  * m_[7] * m_[13] - 
                  m_[4]  * m_[1] * m_[15] + 
                  m_[4]  * m_[3] * m_[13] + 
                  m_[12] * m_[1] * m_[7] - 
                  m_[12] * m_[3] * m_[5];

        inv[14] = -m_[0]  * m_[5] * m_[14] + 
                   m_[0]  * m_[6] * m_[13] + 
                   m_[4]  * m_[1] * m_[14] - 
                   m_[4]  * m_[2] * m_[13] - 
                   m_[12] * m_[1] * m_[6] + 
                   m_[12] * m_[2] * m_[5];

        inv[3] = -m_[1] * m_[6] * m_[11] + 
                  m_[1] * m_[7] * m_[10] + 
                  m_[5] * m_[2] * m_[11] - 
                  m_[5] * m_[3] * m_[10] - 
                  m_[9] * m_[2] * m_[7] + 
                  m_[9] * m_[3] * m_[6];

        inv[7] = m_[0] * m_[6] * m_[11] - 
                 m_[0] * m_[7] * m_[10] - 
                 m_[4] * m_[2] * m_[11] + 
                 m_[4] * m_[3] * m_[10] + 
                 m_[8] * m_[2] * m_[7] - 
                 m_[8] * m_[3] * m_[6];

        inv[11] = -m_[0] * m_[5] * m_[11] + 
                   m_[0] * m_[7] * m_[9] + 
                   m_[4] * m_[1] * m_[11] - 
                   m_[4] * m_[3] * m_[9] - 
                   m_[8] * m_[1] * m_[7] + 
                   m_[8] * m_[3] * m_[5];

        inv[15] = m_[0] * m_[5] * m_[10] - 
                  m_[0] * m_[6] * m_[9] - 
                  m_[4] * m_[1] * m_[10] + 
                  m_[4] * m_[2] * m_[9] + 
                  m_[8] * m_[1] * m_[6] - 
                  m_[8] * m_[2] * m_[5];

        det = m_[0] * inv[0] + m_[1] * inv[4] + m_[2] * inv[8] + m_[3] * inv[12];

        if (std::abs(det) < 1e-6f) {
            return identity();
        }

        det = 1.0f / det;
        Mat4 result;
        for (int i = 0; i < 16; i++) {
            result.m[i] = inv[i] * det;
        }
        return result;
    }
};

} // namespace AuraMotion
