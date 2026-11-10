package com.jlj.kuiklybase.perspectivecrop

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import platform.Foundation.NSData
import platform.UIKit.UIImage

/**
 * @CreateDate: 2026/11/09
 * @Author: jlj
 * @Description: 壳侧读放大镜 BlitStore（Swift 经 ObjC 名 PerspectiveCropIosMagnifierBridge 调用；exact 免 framework 前缀）。
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName("PerspectiveCropIosMagnifierBridge", exact = true)
object PerspectiveCropIosMagnifierBridge {
    fun generation(): Int = MagnifierBlitStore.generation

    fun imageOrNull(): UIImage? = MagnifierBlitStore.uiImageOrNull()

    /** PNG 字节；壳用 `UIImage(data:)` 本地解码，避免 KN UIImage 跨桥绘制失败。 */
    fun pngDataOrNull(): NSData? = MagnifierBlitStore.pngDataOrNull()

    /** 顶左行优先 RGBA；壳 `CGImageCreate`，不依赖 KN UIImage。 */
    fun rgbaDataOrNull(): NSData? = MagnifierBlitStore.rgbaDataOrNull()

    fun blitWidth(): Int = MagnifierBlitStore.blitWidth()

    fun blitHeight(): Int = MagnifierBlitStore.blitHeight()

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

    /** 四点坐标个数（期望 8：x0,y0…x3,y3）；0 表示无线。 */
    fun viewPointCount(): Int = MagnifierBlitStore.viewPointsXy.size

    fun viewPointAt(index: Int): Float {
        val xy = MagnifierBlitStore.viewPointsXy
        if (index < 0 || index >= xy.size) return 0f
        return xy[index]
    }
}
