package com.jlj.kuiklybase.perspectivecrop

import android.graphics.Bitmap

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Android 放大镜像素仓；原生 View 读 Bitmap，避免 props 传 IntArray。
 */
object MagnifierBlitStore {

    @Volatile
    var generation: Int = 0
        private set

    @Volatile
    private var _bitmap: Bitmap? = null

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

    /** 选区四点视图坐标，长度 8：x0,y0,…x3,y3；不足 8 表示无线。 */
    @Volatile
    private var _viewPointsXy: FloatArray = FloatArray(0)

    /** 返回拷贝，避免调用方改写污染仓内数组。 */
    val viewPointsXy: FloatArray
        get() = _viewPointsXy.copyOf()

    fun bitmapOrNull(): Bitmap? {
        val b = _bitmap
        return if (b != null && !b.isRecycled) b else null
    }

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
            clearBitmapOnly()
            generation += 1
            return
        }
        val w = patch.width
        val h = patch.height
        val pixels = patch.argbPixels
        if (pixels.size < w * h) {
            clearBitmapOnly()
            generation += 1
            return
        }
        val existing = _bitmap
        val bmp = if (existing != null && !existing.isRecycled &&
            existing.width == w && existing.height == h
        ) {
            existing
        } else {
            existing?.takeIf { !it.isRecycled }?.recycle()
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { _bitmap = it }
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        borderWidth = config.magnifierBorderWidth.coerceAtLeast(1f)
        borderColor = config.magnifierBorderColor.toInt()
        showCross = config.showMagnifierCenterCross
        crossColor = config.magnifierCrossColor.toInt()
        crossLineWidth = config.magnifierCrossLineWidth.coerceAtLeast(1f)
        crossLineLength = config.magnifierCrossLineLength.coerceAtLeast(1f)
        generation += 1
    }

    fun clear() {
        clearBitmapOnly()
        showCropLineMirror = false
        _viewPointsXy = FloatArray(0)
        generation += 1
    }

    private fun clearBitmapOnly() {
        _bitmap?.takeIf { !it.isRecycled }?.recycle()
        _bitmap = null
    }
}
