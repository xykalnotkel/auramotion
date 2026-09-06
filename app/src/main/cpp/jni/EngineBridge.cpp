#include "EngineBridge.hpp"
#include "renderer/Renderer.hpp"
#include "scene/ShapeLayer.hpp"
#include <memory>
#include <string>

using namespace AuraMotion;

static Renderer* getRenderer(jlong handle) {
    return reinterpret_cast<Renderer*>(handle);
}

static std::string jstringToString(JNIEnv *env, jstring jstr) {
    if (!jstr) return "";
    const char *chars = env->GetStringUTFChars(jstr, nullptr);
    std::string str(chars);
    env->ReleaseStringUTFChars(jstr, chars);
    return str;
}

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeCreateRenderer(JNIEnv *env, jobject thiz) {
    Renderer* renderer = new Renderer();
    return reinterpret_cast<jlong>(renderer);
}

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeDestroyRenderer(JNIEnv *env, jobject thiz, jlong handle) {
    Renderer* renderer = getRenderer(handle);
    delete renderer;
}

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeInit(JNIEnv *env, jobject thiz, jlong handle, jint width, jint height) {
    Renderer* renderer = getRenderer(handle);
    if (renderer) {
        renderer->init(width, height);
    }
}

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeResize(JNIEnv *env, jobject thiz, jlong handle, jint width, jint height) {
    Renderer* renderer = getRenderer(handle);
    if (renderer) {
        renderer->resize(width, height);
    }
}

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeTick(JNIEnv *env, jobject thiz, jlong handle, jfloat delta_time) {
    Renderer* renderer = getRenderer(handle);
    if (renderer) {
        renderer->tick(delta_time);
    }
}

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeRender(JNIEnv *env, jobject thiz, jlong handle) {
    Renderer* renderer = getRenderer(handle);
    if (renderer) {
        renderer->render();
    }
}

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeSeek(JNIEnv *env, jobject thiz, jlong handle, jfloat time) {
    Renderer* renderer = getRenderer(handle);
    if (renderer) {
        renderer->seek(time);
    }
}

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeSetPlaying(JNIEnv *env, jobject thiz, jlong handle, jboolean is_playing) {
    Renderer* renderer = getRenderer(handle);
    if (renderer && renderer->scene) {
        renderer->scene->timeline.isPlaying = is_playing;
    }
}

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeAddShapeLayer(
    JNIEnv *env, jobject thiz, jlong handle, jstring id, jstring name, jint shape_type) {
    Renderer* renderer = getRenderer(handle);
    if (renderer && renderer->scene) {
        std::string layerId = jstringToString(env, id);
        std::string layerName = jstringToString(env, name);
        auto shapeLayer = std::make_shared<ShapeLayer>(layerId, layerName);
        shapeLayer->shapeType = static_cast<ShapeType>(shape_type);
        renderer->scene->addLayer(shapeLayer);
    }
}

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeAddTextLayer(
    JNIEnv *env, jobject thiz, jlong handle, jstring id, jstring name, jstring text) {
    Renderer* renderer = getRenderer(handle);
    if (renderer && renderer->scene) {
        std::string layerId = jstringToString(env, id);
        std::string layerName = jstringToString(env, name);
        std::string textContent = jstringToString(env, text);
        auto textLayer = std::make_shared<TextLayer>(layerId, layerName);
        textLayer->text = textContent;
        renderer->scene->addLayer(textLayer);
    }
}

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeAddKeyframeFloat(
    JNIEnv *env, jobject thiz, jlong handle, jstring layer_id, jstring prop_name,
    jfloat time, jfloat value, jfloat cp1x, jfloat cp1y, jfloat cp2x, jfloat cp2y) {
    Renderer* renderer = getRenderer(handle);
    if (renderer && renderer->scene) {
        std::string layerId = jstringToString(env, layer_id);
        std::string propName = jstringToString(env, prop_name);
        auto layer = renderer->scene->findLayer(layerId);
        if (layer) {
            BezierCurve curve(cp1x, cp1y, cp2x, cp2y);
            if (propName == "opacity") {
                layer->opacity.addKeyframe(time, value, curve);
            }
        }
    }
}

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeAddKeyframeVec2(
    JNIEnv *env, jobject thiz, jlong handle, jstring layer_id, jstring prop_name,
    jfloat time, jfloat x, jfloat y, jfloat cp1x, jfloat cp1y, jfloat cp2x, jfloat cp2y) {
    Renderer* renderer = getRenderer(handle);
    if (renderer && renderer->scene) {
        std::string layerId = jstringToString(env, layer_id);
        std::string propName = jstringToString(env, prop_name);
        auto layer = renderer->scene->findLayer(layerId);
        if (layer) {
            BezierCurve curve(cp1x, cp1y, cp2x, cp2y);
            if (propName == "scale") {
                layer->scale.addKeyframe(time, Vec2(x, y), curve);
            } else if (propName == "anchorPoint") {
                layer->anchorPoint.addKeyframe(time, Vec2(x, y), curve);
            } else if (propName == "skew") {
                layer->skew.addKeyframe(time, Vec2(x, y), curve);
            }
        }
    }
}

} // extern "C"
