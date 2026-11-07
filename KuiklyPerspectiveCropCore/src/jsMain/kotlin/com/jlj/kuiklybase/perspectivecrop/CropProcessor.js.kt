package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: JS CPU 单应透视出图（Q33/Q35）；旋源图后按显示坐标四点变换（Q17）。
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
        val decoded = try {
            decodeJsPixelBufferForCrop(imageSrc, rotationSteps, JS_CROP_MAX_EDGE)
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
