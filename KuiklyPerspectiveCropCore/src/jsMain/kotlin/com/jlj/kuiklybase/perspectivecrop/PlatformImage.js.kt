package com.jlj.kuiklybase.perspectivecrop

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: JS 出图句柄；[saveToFile] 返回 blob:（Q37）。
 */
actual class PlatformImage internal constructor(
    private val _argb: IntArray,
    private val _width: Int,
    private val _height: Int,
    private val _outputFormat: CropOutputFormat,
    private val _jpegQuality: Int,
) {
    actual val width: Int get() = _width
    actual val height: Int get() = _height

    actual fun saveToFile(): String = encodeToBlobUrl()

    actual fun saveToFile(path: String): String {
        // H5 无本地落盘；忽略 path，仍返回 blob:（Q37）
        if (path.isEmpty()) {
            throw CropException(CropException.WRITE_FAILED, "output path is empty")
        }
        return encodeToBlobUrl()
    }

    private fun encodeToBlobUrl(): String {
        try {
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.width = _width
            canvas.height = _height
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
            val imageData = ctx.createImageData(_width.toDouble(), _height.toDouble())
            writeArgbToClamped(imageData.data.asDynamic(), _argb)
            ctx.putImageData(imageData, 0.0, 0.0)

            val mime = when (_outputFormat) {
                CropOutputFormat.Png -> "image/png"
                CropOutputFormat.Jpeg -> "image/jpeg"
            }
            val dataUrl = if (_outputFormat == CropOutputFormat.Jpeg) {
                val q = (_jpegQuality.coerceIn(0, 100) / 100.0)
                canvas.toDataURL(mime, q)
            } else {
                canvas.toDataURL(mime)
            }
            return dataUrlToBlobObjectUrl(dataUrl, mime)
        } catch (e: CropException) {
            throw e
        } catch (e: Exception) {
            throw CropException(CropException.WRITE_FAILED, "failed to encode blob URL", e)
        }
    }
}

private fun writeArgbToClamped(dst: dynamic, argb: IntArray) {
    var p = 0
    for (px in argb) {
        dst[p] = (px ushr 16) and 0xFF
        dst[p + 1] = (px ushr 8) and 0xFF
        dst[p + 2] = px and 0xFF
        dst[p + 3] = (px ushr 24) and 0xFF
        p += 4
    }
}

private fun dataUrlToBlobObjectUrl(dataUrl: String, mime: String): String {
    val comma = dataUrl.indexOf(',')
    if (comma < 0) {
        throw CropException(CropException.WRITE_FAILED, "invalid data URL")
    }
    val b64 = dataUrl.substring(comma + 1)
    val bin = window.asDynamic().atob(b64) as String
    val len = bin.length
    val bytes = ByteArray(len) { i -> bin[i].code.toByte() }
    val blob = Blob(arrayOf(bytes), BlobPropertyBag(type = mime))
    return URL.createObjectURL(blob)
}
