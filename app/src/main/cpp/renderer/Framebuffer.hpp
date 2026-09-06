#pragma once

#include "math/MathUtils.hpp"
#include <cstdint>

namespace AuraMotion {

class Framebuffer {
public:
    uint32_t fboId = 0;
    uint32_t textureId = 0;
    int width = 0;
    int height = 0;

    Framebuffer() = default;
    Framebuffer(int w, int h) : width(w), height(h) {}

    void init(int w, int h) {
        width = w;
        height = h;
        // In real GLES: glGenFramebuffers, glGenTextures, glTexImage2D, glFramebufferTexture2D
    }

    void bind() const {
        // glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        // glViewport(0, 0, width, height);
    }

    void unbind() const {
        // glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    void resize(int w, int h) {
        if (width != w || height != h) {
            destroy();
            init(w, h);
        }
    }

    void destroy() {
        if (fboId != 0) {
            // glDeleteFramebuffers(1, &fboId);
            fboId = 0;
        }
        if (textureId != 0) {
            // glDeleteTextures(1, &textureId);
            textureId = 0;
        }
    }
};

class Texture {
public:
    uint32_t id = 0;
    int width = 0;
    int height = 0;
    int channels = 4;

    Texture() = default;
    Texture(int w, int h, int ch = 4) : width(w), height(h), channels(ch) {}

    void bind(uint32_t slot = 0) const {
        // glActiveTexture(GL_TEXTURE0 + slot);
        // glBindTexture(GL_TEXTURE_2D, id);
    }

    void destroy() {
        if (id != 0) {
            // glDeleteTextures(1, &id);
            id = 0;
        }
    }
};

} // namespace AuraMotion
