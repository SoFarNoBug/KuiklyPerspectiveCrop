package com.jlj.kuiklybase.perspectivecrop

import kotlin.concurrent.Volatile

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: Ohos 放大镜像素仓；整包不可变快照 + @Volatile 引用替换，供 NAPI 原子读取。
 */
object MagnifierBlitStore {

    @Volatile
    private var _state: MagnifierBlitSnapshot = MagnifierBlitSnapshot.EMPTY

    val generation: Int
        get() = _state.generation

    fun publish(
        patch: MagnifierPatch?,
        config: PerspectiveCropConfig,
        viewPoints: List<CropPoint> = emptyList(),
        touchViewX: Float = 0f,
        touchViewY: Float = 0f,
    ) {
        val nextGen = _state.generation + 1
        val pointsXy = if (viewPoints.size == 4) {
            floatArrayOf(
                viewPoints[0].x, viewPoints[0].y,
                viewPoints[1].x, viewPoints[1].y,
                viewPoints[2].x, viewPoints[2].y,
                viewPoints[3].x, viewPoints[3].y,
            )
        } else {
            FloatArray(0)
        }
        val mirror = config.showMagnifierCropLineMirror
        val cropColor = config.borderColor.toInt()
        val cropW = config.borderWidthPx.coerceAtLeast(1f)

        if (patch == null || patch.width <= 0 || patch.height <= 0) {
            _state = MagnifierBlitSnapshot(
                generation = nextGen,
                width = 0,
                height = 0,
                argbPixels = IntArray(0),
                borderWidth = 2f,
                borderColor = 0xFFFFFFFF.toInt(),
                showCross = false,
                crossColor = 0xFFFFFFFF.toInt(),
                crossLineWidth = 3f,
                crossLineLength = 15f,
                showCropLineMirror = mirror,
                cropLineColor = cropColor,
                cropLineWidth = cropW,
                touchViewX = touchViewX,
                touchViewY = touchViewY,
                viewPointsXy = pointsXy,
            )
            return
        }
        val w = patch.width
        val h = patch.height
        val pixels = patch.argbPixels
        if (pixels.size < w * h) {
            _state = MagnifierBlitSnapshot(
                generation = nextGen,
                width = 0,
                height = 0,
                argbPixels = IntArray(0),
                borderWidth = 2f,
                borderColor = 0xFFFFFFFF.toInt(),
                showCross = false,
                crossColor = 0xFFFFFFFF.toInt(),
                crossLineWidth = 3f,
                crossLineLength = 15f,
                showCropLineMirror = mirror,
                cropLineColor = cropColor,
                cropLineWidth = cropW,
                touchViewX = touchViewX,
                touchViewY = touchViewY,
                viewPointsXy = pointsXy,
            )
            return
        }
        // 新缓冲，禁止原地改旧数组（避免 snapshot 读到半更新像素）
        val argb = IntArray(w * h)
        pixels.copyInto(argb, 0, 0, w * h)
        _state = MagnifierBlitSnapshot(
            generation = nextGen,
            width = w,
            height = h,
            argbPixels = argb,
            borderWidth = config.magnifierBorderWidth.coerceAtLeast(1f),
            borderColor = config.magnifierBorderColor.toInt(),
            showCross = config.showMagnifierCenterCross,
            crossColor = config.magnifierCrossColor.toInt(),
            crossLineWidth = config.magnifierCrossLineWidth.coerceAtLeast(1f),
            crossLineLength = config.magnifierCrossLineLength.coerceAtLeast(1f),
            showCropLineMirror = mirror,
            cropLineColor = cropColor,
            cropLineWidth = cropW,
            touchViewX = touchViewX,
            touchViewY = touchViewY,
            viewPointsXy = pointsXy,
        )
    }

    fun clear() {
        _state = MagnifierBlitSnapshot.EMPTY.copy(generation = _state.generation + 1)
    }

    /**
     * 只读快照：先取稳定引用，再拷贝可变数组，供 NAPI/@CName 跨语言读取。
     */
    fun snapshot(): MagnifierBlitSnapshot {
        val s = _state
        return s.copy(
            argbPixels = s.argbPixels.copyOf(),
            viewPointsXy = s.viewPointsXy.copyOf(),
        )
    }
}

/**
 * MagnifierBlitStore 跨语言快照；[argbPixels] 为行优先 ARGB8888（发布后视为只读）。
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

    companion object {
        val EMPTY = MagnifierBlitSnapshot(
            generation = 0,
            width = 0,
            height = 0,
            argbPixels = IntArray(0),
            borderWidth = 2f,
            borderColor = 0xFFFFFFFF.toInt(),
            showCross = false,
            crossColor = 0xFFFFFFFF.toInt(),
            crossLineWidth = 3f,
            crossLineLength = 15f,
            showCropLineMirror = false,
            cropLineColor = 0xFFFF3B30.toInt(),
            cropLineWidth = 2f,
            touchViewX = 0f,
            touchViewY = 0f,
            viewPointsXy = FloatArray(0),
        )
    }
}
