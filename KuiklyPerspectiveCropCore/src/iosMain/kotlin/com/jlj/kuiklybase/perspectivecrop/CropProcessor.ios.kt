package com.jlj.kuiklybase.perspectivecrop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: iOS 出图：Auto/Native → Core Image 透视；Common → CPU 单应。
 */
actual class CropProcessor actual constructor() {

    actual suspend fun process(
        imageSrc: String,
        points: List<CropPoint>,
        rotationSteps: Int,
        config: PerspectiveCropConfig,
    ): PlatformImage = withContext(Dispatchers.Default) {
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

        val buf = try {
            decodeIosCropBuffer(imageSrc, rotationSteps, CROP_MAX_EDGE)
        } catch (e: CropException) {
            throw e
        } catch (e: Exception) {
            throw CropException(CropException.DECODE_FAILED, "cannot decode imageSrc=$imageSrc", e)
        }

        val pointScale = buf.scale.coerceAtLeast(1e-6f)
        val scaledPoints = points.map { CropPoint(it.x * pointScale, it.y * pointScale) }

        val warped = try {
            if (useNative) {
                perspectiveCropCoreImage(buf, scaledPoints)
            } else {
                perspectiveWarpArgb(buf.argb, buf.width, buf.height, scaledPoints)
            }
        } catch (e: CropException) {
            throw e
        } catch (e: Exception) {
            throw CropException(CropException.TRANSFORM_FAILED, "perspective transform failed", e)
        }

        PlatformImage(
            warped.first,
            warped.second,
            warped.third,
            config.outputFormat,
            config.jpegQuality,
        )
    }
}
