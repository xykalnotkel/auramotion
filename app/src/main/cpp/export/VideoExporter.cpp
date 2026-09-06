#include "VideoExporter.hpp"
#include <iostream>
#include <thread>
#include <chrono>

namespace AuraMotion {

bool VideoExporter::exportScene(
    std::shared_ptr<Scene> scene,
    const ExportConfig& config,
    ProgressCallback onProgress
) {
    if (!scene) return false;
    isCancelled_ = false;

    float duration = scene->timeline.duration;
    int totalFrames = static_cast<int>(std::ceil(duration * config.fps));
    if (totalFrames <= 0) totalFrames = 1;

    Compositor offlineCompositor;
    offlineCompositor.init(config.width, config.height);

    for (int frame = 0; frame < totalFrames; ++frame) {
        if (isCancelled_) {
            return false;
        }

        float time = static_cast<float>(frame) / static_cast<float>(config.fps);
        scene->update(time);

        // Render frame to offscreen FBO
        offlineCompositor.renderScene(*scene, time);

        // Read pixels & push to encoder (MediaCodec / FFmpeg / WebCodecs)
        float progress = static_cast<float>(frame + 1) / static_cast<float>(totalFrames);
        if (onProgress) {
            onProgress(progress, frame + 1, totalFrames);
        }
    }

    return true;
}

} // namespace AuraMotion
