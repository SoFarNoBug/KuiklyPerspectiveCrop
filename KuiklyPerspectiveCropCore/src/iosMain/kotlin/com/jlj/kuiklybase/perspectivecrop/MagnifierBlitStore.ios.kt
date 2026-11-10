package com.jlj.kuiklybase.perspectivecrop

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGImageRelease
import platform.Foundation.NSData
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import kotlin.concurrent.Volatile

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: iOS 放大镜像素仓；原生 View 读 RGBA NSData / PNG / UIImage，避免 props 传 IntArray。
 */
object MagnifierBlitStore {

    @Volatile
    var generation: Int = 0
        private set

    @Volatile
    private var _image: UIImage? = null

    /** PNG 字节；壳侧用 `UIImage(data:)` 解码。 */
    @Volatile
    private var _pngData: NSData? = null

    /** 顶左行优先 RGBA；壳侧直接 CGImageCreate，绕开 KN UIImage 桥。 */
    @Volatile
    private var _rgbaData: NSData? = null

    @Volatile
    private var _blitWidth: Int = 0

    @Volatile
    private var _blitHeight: Int = 0

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

    fun pngDataOrNull(): NSData? = _pngData

    fun rgbaDataOrNull(): NSData? = _rgbaData

    fun blitWidth(): Int = _blitWidth

    fun blitHeight(): Int = _blitHeight

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

        // 先落 RGBA NSData（壳主路径）；CG/PNG 失败也不丢像素。
        val rgbaBytes = argbToRgbaBytes(pixels, w, h)
        val rgbaData = try {
            rgbaBytes.toNSDataCopy()
        } catch (_: Exception) {
            clearImageOnly()
            generation += 1
            return
        }
        _rgbaData = rgbaData
        _blitWidth = w
        _blitHeight = h

        val cg = try {
            argbToCgImage(pixels, w, h)
        } catch (_: Exception) {
            null
        }
        if (cg != null) {
            try {
                val raw = UIImage.imageWithCGImage(cg)
                val png = raw?.let { UIImagePNGRepresentation(it) }
                _pngData = if (png != null && png.length.toInt() > 0) png else null
                _image = _pngData?.let { UIImage.imageWithData(it) } ?: raw
            } finally {
                CGImageRelease(cg)
            }
        } else {
            _pngData = null
            _image = null
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
        _pngData = null
        _rgbaData = null
        _blitWidth = 0
        _blitHeight = 0
    }
}
