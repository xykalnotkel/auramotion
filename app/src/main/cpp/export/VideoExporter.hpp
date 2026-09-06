#pragma once

#include "renderer/Renderer.hpp"
#include <string>
#include <functional>

namespace AuraMotion {

enum class ExportFormat {
    MP4_H264,
    MP4_HEVC,
    GIF,
    PNG_SEQUENCE
};

struct ExportConfig {
    std::string outputPath;
    int width = 1080;
    int height = 1920;
    int fps = 60;
    int bitrateBps = 16000000; // 16 Mbps
    ExportFormat format = ExportFormat::MP4_H264;
};

class VideoExporter {
public:
    using ProgressCallback = std::function<void(float progress, int currentFrame, int totalFrames)>;

    VideoExporter() = default;

    bool exportScene(
        std::shared_ptr<Scene> scene,
        const ExportConfig& config,
        ProgressCallback onProgress
    );

    void cancel() {
        isCancelled_ = true;
    }

    bool isCancelled() const {
        return isCancelled_;
    }

private:
    bool isCancelled_ = false;
};

} // namespace AuraMotion
