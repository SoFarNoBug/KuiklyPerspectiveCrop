package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Ohos 出图：Common CPU 单应；Native 本期明确失败。
 */
actual class CropProcessor actual constructor() {

    actual suspend fun process(
        imageSrc: String,
        points: List<CropPoint>,
        rotationSteps: Int,
        config: PerspectiveCropConfig,
    ): PlatformImage {
        if (points.size != 4) {
            throw CropException(CropException.ILLEGAL_POINTS, "need 4 points, got ${points.size}")
        }
        if (imageSrc.isEmpty()) {
            throw CropException(CropException.EMPTY_IMAGE_SRC, "imageSrc is empty")
        }
        when (config.cropEngine) {
            CropEngine.Native -> throw CropException(
                CropException.UNSUPPORTED_PLATFORM,
                "鸿蒙尚未提供原生出图引擎（Native）；请改用 Auto 或 Common",
            )
            CropEngine.Auto, CropEngine.Common -> Unit
        }

        val decoded = try {
            decodeOhosPixelBufferForCrop(imageSrc, rotationSteps, CROP_MAX_EDGE)
        } catch (e: CropException) {
            throw e
        } catch (e: Exception) {
            throw CropException(CropException.DECODE_FAILED, "cannot decode imageSrc=$imageSrc", e)
        }

        val pointScale = decoded.scale.coerceAtLeast(1e-6f)
        val scaledPoints = points.map { CropPoint(it.x * pointScale, it.y * pointScale) }

        return try {
            val warped = perspectiveWarpArgb(
                decoded.argb,
                decoded.width,
                decoded.height,
                scaledPoints,
            )
            PlatformImage(
                warped.first,
                warped.second,
                warped.third,
                config.outputFormat,
                config.jpegQuality,
            )
        } catch (e: CropException) {
            throw e
        } catch (e: Exception) {
            throw CropException(CropException.TRANSFORM_FAILED, "perspective transform failed", e)
        }
    }
}
