package com.jlj.kuiklybase.perspectivecrop.overlay

import com.jlj.kuiklybase.perspectivecrop.CropGeometry
import com.jlj.kuiklybase.perspectivecrop.CropPoint
import com.jlj.kuiklybase.perspectivecrop.MagnifierPatch
import com.jlj.kuiklybase.perspectivecrop.MagnifierPlacement
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropConfig
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.geometry.Rect
import com.tencent.kuikly.compose.ui.geometry.Size
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Path
import com.tencent.kuikly.compose.ui.graphics.drawscope.DrawScope
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke
import com.tencent.kuikly.compose.ui.graphics.drawscope.clipPath
import kotlin.math.hypot

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
        val color = Color(config.guideLineColor)
        val stroke = config.guideLineWidth.coerceAtLeast(0.5f)
        // 三分线：沿对边线性插值（优于 AABB）
        fun lerp(a: CropPoint, b: CropPoint, t: Float) =
            Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
        val tl = viewPoints[0]
        val tr = viewPoints[1]
        val br = viewPoints[2]
        val bl = viewPoints[3]
        for (t in listOf(1f / 3f, 2f / 3f)) {
            val top = lerp(tl, tr, t)
            val bottom = lerp(bl, br, t)
            drawLine(color, top, bottom, strokeWidth = stroke)
            val left = lerp(tl, bl, t)
            val right = lerp(tr, br, t)
            drawLine(color, left, right, strokeWidth = stroke)
        }
    }
}

/**
 * 真像素默认放大镜：消费 [MagnifierPatch] 像素块画圆镜（Q25/Q29）。
 * 无 patch 时仅画空圆边框占位（非 Android stub）。
 */
object DefaultMagnifierOverlayPainter : MagnifierOverlayPainter {
    override fun DrawScope.drawMagnifier(
        viewPoints: List<CropPoint>,
        touchViewX: Float,
        touchViewY: Float,
        config: PerspectiveCropConfig,
        patch: MagnifierPatch?,
    ) {
        val radius = patch?.displayRadiusPx
            ?: (size.width * config.magnifierRadiusRatio).coerceAtLeast(24f)
        val (cx, cy) = magnifierCenter(
            placement = config.magnifierPlacement,
            radius = radius,
            touchViewX = touchViewX,
            touchViewY = touchViewY,
            canvasW = size.width,
            canvasH = size.height,
        )

        val clip = Path().apply {
            addOval(Rect(cx - radius, cy - radius, cx + radius, cy + radius))
        }
        clipPath(clip) {
            drawCircle(color = Color(0xFF222222), radius = radius, center = Offset(cx, cy))
            if (patch != null && patch.width > 0 && patch.height > 0) {
                // Kuikly 无 BitmapShader：网格 drawRect；拖动粗网格、停指升密（patch.drawGrid）
                val grid = patch.drawGrid.coerceIn(8, 48)
                val dest = radius * 2f
                val cellW = dest / grid
                val cellH = dest / grid
                val left = cx - radius
                val top = cy - radius
                val pw = patch.width
                val ph = patch.height
                val pixels = patch.argbPixels
                for (gy in 0 until grid) {
                    val py = ((gy + 0.5f) * ph / grid).toInt().coerceIn(0, ph - 1)
                    val row = py * pw
                    for (gx in 0 until grid) {
                        val px = ((gx + 0.5f) * pw / grid).toInt().coerceIn(0, pw - 1)
                        drawRect(
                            color = Color(pixels[row + px]),
                            topLeft = Offset(left + gx * cellW, top + gy * cellH),
                            size = Size(cellW + 1f, cellH + 1f),
                        )
                    }
                }
            }
        }

        drawCircle(
            color = Color(config.magnifierBorderColor),
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = config.magnifierBorderWidth.coerceAtLeast(1f)),
        )

        if (config.showMagnifierCenterCross) {
            val half = config.magnifierCrossLineLength / 2f
            val cross = Color(config.magnifierCrossColor)
            val w = config.magnifierCrossLineWidth.coerceAtLeast(1f)
            drawLine(cross, Offset(cx - half, cy), Offset(cx + half, cy), strokeWidth = w)
            drawLine(cross, Offset(cx, cy - half), Offset(cx, cy + half), strokeWidth = w)
        }
    }
}

internal fun magnifierCenter(
    placement: MagnifierPlacement,
    radius: Float,
    touchViewX: Float,
    touchViewY: Float,
    canvasW: Float,
    canvasH: Float,
): Pair<Float, Float> {
    return when (placement) {
        MagnifierPlacement.TopLeft -> radius to radius
        MagnifierPlacement.TopRight -> (canvasW - radius) to radius
        MagnifierPlacement.TopAutoFlip -> {
            val nearTopLeft = hypot(touchViewX.toDouble(), touchViewY.toDouble()) < (radius * 2.5)
            val cx = if (nearTopLeft) canvasW - radius else radius
            cx to radius
        }
        MagnifierPlacement.AboveTouch -> {
            var cx = touchViewX
            var cy = touchViewY - radius * 1.35f
            cx = cx.coerceIn(radius, canvasW - radius)
            cy = cy.coerceIn(radius, canvasH - radius)
            cx to cy
        }
    }
}

/** 空放大镜（自定义皮肤可注入）。 */
object NoOpMagnifierOverlayPainter : MagnifierOverlayPainter {
    override fun DrawScope.drawMagnifier(
        viewPoints: List<CropPoint>,
        touchViewX: Float,
        touchViewY: Float,
        config: PerspectiveCropConfig,
        patch: MagnifierPatch?,
    ) = Unit
}

/** 便于测试用的 mid 计算桥接。 */
fun midpointsInView(viewPoints: List<CropPoint>): List<CropPoint> =
    if (viewPoints.size == 4) CropGeometry.edgeMidpoints(viewPoints) else emptyList()
