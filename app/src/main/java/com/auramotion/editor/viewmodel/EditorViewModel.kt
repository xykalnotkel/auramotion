package com.auramotion.editor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auramotion.editor.engine.NativeEngine
import com.auramotion.editor.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

data class EditorUiState(
    val project: ProjectConfig = ProjectConfig(),
    val selectedLayerId: String? = null,
    val currentTime: Float = 0.0f,
    val isPlaying: Boolean = false,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0.0f,
    val activeCurveProperty: String? = null, // "position", "scale", "rotation", "opacity"
    val showEffectsDialog: Boolean = false,
    val showShapeDialog: Boolean = false,
    val showExportDialog: Boolean = false
)

class EditorViewModel : ViewModel() {

    private val nativeEngine = NativeEngine()

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<ProjectConfig>()
    private val redoStack = mutableListOf<ProjectConfig>()

    private var playbackJob: Job? = null

    init {
        createDefaultTemplate()
    }

    private fun createDefaultTemplate() {
        val neonCircle = LayerData(
            id = "layer_shape_01",
            name = "Neon Ring",
            type = LayerType.SHAPE,
            startTime = 0.0f,
            endTime = 5.0f,
            posX = 0f,
            posY = -100f,
            scaleX = 1.0f,
            scaleY = 1.0f,
            shapeConfig = ShapeConfig(
                shapeType = ShapeType.CIRCLE,
                width = 280f,
                height = 280f,
                fillColor = 0x00000000,
                strokeColor = 0xFF00E5FF,
                strokeWidth = 12f,
                hasFill = false,
                hasStroke = true
            ),
            effects = listOf(
                EffectConfig(name = "Neon Glow", type = EffectType.GLOW, params = mapOf("intensity" to 2.2f, "radius" to 20f))
            ),
            rotKeyframes = listOf(
                KeyframeData(time = 0.0f, value = 0f, curve = CurveData.EaseInOut),
                KeyframeData(time = 2.5f, value = 180f, curve = CurveData.EaseInOut),
                KeyframeData(time = 5.0f, value = 360f, curve = CurveData.EaseInOut)
            )
        )

        val titleText = LayerData(
            id = "layer_text_01",
            name = "Glow Title",
            type = LayerType.TEXT,
            startTime = 0.0f,
            endTime = 5.0f,
            posX = 0f,
            posY = 140f,
            textConfig = TextConfig(
                text = "AURAMOTION",
                fontSize = 52f,
                color = 0xFFFFFFFF,
                letterSpacing = 4f
            ),
            effects = listOf(
                EffectConfig(name = "RGB Split", type = EffectType.CHROMATIC_ABERRATION, params = mapOf("distance" to 14f))
            ),
            scaleKeyframes = listOf(
                KeyframeData(time = 0.0f, value = Pair(0.5f, 0.5f), curve = CurveData.Overshoot),
                KeyframeData(time = 1.0f, value = Pair(1.0f, 1.0f), curve = CurveData.EaseInOut)
            )
        )

        val project = ProjectConfig(
            title = "Cyberpunk Title Motion",
            layers = listOf(neonCircle, titleText)
        )

        _uiState.update { it.copy(project = project, selectedLayerId = neonCircle.id) }
    }

    fun selectLayer(layerId: String?) {
        _uiState.update { it.copy(selectedLayerId = layerId) }
    }

    fun seek(time: Float) {
        val clampedTime = time.coerceIn(0f, _uiState.value.project.duration)
        _uiState.update { it.copy(currentTime = clampedTime) }
        nativeEngine.seek(clampedTime)
    }

