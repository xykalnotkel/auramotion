#pragma once

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeCreateRenderer(JNIEnv *env, jobject thiz);

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeDestroyRenderer(JNIEnv *env, jobject thiz, jlong handle);

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeInit(JNIEnv *env, jobject thiz, jlong handle, jint width, jint height);

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeResize(JNIEnv *env, jobject thiz, jlong handle, jint width, jint height);

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeTick(JNIEnv *env, jobject thiz, jlong handle, jfloat delta_time);

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeRender(JNIEnv *env, jobject thiz, jlong handle);

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeSeek(JNIEnv *env, jobject thiz, jlong handle, jfloat time);

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeSetPlaying(JNIEnv *env, jobject thiz, jlong handle, jboolean is_playing);

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeAddShapeLayer(JNIEnv *env, jobject thiz, jlong handle, jstring id, jstring name, jint shape_type);

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeAddTextLayer(JNIEnv *env, jobject thiz, jlong handle, jstring id, jstring name, jstring text);

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeAddKeyframeFloat(
    JNIEnv *env, jobject thiz, jlong handle, jstring layer_id, jstring prop_name,
    jfloat time, jfloat value, jfloat cp1x, jfloat cp1y, jfloat cp2x, jfloat cp2y);

JNIEXPORT void JNICALL
Java_com_auramotion_editor_engine_NativeEngine_nativeAddKeyframeVec2(
    JNIEnv *env, jobject thiz, jlong handle, jstring layer_id, jstring prop_name,
    jfloat time, jfloat x, jfloat y, jfloat cp1x, jfloat cp1y, jfloat cp2x, jfloat cp2y);

#ifdef __cplusplus
}
#endif
