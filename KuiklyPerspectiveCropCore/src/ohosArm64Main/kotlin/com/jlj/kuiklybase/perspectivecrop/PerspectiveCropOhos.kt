package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: 鸿蒙宿主初始化；resfile 供 assets:// 解码，cache 供 cropToFile 落盘。
 * NAPI 导出见 Demo `perspectiveCropDemo/.../PerspectiveCropOhosNative.kt`。
 */
object PerspectiveCropOhos {
    private var _resfileDir: String? = null
    private var _cacheDir: String? = null

    /**
     * @param resfileDir hap `resourceDir`（assets://）
     * @param cacheDir 可写缓存目录（如 `context.cacheDir`）；空则 saveToFile() 失败
     */
    fun init(resfileDir: String, cacheDir: String = "") {
        if (resfileDir.isNotEmpty()) {
            _resfileDir = resfileDir.trimEnd('/')
        }
        if (cacheDir.isNotEmpty()) {
            _cacheDir = cacheDir.trimEnd('/')
        }
    }

    internal fun resfileDirOrNull(): String? = _resfileDir?.takeIf { it.isNotEmpty() }

    internal fun cacheDirOrNull(): String? = _cacheDir?.takeIf { it.isNotEmpty() }
}
