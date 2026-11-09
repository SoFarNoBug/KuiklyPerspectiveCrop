package com.jlj.kuiklybase.perspectivecrop

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetMinX
import platform.CoreGraphics.CGRectGetMinY
import platform.CoreGraphics.CGRectGetWidth
import platform.CoreGraphics.CGRectMake
import platform.CoreImage.CIContext
import platform.CoreImage.CIImage
import platform.CoreImage.CIVector
import platform.CoreImage.createCGImage
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: iOS Native 透视：Core Image 四点校正，dest 尺寸对齐 Android/Ohos hypot。
 */
@OptIn(ExperimentalForeignApi::class)
internal fun perspectiveCropCoreImage(
    buf: IosCropBuffer,
    pointsInSrcSpace: List<CropPoint>,
): Triple<IntArray, Int, Int> {
    if (pointsInSrcSpace.size != 4) {
        throw CropException(
            CropException.ILLEGAL_POINTS,
            "need 4 points, got ${pointsInSrcSpace.size}",
        )
    }
    val outW = max(
        1,
        hypot(
            (pointsInSrcSpace[1].x - pointsInSrcSpace[0].x).toDouble(),
            (pointsInSrcSpace[1].y - pointsInSrcSpace[0].y).toDouble(),
        ).roundToInt(),
    )
    val outH = max(
        1,
        hypot(
            (pointsInSrcSpace[3].x - pointsInSrcSpace[0].x).toDouble(),
            (pointsInSrcSpace[3].y - pointsInSrcSpace[0].y).toDouble(),
        ).roundToInt(),
    )
    if (max(outW, outH) > CROP_MAX_EDGE) {
        throw CropException(
            CropException.TRANSFORM_FAILED,
            "output edge ${max(outW, outH)} exceeds hard cap $CROP_MAX_EDGE",
        )
    }

    val srcCg = argbToCgImage(buf.argb, buf.width, buf.height)
    try {
        val ciSrc = CIImage.imageWithCGImage(srcCg)
            ?: throw CropException(CropException.TRANSFORM_FAILED, "CIImage.imageWithCGImage failed")
        // ARGB/UIKit 顶左 → CI 底左
        val imgH = buf.height.toDouble()
        fun ciVec(p: CropPoint): CIVector =
            CIVector.vectorWithX(p.x.toDouble(), Y = imgH - p.y.toDouble())

        val params: Map<Any?, Any?> = mapOf(
            "inputTopLeft" to ciVec(pointsInSrcSpace[0]),
            "inputTopRight" to ciVec(pointsInSrcSpace[1]),
            "inputBottomRight" to ciVec(pointsInSrcSpace[2]),
            "inputBottomLeft" to ciVec(pointsInSrcSpace[3]),
        )

        val output = ciSrc.imageByApplyingFilter(
            "CIPerspectiveCorrection",
            withInputParameters = params,
        ) ?: throw CropException(
            CropException.TRANSFORM_FAILED,
            "imageByApplyingFilter(CIPerspectiveCorrection) returned null",
        )

        val context = CIContext.contextWithOptions(null)
        val extent = output.extent
        val fromRect = CGRectMake(
            CGRectGetMinX(extent),
            CGRectGetMinY(extent),
            CGRectGetWidth(extent),
            CGRectGetHeight(extent),
        )
        val rendered = context.createCGImage(output, fromRect = fromRect)
            ?: throw CropException(CropException.TRANSFORM_FAILED, "CIContext.createCGImage failed")
        var owned: CGImageRef = rendered
        try {
            val rw = CGImageGetWidth(rendered).toInt().coerceAtLeast(1)
            val rh = CGImageGetHeight(rendered).toInt().coerceAtLeast(1)
            if (rw != outW || rh != outH) {
                owned = scaleCgImageExact(rendered, outW, outH)
                CGImageRelease(rendered)
            }
            return Triple(cgImageToArgb(owned, outW, outH), outW, outH)
        } finally {
            CGImageRelease(owned)
        }
    } finally {
        CGImageRelease(srcCg)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun scaleCgImageExact(src: CGImageRef, outW: Int, outH: Int): CGImageRef {
    val colorSpace = CGColorSpaceCreateDeviceRGB()
        ?: throw CropException(CropException.TRANSFORM_FAILED, "CGColorSpaceCreateDeviceRGB failed")
    val ctx = CGBitmapContextCreate(
        null,
        outW.toULong(),
        outH.toULong(),
        8u,
        (outW * 4).toULong(),
        colorSpace,
        iosRgbaBitmapInfo,
    )
    CGColorSpaceRelease(colorSpace)
    if (ctx == null) {
        throw CropException(CropException.TRANSFORM_FAILED, "scale CGBitmapContextCreate failed")
    }
    try {
        CGContextDrawImage(ctx, CGRectMake(0.0, 0.0, outW.toDouble(), outH.toDouble()), src)
        return CGBitmapContextCreateImage(ctx)
            ?: throw CropException(CropException.TRANSFORM_FAILED, "scale CGBitmapContextCreateImage null")
    } finally {
        CGContextRelease(ctx)
    }
}
