package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Ohos PlatformImage stub。
 */
actual class PlatformImage {
    actual val width: Int get() = 0
    actual val height: Int get() = 0

    actual fun saveToFile(): String =
        throw CropException(CropException.UNSUPPORTED_PLATFORM, "PlatformImage.saveToFile stub on Ohos")

    actual fun saveToFile(path: String): String =
        throw CropException(CropException.UNSUPPORTED_PLATFORM, "PlatformImage.saveToFile stub on Ohos")
}
