package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: iOS stub：明确失败（Q1/Q16）。
 */
actual class CropProcessor actual constructor() {
    actual suspend fun process(
        imageSrc: String,
        points: List<CropPoint>,
        rotationSteps: Int,
        config: PerspectiveCropConfig,
    ): PlatformImage {
        throw CropException(
            CropException.UNSUPPORTED_PLATFORM,
            "CropProcessor is not implemented on iOS (stub)",
        )
    }
}
