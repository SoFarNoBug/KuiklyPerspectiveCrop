package com.jlj.kuiklyperspectivecropui

import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropIos

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: 壳侧调用入口；转发 PerspectiveCropIos.init（Swift 可见）。
 */
object PerspectiveCropIosBridge {
    fun configure(cacheDir: String) {
        PerspectiveCropIos.init(cacheDir)
    }
}
