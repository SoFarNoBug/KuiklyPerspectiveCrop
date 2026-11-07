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

    fun bitmapOrNull(): Bitmap? {
        val b = _bitmap
        return if (b != null && !b.isRecycled) b else null
    }

    fun publish(patch: MagnifierPatch?, config: PerspectiveCropConfig) {
        if (patch == null || patch.width <= 0 || patch.height <= 0) {
            clear()
            return
        }
        val w = patch.width
        val h = patch.height
        val pixels = patch.argbPixels
        if (pixels.size < w * h) {
            clear()
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
        _bitmap?.takeIf { !it.isRecycled }?.recycle()
        _bitmap = null
        generation += 1
    }
}
