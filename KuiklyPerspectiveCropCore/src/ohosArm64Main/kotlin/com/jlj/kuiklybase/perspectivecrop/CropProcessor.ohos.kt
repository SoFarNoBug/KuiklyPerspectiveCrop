package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Ohos 出图：Auto/Native → Drawing SetPolyToPoly；Common → CPU 单应。
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

        val useNative = when (config.cropEngine) {
            CropEngine.Auto, CropEngine.Native -> true
            CropEngine.Common -> false
        }

        val cropPm = try {
            decodeOhosPixelmapForCrop(imageSrc, rotationSteps, CROP_MAX_EDGE)
        } catch (e: CropException) {
            throw e
        } catch (e: Exception) {
            throw CropException(CropException.DECODE_FAILED, "cannot decode imageSrc=$imageSrc", e)
        }

        try {
            val pointScale = cropPm.scale.coerceAtLeast(1e-6f)
            val scaledPoints = points.map { CropPoint(it.x * pointScale, it.y * pointScale) }

            val warped = try {
                if (useNative) {
                    perspectiveCropDrawing(cropPm, scaledPoints)
                } else {
                    val argb = readOhosPixelmapArgb(cropPm)
                    perspectiveWarpArgb(argb, cropPm.width, cropPm.height, scaledPoints)
                }
            } catch (e: CropException) {
                throw e
            } catch (e: Exception) {
                throw CropException(CropException.TRANSFORM_FAILED, "perspective transform failed", e)
            }

            return PlatformImage(
                warped.first,
                warped.second,
                warped.third,
                config.outputFormat,
                config.jpegQuality,
            )
        } finally {
            cropPm.close()
        }
    }
}
