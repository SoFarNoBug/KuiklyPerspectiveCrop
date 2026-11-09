package com.jlj.kuiklybase.perspectivecrop

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: iOS 出图句柄；ARGB + PNG/JPEG 落盘 file://。
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

    @OptIn(ExperimentalForeignApi::class)
    actual fun saveToFile(): String {
        val cache = PerspectiveCropIos.requireCacheDir()
        val ext = if (_outputFormat == CropOutputFormat.Png) "png" else "jpg"
        val path = "$cache/perspective_crop_${nextCropFileToken()}.$ext"
        return saveToFile(path)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun saveToFile(path: String): String {
        if (path.isEmpty()) {
            throw CropException(CropException.WRITE_FAILED, "output path is empty")
        }
        val absolute = when {
            path.startsWith("file://") -> path.removePrefix("file://")
            else -> path
        }
        val slash = absolute.lastIndexOf('/')
        if (slash > 0) {
            val parent = absolute.substring(0, slash)
            platform.posix.mkdir(parent, 511u) // 0777；已存在忽略
        }
        try {
            writeArgbToFile(_argb, _width, _height, absolute, _outputFormat, _jpegQuality)
        } catch (e: CropException) {
            throw e
        } catch (e: Exception) {
            throw CropException(CropException.WRITE_FAILED, "failed to write $absolute", e)
        }
        return if (absolute.startsWith("/")) "file://$absolute" else absolute
    }
}
