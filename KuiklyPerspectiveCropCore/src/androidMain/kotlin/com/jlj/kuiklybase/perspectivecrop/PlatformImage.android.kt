package com.jlj.kuiklybase.perspectivecrop

import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Android 出图句柄；持有 Bitmap，不暴露到 common。
 */
actual class PlatformImage internal constructor(
    private val _bitmap: Bitmap,
    private val _outputFormat: CropOutputFormat,
    private val _jpegQuality: Int,
) {
    actual val width: Int get() = _bitmap.width
    actual val height: Int get() = _bitmap.height

    actual fun saveToFile(): String {
        val context = PerspectiveCropAndroid.requireContext()
        val ext = if (_outputFormat == CropOutputFormat.Png) "png" else "jpg"
        val file = File(context.cacheDir, "perspective_crop_${System.currentTimeMillis()}.$ext")
        return saveToFile(file.absolutePath)
    }

    actual fun saveToFile(path: String): String {
        // 允许调用方传入 file://；落盘仍用本地 File
        val file = if (path.startsWith("file://")) {
            File(Uri.parse(path).path ?: path.removePrefix("file://"))
        } else {
            File(path)
        }
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        try {
            FileOutputStream(file).use { out ->
                val ok = when (_outputFormat) {
                    CropOutputFormat.Png -> _bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    CropOutputFormat.Jpeg -> {
                        val q = _jpegQuality.coerceIn(0, 100)
                        _bitmap.compress(Bitmap.CompressFormat.JPEG, q, out)
                    }
                }
                if (!ok) {
                    throw CropException(CropException.WRITE_FAILED, "Bitmap.compress returned false")
                }
            }
        } catch (e: CropException) {
            throw e
        } catch (e: Exception) {
            throw CropException(CropException.WRITE_FAILED, "failed to write $path", e)
        }
        // KRImageAdapter 只认 file:// / assets:// / http；裸绝对路径会空白
        return Uri.fromFile(file).toString()
    }

    internal fun bitmapOrNull(): Bitmap = _bitmap
}
