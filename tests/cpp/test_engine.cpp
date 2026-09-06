#include "math/Vec2.hpp"
#include "math/Vec3.hpp"
#include "math/Vec4.hpp"
#include "math/Mat4.hpp"
#include "animation/BezierCurve.hpp"
#include "animation/AnimatableProperty.hpp"
#include "scene/Scene.hpp"
#include "scene/ShapeLayer.hpp"
#include "scene/TextLayer.hpp"
#include "effects/GlowEffect.hpp"
#include "effects/GaussianBlurEffect.hpp"
#include "effects/ChromaticAberrationEffect.hpp"
#include "export/VideoExporter.hpp"

#include <iostream>
#include <cassert>
#include <cmath>

using namespace AuraMotion;

void testMath() {
    std::cout << "[Test] Math & Matrix Operations..." << std::endl;
    Vec2 v1(10.0f, 20.0f);
    Vec2 v2(5.0f, 10.0f);
    Vec2 sum = v1 + v2;
    assert(sum.x == 15.0f && sum.y == 30.0f);

    Mat4 t = Mat4::translation(100.0f, 200.0f);
    Vec2 p(0.0f, 0.0f);
    Vec2 transformed = t.transformPoint(p);
    assert(std::abs(transformed.x - 100.0f) < 1e-4f);
    assert(std::abs(transformed.y - 200.0f) < 1e-4f);

    Mat4 inv = t.inverted();
    Vec2 back = inv.transformPoint(transformed);
    assert(std::abs(back.x) < 1e-4f && std::abs(back.y) < 1e-4f);
    std::cout << "  ✓ Math tests passed!" << std::endl;
}

void testBezierAndKeyframes() {
    std::cout << "[Test] Cubic Bezier & Keyframe Interpolation..." << std::endl;
    BezierCurve curve = BezierCurve::EaseInOut();
    
    float startVal = curve.solve(0.0f);
    float midVal = curve.solve(0.5f);
    float endVal = curve.solve(1.0f);

    assert(std::abs(startVal - 0.0f) < 1e-4f);
    assert(std::abs(midVal - 0.5f) < 0.05f);
    assert(std::abs(endVal - 1.0f) < 1e-4f);

    AnimatableProperty<float> opacity("Opacity", 1.0f);
    opacity.addKeyframe(0.0f, 0.0f, BezierCurve::EaseIn());
    opacity.addKeyframe(1.0f, 100.0f, BezierCurve::EaseOut());
    opacity.addKeyframe(2.0f, 50.0f, BezierCurve::Linear());

    assert(opacity.evaluate(0.0f) == 0.0f);
    assert(opacity.evaluate(1.0f) == 100.0f);
    assert(opacity.evaluate(2.0f) == 50.0f);
    assert(opacity.evaluate(1.5f) == 75.0f); // linear interpolation between 100 and 50

    std::cout << "  ✓ Keyframing tests passed!" << std::endl;
}

void testSceneAndEffects() {
    std::cout << "[Test] Scene, Layers & Effects Pipeline..." << std::endl;
    Scene scene;
    scene.width = 1080;
    scene.height = 1920;

    auto shape = std::make_shared<ShapeLayer>("rect_01", "Neon Card");
    shape->shapeType = ShapeType::RoundedRectangle;
    shape->position.addKeyframe(0.0f, Vec3(0.0f, -500.0f, 0.0f));
    shape->position.addKeyframe(1.0f, Vec3(0.0f, 0.0f, 0.0f), BezierCurve::Overshoot());

    auto glow = std::make_shared<GlowEffect>("glow_01");
    glow->intensity.addKeyframe(0.0f, 0.0f);
    glow->intensity.addKeyframe(1.0f, 2.5f);
    shape->addEffect(glow);

    auto text = std::make_shared<TextLayer>("text_01", "Title");
    text->text = "ALIGHT MOTION CLONE";

    scene.addLayer(shape);
    scene.addLayer(text);

    assert(scene.layers.size() == 2);
    
    // Evaluate at t = 1.0s
    scene.update(1.0f);
    assert(std::abs(glow->currentIntensity - 2.5f) < 1e-4f);

    std::cout << "  ✓ Scene & Effects tests passed!" << std::endl;
}

void testOfflineExport() {
    std::cout << "[Test] Offline Video Export Simulation..." << std::endl;
    auto scene = std::make_shared<Scene>();
    scene->timeline.duration = 1.0f; // 1 second test
    scene->timeline.fps = 30.0f;

    auto shape = std::make_shared<ShapeLayer>("shape_export", "Export Shape");
    scene->addLayer(shape);

    VideoExporter exporter;
    ExportConfig config;
    config.width = 720;
    config.height = 1280;
    config.fps = 30;

    int progressCalls = 0;
    bool success = exporter.exportScene(scene, config, [&](float progress, int frame, int total) {
        progressCalls++;
    });

    assert(success);
    assert(progressCalls == 30);
    std::cout << "  ✓ Export tests passed (30/30 frames rendered)!" << std::endl;
}

int main() {
    std::cout << "=== AuraMotion Native C++ Engine Test Suite ===" << std::endl;
    testMath();
    testBezierAndKeyframes();
    testSceneAndEffects();
    testOfflineExport();
    std::cout << "=== All Tests Passed Successfully! ===" << std::endl;
    return 0;
}
