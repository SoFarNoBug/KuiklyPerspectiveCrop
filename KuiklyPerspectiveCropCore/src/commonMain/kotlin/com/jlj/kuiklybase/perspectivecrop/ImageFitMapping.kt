package com.jlj.kuiklybase.perspectivecrop

/**
 * ContentScale.Fit 下：容器 → 内容绘制矩形 → 图像素 双向映射。
 * [insetLeft]/[insetTop]/[insetRight]/[insetBottom] 扣出可用区后再 Fit 居中（对齐 freequadcrop padding）。
 */
data class ImageFitMapping(
    val containerW: Float,
    val containerH: Float,
    val imageW: Float,
    val imageH: Float,
    val insetLeft: Float = 0f,
    val insetTop: Float = 0f,
    val insetRight: Float = 0f,
    val insetBottom: Float = 0f,
) {
    /** 钳制后实际生效的内缩（视图 px）。 */
    val effectiveInsetLeft: Float
    val effectiveInsetTop: Float
    val effectiveInsetRight: Float
    val effectiveInsetBottom: Float

    /** 扣 inset 后的可用宽高（至少 1）。 */
    val availW: Float
    val availH: Float

    val contentLeft: Float
    val contentTop: Float
    val contentW: Float
    val contentH: Float
    val scale: Float

    init {
        require(containerW > 0f && containerH > 0f && imageW > 0f && imageH > 0f)
        var l = insetLeft.coerceAtLeast(0f)
        var t = insetTop.coerceAtLeast(0f)
        var r = insetRight.coerceAtLeast(0f)
        var b = insetBottom.coerceAtLeast(0f)
        val maxHoriz = (containerW - 1f).coerceAtLeast(0f)
        val sumH = l + r
        if (sumH > maxHoriz && sumH > 0f) {
            val s = maxHoriz / sumH
            l *= s
            r *= s
        }
        val maxVert = (containerH - 1f).coerceAtLeast(0f)
        val sumV = t + b
        if (sumV > maxVert && sumV > 0f) {
            val s = maxVert / sumV
            t *= s
            b *= s
        }
        effectiveInsetLeft = l
        effectiveInsetTop = t
        effectiveInsetRight = r
        effectiveInsetBottom = b
        availW = (containerW - l - r).coerceAtLeast(1f)
        availH = (containerH - t - b).coerceAtLeast(1f)
        val sx = availW / imageW
        val sy = availH / imageH
        scale = minOf(sx, sy)
        contentW = imageW * scale
        contentH = imageH * scale
        contentLeft = l + (availW - contentW) / 2f
        contentTop = t + (availH - contentH) / 2f
    }

    fun imageToView(p: CropPoint): CropPoint =
        CropPoint(contentLeft + p.x * scale, contentTop + p.y * scale)

    fun viewToImage(vx: Float, vy: Float): CropPoint =
        CropPoint((vx - contentLeft) / scale, (vy - contentTop) / scale)

    fun viewToImageClamped(vx: Float, vy: Float): CropPoint =
        viewToImage(vx, vy).coerceIn(0f, 0f, imageW, imageH)
}
