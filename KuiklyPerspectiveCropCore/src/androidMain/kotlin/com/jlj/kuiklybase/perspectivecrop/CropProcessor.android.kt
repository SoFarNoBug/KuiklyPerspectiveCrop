package com.jlj.kuiklybase.perspectivecrop

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import java.io.File
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Android 透视出图：解码 → 旋源图 → setPolyToPoly（Q17/Q21）。
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
        val context = PerspectiveCropAndroid.requireContext()
        val decoded = decodeBitmap(context, imageSrc)
            ?: throw CropException(CropException.DECODE_FAILED, "cannot decode imageSrc=$imageSrc")

        val steps = ((rotationSteps % 4) + 4) % 4
        val source = if (steps == 0) {
            decoded
        } else {
            rotateBitmap(decoded, steps * 90f).also {
                if (it !== decoded && !decoded.isRecycled) {
                    decoded.recycle()
                }
            }
        }

        return try {
            val cropped = perspectiveCrop(source, points)
            PlatformImage(cropped, config.outputFormat, config.jpegQuality)
        } catch (e: CropException) {
            throw e
        } catch (e: Exception) {
            throw CropException(CropException.TRANSFORM_FAILED, "perspective transform failed", e)
        } finally {
            if (!source.isRecycled) {
                // 结果图可能复用或独立；perspectiveCrop 总是新建 bitmap，可回收 source
                source.recycle()
            }
        }
    }

    private fun decodeBitmap(context: android.content.Context, imageSrc: String): Bitmap? {
        return try {
            when {
                imageSrc.startsWith("assets://") -> {
                    val assetPath = imageSrc.removePrefix("assets://")
                    context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
                }
                imageSrc.startsWith("content://") || imageSrc.startsWith("file://") -> {
                    val uri = Uri.parse(imageSrc)
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                }
                else -> {
                    val file = File(imageSrc)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else {
                        // 兜底：当 assets 相对名
                        context.assets.open(imageSrc).use { BitmapFactory.decodeStream(it) }
                    }
                }
            }
        } catch (e: Exception) {
            throw CropException(CropException.DECODE_FAILED, "decode failed for $imageSrc", e)
        }
    }

    private fun rotateBitmap(origin: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return origin
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(origin, 0, 0, origin.width, origin.height, matrix, true)
    }

    /**
     * 结果尺寸：宽=上边长，高=左边长；不再对结果加 degrees（旋转已在源图完成）。
     */
    private fun perspectiveCrop(bitmap: Bitmap, points: List<CropPoint>): Bitmap {
        val srcPoints = floatArrayOf(
            points[0].x, points[0].y,
            points[1].x, points[1].y,
            points[2].x, points[2].y,
            points[3].x, points[3].y,
        )
        val width = max(
            1,
            hypot(
                (srcPoints[2] - srcPoints[0]).toDouble(),
                (srcPoints[3] - srcPoints[1]).toDouble(),
            ).roundToInt(),
        )
        val height = max(
            1,
            hypot(
                (srcPoints[6] - srcPoints[0]).toDouble(),
                (srcPoints[7] - srcPoints[1]).toDouble(),
            ).roundToInt(),
        )
        val destPoints = floatArrayOf(
            0f, 0f,
            width.toFloat(), 0f,
            width.toFloat(), height.toFloat(),
            0f, height.toFloat(),
        )
        val matrix = Matrix()
        val ok = matrix.setPolyToPoly(srcPoints, 0, destPoints, 0, 4)
        if (!ok) {
            throw CropException(CropException.TRANSFORM_FAILED, "setPolyToPoly returned false")
        }
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(bitmap, matrix, paint)
        return result
    }
}
