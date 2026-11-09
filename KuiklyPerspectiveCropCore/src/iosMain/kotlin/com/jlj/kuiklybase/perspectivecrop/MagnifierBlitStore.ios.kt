package com.jlj.kuiklybase.perspectivecrop

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGImageRelease
import platform.UIKit.UIImage
import kotlin.concurrent.Volatile

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: iOS 放大镜像素仓；原生 View 读 UIImage，避免 props 传 IntArray。
 */
object MagnifierBlitStore {

    @Volatile
    var generation: Int = 0
        private set

    @Volatile
    private var _image: UIImage? = null

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

    fun uiImageOrNull(): UIImage? = _image

    @OptIn(ExperimentalForeignApi::class)
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
            clearImageOnly()
            generation += 1
            return
        }
        val w = patch.width
        val h = patch.height
        val pixels = patch.argbPixels
        if (pixels.size < w * h) {
            clearImageOnly()
            generation += 1
            return
        }
        val cg = try {
            argbToCgImage(pixels, w, h)
        } catch (_: Exception) {
            clearImageOnly()
            generation += 1
            return
        }
        try {
            _image = UIImage.imageWithCGImage(cg)
        } finally {
            CGImageRelease(cg)
        }
        borderWidth = config.magnifierBorderWidth.coerceAtLeast(1f)
        borderColor = config.magnifierBorderColor.toInt()
        showCross = config.showMagnifierCenterCross
        crossColor = config.magnifierCrossColor.toInt()
        crossLineWidth = config.magnifierCrossLineWidth.coerceAtLeast(1f)
        crossLineLength = config.magnifierCrossLineLength.coerceAtLeast(1f)
        generation += 1
    }

    fun clear() {
        clearImageOnly()
        showCropLineMirror = false
        _viewPointsXy = FloatArray(0)
        generation += 1
    }

    private fun clearImageOnly() {
        _image = null
    }
}
