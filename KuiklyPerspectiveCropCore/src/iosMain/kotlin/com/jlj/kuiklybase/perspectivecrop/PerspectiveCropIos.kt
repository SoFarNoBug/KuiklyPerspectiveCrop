package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: iOS 宿主初始化；cacheDir 供 cropToFile 落盘；assets:// 走 Bundle 搜索。
 */
object PerspectiveCropIos {
    private var _cacheDir: String? = null

    /**
     * @param cacheDir 可写缓存目录（如 NSCachesDirectory）；空则无参 saveToFile() 失败
     */
    fun init(cacheDir: String) {
        if (cacheDir.isNotEmpty()) {
            _cacheDir = cacheDir.trimEnd('/')
        }
    }

    internal fun cacheDirOrNull(): String? = _cacheDir?.takeIf { it.isNotEmpty() }

    internal fun requireCacheDir(): String =
        cacheDirOrNull()
            ?: throw CropException(
                CropException.NOT_INITIALIZED,
                "Call PerspectiveCropIos.init(cacheDir) before saveToFile()",
            )
}
