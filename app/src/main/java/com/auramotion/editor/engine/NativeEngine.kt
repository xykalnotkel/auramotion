package com.auramotion.editor.engine

import android.view.Surface
import androidx.annotation.Keep

/**
 * NativeEngine: High-performance JNI bridge interfacing Kotlin Jetpack Compose UI
 * directly with the C++20 rendering core, OpenGL ES 3.0 shaders, and Bezier keyframe solvers.
 */
@Keep
class NativeEngine {

    companion object {
        init {
            try {
                System.loadLibrary("auramotion")
            } catch (e: UnsatisfiedLinkError) {
                // Native library not loaded in desktop preview mode
            }
        }
    }

    private var nativeHandle: Long = 0

    init {
        try {
            nativeHandle = nativeCreateRenderer()
        } catch (e: UnsatisfiedLinkError) {
            nativeHandle = 0
        }
    }

    fun init(width: Int, height: Int) {
        if (nativeHandle != 0L) {
            nativeInit(nativeHandle, width, height)
        }
    }

    fun resize(width: Int, height: Int) {
        if (nativeHandle != 0L) {
            nativeResize(nativeHandle, width, height)
        }
    }

    fun tick(deltaTime: Float) {
        if (nativeHandle != 0L) {
            nativeTick(nativeHandle, deltaTime)
        }
    }

    fun render() {
        if (nativeHandle != 0L) {
            nativeRender(nativeHandle)
        }
    }

    fun seek(time: Float) {
        if (nativeHandle != 0L) {
            nativeSeek(nativeHandle, time)
        }
    }

    fun setPlaying(isPlaying: Boolean) {
        if (nativeHandle != 0L) {
            nativeSetPlaying(nativeHandle, isPlaying)
        }
    }

    fun addShapeLayer(id: String, name: String, shapeType: Int) {
        if (nativeHandle != 0L) {
            nativeAddShapeLayer(nativeHandle, id, name, shapeType)
        }
    }

    fun addTextLayer(id: String, name: String, text: String) {
        if (nativeHandle != 0L) {
            nativeAddTextLayer(nativeHandle, id, name, text)
        }
    }

    fun addKeyframeFloat(layerId: String, propName: String, time: Float, value: Float, cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float) {
        if (nativeHandle != 0L) {
            nativeAddKeyframeFloat(nativeHandle, layerId, propName, time, value, cp1x, cp1y, cp2x, cp2y)
        }
    }

    fun addKeyframeVec2(layerId: String, propName: String, time: Float, x: Float, y: Float, cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float) {
        if (nativeHandle != 0L) {
            nativeAddKeyframeVec2(nativeHandle, layerId, propName, time, x, y, cp1x, cp1y, cp2x, cp2y)
        }
    }

    fun destroy() {
        if (nativeHandle != 0L) {
            nativeDestroyRenderer(nativeHandle)
            nativeHandle = 0
        }
    }

    // --- Native JNI declarations ---
    private external fun nativeCreateRenderer(): Long
    private external fun nativeDestroyRenderer(handle: Long)
    private external fun nativeInit(handle: Long, width: Int, height: Int)
    private external fun nativeResize(handle: Long, width: Int, height: Int)
    private external fun nativeTick(handle: Long, deltaTime: Float)
    private external fun nativeRender(handle: Long)
    private external fun nativeSeek(handle: Long, time: Float)
    private external fun nativeSetPlaying(handle: Long, isPlaying: Boolean)
    private external fun nativeAddShapeLayer(handle: Long, id: String, name: String, shapeType: Int)
    private external fun nativeAddTextLayer(handle: Long, id: String, name: String, text: String)
    private external fun nativeAddKeyframeFloat(
        handle: Long, layerId: String, propName: String,
        time: Float, value: Float, cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float
    )
    private external fun nativeAddKeyframeVec2(
        handle: Long, layerId: String, propName: String,
        time: Float, x: Float, y: Float, cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float
    )
}
