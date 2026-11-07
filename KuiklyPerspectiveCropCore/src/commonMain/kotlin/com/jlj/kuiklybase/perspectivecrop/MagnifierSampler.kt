package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 放大镜降采样采样器（Q14/Q23/Q34）。Android+JS 实装；iOS stub。
 * 缓存键含 imageSrc + uiRotationSteps + magnifierCacheMaxEdgePx（Q24/Q31）。
 * 全图解码只允许在 [warmCache]（suspend，须协程/后台调用）；[sample] 仅读缓存裁 patch。
 */
expect class MagnifierSampler() {
    /**
     * 预热降采样缓存（suspend：JS 可 await 解码；Android 内仍同步）。禁止手势路径同步解码。
     */
    suspend fun warmCache(
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
