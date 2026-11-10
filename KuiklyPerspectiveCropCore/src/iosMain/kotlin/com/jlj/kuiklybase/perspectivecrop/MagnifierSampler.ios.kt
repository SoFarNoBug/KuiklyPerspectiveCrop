package com.jlj.kuiklybase.perspectivecrop

import kotlin.math.roundToInt

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: iOS 真像素放大镜采样（网格兜底）；解码在 [warmCache]，手势路径只读缓存。
 */
actual class MagnifierSampler actual constructor() {

    private var _cacheKey: String? = null
    private var _cache: IosCropBuffer? = null
    /** 与 Android/Ohos 对齐：缓存坐标按显示宽映射。 */
    private var _cacheScale: Float = 1f
    private val _maxPatchEdge = 96

    actual suspend fun warmCache(
        imageSrc: String,
        uiRotationSteps: Int,
        imageWidth: Float,
        config: PerspectiveCropConfig,
    ) {
        if (imageSrc.isEmpty() || imageWidth <= 0f) return
        val maxEdge = config.magnifierCacheMaxEdgePx.coerceAtLeast(64)
        val key = "$imageSrc|$uiRotationSteps|$maxEdge"
        if (_cacheKey == key && _cache != null) return
        try {
            val buf = decodeIosCropBuffer(imageSrc, uiRotationSteps, maxEdge)
            _cache = buf
            _cacheScale = buf.width / imageWidth.coerceAtLeast(1f)
            _cacheKey = key
        } catch (_: Exception) {
            _cache = null
            _cacheKey = null
            _cacheScale = 1f
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

        val scale = _cacheScale
        val radius = (viewWidth * config.magnifierRadiusRatio).coerceAtLeast(24f)
        val s = viewScale.coerceAtLeast(1e-3f)
        val halfImage = radius / s
        val halfCache = (halfImage * scale).roundToInt().coerceIn(8, _maxPatchEdge / 2)

        val cx = (imageX * scale).roundToInt().coerceIn(0, buf.width - 1)
        val cy = (imageY * scale).roundToInt().coerceIn(0, buf.height - 1)
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
        // 证据（2026-11-10）：红顶蓝底标记 → 壳 UIImage.draw 保序；镜内景物仍倒 →
        // IosCropBuffer 相对 Stage 预览为 Y 颠倒。拷贝时按行翻转，使触点区域与正立方向对齐。
        // 不改 decode/出图路径（裁切已验无明显颠倒）。
        for (y in 0 until h) {
            val srcRow = ((buf.height - 1) - (top + y)) * sw + left
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
        _cacheScale = 1f
    }
}
