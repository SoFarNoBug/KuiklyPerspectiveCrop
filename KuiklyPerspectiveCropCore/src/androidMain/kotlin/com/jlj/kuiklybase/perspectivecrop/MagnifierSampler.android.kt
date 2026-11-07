package com.jlj.kuiklybase.perspectivecrop

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Android 真像素放大镜采样；解码仅在 [warmCache]，手势路径只读缓存。
 */
actual class MagnifierSampler actual constructor() {

    private val _lock = Any()
    private var _cacheKey: String? = null
    private var _cacheBitmap: Bitmap? = null
    private var _cacheScale: Float = 1f

    /** 单边采样上限（缓存像素），避免过大 getPixels。 */
    private val _maxPatchEdge = 96

    actual fun warmCache(
        imageSrc: String,
        uiRotationSteps: Int,
        imageWidth: Float,
        config: PerspectiveCropConfig,
    ) {
        if (imageSrc.isEmpty() || imageWidth <= 0f) return
        val maxEdge = config.magnifierCacheMaxEdgePx.coerceAtLeast(64)
        val key = "$imageSrc|$uiRotationSteps|$maxEdge"
        synchronized(_lock) {
            ensureCacheLocked(imageSrc, uiRotationSteps, maxEdge, key, imageWidth)
        }
    }

    actual fun sample(
        imageSrc: String,
        uiRotationSteps: Int,
        imageX: Float,
        imageY: Float,
        imageWidth: Float,
        imageHeight: Float,
        touchViewX: Float,
        touchViewY: Float,
        viewWidth: Float,
        viewScale: Float,
        config: PerspectiveCropConfig,
    ): MagnifierPatch? {
        if (imageSrc.isEmpty() || imageWidth <= 0f || imageHeight <= 0f) return null
        val maxEdge = config.magnifierCacheMaxEdgePx.coerceAtLeast(64)
        val key = "$imageSrc|$uiRotationSteps|$maxEdge"

        val bmp: Bitmap
        val scale: Float
        synchronized(_lock) {
            val cached = _cacheBitmap
            if (_cacheKey != key || cached == null || cached.isRecycled) {
                return null
            }
            bmp = cached
            scale = _cacheScale
        }

        val radius = (viewWidth * config.magnifierRadiusRatio).coerceAtLeast(24f)
        // 镜面直径对应的图像素 → 缓存像素，使圆内内容与屏幕 ~1:1（旧库 BitmapShader 口径）
        val s = viewScale.coerceAtLeast(1e-3f)
        val halfImage = radius / s
        val halfCache = (halfImage * scale).roundToInt().coerceIn(8, _maxPatchEdge / 2)

        val cx = (imageX * scale).roundToInt().coerceIn(0, bmp.width - 1)
        val cy = (imageY * scale).roundToInt().coerceIn(0, bmp.height - 1)
        val left = (cx - halfCache).coerceAtLeast(0)
        val top = (cy - halfCache).coerceAtLeast(0)
        val right = (cx + halfCache).coerceAtMost(bmp.width)
        val bottom = (cy + halfCache).coerceAtMost(bmp.height)
        val w = right - left
        val h = bottom - top
        if (w <= 0 || h <= 0) return null

        val pixels = IntArray(w * h)
        synchronized(_lock) {
            val still = _cacheBitmap
            if (still !== bmp || still.isRecycled) return null
            still.getPixels(pixels, 0, w, left, top, w, h)
        }

        return MagnifierPatch(
            argbPixels = pixels,
            width = w,
            height = h,
            touchViewX = touchViewX,
            touchViewY = touchViewY,
            displayRadiusPx = radius,
        )
    }

    actual fun clear() {
        synchronized(_lock) {
            _cacheBitmap?.takeIf { !it.isRecycled }?.recycle()
            _cacheBitmap = null
            _cacheKey = null
            _cacheScale = 1f
        }
    }

    private fun ensureCacheLocked(
        imageSrc: String,
        uiRotationSteps: Int,
        maxEdge: Int,
        key: String,
        imageWidth: Float,
    ): Bitmap? {
        if (_cacheKey == key && _cacheBitmap != null && _cacheBitmap?.isRecycled == false) {
            return _cacheBitmap
        }
        _cacheBitmap?.takeIf { !it.isRecycled }?.recycle()
        _cacheBitmap = null
        _cacheKey = null
        _cacheScale = 1f

        val context = try {
            PerspectiveCropAndroid.requireContext()
        } catch (_: CropException) {
            return null
        }
        val decoded = decodeBitmap(context, imageSrc) ?: return null
        val steps = ((uiRotationSteps % 4) + 4) % 4
        val rotated = if (steps == 0) {
            decoded
        } else {
            rotateBitmap(decoded, steps * 90f).also {
                if (it !== decoded && !decoded.isRecycled) decoded.recycle()
            }
        }
        val scaled = scaleToMaxEdge(rotated, maxEdge).also {
            if (it !== rotated && !rotated.isRecycled) rotated.recycle()
        }
        val copy = scaled.copy(Bitmap.Config.RGB_565, false)
        if (!scaled.isRecycled) scaled.recycle()
        _cacheBitmap = copy
        _cacheScale = copy.width / imageWidth.coerceAtLeast(1f)
        _cacheKey = key
        return copy
    }

    private fun scaleToMaxEdge(origin: Bitmap, maxEdge: Int): Bitmap {
        val longest = max(origin.width, origin.height)
        if (longest <= maxEdge) return origin
        val scale = maxEdge.toFloat() / longest
        val w = max(1, (origin.width * scale).roundToInt())
        val h = max(1, (origin.height * scale).roundToInt())
        return Bitmap.createScaledBitmap(origin, w, h, true)
    }

    private fun rotateBitmap(origin: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return origin
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(origin, 0, 0, origin.width, origin.height, matrix, true)
    }

    private fun decodeBitmap(context: android.content.Context, imageSrc: String): Bitmap? {
        return try {
            when {
                imageSrc.startsWith("assets://") -> {
                    val assetPath = imageSrc.removePrefix("assets://")
                    context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
                }
                imageSrc.startsWith("content://") || imageSrc.startsWith("file://") -> {
                    val uri = Uri.parse(imageSrc)
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                }
                else -> {
                    val file = File(imageSrc)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else {
                        context.assets.open(imageSrc).use { BitmapFactory.decodeStream(it) }
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
