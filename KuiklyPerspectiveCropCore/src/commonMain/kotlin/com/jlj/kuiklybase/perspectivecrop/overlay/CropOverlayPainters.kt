package com.jlj.kuiklybase.perspectivecrop.overlay

import com.jlj.kuiklybase.perspectivecrop.CropPoint
import com.jlj.kuiklybase.perspectivecrop.MagnifierPatch
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropConfig
import com.tencent.kuikly.compose.ui.graphics.drawscope.DrawScope

/**
 * 六层可分别注入的叠层 SPI（Q13）。可有门面聚合，但分层必须可单独替换。
 */
fun interface MaskOverlayPainter {
    fun DrawScope.drawMask(viewPoints: List<CropPoint>, config: PerspectiveCropConfig)
}

fun interface BorderOverlayPainter {
    fun DrawScope.drawBorder(viewPoints: List<CropPoint>, config: PerspectiveCropConfig)
}

fun interface CornerOverlayPainter {
    fun DrawScope.drawCorners(viewPoints: List<CropPoint>, config: PerspectiveCropConfig)
}

fun interface EdgeMidOverlayPainter {
    fun DrawScope.drawEdgeMids(viewMids: List<CropPoint>, config: PerspectiveCropConfig)
}

fun interface GuideOverlayPainter {
    fun DrawScope.drawGuides(viewPoints: List<CropPoint>, config: PerspectiveCropConfig)
}

fun interface MagnifierOverlayPainter {
    fun DrawScope.drawMagnifier(
        viewPoints: List<CropPoint>,
        touchViewX: Float,
        touchViewY: Float,
        config: PerspectiveCropConfig,
        patch: MagnifierPatch?,
    )
}

data class CropOverlayPainters(
    val mask: MaskOverlayPainter = DefaultMaskOverlayPainter,
    val border: BorderOverlayPainter = DefaultBorderOverlayPainter,
    val corner: CornerOverlayPainter = DefaultCornerOverlayPainter,
    val edgeMid: EdgeMidOverlayPainter = DefaultEdgeMidOverlayPainter,
    val guide: GuideOverlayPainter = DefaultGuideOverlayPainter,
    val magnifier: MagnifierOverlayPainter = DefaultMagnifierOverlayPainter,
)
