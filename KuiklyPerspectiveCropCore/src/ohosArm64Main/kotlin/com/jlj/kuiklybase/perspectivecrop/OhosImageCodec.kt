package com.jlj.kuiklybase.perspectivecrop

import cnames.structs.OH_DecodingOptions
import cnames.structs.OH_ImageSourceNative
import cnames.structs.OH_ImageSource_Info
import cnames.structs.OH_PixelmapNative
import cnames.structs.OH_Pixelmap_ImageInfo
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import ohos.image.IMAGE_SUCCESS_MIN
import ohos.image.Image_Size
import ohos.image.OH_DecodingOptions_Create
import ohos.image.OH_DecodingOptions_Release
import ohos.image.OH_DecodingOptions_SetDesiredSize
import ohos.image.OH_DecodingOptions_SetPixelFormat
import ohos.image.OH_DecodingOptions_SetRotate
import ohos.image.OH_ImageSourceInfo_Create
import ohos.image.OH_ImageSourceInfo_GetHeight
import ohos.image.OH_ImageSourceInfo_GetWidth
import ohos.image.OH_ImageSourceInfo_Release
import ohos.image.OH_ImageSourceNative_CreateFromUri
import ohos.image.OH_ImageSourceNative_CreatePixelmap
import ohos.image.OH_ImageSourceNative_GetImageInfo
import ohos.image.OH_ImageSourceNative_Release
import ohos.image.OH_PixelmapImageInfo_Create
import ohos.image.OH_PixelmapImageInfo_GetHeight
import ohos.image.OH_PixelmapImageInfo_GetWidth
import ohos.image.OH_PixelmapImageInfo_Release
import ohos.image.OH_PixelmapNative_GetArgbPixels
import ohos.image.OH_PixelmapNative_GetImageInfo
import ohos.image.OH_PixelmapNative_Release
import ohos.image.PIXEL_FORMAT_RGBA_8888_MIN
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: Ohos OH Image NDK → ARGB 降采样缓冲（放大镜缓存）。
 */
internal class OhosPixelBuffer(
    val argb: IntArray,
    val width: Int,
    val height: Int,
    /** cacheWidth / imageWidth（显示坐标系，与 JS/Android 一致）。 */
    val scale: Float,
)

/**
 * 解析 imageSrc → 可供 CreateFromUri 的 file URI。
 * - assets://x → {resfileDir}/x
 * - file://… / 绝对路径原样（绝对路径补 file://）
 */
