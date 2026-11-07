package com.jlj.kuiklybase.perspectivecrop.overlay

import com.jlj.kuiklybase.perspectivecrop.CropGeometry
import com.jlj.kuiklybase.perspectivecrop.CropPoint
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropConfig
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Path
import com.tencent.kuikly.compose.ui.graphics.drawscope.DrawScope
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke

/**
 * 蒙版：外框顺时针 + 选区逆时针，靠非零环绕规则挖洞（不用 blendMode / DST_OUT）。
 */
object DefaultMaskOverlayPainter : MaskOverlayPainter {
    override fun DrawScope.drawMask(viewPoints: List<CropPoint>, config: PerspectiveCropConfig) {
        if (viewPoints.size != 4) return
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
            // reverse winding for hole
            moveTo(viewPoints[0].x, viewPoints[0].y)
            lineTo(viewPoints[3].x, viewPoints[3].y)
            lineTo(viewPoints[2].x, viewPoints[2].y)
            lineTo(viewPoints[1].x, viewPoints[1].y)
            close()
        }
        drawPath(path, color = Color.Black.copy(alpha = config.maskAlpha))
    }
}

object DefaultBorderOverlayPainter : BorderOverlayPainter {
    override fun DrawScope.drawBorder(viewPoints: List<CropPoint>, config: PerspectiveCropConfig) {
        if (viewPoints.size != 4) return
        val path = Path().apply {
            moveTo(viewPoints[0].x, viewPoints[0].y)
            lineTo(viewPoints[1].x, viewPoints[1].y)
            lineTo(viewPoints[2].x, viewPoints[2].y)
            lineTo(viewPoints[3].x, viewPoints[3].y)
            close()
        }
        drawPath(
            path,
            color = Color(config.borderColor),
            style = Stroke(width = config.borderWidthPx),
        )
    }
}

object DefaultCornerOverlayPainter : CornerOverlayPainter {
    override fun DrawScope.drawCorners(viewPoints: List<CropPoint>, config: PerspectiveCropConfig) {
        if (viewPoints.size != 4) return
        val color = Color(config.cornerColor)
        val r = config.cornerRadiusPx
        for (p in viewPoints) {
            drawCircle(color = color, radius = r, center = Offset(p.x, p.y))
        }
    }
}

object DefaultEdgeMidOverlayPainter : EdgeMidOverlayPainter {
    override fun DrawScope.drawEdgeMids(viewMids: List<CropPoint>, config: PerspectiveCropConfig) {
        if (!config.showEdgeMidPoints || viewMids.size != 4) return
        val color = Color(config.cornerColor).copy(alpha = 0.85f)
        val r = config.cornerRadiusPx * 0.75f
        for (p in viewMids) {
            drawCircle(color = color, radius = r, center = Offset(p.x, p.y))
        }
    }
}

object DefaultGuideOverlayPainter : GuideOverlayPainter {
    override fun DrawScope.drawGuides(viewPoints: List<CropPoint>, config: PerspectiveCropConfig) {
        if (!config.showGuideLines || viewPoints.size != 4) return
        // M1 可空实现占位：三分线近似用 AABB
        val minX = viewPoints.minOf { it.x }
        val maxX = viewPoints.maxOf { it.x }
        val minY = viewPoints.minOf { it.y }
        val maxY = viewPoints.maxOf { it.y }
        val color = Color.White.copy(alpha = 0.35f)
        val x1 = minX + (maxX - minX) / 3f
        val x2 = minX + (maxX - minX) * 2f / 3f
        val y1 = minY + (maxY - minY) / 3f
        val y2 = minY + (maxY - minY) * 2f / 3f
        drawLine(color, Offset(x1, minY), Offset(x1, maxY), strokeWidth = 1f)
        drawLine(color, Offset(x2, minY), Offset(x2, maxY), strokeWidth = 1f)
        drawLine(color, Offset(minX, y1), Offset(maxX, y1), strokeWidth = 1f)
        drawLine(color, Offset(minX, y2), Offset(maxX, y2), strokeWidth = 1f)
    }
}

/** M1 空实现；真像素放大镜 → M3。 */
object NoOpMagnifierOverlayPainter : MagnifierOverlayPainter {
    override fun DrawScope.drawMagnifier(
        viewPoints: List<CropPoint>,
        touchViewX: Float,
        touchViewY: Float,
        config: PerspectiveCropConfig,
    ) = Unit
}

/** 便于测试用的 mid 计算桥接。 */
fun midpointsInView(viewPoints: List<CropPoint>): List<CropPoint> =
    if (viewPoints.size == 4) CropGeometry.edgeMidpoints(viewPoints) else emptyList()
