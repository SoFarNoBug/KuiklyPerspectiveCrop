package com.jlj.kuiklybase.perspectivecrop

import android.app.Application
import android.content.Context

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Android 宿主初始化（Q20）；保存 Application Context 供解码/缓存。
 */
object PerspectiveCropAndroid {
    @Volatile
    private var _appContext: Context? = null

    fun init(application: Application) {
        _appContext = application.applicationContext
    }

    internal fun requireContext(): Context {
        return _appContext
            ?: throw CropException(
                CropException.NOT_INITIALIZED,
                "Call PerspectiveCropAndroid.init(application) before crop",
            )
    }
}
