package com.jlj.kuiklybase.perspectivecrop

import kotlin.concurrent.Volatile

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: Ohos 放大镜像素仓；ArkTS View 经 NAPI 拉 [snapshot]，避免 props 传 IntArray。
 */
object MagnifierBlitStore {

    @Volatile
    var generation: Int = 0
        private set

    @Volatile
    private var _argb: IntArray? = null

    @Volatile
    private var _width: Int = 0

    @Volatile
    private var _height: Int = 0

    @Volatile
    var borderWidth: Float = 2f
        private set

    @Volatile
    var borderColor: Int = 0xFFFFFFFF.toInt()
        private set

    @Volatile
    var showCross: Boolean = false
        private set

    @Volatile
    var crossColor: Int = 0xFFFFFFFF.toInt()
        private set

    @Volatile
    var crossLineWidth: Float = 3f
        private set

    @Volatile
    var crossLineLength: Float = 15f
        private set

    @Volatile
    var showCropLineMirror: Boolean = true
        private set

    @Volatile
    var cropLineColor: Int = 0xFFFF3B30.toInt()
        private set

    @Volatile
    var cropLineWidth: Float = 2f
        private set

    @Volatile
    var touchViewX: Float = 0f
        private set

    @Volatile
    var touchViewY: Float = 0f
        private set

    @Volatile
    private var _viewPointsXy: FloatArray = FloatArray(0)

    val viewPointsXy: FloatArray
        get() = _viewPointsXy.copyOf()

    fun publish(
        patch: MagnifierPatch?,
        config: PerspectiveCropConfig,
        viewPoints: List<CropPoint> = emptyList(),
        touchViewX: Float = 0f,
        touchViewY: Float = 0f,
    ) {
        showCropLineMirror = config.showMagnifierCropLineMirror
        cropLineColor = config.borderColor.toInt()
        cropLineWidth = config.borderWidthPx.coerceAtLeast(1f)
        this.touchViewX = touchViewX
        this.touchViewY = touchViewY
        _viewPointsXy = if (viewPoints.size == 4) {
            floatArrayOf(
                viewPoints[0].x, viewPoints[0].y,
                viewPoints[1].x, viewPoints[1].y,
                viewPoints[2].x, viewPoints[2].y,
                viewPoints[3].x, viewPoints[3].y,
            )
        } else {
            FloatArray(0)
        }

        if (patch == null || patch.width <= 0 || patch.height <= 0) {
            clearPixelsOnly()
            generation += 1
            return
        }
        val w = patch.width
        val h = patch.height
        val pixels = patch.argbPixels
        if (pixels.size < w * h) {
            clearPixelsOnly()
            generation += 1
            return
        }
        val existing = _argb
        val buf = if (existing != null && existing.size == w * h) {
            existing
        } else {
            IntArray(w * h).also { _argb = it }
        }
        pixels.copyInto(buf, 0, 0, w * h)
        _width = w
        _height = h
        borderWidth = config.magnifierBorderWidth.coerceAtLeast(1f)
        borderColor = config.magnifierBorderColor.toInt()
        showCross = config.showMagnifierCenterCross
        crossColor = config.magnifierCrossColor.toInt()
        crossLineWidth = config.magnifierCrossLineWidth.coerceAtLeast(1f)
        crossLineLength = config.magnifierCrossLineLength.coerceAtLeast(1f)
        generation += 1
    }

    fun clear() {
        clearPixelsOnly()
        showCropLineMirror = false
        _viewPointsXy = FloatArray(0)
        generation += 1
    }

    /**
     * 只读快照（拷贝像素与点），供 NAPI/@CName 跨语言读取。
     */
    fun snapshot(): MagnifierBlitSnapshot {
        val w = _width
        val h = _height
        val src = _argb
        val argb = if (src != null && w > 0 && h > 0 && src.size >= w * h) {
            src.copyOf(w * h)
        } else {
            IntArray(0)
        }
        return MagnifierBlitSnapshot(
            generation = generation,
            width = if (argb.isEmpty()) 0 else w,
            height = if (argb.isEmpty()) 0 else h,
            argbPixels = argb,
            borderWidth = borderWidth,
            borderColor = borderColor,
            showCross = showCross,
            crossColor = crossColor,
            crossLineWidth = crossLineWidth,
            crossLineLength = crossLineLength,
            showCropLineMirror = showCropLineMirror,
            cropLineColor = cropLineColor,
            cropLineWidth = cropLineWidth,
            touchViewX = touchViewX,
            touchViewY = touchViewY,
            viewPointsXy = _viewPointsXy.copyOf(),
        )
    }

    private fun clearPixelsOnly() {
        _argb = null
        _width = 0
        _height = 0
    }
}

/**
 * MagnifierBlitStore 跨语言快照；[argbPixels] 为拷贝，行优先 ARGB8888。
 */
data class MagnifierBlitSnapshot(
    val generation: Int,
    val width: Int,
    val height: Int,
    val argbPixels: IntArray,
    val borderWidth: Float,
    val borderColor: Int,
    val showCross: Boolean,
    val crossColor: Int,
    val crossLineWidth: Float,
    val crossLineLength: Float,
    val showCropLineMirror: Boolean,
    val cropLineColor: Int,
    val cropLineWidth: Float,
    val touchViewX: Float,
    val touchViewY: Float,
    val viewPointsXy: FloatArray,
) {
    /** ARGB Int → RGBA 字节（供 OH PixelMap RGBA_8888）。 */
    fun toRgbaBytes(): ByteArray {
        val n = width * height
        if (n <= 0 || argbPixels.size < n) return ByteArray(0)
        val out = ByteArray(n * 4)
        var o = 0
        for (i in 0 until n) {
            val c = argbPixels[i]
            out[o++] = ((c ushr 16) and 0xFF).toByte()
            out[o++] = ((c ushr 8) and 0xFF).toByte()
            out[o++] = (c and 0xFF).toByte()
            out[o++] = ((c ushr 24) and 0xFF).toByte()
        }
        return out
    }
}
