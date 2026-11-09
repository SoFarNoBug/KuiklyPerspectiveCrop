package com.jlj.kuiklybase.perspectivecrop

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextConcatCTM
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGContextRotateCTM
import platform.CoreGraphics.CGContextTranslateCTM
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.kCGBitmapByteOrder32Big
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePNGRepresentation
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.concurrent.AtomicLong

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: iOS 出图解码：assets/file 解析、旋源、CROP_MAX_EDGE 缩放、ARGB 抽取。
 */

/** 出图缓冲；[scale] = bufWidth / rotatedNaturalWidth，用于映射显示坐标四点。 */
internal class IosCropBuffer(
    val argb: IntArray,
    val width: Int,
    val height: Int,
    val scale: Float,
)

/**
 * iOS CGBitmapContext 仅可靠支持预乘 alpha（`kCGImageAlphaLast` 会 Create 失败 → 黑镜/裁剪炸）。
 * 样例与出图多为不透明（A=255），预乘与直通 ARGB 等价；半透明边缘与 Android 可能略差。
 */
@OptIn(ExperimentalForeignApi::class)
internal val iosRgbaBitmapInfo: UInt =
    CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value or kCGBitmapByteOrder32Big.toUInt()

private val _cropFileSeq = AtomicLong(0L)

/** 缓存文件名用：毫秒时间戳 + 序号，避免同秒覆盖。 */
internal fun nextCropFileToken(): String {
    val ms = (NSDate().timeIntervalSince1970 * 1000.0).toLong()
    val seq = _cropFileSeq.addAndGet(1L)
    return "${ms}_$seq"
}

