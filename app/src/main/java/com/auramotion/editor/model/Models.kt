package com.auramotion.editor.model

import androidx.annotation.Keep
import java.util.UUID

@Keep
enum class LayerType {
    SHAPE,
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    ADJUSTMENT,
    NULL_GROUP
}

@Keep
enum class ShapeType {
    RECTANGLE,
    ROUNDED_RECTANGLE,
    CIRCLE,
    STAR,
    POLYGON,
    HEART
}

@Keep
enum class BlendMode {
    NORMAL,
    ADD,
    MULTIPLY,
    SCREEN,
    OVERLAY,
    DARKEN,
    LIGHTEN,
    COLOR_DODGE,
    COLOR_BURN,
    DIFFERENCE
}

@Keep
enum class EffectType {
    GAUSSIAN_BLUR,
    GLOW,
    CHROMATIC_ABERRATION,
    COLOR_GRADING,
    WAVE_WARP,
    VIGNETTE
}

@Keep
data class CurveData(
    val cp1x: Float = 0.42f,
    val cp1y: Float = 0.0f,
    val cp2x: Float = 0.58f,
    val cp2y: Float = 1.0f
) {
    companion object {
        val Linear = CurveData(0.0f, 0.0f, 1.0f, 1.0f)
        val EaseIn = CurveData(0.42f, 0.0f, 1.0f, 1.0f)
        val EaseOut = CurveData(0.0f, 0.0f, 0.58f, 1.0f)
        val EaseInOut = CurveData(0.42f, 0.0f, 0.58f, 1.0f)
        val FastInSlowOut = CurveData(0.12f, 0.0f, 0.39f, 0.0f)
        val Overshoot = CurveData(0.34f, 1.56f, 0.64f, 1.0f)
        val Anticipate = CurveData(0.36f, 0.0f, 0.66f, -0.56f)
    }
}

@Keep
data class KeyframeData<T>(
    val id: String = UUID.randomUUID().toString(),
    val time: Float,
    val value: T,
    val curve: CurveData = CurveData.EaseInOut
)

@Keep
data class ShapeConfig(
    val shapeType: ShapeType = ShapeType.ROUNDED_RECTANGLE,
    val width: Float = 240f,
    val height: Float = 240f,
    val cornerRadius: Float = 32f,
    val pointsCount: Int = 5,
    val fillColor: Long = 0xFF00E5FF, // Neon Cyan
    val strokeColor: Long = 0xFFFFFFFF,
    val strokeWidth: Float = 4f,
    val hasFill: Boolean = true,
    val hasStroke: Boolean = true
)

@Keep
data class TextConfig(
    val text: String = "AuraMotion",
    val fontSize: Float = 56f,
    val fontFamily: String = "Sans-Serif",
    val color: Long = 0xFFFFFFFF,
    val strokeColor: Long = 0xFF000000,
    val strokeWidth: Float = 0f,
    val letterSpacing: Float = 2f
)

@Keep
data class EffectConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: EffectType,
    val enabled: Boolean = true,
    val params: Map<String, Float> = mapOf()
)

@Keep
data class LayerData(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: LayerType,
    val startTime: Float = 0.0f,
    val endTime: Float = 5.0f,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val parentId: String? = null,
    val blendMode: BlendMode = BlendMode.NORMAL,

    // Transform State
    val posX: Float = 0f,
    val posY: Float = 0f,
    val posZ: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotationZ: Float = 0f,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val opacity: Float = 1f,
    val anchorX: Float = 0.5f,
    val anchorY: Float = 0.5f,

    // Keyframes
    val posKeyframes: List<KeyframeData<Pair<Float, Float>>> = listOf(),
    val scaleKeyframes: List<KeyframeData<Pair<Float, Float>>> = listOf(),
    val rotKeyframes: List<KeyframeData<Float>> = listOf(),
    val opacityKeyframes: List<KeyframeData<Float>> = listOf(),

    // Content Config
    val shapeConfig: ShapeConfig? = null,
    val textConfig: TextConfig? = null,
    val effects: List<EffectConfig> = listOf()
)

@Keep
data class ProjectConfig(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Motion Project",
    val width: Int = 1080,
    val height: Int = 1920,
    val fps: Int = 60,
    val duration: Float = 5.0f,
    val backgroundColor: Long = 0xFF0D0F12,
    val layers: List<LayerData> = listOf()
)
