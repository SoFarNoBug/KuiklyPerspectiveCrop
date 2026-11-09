package com.jlj.kuiklybase.perspectivecrop

import kotlin.math.roundToInt

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: Ohos 真像素放大镜采样（网格兜底）；解码在 [warmCache]，手势路径只读缓存。
 */
actual class MagnifierSampler actual constructor() {

    private var _cacheKey: String? = null
    private var _cache: OhosPixelBuffer? = null
    /** 与 Android/JS 对齐；原生贴图路径不再吃 N×N drawRect。 */
    private val _maxPatchEdge = 96

    actual suspend fun warmCache(
        imageSrc: String,
        uiRotationSteps: Int,
        imageWidth: Float,
        config: PerspectiveCropConfig,
    ) {
        if (imageSrc.isEmpty() || imageWidth <= 0f) return
        if (PerspectiveCropOhos.resfileDirOrNull() == null &&
            imageSrc.startsWith("assets://")
        ) {
            // 未 init：静默失败（对齐 Android 放大镜）
            return
        }
        val maxEdge = config.magnifierCacheMaxEdgePx.coerceAtLeast(64)
        val key = "$imageSrc|$uiRotationSteps|$maxEdge"
        if (_cacheKey == key && _cache != null) return
        try {
            val buf = decodeOhosPixelBuffer(imageSrc, uiRotationSteps, imageWidth, maxEdge)
            _cache = buf
            _cacheKey = if (buf != null) key else null
        } catch (_: Exception) {
            _cache = null
            _cacheKey = null
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
        val buf = _cache
        if (_cacheKey != key || buf == null) return null

        val radius = (viewWidth * config.magnifierRadiusRatio).coerceAtLeast(24f)
        val s = viewScale.coerceAtLeast(1e-3f)
        val halfImage = radius / s
        val halfCache = (halfImage * buf.scale).roundToInt().coerceIn(8, _maxPatchEdge / 2)

        val cx = (imageX * buf.scale).roundToInt().coerceIn(0, buf.width - 1)
        val cy = (imageY * buf.scale).roundToInt().coerceIn(0, buf.height - 1)
        val left = (cx - halfCache).coerceAtLeast(0)
        val top = (cy - halfCache).coerceAtLeast(0)
        val right = (cx + halfCache).coerceAtMost(buf.width)
        val bottom = (cy + halfCache).coerceAtMost(buf.height)
        val w = right - left
        val h = bottom - top
        if (w <= 0 || h <= 0) return null

        val need = w * h
        val out = IntArray(need)
        val src = buf.argb
        val sw = buf.width
        for (y in 0 until h) {
            val srcRow = (top + y) * sw + left
            val dstRow = y * w
            for (x in 0 until w) {
                out[dstRow + x] = src[srcRow + x]
            }
        }

        return MagnifierPatch(
            argbPixels = out,
            width = w,
            height = h,
            touchViewX = touchViewX,
            touchViewY = touchViewY,
            displayRadiusPx = radius,
            drawGrid = MagnifierDrawGrid.DRAG,
        )
    }

    actual fun clear() {
        _cache = null
        _cacheKey = null
    }
}