    fun togglePlayback() {
        val isCurrentlyPlaying = _uiState.value.isPlaying
        if (isCurrentlyPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        if (_uiState.value.isPlaying) return
        _uiState.update { it.copy(isPlaying = true) }
        nativeEngine.setPlaying(true)

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive && _uiState.value.isPlaying) {
                val now = System.nanoTime()
                val delta = (now - lastTime) / 1_000_000_000f
                lastTime = now

                var newTime = _uiState.value.currentTime + delta
                val duration = _uiState.value.project.duration

                if (newTime >= duration) {
                    newTime %= duration
                }

                _uiState.update { it.copy(currentTime = newTime) }
                nativeEngine.tick(delta)
                delay(16) // ~60fps UI tick
            }
        }
    }

    fun pause() {
        _uiState.update { it.copy(isPlaying = false) }
        nativeEngine.setPlaying(false)
        playbackJob?.cancel()
    }

    fun addShapeLayer(shapeType: ShapeType = ShapeType.ROUNDED_RECTANGLE) {
        recordUndo()
        val newLayer = LayerData(
            id = "layer_shape_${UUID.randomUUID().toString().take(6)}",
            name = when (shapeType) {
                ShapeType.RECTANGLE -> "Rectangle"
                ShapeType.ROUNDED_RECTANGLE -> "Rounded Card"
                ShapeType.CIRCLE -> "Circle"
                ShapeType.STAR -> "Star 5-Point"
                ShapeType.POLYGON -> "Hexagon"
                ShapeType.HEART -> "Heart Shape"
            },
            type = LayerType.SHAPE,
            shapeConfig = ShapeConfig(shapeType = shapeType)
        )
        _uiState.update { state ->
            val updated = state.project.copy(layers = state.project.layers + newLayer)
            state.copy(project = updated, selectedLayerId = newLayer.id, showShapeDialog = false)
        }
    }

    fun addTextLayer(text: String = "Motion Text") {
        recordUndo()
        val newLayer = LayerData(
            id = "layer_text_${UUID.randomUUID().toString().take(6)}",
            name = "Text ($text)",
            type = LayerType.TEXT,
            textConfig = TextConfig(text = text)
        )
        _uiState.update { state ->
            val updated = state.project.copy(layers = state.project.layers + newLayer)
            state.copy(project = updated, selectedLayerId = newLayer.id)
        }
    }

    fun deleteSelectedLayer() {
        val selectedId = _uiState.value.selectedLayerId ?: return
        recordUndo()
        _uiState.update { state ->
            val updated = state.project.copy(layers = state.project.layers.filter { it.id != selectedId })
            state.copy(project = updated, selectedLayerId = updated.layers.lastOrNull()?.id)
        }
    }

    fun updateLayerTransform(
        layerId: String,
        posX: Float? = null,
        posY: Float? = null,
        scaleX: Float? = null,
        scaleY: Float? = null,
        rotZ: Float? = null,
        opacity: Float? = null
    ) {
        _uiState.update { state ->
            val updatedLayers = state.project.layers.map { layer ->
                if (layer.id == layerId) {
                    layer.copy(
                        posX = posX ?: layer.posX,
                        posY = posY ?: layer.posY,
                        scaleX = scaleX ?: layer.scaleX,
                        scaleY = scaleY ?: layer.scaleY,
                        rotationZ = rotZ ?: layer.rotationZ,
                        opacity = opacity ?: layer.opacity
                    )
                } else layer
            }
            state.copy(project = state.project.copy(layers = updatedLayers))
        }
    }

    fun toggleKeyframeAtPlayhead(layerId: String, property: String) {
        recordUndo()
        val time = _uiState.value.currentTime
        _uiState.update { state ->
            val updatedLayers = state.project.layers.map { layer ->
                if (layer.id == layerId) {
                    when (property) {
                        "position" -> {
                            val existing = layer.posKeyframes.find { kotlin.math.abs(it.time - time) < 0.05f }
                            val newKeyframes = if (existing != null) {
                                layer.posKeyframes.filter { it.id != existing.id }
                            } else {
                                (layer.posKeyframes + KeyframeData(time = time, value = Pair(layer.posX, layer.posY))).sortedBy { it.time }
                            }
                            layer.copy(posKeyframes = newKeyframes)
                        }
                        "scale" -> {
                            val existing = layer.scaleKeyframes.find { kotlin.math.abs(it.time - time) < 0.05f }
                            val newKeyframes = if (existing != null) {
                                layer.scaleKeyframes.filter { it.id != existing.id }
                            } else {
                                (layer.scaleKeyframes + KeyframeData(time = time, value = Pair(layer.scaleX, layer.scaleY))).sortedBy { it.time }
                            }
                            layer.copy(scaleKeyframes = newKeyframes)
                        }
                        "rotation" -> {
                            val existing = layer.rotKeyframes.find { kotlin.math.abs(it.time - time) < 0.05f }
                            val newKeyframes = if (existing != null) {
                                layer.rotKeyframes.filter { it.id != existing.id }
                            } else {
                                (layer.rotKeyframes + KeyframeData(time = time, value = layer.rotationZ)).sortedBy { it.time }
                            }
                            layer.copy(rotKeyframes = newKeyframes)
                        }
                        "opacity" -> {
                            val existing = layer.opacityKeyframes.find { kotlin.math.abs(it.time - time) < 0.05f }
                            val newKeyframes = if (existing != null) {
                                layer.opacityKeyframes.filter { it.id != existing.id }
                            } else {
                                (layer.opacityKeyframes + KeyframeData(time = time, value = layer.opacity)).sortedBy { it.time }
                            }
                            layer.copy(opacityKeyframes = newKeyframes)
                        }
                        else -> layer
                    }
                } else layer
            }
            state.copy(project = state.project.copy(layers = updatedLayers))
        }
    }

    fun updateKeyframeCurve(layerId: String, property: String, keyframeId: String, curve: CurveData) {
        _uiState.update { state ->
            val updatedLayers = state.project.layers.map { layer ->
                if (layer.id == layerId) {
                    when (property) {
                        "position" -> layer.copy(posKeyframes = layer.posKeyframes.map { if (it.id == keyframeId) it.copy(curve = curve) else it })
                        "scale" -> layer.copy(scaleKeyframes = layer.scaleKeyframes.map { if (it.id == keyframeId) it.copy(curve = curve) else it })
                        "rotation" -> layer.copy(rotKeyframes = layer.rotKeyframes.map { if (it.id == keyframeId) it.copy(curve = curve) else it })
                        "opacity" -> layer.copy(opacityKeyframes = layer.opacityKeyframes.map { if (it.id == keyframeId) it.copy(curve = curve) else it })
                        else -> layer
                    }
                } else layer
            }
            state.copy(project = state.project.copy(layers = updatedLayers))
        }
    }

    fun addEffectToSelected(type: EffectType) {
        val selectedId = _uiState.value.selectedLayerId ?: return
        recordUndo()
        val fx = when (type) {
            EffectType.GLOW -> EffectConfig(name = "Bloom & Glow", type = type, params = mapOf("intensity" to 1.8f, "radius" to 16f))
            EffectType.CHROMATIC_ABERRATION -> EffectConfig(name = "Chromatic Split", type = type, params = mapOf("distance" to 12f, "angle" to 0f))
            EffectType.GAUSSIAN_BLUR -> EffectConfig(name = "Gaussian Blur", type = type, params = mapOf("radius" to 10f))
            EffectType.COLOR_GRADING -> EffectConfig(name = "Color Grading", type = type, params = mapOf("contrast" to 1.2f, "saturation" to 1.3f))
            EffectType.WAVE_WARP -> EffectConfig(name = "Wave Warp", type = type, params = mapOf("amplitude" to 20f, "frequency" to 5f))
            EffectType.VIGNETTE -> EffectConfig(name = "Vignette Dark", type = type, params = mapOf("radius" to 0.8f, "softness" to 0.4f))
        }

        _uiState.update { state ->
            val updated = state.project.layers.map { layer ->
                if (layer.id == selectedId) layer.copy(effects = layer.effects + fx) else layer
            }
            state.copy(project = state.project.copy(layers = updated), showEffectsDialog = false)
        }
    }

    fun toggleEffect(layerId: String, effectId: String) {
        _uiState.update { state ->
            val updated = state.project.layers.map { layer ->
                if (layer.id == layerId) {
                    val newEffects = layer.effects.map { if (it.id == effectId) it.copy(enabled = !it.enabled) else it }
                    layer.copy(effects = newEffects)
                } else layer
            }
            state.copy(project = state.project.copy(layers = updated))
        }
    }

    private fun recordUndo() {
        undoStack.add(_uiState.value.project)
        redoStack.clear()
        if (undoStack.size > 50) undoStack.removeAt(0)
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_uiState.value.project)
            val prev = undoStack.removeAt(undoStack.lastIndex)
            _uiState.update { it.copy(project = prev) }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_uiState.value.project)
            val next = redoStack.removeAt(redoStack.lastIndex)
            _uiState.update { it.copy(project = next) }
        }
    }

    fun setDialogVisible(type: String, visible: Boolean) {
        _uiState.update {
            when (type) {
                "effects" -> it.copy(showEffectsDialog = visible)
                "shape" -> it.copy(showShapeDialog = visible)
                "export" -> it.copy(showExportDialog = visible)
                else -> it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        nativeEngine.destroy()
    }
}
