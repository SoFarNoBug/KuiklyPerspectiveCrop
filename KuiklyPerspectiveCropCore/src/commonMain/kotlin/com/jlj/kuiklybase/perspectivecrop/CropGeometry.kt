package com.jlj.kuiklybase.perspectivecrop

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 纯几何：凸性 / 夹角 / 命中 / 软钳制。与 UI 框架无关。
 */
object CropGeometry {

    fun isValidQuad(
        points: List<CropPoint>,
        imageW: Float,
        imageH: Float,
        minWidth: Float,
        minAngleDeg: Float,
    ): Boolean {
        if (points.size != 4) return false
        if (points.any { it.x < -0.5f || it.y < -0.5f || it.x > imageW + 0.5f || it.y > imageH + 0.5f }) {
            return false
        }
        if (!isConvex(points)) return false
        if (minWidth > 0f && minEdgeLength(points) < minWidth) return false
        if (minAngleDeg > 0f && !checkAngleLimit(points, minAngleDeg.toDouble())) return false
        return true
    }

    fun isConvex(points: List<CropPoint>): Boolean {
        if (points.size != 4) return false
        var sign = 0
        for (i in 0 until 4) {
            val a = points[i]
            val b = points[(i + 1) % 4]
            val c = points[(i + 2) % 4]
            val cross = (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x)
            if (abs(cross) < 1e-4f) continue
            val s = if (cross > 0f) 1 else -1
            if (sign == 0) sign = s
            else if (sign != s) return false
        }
        return sign != 0
    }

    fun minEdgeLength(points: List<CropPoint>): Float {
        var m = Float.MAX_VALUE
        for (i in 0 until 4) {
            val a = points[i]
            val b = points[(i + 1) % 4]
            m = min(m, hypot(b.x - a.x, b.y - a.y))
        }
        return m
    }

    fun checkAngleLimit(points: List<CropPoint>, minAngleDeg: Double): Boolean {
        for (i in 0 until 4) {
            val vertex = points[(i + 1) % 4]
            val prev = points[i]
            val next = points[(i + 2) % 4]
            if (angleDeg(vertex, prev, next) < minAngleDeg) return false
        }
        return true
    }

    fun angleDeg(vertex: CropPoint, a: CropPoint, b: CropPoint): Double {
        val v1x = (a.x - vertex.x).toDouble()
        val v1y = (a.y - vertex.y).toDouble()
        val v2x = (b.x - vertex.x).toDouble()
        val v2y = (b.y - vertex.y).toDouble()
        val len1 = hypot(v1x, v1y)
        val len2 = hypot(v2x, v2y)
        if (len1 == 0.0 || len2 == 0.0) return 0.0
        val cos = ((v1x * v2x + v1y * v2y) / (len1 * len2)).coerceIn(-1.0, 1.0)
        return acos(cos) * 180.0 / PI
    }

    fun contains(points: List<CropPoint>, x: Float, y: Float): Boolean {
        // Ray casting
        var inside = false
        var j = 3
        for (i in 0 until 4) {
            val pi = points[i]
            val pj = points[j]
            val intersect = ((pi.y > y) != (pj.y > y)) &&
                (x < (pj.x - pi.x) * (y - pi.y) / (pj.y - pi.y + 1e-12f) + pi.x)
            if (intersect) inside = !inside
            j = i
        }
        return inside
    }

    fun distance(a: CropPoint, b: CropPoint): Float = hypot(b.x - a.x, b.y - a.y)

    fun pointToSegmentDistance(px: Float, py: Float, ax: Float, ay: Float, bx: Float, by: Float): Float {
        val dx = bx - ax
        val dy = by - ay
        val len2 = dx * dx + dy * dy
        if (len2 < 1e-6f) return hypot(px - ax, py - ay)
        var t = ((px - ax) * dx + (py - ay) * dy) / len2
        t = t.coerceIn(0f, 1f)
        val qx = ax + t * dx
        val qy = ay + t * dy
        return hypot(px - qx, py - qy)
    }

    /**
     * 边命中距离：投影须落在线段（或 [projectionPad] 外延）上，返回到投影点距离；未命中返回 +∞。
     * [projectionPad] ≤ 0 → 严格线段；> 0 → 沿边两端各外延 pad（图像素）。
     */
    fun edgeHitDistance(
        px: Float,
        py: Float,
        ax: Float,
        ay: Float,
        bx: Float,
        by: Float,
        projectionPad: Float,
    ): Float {
        val dx = bx - ax
        val dy = by - ay
        val len2 = dx * dx + dy * dy
        if (len2 < 1e-6f) return hypot(px - ax, py - ay)
        val len = hypot(dx, dy)
        val t = ((px - ax) * dx + (py - ay) * dy) / len2
        val tMin: Float
        val tMax: Float
        if (projectionPad <= 0f) {
            tMin = 0f
            tMax = 1f
        } else {
            val padT = projectionPad / len
            tMin = -padT
            tMax = 1f + padT
        }
        if (t < tMin || t > tMax) return Float.POSITIVE_INFINITY
        val qx = ax + t * dx
        val qy = ay + t * dy
        return hypot(px - qx, py - qy)
    }

