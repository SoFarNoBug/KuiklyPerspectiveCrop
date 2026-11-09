package com.jlj.kuiklyperspectivecropui

import com.jlj.kuiklybase.perspectivecrop.MagnifierBlitStore
import platform.UIKit.UIImage

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: 壳侧读放大镜 BlitStore（Swift/ObjC 经 Demo framework 调用）。
 */
object PerspectiveCropIosMagnifierBridge {
    fun generation(): Int = MagnifierBlitStore.generation

    fun imageOrNull(): UIImage? = MagnifierBlitStore.uiImageOrNull()

    fun borderWidth(): Float = MagnifierBlitStore.borderWidth

    fun borderColorArgb(): Int = MagnifierBlitStore.borderColor

    fun showCross(): Boolean = MagnifierBlitStore.showCross

    fun crossColorArgb(): Int = MagnifierBlitStore.crossColor

    fun crossLineWidth(): Float = MagnifierBlitStore.crossLineWidth

    fun crossLineLength(): Float = MagnifierBlitStore.crossLineLength

    fun showCropLineMirror(): Boolean = MagnifierBlitStore.showCropLineMirror

    fun cropLineColorArgb(): Int = MagnifierBlitStore.cropLineColor

    fun cropLineWidth(): Float = MagnifierBlitStore.cropLineWidth

    fun touchViewX(): Float = MagnifierBlitStore.touchViewX

    fun touchViewY(): Float = MagnifierBlitStore.touchViewY

    /** 长度 8：x0,y0…x3,y3；空表示无线。 */
    fun viewPointsXy(): FloatArray = MagnifierBlitStore.viewPointsXy
}