internal fun resolveOhosImageUri(imageSrc: String): String? {
    return when {
        imageSrc.startsWith("assets://") -> {
            val dir = PerspectiveCropOhos.resfileDirOrNull() ?: return null
            val rel = imageSrc.removePrefix("assets://").trimStart('/')
            if (rel.isEmpty()) return null
            "file://$dir/$rel"
        }
        imageSrc.startsWith("file://") -> imageSrc
        imageSrc.startsWith("/") -> "file://$imageSrc"
        else -> {
            val dir = PerspectiveCropOhos.resfileDirOrNull() ?: return null
            "file://$dir/${imageSrc.trimStart('/')}"
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun decodeOhosPixelBuffer(
    imageSrc: String,
    uiRotationSteps: Int,
    imageWidth: Float,
    maxEdge: Int,
): OhosPixelBuffer? {
    if (imageSrc.isEmpty() || imageWidth <= 0f) return null
    val uri = resolveOhosImageUri(imageSrc) ?: return null
    val edge = maxEdge.coerceAtLeast(64)
    val steps = ((uiRotationSteps % 4) + 4) % 4
    val rotateDeg = steps * 90f

    return memScoped {
        val writable = allocArray<ByteVar>(uri.length + 1)
        for (i in uri.indices) {
            writable[i] = uri[i].code.toByte()
        }
        writable[uri.length] = 0

        val sourcePtr = alloc<CPointerVar<OH_ImageSourceNative>>()
        if (OH_ImageSourceNative_CreateFromUri(writable, uri.length.toULong(), sourcePtr.ptr) != IMAGE_SUCCESS_MIN ||
            sourcePtr.value == null
        ) {
            return@memScoped null
        }
        val source = sourcePtr.value!!

        try {
            val infoPtr = alloc<CPointerVar<OH_ImageSource_Info>>()
            if (OH_ImageSourceInfo_Create(infoPtr.ptr) != IMAGE_SUCCESS_MIN || infoPtr.value == null) {
                return@memScoped null
            }
            val info = infoPtr.value!!
            try {
                if (OH_ImageSourceNative_GetImageInfo(source, 0, info) != IMAGE_SUCCESS_MIN) {
                    return@memScoped null
                }
                val wVar = alloc<UIntVar>()
                val hVar = alloc<UIntVar>()
                OH_ImageSourceInfo_GetWidth(info, wVar.ptr)
                OH_ImageSourceInfo_GetHeight(info, hVar.ptr)
                val natW = wVar.value.toInt().coerceAtLeast(1)
                val natH = hVar.value.toInt().coerceAtLeast(1)

                val longest = max(natW, natH)
                val scaleDown = if (longest > edge) edge.toFloat() / longest else 1f
                val preW = max(1, (natW * scaleDown).roundToInt())
                val preH = max(1, (natH * scaleDown).roundToInt())
                val desired = alloc<Image_Size>()
                desired.width = preW.toUInt()
                desired.height = preH.toUInt()

                val optPtr = alloc<CPointerVar<OH_DecodingOptions>>()
                if (OH_DecodingOptions_Create(optPtr.ptr) != IMAGE_SUCCESS_MIN || optPtr.value == null) {
                    return@memScoped null
                }
                val options = optPtr.value!!
                try {
                    OH_DecodingOptions_SetPixelFormat(options, PIXEL_FORMAT_RGBA_8888_MIN.toInt())
                    OH_DecodingOptions_SetDesiredSize(options, desired.ptr)
                    if (rotateDeg != 0f) {
                        OH_DecodingOptions_SetRotate(options, rotateDeg)
                    }

                    val pmPtr = alloc<CPointerVar<OH_PixelmapNative>>()
                    if (OH_ImageSourceNative_CreatePixelmap(source, options, pmPtr.ptr) != IMAGE_SUCCESS_MIN ||
                        pmPtr.value == null
                    ) {
                        return@memScoped null
                    }
                    val pixelmap = pmPtr.value!!
                    try {
                        val pmInfoPtr = alloc<CPointerVar<OH_Pixelmap_ImageInfo>>()
                        if (OH_PixelmapImageInfo_Create(pmInfoPtr.ptr) != IMAGE_SUCCESS_MIN || pmInfoPtr.value == null) {
                            return@memScoped null
                        }
                        val pmInfo = pmInfoPtr.value!!
                        try {
                            if (OH_PixelmapNative_GetImageInfo(pixelmap, pmInfo) != IMAGE_SUCCESS_MIN) {
                                return@memScoped null
                            }
                            val outWVar = alloc<UIntVar>()
                            val outHVar = alloc<UIntVar>()
                            OH_PixelmapImageInfo_GetWidth(pmInfo, outWVar.ptr)
                            OH_PixelmapImageInfo_GetHeight(pmInfo, outHVar.ptr)
                            val outW = outWVar.value.toInt().coerceAtLeast(1)
                            val outH = outHVar.value.toInt().coerceAtLeast(1)
                            val sizeVar = alloc<ULongVar>()
                            sizeVar.value = (outW.toLong() * outH * 4L).toULong()
                            val bytes = UByteArray(outW * outH * 4)
                            val readRc = bytes.usePinned { pinned ->
                                OH_PixelmapNative_GetArgbPixels(
                                    pixelmap,
                                    pinned.addressOf(0).reinterpret(),
                                    sizeVar.ptr,
                                )
                            }
                            if (readRc != IMAGE_SUCCESS_MIN) {
                                return@memScoped null
                            }
                            // GetArgbPixels：每像素 4 字节 A,R,G,B
                            val argb = IntArray(outW * outH)
                            var p = 0
                            for (i in argb.indices) {
                                val a = bytes[p].toInt() and 0xFF
                                val r = bytes[p + 1].toInt() and 0xFF
                                val g = bytes[p + 2].toInt() and 0xFF
                                val b = bytes[p + 3].toInt() and 0xFF
                                argb[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
                                p += 4
                            }
                            val scale = outW / imageWidth.coerceAtLeast(1f)
                            OhosPixelBuffer(argb, outW, outH, scale)
                        } finally {
                            OH_PixelmapImageInfo_Release(pmInfo)
                        }
                    } finally {
                        OH_PixelmapNative_Release(pixelmap)
                    }
                } finally {
                    OH_DecodingOptions_Release(options)
                }
            } finally {
                OH_ImageSourceInfo_Release(info)
            }
        } finally {
            OH_ImageSourceNative_Release(source)
        }
    }
}

/**
 * 出图解码：旋源图后按 [maxEdge] 缩放。
 * [OhosPixelBuffer.scale] = bufWidth / rotatedNaturalWidth（对齐 JS decodeForCrop）。
 */
@OptIn(ExperimentalForeignApi::class)
internal fun decodeOhosPixelBufferForCrop(
    imageSrc: String,
    uiRotationSteps: Int,
    maxEdge: Int,
): OhosPixelBuffer {
    if (imageSrc.isEmpty()) {
        throw CropException(CropException.EMPTY_IMAGE_SRC, "imageSrc is empty")
    }
    val uri = resolveOhosImageUri(imageSrc)
        ?: throw CropException(
            CropException.DECODE_FAILED,
            "cannot resolve imageSrc=$imageSrc (need PerspectiveCropOhos.init for assets://)",
        )
    val edge = maxEdge.coerceAtLeast(64)
    val steps = ((uiRotationSteps % 4) + 4) % 4
    val rotateDeg = steps * 90f

    return memScoped {
        val writable = allocArray<ByteVar>(uri.length + 1)
        for (i in uri.indices) {
            writable[i] = uri[i].code.toByte()
        }
        writable[uri.length] = 0

        val sourcePtr = alloc<CPointerVar<OH_ImageSourceNative>>()
        if (OH_ImageSourceNative_CreateFromUri(writable, uri.length.toULong(), sourcePtr.ptr) != IMAGE_SUCCESS_MIN ||
            sourcePtr.value == null
        ) {
            throw CropException(CropException.DECODE_FAILED, "CreateFromUri failed for $uri")
        }
        val source = sourcePtr.value!!
        try {
            val infoPtr = alloc<CPointerVar<OH_ImageSource_Info>>()
            if (OH_ImageSourceInfo_Create(infoPtr.ptr) != IMAGE_SUCCESS_MIN || infoPtr.value == null) {
                throw CropException(CropException.DECODE_FAILED, "ImageSourceInfo_Create failed")
            }
            val info = infoPtr.value!!
            try {
                if (OH_ImageSourceNative_GetImageInfo(source, 0, info) != IMAGE_SUCCESS_MIN) {
                    throw CropException(CropException.DECODE_FAILED, "GetImageInfo failed")
                }
                val wVar = alloc<UIntVar>()
                val hVar = alloc<UIntVar>()
                OH_ImageSourceInfo_GetWidth(info, wVar.ptr)
                OH_ImageSourceInfo_GetHeight(info, hVar.ptr)
                val natW = wVar.value.toInt().coerceAtLeast(1)
                val natH = hVar.value.toInt().coerceAtLeast(1)
                val swap = steps % 2 != 0
                val rotNatW = if (swap) natH else natW
                val rotNatH = if (swap) natW else natH

                val longest = max(rotNatW, rotNatH)
                val scaleDown = if (longest > edge) edge.toFloat() / longest else 1f
                // DesiredSize 在旋转前相对源图；旋转后尺寸由 NDK 处理
                val preW = max(1, (natW * scaleDown).roundToInt())
                val preH = max(1, (natH * scaleDown).roundToInt())
                val desired = alloc<Image_Size>()
                desired.width = preW.toUInt()
                desired.height = preH.toUInt()

                val optPtr = alloc<CPointerVar<OH_DecodingOptions>>()
                if (OH_DecodingOptions_Create(optPtr.ptr) != IMAGE_SUCCESS_MIN || optPtr.value == null) {
                    throw CropException(CropException.DECODE_FAILED, "DecodingOptions_Create failed")
                }
                val options = optPtr.value!!
                try {
                    OH_DecodingOptions_SetPixelFormat(options, PIXEL_FORMAT_RGBA_8888_MIN.toInt())
                    OH_DecodingOptions_SetDesiredSize(options, desired.ptr)
                    if (rotateDeg != 0f) {
                        OH_DecodingOptions_SetRotate(options, rotateDeg)
                    }
                    val pmPtr = alloc<CPointerVar<OH_PixelmapNative>>()
                    if (OH_ImageSourceNative_CreatePixelmap(source, options, pmPtr.ptr) != IMAGE_SUCCESS_MIN ||
                        pmPtr.value == null
                    ) {
                        throw CropException(CropException.DECODE_FAILED, "CreatePixelmap failed")
                    }
                    val pixelmap = pmPtr.value!!
                    try {
                        val pmInfoPtr = alloc<CPointerVar<OH_Pixelmap_ImageInfo>>()
                        if (OH_PixelmapImageInfo_Create(pmInfoPtr.ptr) != IMAGE_SUCCESS_MIN ||
                            pmInfoPtr.value == null
                        ) {
                            throw CropException(CropException.DECODE_FAILED, "PixelmapImageInfo_Create failed")
                        }
                        val pmInfo = pmInfoPtr.value!!
                        try {
                            if (OH_PixelmapNative_GetImageInfo(pixelmap, pmInfo) != IMAGE_SUCCESS_MIN) {
                                throw CropException(CropException.DECODE_FAILED, "GetImageInfo(pixelmap) failed")
                            }
                            val outWVar = alloc<UIntVar>()
                            val outHVar = alloc<UIntVar>()
                            OH_PixelmapImageInfo_GetWidth(pmInfo, outWVar.ptr)
                            OH_PixelmapImageInfo_GetHeight(pmInfo, outHVar.ptr)
                            val outW = outWVar.value.toInt().coerceAtLeast(1)
                            val outH = outHVar.value.toInt().coerceAtLeast(1)
                            val sizeVar = alloc<ULongVar>()
                            sizeVar.value = (outW.toLong() * outH * 4L).toULong()
                            val bytes = UByteArray(outW * outH * 4)
                            val readRc = bytes.usePinned { pinned ->
                                OH_PixelmapNative_GetArgbPixels(
                                    pixelmap,
                                    pinned.addressOf(0).reinterpret(),
                                    sizeVar.ptr,
                                )
                            }
                            if (readRc != IMAGE_SUCCESS_MIN) {
                                throw CropException(CropException.DECODE_FAILED, "GetArgbPixels failed rc=$readRc")
                            }
                            val argb = IntArray(outW * outH)
                            var p = 0
                            for (i in argb.indices) {
                                val a = bytes[p].toInt() and 0xFF
                                val r = bytes[p + 1].toInt() and 0xFF
                                val g = bytes[p + 2].toInt() and 0xFF
                                val b = bytes[p + 3].toInt() and 0xFF
                                argb[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
                                p += 4
                            }
                            val scale = outW.toFloat() / rotNatW.coerceAtLeast(1).toFloat()
                            OhosPixelBuffer(argb, outW, outH, scale)
                        } finally {
                            OH_PixelmapImageInfo_Release(pmInfo)
                        }
                    } finally {
                        OH_PixelmapNative_Release(pixelmap)
                    }
                } finally {
                    OH_DecodingOptions_Release(options)
                }
            } finally {
                OH_ImageSourceInfo_Release(info)
            }
        } finally {
            OH_ImageSourceNative_Release(source)
        }
    }
}
