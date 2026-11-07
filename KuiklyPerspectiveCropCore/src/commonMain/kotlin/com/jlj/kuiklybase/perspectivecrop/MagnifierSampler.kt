package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 放大镜降采样采样器（Q14/Q23）。Android 实装；其它端 stub。
 * 缓存键含 imageSrc + uiRotationSteps + magnifierCacheMaxEdgePx（Q24/Q31）。
 * 全图解码只允许在 [warmCache]（须后台调用）；[sample] 仅读缓存裁 patch。
 */
expect class MagnifierSampler() {
    /**
     * 后台预热降采样缓存。须在 IO/后台线程调用（禁止手势/主路径同步解码）。
     */
    fun warmCache(
        imageSrc: String,
        uiRotationSteps: Int,
        imageWidth: Float,
        config: PerspectiveCropConfig,
    )

    /**
     * 从已预热缓存裁 patch。缓存未就绪或键不匹配时返回 null（不解码）。
     */
    /**
     * @param viewScale [ImageFitMapping.scale]（view = image * scale）；用于按镜面半径取 ~1:1 视图像素块（对齐旧库 loupe，非额外数码变焦）。
     */
    fun sample(
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
    ): MagnifierPatch?

    /** 丢弃缓存（换图 / 销毁时）。 */
    fun clear()
}
