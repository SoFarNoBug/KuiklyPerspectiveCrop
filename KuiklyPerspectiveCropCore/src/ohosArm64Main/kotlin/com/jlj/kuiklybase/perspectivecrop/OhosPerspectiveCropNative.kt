package com.jlj.kuiklybase.perspectivecrop

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.usePinned
import ohos.drawing.ALPHA_FORMAT_UNPREMUL_MIN
import ohos.drawing.COLOR_FORMAT_BGRA_8888_MIN
import ohos.drawing.OH_Drawing_BitmapBuild
import ohos.drawing.OH_Drawing_BitmapCreate
import ohos.drawing.OH_Drawing_BitmapDestroy
import ohos.drawing.OH_Drawing_BitmapFormat_Min
import ohos.drawing.OH_Drawing_BitmapGetPixels
import ohos.drawing.OH_Drawing_CanvasBind
import ohos.drawing.OH_Drawing_CanvasCreate
import ohos.drawing.OH_Drawing_CanvasDestroy
import ohos.drawing.OH_Drawing_CanvasDrawPixelMapRect
import ohos.drawing.OH_Drawing_CanvasSetMatrix
import ohos.drawing.OH_Drawing_MatrixCreate
import ohos.drawing.OH_Drawing_MatrixDestroy
import ohos.drawing.OH_Drawing_MatrixSetPolyToPoly
import ohos.drawing.OH_Drawing_PixelMapDissolve
import ohos.drawing.OH_Drawing_PixelMapGetFromOhPixelMapNative
import ohos.drawing.OH_Drawing_Point2D
import ohos.drawing.OH_Drawing_RectCreate
import ohos.drawing.OH_Drawing_RectDestroy
import platform.posix.memcpy
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: 鸿蒙 Native 透视：OH Drawing SetPolyToPoly + CanvasDrawPixelMapRect（对齐 Android）。
 * 调用方负责 [OhosCropPixelmap.close]；本函数不释放源 PixelMap。
 */
@OptIn(ExperimentalForeignApi::class)
internal fun perspectiveCropDrawing(
    cropPm: OhosCropPixelmap,
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

    return memScoped {
        val drawingPm = OH_Drawing_PixelMapGetFromOhPixelMapNative(cropPm.ptr)
            ?: throw CropException(CropException.TRANSFORM_FAILED, "PixelMapGetFromOhPixelMapNative failed")
        try {
            val bitmap = OH_Drawing_BitmapCreate()
                ?: throw CropException(CropException.TRANSFORM_FAILED, "BitmapCreate failed")
            try {
                val fmt = alloc<OH_Drawing_BitmapFormat_Min>()
                // Drawing BGRA_8888：内存字节序 B,G,R,A（非 GetArgbPixels 的 A,R,G,B）
                fmt.colorFormat = COLOR_FORMAT_BGRA_8888_MIN
                fmt.alphaFormat = ALPHA_FORMAT_UNPREMUL_MIN
                OH_Drawing_BitmapBuild(bitmap, outW.toUInt(), outH.toUInt(), fmt.ptr)

                val canvas = OH_Drawing_CanvasCreate()
                    ?: throw CropException(CropException.TRANSFORM_FAILED, "CanvasCreate failed")
                try {
                    OH_Drawing_CanvasBind(canvas, bitmap)

                    val matrix = OH_Drawing_MatrixCreate()
                        ?: throw CropException(CropException.TRANSFORM_FAILED, "MatrixCreate failed")
                    try {
                        val srcPts = allocArray<OH_Drawing_Point2D>(4)
                        val dstPts = allocArray<OH_Drawing_Point2D>(4)
                        for (i in 0 until 4) {
                            srcPts[i].x = pointsInSrcSpace[i].x
                            srcPts[i].y = pointsInSrcSpace[i].y
                        }
                        dstPts[0].x = 0f; dstPts[0].y = 0f
                        dstPts[1].x = outW.toFloat(); dstPts[1].y = 0f
                        dstPts[2].x = outW.toFloat(); dstPts[2].y = outH.toFloat()
                        dstPts[3].x = 0f; dstPts[3].y = outH.toFloat()

                        if (!OH_Drawing_MatrixSetPolyToPoly(matrix, srcPts, dstPts, 4u)) {
                            throw CropException(
                                CropException.TRANSFORM_FAILED,
                                "OH_Drawing_MatrixSetPolyToPoly returned false",
                            )
                        }
                        OH_Drawing_CanvasSetMatrix(canvas, matrix)

                        val srcRect = OH_Drawing_RectCreate(
                            0f,
                            0f,
                            cropPm.width.toFloat(),
                            cropPm.height.toFloat(),
                        )
                            ?: throw CropException(CropException.TRANSFORM_FAILED, "RectCreate(src) failed")
                        val dstRect = OH_Drawing_RectCreate(
                            0f,
                            0f,
                            cropPm.width.toFloat(),
                            cropPm.height.toFloat(),
                        )
                            ?: throw CropException(CropException.TRANSFORM_FAILED, "RectCreate(dst) failed")
                        try {
                            OH_Drawing_CanvasDrawPixelMapRect(
                                canvas,
                                drawingPm,
                                srcRect,
                                dstRect,
                                null,
                            )
                        } finally {
                            OH_Drawing_RectDestroy(srcRect)
                            OH_Drawing_RectDestroy(dstRect)
                        }
                    } finally {
                        OH_Drawing_MatrixDestroy(matrix)
                    }
                } finally {
                    OH_Drawing_CanvasDestroy(canvas)
                }

                val pixels = OH_Drawing_BitmapGetPixels(bitmap)
                    ?: throw CropException(CropException.TRANSFORM_FAILED, "BitmapGetPixels returned null")
                val byteCount = outW * outH * 4
                val bytes = ByteArray(byteCount)
                bytes.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), pixels, byteCount.toULong())
                }
                // BGRA_8888 → packed ARGB Int
                val argb = IntArray(outW * outH)
                var p = 0
                for (i in argb.indices) {
                    val b = bytes[p].toInt() and 0xFF
                    val g = bytes[p + 1].toInt() and 0xFF
                    val r = bytes[p + 2].toInt() and 0xFF
                    val a = bytes[p + 3].toInt() and 0xFF
                    argb[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
                    p += 4
                }
                Triple(argb, outW, outH)
            } finally {
                OH_Drawing_BitmapDestroy(bitmap)
            }
        } finally {
            OH_Drawing_PixelMapDissolve(drawingPm)
        }
    }
}