@OptIn(ExperimentalForeignApi::class)
internal fun resolveIosImagePath(imageSrc: String): String? {
    if (imageSrc.isEmpty()) return null
    return when {
        imageSrc.startsWith("assets://") -> {
            val rel = imageSrc.removePrefix("assets://").trimStart('/')
            findBundleResourcePath(rel)
        }
        imageSrc.startsWith("file://") -> imageSrc.removePrefix("file://")
        else -> imageSrc
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun findBundleResourcePath(relPath: String): String? {
    val fileName = relPath.substringAfterLast('/')
    val base = fileName.substringBeforeLast('.', missingDelimiterValue = fileName)
    val ext = fileName.substringAfterLast('.', missingDelimiterValue = "").ifEmpty { null }

    fun lookup(bundle: NSBundle): String? {
        if (relPath.contains('/')) {
            val dir = relPath.substringBeforeLast('/')
            bundle.pathForResource(base, ofType = ext, inDirectory = dir)?.let { return it }
        }
        return bundle.pathForResource(base, ofType = ext)
    }

    lookup(NSBundle.mainBundle)?.let { return it }

    @Suppress("UNCHECKED_CAST")
    val bundles = NSBundle.allBundles as List<NSBundle>
    for (bundle in bundles) {
        if (bundle == NSBundle.mainBundle) continue
        lookup(bundle)?.let { return it }
    }
    @Suppress("UNCHECKED_CAST")
    val frameworks = NSBundle.allFrameworks as List<NSBundle>
    for (bundle in frameworks) {
        lookup(bundle)?.let { return it }
    }
    return null
}

@OptIn(ExperimentalForeignApi::class)
internal fun decodeIosCropBuffer(
    imageSrc: String,
    uiRotationSteps: Int,
    maxEdge: Int,
): IosCropBuffer {
    if (imageSrc.isEmpty()) {
        throw CropException(CropException.EMPTY_IMAGE_SRC, "imageSrc is empty")
    }
    val path = resolveIosImagePath(imageSrc)
        ?: throw CropException(
            CropException.DECODE_FAILED,
            "cannot resolve imageSrc=$imageSrc (assets:// needs resource in main/framework bundle)",
        )
    val image = loadUiImage(path)
        ?: throw CropException(CropException.DECODE_FAILED, "UIImage failed for $path")
    val steps = ((uiRotationSteps % 4) + 4) % 4
    val rotated = rotateUiImage(image, steps)
    val cgImage = rotated.CGImage
        ?: throw CropException(CropException.DECODE_FAILED, "rotated image has no CGImage")
    val natW = CGImageGetWidth(cgImage).toInt().coerceAtLeast(1)
    val natH = CGImageGetHeight(cgImage).toInt().coerceAtLeast(1)
    val edge = maxEdge.coerceAtLeast(64)
    val longest = max(natW, natH)
    val scaleDown = if (longest > edge) edge.toFloat() / longest else 1f
    val outW = max(1, (natW * scaleDown).roundToInt())
    val outH = max(1, (natH * scaleDown).roundToInt())
    val scaledCg = if (outW == natW && outH == natH) {
        cgImage
    } else {
        scaleCgImage(cgImage, outW, outH)
            ?: throw CropException(CropException.DECODE_FAILED, "scaleCgImage failed")
    }
    try {
        val argb = cgImageToArgb(scaledCg, outW, outH)
        val scale = outW.toFloat() / natW.toFloat()
        return IosCropBuffer(argb, outW, outH, scale)
    } finally {
        if (scaledCg != cgImage) {
            CGImageRelease(scaledCg)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun loadUiImage(path: String): UIImage? {
    UIImage.imageWithContentsOfFile(path)?.let { return it }
    val data = NSData.dataWithContentsOfFile(path) ?: return null
    return UIImage.imageWithData(data)
}

/**
 * 旋源：纯 CGBitmapContext（底左原点），避免 UIGraphics 已翻转 CTM + 再手工旋转的双重翻转。
 */
@OptIn(ExperimentalForeignApi::class)
private fun rotateUiImage(image: UIImage, steps: Int): UIImage {
    if (steps == 0) return image
    val src = image.CGImage
        ?: throw CropException(CropException.DECODE_FAILED, "rotate: UIImage has no CGImage")
    val srcW = CGImageGetWidth(src).toInt().coerceAtLeast(1)
    val srcH = CGImageGetHeight(src).toInt().coerceAtLeast(1)
    val swap = steps % 2 != 0
    val outW = if (swap) srcH else srcW
    val outH = if (swap) srcW else srcH
    val colorSpace = CGColorSpaceCreateDeviceRGB()
        ?: throw CropException(CropException.DECODE_FAILED, "rotate: colorSpace null")
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
        throw CropException(CropException.DECODE_FAILED, "rotate: CGBitmapContextCreate failed")
    }
    try {
        when (steps) {
            1 -> {
                CGContextTranslateCTM(ctx, outW.toDouble(), 0.0)
                CGContextRotateCTM(ctx, PI / 2.0)
            }
            2 -> {
                CGContextTranslateCTM(ctx, outW.toDouble(), outH.toDouble())
                CGContextRotateCTM(ctx, PI)
            }
            3 -> {
                CGContextTranslateCTM(ctx, 0.0, outH.toDouble())
                CGContextRotateCTM(ctx, -PI / 2.0)
            }
        }
        CGContextDrawImage(ctx, CGRectMake(0.0, 0.0, srcW.toDouble(), srcH.toDouble()), src)
        val outCg = CGBitmapContextCreateImage(ctx)
            ?: throw CropException(CropException.DECODE_FAILED, "rotate: CreateImage null")
        try {
            return UIImage.imageWithCGImage(outCg)
                ?: throw CropException(CropException.DECODE_FAILED, "rotate: UIImage null")
        } finally {
            CGImageRelease(outCg)
        }
    } finally {
        CGContextRelease(ctx)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun scaleCgImage(src: CGImageRef, outW: Int, outH: Int): CGImageRef? {
    val colorSpace = CGColorSpaceCreateDeviceRGB() ?: return null
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
    if (ctx == null) return null
    CGContextDrawImage(ctx, CGRectMake(0.0, 0.0, outW.toDouble(), outH.toDouble()), src)
    val out = CGBitmapContextCreateImage(ctx)
    CGContextRelease(ctx)
    return out
}

/**
 * CGImage → packed ARGB IntArray（A<<24|R<<16|G<<8|B），顶左原点、行优先。
 */
@OptIn(ExperimentalForeignApi::class)
internal fun cgImageToArgb(image: CGImageRef, width: Int, height: Int): IntArray {
    val colorSpace = CGColorSpaceCreateDeviceRGB()
        ?: throw CropException(CropException.DECODE_FAILED, "CGColorSpaceCreateDeviceRGB failed")
    val bytes = ByteArray(width * height * 4)
    bytes.usePinned { pinned ->
        val ctx = CGBitmapContextCreate(
            pinned.addressOf(0),
            width.toULong(),
            height.toULong(),
            8u,
            (width * 4).toULong(),
            colorSpace,
            iosRgbaBitmapInfo,
        )
        CGColorSpaceRelease(colorSpace)
        if (ctx == null) {
            throw CropException(CropException.DECODE_FAILED, "CGBitmapContextCreate failed")
        }
        try {
            CGContextTranslateCTM(ctx, 0.0, height.toDouble())
            CGContextConcatCTM(ctx, CGAffineTransformMakeScale(1.0, -1.0))
            CGContextDrawImage(ctx, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), image)
        } finally {
            CGContextRelease(ctx)
        }
    }
    val argb = IntArray(width * height)
    var p = 0
    for (i in argb.indices) {
        val r = bytes[p].toInt() and 0xFF
        val g = bytes[p + 1].toInt() and 0xFF
        val b = bytes[p + 2].toInt() and 0xFF
        val a = bytes[p + 3].toInt() and 0xFF
        argb[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        p += 4
    }
    return argb
}

@OptIn(ExperimentalForeignApi::class)
internal fun argbToCgImage(argb: IntArray, width: Int, height: Int): CGImageRef {
    // CGBitmap 第 0 行为底；ARGB 为顶左行优先 → 按行倒序写入
    val bytes = ByteArray(width * height * 4)
    val stride = width * 4
    for (y in 0 until height) {
        val srcOff = y * width
        val dstOff = (height - 1 - y) * stride
        var p = dstOff
        for (x in 0 until width) {
            val pixel = argb[srcOff + x]
            bytes[p] = ((pixel ushr 16) and 0xFF).toByte()
            bytes[p + 1] = ((pixel ushr 8) and 0xFF).toByte()
            bytes[p + 2] = (pixel and 0xFF).toByte()
            bytes[p + 3] = ((pixel ushr 24) and 0xFF).toByte()
            p += 4
        }
    }
    val colorSpace = CGColorSpaceCreateDeviceRGB()
        ?: throw CropException(CropException.TRANSFORM_FAILED, "CGColorSpaceCreateDeviceRGB failed")
    return bytes.usePinned { pinned ->
        val ctx = CGBitmapContextCreate(
            pinned.addressOf(0),
            width.toULong(),
            height.toULong(),
            8u,
            (width * 4).toULong(),
            colorSpace,
            iosRgbaBitmapInfo,
        )
        CGColorSpaceRelease(colorSpace)
        if (ctx == null) {
            throw CropException(CropException.TRANSFORM_FAILED, "CGBitmapContextCreate(argb) failed")
        }
        try {
            CGBitmapContextCreateImage(ctx)
                ?: throw CropException(CropException.TRANSFORM_FAILED, "CGBitmapContextCreateImage null")
        } finally {
            CGContextRelease(ctx)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun writeArgbToFile(
    argb: IntArray,
    width: Int,
    height: Int,
    absolutePath: String,
    format: CropOutputFormat,
    jpegQuality: Int,
): String {
    val cg = argbToCgImage(argb, width, height)
    try {
        val uiImage = UIImage.imageWithCGImage(cg)
            ?: throw CropException(CropException.WRITE_FAILED, "UIImage.imageWithCGImage failed")
        val data: NSData = when (format) {
            CropOutputFormat.Png -> UIImagePNGRepresentation(uiImage)
                ?: throw CropException(CropException.WRITE_FAILED, "PNGRepresentation null")
            CropOutputFormat.Jpeg -> {
                val q = jpegQuality.coerceIn(0, 100).toDouble() / 100.0
                UIImageJPEGRepresentation(uiImage, q)
                    ?: throw CropException(CropException.WRITE_FAILED, "JPEGRepresentation null")
            }
        }
        val ok = data.writeToFile(absolutePath, atomically = true)
        if (!ok) {
            throw CropException(CropException.WRITE_FAILED, "writeToFile failed: $absolutePath")
        }
        return absolutePath
    } finally {
        CGImageRelease(cg)
    }
}
