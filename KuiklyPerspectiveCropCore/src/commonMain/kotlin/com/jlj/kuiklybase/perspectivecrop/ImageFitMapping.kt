package com.jlj.kuiklybase.perspectivecrop

/**
 * ContentScale.Fit 下：容器 → 内容绘制矩形 → 图像素 双向映射。
 */
data class ImageFitMapping(
    val containerW: Float,
    val containerH: Float,
    val imageW: Float,
    val imageH: Float,
) {
    val contentLeft: Float
    val contentTop: Float
    val contentW: Float
    val contentH: Float
    val scale: Float

    init {
        require(containerW > 0f && containerH > 0f && imageW > 0f && imageH > 0f)
        val sx = containerW / imageW
        val sy = containerH / imageH
        scale = minOf(sx, sy)
        contentW = imageW * scale
        contentH = imageH * scale
        contentLeft = (containerW - contentW) / 2f
        contentTop = (containerH - contentH) / 2f
    }

    fun imageToView(p: CropPoint): CropPoint =
        CropPoint(contentLeft + p.x * scale, contentTop + p.y * scale)

    fun viewToImage(vx: Float, vy: Float): CropPoint =
        CropPoint((vx - contentLeft) / scale, (vy - contentTop) / scale)

    fun viewToImageClamped(vx: Float, vy: Float): CropPoint =
        viewToImage(vx, vy).coerceIn(0f, 0f, imageW, imageH)
}
