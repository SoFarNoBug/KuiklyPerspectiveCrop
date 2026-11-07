package com.jlj.kuiklybase.perspectivecrop

import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.ImageData
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * JS 侧像素缓冲（行优先 ARGB8888）+ 相对 [imageWidth] 的缩放比。
 */
internal class JsPixelBuffer(
    val argb: IntArray,
    val width: Int,
    val height: Int,
    /** cacheWidth / imageWidth（显示坐标系）。 */
    val scale: Float,
)

/**
 * H5 解码：`assets://` → `/assets/…`；其余 URL 原样（Q36）。
 */
internal fun resolveJsImageUrl(imageSrc: String): String {
    return when {
        imageSrc.startsWith("assets://") -> {
            val path = imageSrc.removePrefix("assets://").trimStart('/')
            "/assets/$path"
        }
        else -> imageSrc
    }
}

internal suspend fun decodeJsPixelBuffer(
    imageSrc: String,
    uiRotationSteps: Int,
    imageWidth: Float,
    maxEdge: Int,
): JsPixelBuffer {
    val url = resolveJsImageUrl(imageSrc)
    val img = loadHtmlImage(url)
    val steps = ((uiRotationSteps % 4) + 4) % 4
    val rotated = drawRotatedToArgb(img, steps)
    val scaled = scaleArgbToMaxEdge(rotated.first, rotated.second, rotated.third, maxEdge.coerceAtLeast(64))
    val scale = scaled.second / imageWidth.coerceAtLeast(1f)
    return JsPixelBuffer(scaled.first, scaled.second, scaled.third, scale)
}

/**
 * 出图解码：旋转后按 [maxEdge] 缩放。
 * [JsPixelBuffer.scale] = bufWidth / rotatedNaturalWidth，用于把图像坐标四点映射到缓冲像素。
 */
internal suspend fun decodeJsPixelBufferForCrop(
    imageSrc: String,
    uiRotationSteps: Int,
    maxEdge: Int,
): JsPixelBuffer {
    val url = resolveJsImageUrl(imageSrc)
    val img = loadHtmlImage(url)
    val steps = ((uiRotationSteps % 4) + 4) % 4
    val rotated = drawRotatedToArgb(img, steps)
    val naturalW = rotated.second
    val scaled = scaleArgbToMaxEdge(rotated.first, rotated.second, rotated.third, maxEdge.coerceAtLeast(64))
    val scale = scaled.second.toFloat() / naturalW.coerceAtLeast(1).toFloat()
    return JsPixelBuffer(scaled.first, scaled.second, scaled.third, scale)
}

private suspend fun loadHtmlImage(url: String): HTMLImageElement =
    suspendCancellableCoroutine { cont ->
        val img = document.createElement("img") as HTMLImageElement
        img.setAttribute("crossOrigin", "anonymous")
        val dyn = img.asDynamic()
        dyn.onload = {
            if (cont.isActive) cont.resume(img)
        }
        dyn.onerror = {
            if (cont.isActive) {
                cont.resumeWithException(
                    CropException(CropException.DECODE_FAILED, "failed to load imageSrc url=$url"),
                )
            }
        }
        cont.invokeOnCancellation {
            dyn.onload = null
            dyn.onerror = null
            img.src = ""
        }
        img.src = url
    }

private fun drawRotatedToArgb(
    img: HTMLImageElement,
    steps: Int,
): Triple<IntArray, Int, Int> {
    val iw = img.naturalWidth.takeIf { it > 0 } ?: img.width
    val ih = img.naturalHeight.takeIf { it > 0 } ?: img.height
    if (iw <= 0 || ih <= 0) {
        throw CropException(CropException.DECODE_FAILED, "image has zero size")
    }
    val swap = steps % 2 != 0
    val outW = if (swap) ih else iw
    val outH = if (swap) iw else ih
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = outW
    canvas.height = outH
    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    ctx.save()
    when (steps) {
        0 -> Unit
        1 -> {
            ctx.translate(outW.toDouble(), 0.0)
            ctx.rotate(kotlin.math.PI / 2)
        }
        2 -> {
            ctx.translate(outW.toDouble(), outH.toDouble())
            ctx.rotate(kotlin.math.PI)
        }
        3 -> {
            ctx.translate(0.0, outH.toDouble())
            ctx.rotate(-kotlin.math.PI / 2)
        }
    }
    ctx.drawImage(img, 0.0, 0.0)
    ctx.restore()
    return imageDataToArgb(ctx.getImageData(0.0, 0.0, outW.toDouble(), outH.toDouble()))
}

private fun scaleArgbToMaxEdge(
    argb: IntArray,
    width: Int,
    height: Int,
    maxEdge: Int,
): Triple<IntArray, Int, Int> {
    val longest = max(width, height)
    if (longest <= maxEdge) return Triple(argb, width, height)
    val scale = maxEdge.toFloat() / longest
    val nw = max(1, (width * scale).roundToInt())
    val nh = max(1, (height * scale).roundToInt())
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = width
    canvas.height = height
    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    val srcData = argbToImageData(argb, width, height)
    ctx.putImageData(srcData, 0.0, 0.0)
    val out = document.createElement("canvas") as HTMLCanvasElement
    out.width = nw
    out.height = nh
    val octx = out.getContext("2d") as CanvasRenderingContext2D
    octx.drawImage(canvas, 0.0, 0.0, nw.toDouble(), nh.toDouble())
    return imageDataToArgb(octx.getImageData(0.0, 0.0, nw.toDouble(), nh.toDouble()))
}

internal fun imageDataToArgb(data: ImageData): Triple<IntArray, Int, Int> {
    val w = data.width
    val h = data.height
    val src = data.data.asDynamic()
    val out = IntArray(w * h)
    var i = 0
    var p = 0
    while (i < out.size) {
        val r = (src[p] as Int) and 0xFF
        val g = (src[p + 1] as Int) and 0xFF
        val b = (src[p + 2] as Int) and 0xFF
        val a = (src[p + 3] as Int) and 0xFF
        out[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        i++
        p += 4
    }
    return Triple(out, w, h)
}

internal fun argbToImageData(argb: IntArray, width: Int, height: Int): ImageData {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = width
    canvas.height = height
    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    val data = ctx.createImageData(width.toDouble(), height.toDouble())
    val dst = data.data.asDynamic()
    var p = 0
    for (px in argb) {
        dst[p] = (px ushr 16) and 0xFF
        dst[p + 1] = (px ushr 8) and 0xFF
        dst[p + 2] = px and 0xFF
        dst[p + 3] = (px ushr 24) and 0xFF
        p += 4
    }
    return data
}
