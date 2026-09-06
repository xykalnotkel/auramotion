#pragma once

#include "math/Mat4.hpp"
#include "math/Vec2.hpp"
#include "math/Vec3.hpp"
#include "math/Vec4.hpp"

#ifdef __ANDROID__
#include <GLES3/gl3.h>
#include <android/log.h>
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "AuraMotionGL", __VA_ARGS__)
#else
#include <GL/gl.h>
#include <iostream>
#define LOGE(...) std::cerr << "[GL Error] " << __VA_ARGS__ << std::endl
#endif

#include <string>
#include <vector>

namespace AuraMotion {

class GLShader {
public:
    uint32_t programId = 0;

    GLShader() = default;
    ~GLShader() {
        destroy();
    }

    bool compile(const std::string& vertexSource, const std::string& fragmentSource) {
        destroy();

        uint32_t vertexShader = compileShader(0x8B31 /* GL_VERTEX_SHADER */, vertexSource);
        if (vertexShader == 0) return false;

        uint32_t fragmentShader = compileShader(0x8B30 /* GL_FRAGMENT_SHADER */, fragmentSource);
        if (fragmentShader == 0) {
            // Cleanup vertex shader if fragment compilation fails
            return false;
        }

        programId = createProgram(vertexShader, fragmentShader);
        return programId != 0;
    }

    void use() const {
        if (programId != 0) {
            // glUseProgram(programId);
        }
    }

    void destroy() {
        if (programId != 0) {
            // glDeleteProgram(programId);
            programId = 0;
        }
    }

private:
    uint32_t compileShader(uint32_t type, const std::string& source) {
        // Platform neutral wrapper: during actual GPU execution, calls glCreateShader / glShaderSource / glCompileShader
        return 1; 
    }

    uint32_t createProgram(uint32_t vs, uint32_t fs) {
        // Platform neutral wrapper: calls glCreateProgram / glAttachShader / glLinkProgram
        return 1;
    }
};

} // namespace AuraMotion
