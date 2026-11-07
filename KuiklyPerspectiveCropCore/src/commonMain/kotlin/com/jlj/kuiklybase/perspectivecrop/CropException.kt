package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 出图失败公开异常；用 [code] 细分原因（Q16）。
 */
class CropException(
    val code: String,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    companion object {
        const val ILLEGAL_POINTS = "ILLEGAL_POINTS"
        const val UNSUPPORTED_PLATFORM = "UNSUPPORTED_PLATFORM"
        const val NOT_INITIALIZED = "NOT_INITIALIZED"
        const val DECODE_FAILED = "DECODE_FAILED"
        const val TRANSFORM_FAILED = "TRANSFORM_FAILED"
        const val WRITE_FAILED = "WRITE_FAILED"
        const val EMPTY_IMAGE_SRC = "EMPTY_IMAGE_SRC"
    }
}
