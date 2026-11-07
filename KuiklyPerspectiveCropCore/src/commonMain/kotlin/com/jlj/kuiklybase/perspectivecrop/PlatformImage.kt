package com.jlj.kuiklybase.perspectivecrop

/**
 * 跨端图像句柄（expect/actual）。
 *
 * common 只暴露可移植能力；Android Bitmap 等放在对应源集，不进 common 公开签名。
 */
expect class PlatformImage {
    val width: Int
    val height: Int

    /**
     * 写出到默认缓存路径，返回可供 `Image(src)` / 再设为 [PerspectiveCropController] imageSrc 的 URI
     *（Android 为 `file://…`，非裸绝对路径）。
     */
    fun saveToFile(): String

    /** 写出到指定路径（可传绝对路径或 `file://`）；返回同上可显示 URI。 */
    fun saveToFile(path: String): String
}
