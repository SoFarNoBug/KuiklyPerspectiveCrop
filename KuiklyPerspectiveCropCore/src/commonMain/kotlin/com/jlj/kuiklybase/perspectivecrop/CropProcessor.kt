package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 透视出图策略（expect/actual）；Android+JS 实装，iOS stub（Q1/Q17/Q33）。
 */
expect class CropProcessor() {
    /**
     * 解码 [imageSrc]，按 [rotationSteps] 旋源图后对 [points] 做透视裁切（suspend，Q34）。
     * @throws CropException 失败时带 code
     */
    suspend fun process(
        imageSrc: String,
        points: List<CropPoint>,
        rotationSteps: Int,
        config: PerspectiveCropConfig,
    ): PlatformImage
}
