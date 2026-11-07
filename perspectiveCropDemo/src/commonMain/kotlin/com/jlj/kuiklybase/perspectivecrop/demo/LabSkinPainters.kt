package com.jlj.kuiklybase.perspectivecrop.demo

import com.jlj.kuiklybase.perspectivecrop.CropPoint
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropConfig
import com.jlj.kuiklybase.perspectivecrop.overlay.BorderOverlayPainter
import com.jlj.kuiklybase.perspectivecrop.overlay.CornerOverlayPainter
import com.jlj.kuiklybase.perspectivecrop.overlay.CropOverlayPainters
import com.jlj.kuiklybase.perspectivecrop.overlay.DefaultBorderOverlayPainter
import com.jlj.kuiklybase.perspectivecrop.overlay.DefaultCornerOverlayPainter
import com.jlj.kuiklybase.perspectivecrop.overlay.DefaultEdgeMidOverlayPainter
import com.jlj.kuiklybase.perspectivecrop.overlay.DefaultGuideOverlayPainter
import com.jlj.kuiklybase.perspectivecrop.overlay.DefaultMagnifierOverlayPainter
import com.jlj.kuiklybase.perspectivecrop.overlay.DefaultMaskOverlayPainter
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Path
import com.tencent.kuikly.compose.ui.graphics.drawscope.DrawScope
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Demo-only 换皮 Painter，证明六层可分别注入（Q13/Q30）；不进 Core。
 */
internal object LabAltBorderPainter : BorderOverlayPainter {
    override fun DrawScope.drawBorder(viewPoints: List<CropPoint>, config: PerspectiveCropConfig) {
        if (viewPoints.size != 4) return
        val path = Path().apply {
            moveTo(viewPoints[0].x, viewPoints[0].y)
            lineTo(viewPoints[1].x, viewPoints[1].y)
            lineTo(viewPoints[2].x, viewPoints[2].y)
            lineTo(viewPoints[3].x, viewPoints[3].y)
            close()
        }
        drawPath(path, color = Color(DemoSkinColors.borderAlt), style = Stroke(width = 4f))
    }
}

internal object LabAltCornerPainter : CornerOverlayPainter {
    override fun DrawScope.drawCorners(viewPoints: List<CropPoint>, config: PerspectiveCropConfig) {
        if (viewPoints.size != 4) return
        val color = Color(DemoSkinColors.accent)
        for (p in viewPoints) {
            drawCircle(color = color, radius = 10f, center = Offset(p.x, p.y))
            drawCircle(
                color = Color.White,
                radius = 10f,
                center = Offset(p.x, p.y),
                style = Stroke(width = 2f),
            )
        }
    }
}

internal fun defaultLabPainters(): CropOverlayPainters = CropOverlayPainters()

internal fun altSkinPainters(): CropOverlayPainters = CropOverlayPainters(
    mask = DefaultMaskOverlayPainter,
    border = LabAltBorderPainter,
    corner = LabAltCornerPainter,
    edgeMid = DefaultEdgeMidOverlayPainter,
    guide = DefaultGuideOverlayPainter,
    magnifier = DefaultMagnifierOverlayPainter,
)
