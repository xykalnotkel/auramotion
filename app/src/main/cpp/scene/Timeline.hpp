#pragma once

#include "math/MathUtils.hpp"
#include <vector>
#include <string>

namespace AuraMotion {

struct Marker {
    float time = 0.0f;
    std::string label;
    uint32_t color = 0xFFFF4081; // Pink
};

class Timeline {
public:
    float currentTime = 0.0f;
    float duration = 5.0f; // seconds
    float fps = 60.0f;
    bool isPlaying = false;
    bool isLooping = true;
    float playbackSpeed = 1.0f;

    std::vector<Marker> markers;

    Timeline() = default;

    void play() { isPlaying = true; }
    void pause() { isPlaying = false; }
    void togglePlay() { isPlaying = !isPlaying; }

    void seek(float time) {
        currentTime = clamp(time, 0.0f, duration);
    }

    void advance(float deltaTime) {
        if (!isPlaying) return;

        currentTime += deltaTime * playbackSpeed;
        if (currentTime >= duration) {
            if (isLooping) {
                currentTime = std::fmod(currentTime, duration);
            } else {
                currentTime = duration;
                isPlaying = false;
            }
        }
    }

    int getCurrentFrame() const {
        return static_cast<int>(std::round(currentTime * fps));
    }

    int getTotalFrames() const {
        return static_cast<int>(std::round(duration * fps));
    }

    void setFrame(int frame) {
        seek(static_cast<float>(frame) / fps);
    }
};

} // namespace AuraMotion