    /**
     * 边拖两端同增：将 (dx,dy) 钳进「两端同时不出图界」的可行区间（交集饱和）。
     * 对齐 freequadcrop [QuadrilateralCropUtils.clampEdgeTranslate]。
     */
    fun clampEdgeTranslate(
        p0: CropPoint,
        p1: CropPoint,
        dx: Float,
        dy: Float,
        imgW: Float,
        imgH: Float,
    ): CropPoint {
        val minDx = max(-p0.x, -p1.x)
        val maxDx = min(imgW - p0.x, imgW - p1.x)
        val outDx = if (minDx <= maxDx) dx.coerceIn(minDx, maxDx) else dx
        val minDy = max(-p0.y, -p1.y)
        val maxDy = min(imgH - p0.y, imgH - p1.y)
        val outDy = if (minDy <= maxDy) dy.coerceIn(minDy, maxDy) else dy
        return CropPoint(outDx, outDy)
    }

    /** 两端点加同一位移后是否都在图内（含半像素容差与 [isValidQuad] 边界一致）。 */
    fun bothEndsInside(
        p0: CropPoint,
        p1: CropPoint,
        dx: Float,
        dy: Float,
        imgW: Float,
        imgH: Float,
    ): Boolean {
        val a = p0 + CropPoint(dx, dy)
        val b = p1 + CropPoint(dx, dy)
        return a.x >= -0.5f && a.y >= -0.5f && a.x <= imgW + 0.5f && a.y <= imgH + 0.5f &&
            b.x >= -0.5f && b.y >= -0.5f && b.x <= imgW + 0.5f && b.y <= imgH + 0.5f
    }

    fun edgeMidpoints(points: List<CropPoint>): List<CropPoint> {
        require(points.size == 4)
        return listOf(
            CropPoint((points[0].x + points[1].x) / 2f, (points[0].y + points[1].y) / 2f),
            CropPoint((points[1].x + points[2].x) / 2f, (points[1].y + points[2].y) / 2f),
            CropPoint((points[2].x + points[3].x) / 2f, (points[2].y + points[3].y) / 2f),
            CropPoint((points[3].x + points[0].x) / 2f, (points[3].y + points[0].y) / 2f),
        )
    }

    /** 全图内缩 inset 比例（0~0.45）生成初始四点。 */
    fun insetFullQuad(imageW: Float, imageH: Float, insetRatio: Float = 0.08f): List<CropPoint> {
        val ix = imageW * insetRatio.coerceIn(0f, 0.45f)
        val iy = imageH * insetRatio.coerceIn(0f, 0.45f)
        return listOf(
            CropPoint(ix, iy),
            CropPoint(imageW - ix, iy),
            CropPoint(imageW - ix, imageH - iy),
            CropPoint(ix, imageH - iy),
        )
    }

    /**
     * 在 [from → to] 线段上找最远合法点（软钳制）。
     */
    fun farthestValid(
        from: CropPoint,
        to: CropPoint,
        iterations: Int = 12,
        isValid: (CropPoint) -> Boolean,
    ): CropPoint {
        if (isValid(to)) return to
        var lo = 0f
        var hi = 1f
        var best = from
        if (!isValid(from)) {
            // 扫描种子
            for (i in 1..16) {
                val t = i / 16f
                val p = lerp(from, to, t)
                if (isValid(p)) {
                    best = p
                    lo = t
                    break
                }
            }
            if (!isValid(best)) return from
        }
        repeat(iterations) {
            val mid = (lo + hi) / 2f
            val p = lerp(from, to, mid)
            if (isValid(p)) {
                best = p
                lo = mid
            } else {
                hi = mid
            }
        }
        return best
    }

    fun lerp(a: CropPoint, b: CropPoint, t: Float): CropPoint =
        CropPoint(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

    fun axisAlignedRect(points: List<CropPoint>): List<CropPoint> {
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        return listOf(
            CropPoint(minX, minY),
            CropPoint(maxX, minY),
            CropPoint(maxX, maxY),
            CropPoint(minX, maxY),
        )
    }

    fun rotateClockwise90(points: List<CropPoint>, imageW: Float, imageH: Float): Pair<List<CropPoint>, Pair<Float, Float>> {
        // (x,y) -> (imageH - y, x); new size (imageH, imageW)
        val rotated = points.map { CropPoint(imageH - it.y, it.x) }
        // reorder to keep TL-TR-BR-BL visual after rotation
        val ordered = listOf(rotated[3], rotated[0], rotated[1], rotated[2])
        return ordered to (imageH to imageW)
    }
}
