package com.jlj.kuiklybase.perspectivecrop

/**
 * 选区状态机：四点、命中、凸性/矩形约束。与 Compose 无关。
 */
class CropSession(
    initialConfig: PerspectiveCropConfig = PerspectiveCropConfig(),
) {
    var config: PerspectiveCropConfig = initialConfig
        private set

    var imageWidth: Float = 0f
        private set
    var imageHeight: Float = 0f
        private set

    /** 图像素空间四点，顺序 TL-TR-BR-BL。 */
    var points: List<CropPoint> = emptyList()
        private set

    /** UI 旋转档位：0/1/2/3 × 90°（顺时针）。像素旋转留给 M2。 */
    var uiRotationSteps: Int = 0
        private set

    private var activeHit: HitTarget = HitTarget.None
    private var lastTouchImage: CropPoint? = null

    fun updateConfig(config: PerspectiveCropConfig) {
        this.config = config
        if (config.rectMode && points.size == 4) {
            points = CropGeometry.axisAlignedRect(points)
        }
    }

    fun setImageSize(width: Float, height: Float, resetSelection: Boolean = true) {
        if (width <= 0f || height <= 0f) return
        imageWidth = width
        imageHeight = height
        if (resetSelection || points.size != 4) {
            points = CropGeometry.insetFullQuad(width, height)
            if (config.rectMode) {
                points = CropGeometry.axisAlignedRect(points)
            }
            uiRotationSteps = 0
            lastTouchImage = null
            activeHit = HitTarget.None
        }
    }

    /** 换图或清空：丢掉选区与 UI 旋转档。 */
    fun clearImage() {
        imageWidth = 0f
        imageHeight = 0f
        points = emptyList()
        uiRotationSteps = 0
        lastTouchImage = null
        activeHit = HitTarget.None
    }

    fun setPoints(newPoints: List<CropPoint>) {
        require(newPoints.size == 4)
        points = if (config.rectMode) CropGeometry.axisAlignedRect(newPoints) else newPoints.toList()
    }

    fun resetFullImage() {
        if (imageWidth <= 0f || imageHeight <= 0f) return
        points = CropGeometry.insetFullQuad(imageWidth, imageHeight, insetRatio = 0f)
        // 全图贴边
        points = listOf(
            CropPoint(0f, 0f),
            CropPoint(imageWidth, 0f),
            CropPoint(imageWidth, imageHeight),
            CropPoint(0f, imageHeight),
        )
    }

    fun rotateClockwiseUi() {
        if (imageWidth <= 0f || imageHeight <= 0f || points.size != 4) return
        val (rotated, size) = CropGeometry.rotateClockwise90(points, imageWidth, imageHeight)
        points = rotated
        imageWidth = size.first
        imageHeight = size.second
        uiRotationSteps = (uiRotationSteps + 1) % 4
        lastTouchImage = null
        activeHit = HitTarget.None
    }

    fun canCrop(): Boolean {
        if (points.size != 4 || imageWidth <= 0f || imageHeight <= 0f) return false
        return CropGeometry.isValidQuad(
            points, imageWidth, imageHeight,
            config.minCropWidth, config.minAngleLimit,
        )
    }

    /**
     * @param viewToImageScale [ImageFitMapping.scale]（view = image * scale）；命中半径配置按**视图像素**，内部除以 scale。
     */
    fun hitTest(imageX: Float, imageY: Float, viewToImageScale: Float = 1f): HitTarget {
        if (points.size != 4) return HitTarget.None
        val s = viewToImageScale.coerceAtLeast(1e-3f)
        val cornerCatch = config.touchCornerCatchDistance / s
        val edgeCatch = config.touchEdgeCatchDistance / s
        val midCatch = config.touchMidCatchDistance / s

        if (cornerCatch > 0f) {
            val corners = listOf(
                HitTarget.Corner0, HitTarget.Corner1, HitTarget.Corner2, HitTarget.Corner3,
            )
            var best: HitTarget = HitTarget.None
            var bestD = cornerCatch
            for (i in 0 until 4) {
                val d = CropGeometry.distance(points[i], CropPoint(imageX, imageY))
                if (d <= bestD) {
                    bestD = d
                    best = corners[i]
                }
            }
            if (best != HitTarget.None) return best
        }

        if (config.showEdgeMidPoints && midCatch > 0f) {
            val mids = CropGeometry.edgeMidpoints(points)
            val midTargets = listOf(
                HitTarget.MidTop, HitTarget.MidRight, HitTarget.MidBottom, HitTarget.MidLeft,
            )
            var best: HitTarget = HitTarget.None
            var bestD = midCatch
            for (i in 0 until 4) {
                val d = CropGeometry.distance(mids[i], CropPoint(imageX, imageY))
                if (d <= bestD) {
                    bestD = d
                    best = midTargets[i]
                }
            }
            if (best != HitTarget.None) return best
        }

        if (edgeCatch > 0f) {
            val edges = listOf(
                Triple(HitTarget.EdgeTop, 0, 1),
                Triple(HitTarget.EdgeRight, 1, 2),
                Triple(HitTarget.EdgeBottom, 2, 3),
                Triple(HitTarget.EdgeLeft, 3, 0),
            )
            var best: HitTarget = HitTarget.None
            var bestD = edgeCatch
            for ((target, a, b) in edges) {
                val d = CropGeometry.pointToSegmentDistance(
                    imageX, imageY,
                    points[a].x, points[a].y,
                    points[b].x, points[b].y,
                )
                if (d <= bestD) {
                    bestD = d
                    best = target
                }
            }
            if (best != HitTarget.None) return best
        }

        if (config.insideDragEnabled && CropGeometry.contains(points, imageX, imageY)) {
            return HitTarget.Inside
        }
        return HitTarget.None
    }

    fun onDragStart(imageX: Float, imageY: Float, viewToImageScale: Float = 1f): HitTarget {
        activeHit = hitTest(imageX, imageY, viewToImageScale)
        lastTouchImage = CropPoint(imageX, imageY)
        return activeHit
    }

    fun onDrag(imageX: Float, imageY: Float) {
        if (activeHit == HitTarget.None || points.size != 4) return
        val touch = CropPoint(imageX, imageY)
        val last = lastTouchImage ?: touch
        when {
            activeHit.isCorner() -> moveCorner(activeHit, touch)
            activeHit.isMid() || activeHit.isEdge() -> moveEdge(activeHit, touch, last)
            activeHit == HitTarget.Inside -> moveInside(touch, last)
            else -> Unit
        }
        lastTouchImage = touch
    }

    fun onDragEnd() {
        activeHit = HitTarget.None
        lastTouchImage = null
    }

    private fun isValidCandidate(candidate: List<CropPoint>): Boolean =
        CropGeometry.isValidQuad(
            candidate, imageWidth, imageHeight,
            config.minCropWidth, config.minAngleLimit,
        )

    private fun moveCorner(hit: HitTarget, touch: CropPoint) {
        val index = when (hit) {
            HitTarget.Corner0 -> 0
            HitTarget.Corner1 -> 1
            HitTarget.Corner2 -> 2
            HitTarget.Corner3 -> 3
            else -> return
        }
        if (config.rectMode) {
            moveRectCorner(index, touch)
            return
        }
        val current = points[index]
        val desired = touch.coerceIn(0f, 0f, imageWidth, imageHeight)
        val accepted = CropGeometry.farthestValid(current, desired) { p ->
            val next = points.toMutableList()
            next[index] = p
            isValidCandidate(next)
        }
        val next = points.toMutableList()
        next[index] = accepted
        points = next
    }

    private fun moveRectCorner(index: Int, touch: CropPoint) {
        val t = touch.coerceIn(0f, 0f, imageWidth, imageHeight)
        val cur = CropGeometry.axisAlignedRect(points)
        val minX = cur[0].x
        val maxX = cur[1].x
        val minY = cur[0].y
        val maxY = cur[2].y
        val minW = config.minCropWidth
        var nMinX = minX
        var nMaxX = maxX
        var nMinY = minY
        var nMaxY = maxY
        when (index) {
            0 -> { // TL
                nMinX = t.x.coerceAtMost(maxX - minW)
                nMinY = t.y.coerceAtMost(maxY - minW)
            }
            1 -> { // TR
                nMaxX = t.x.coerceAtLeast(minX + minW)
                nMinY = t.y.coerceAtMost(maxY - minW)
            }
            2 -> { // BR
                nMaxX = t.x.coerceAtLeast(minX + minW)
                nMaxY = t.y.coerceAtLeast(minY + minW)
            }
            3 -> { // BL
                nMinX = t.x.coerceAtMost(maxX - minW)
                nMaxY = t.y.coerceAtLeast(minY + minW)
            }
        }
        nMinX = nMinX.coerceIn(0f, imageWidth)
        nMaxX = nMaxX.coerceIn(0f, imageWidth)
        nMinY = nMinY.coerceIn(0f, imageHeight)
        nMaxY = nMaxY.coerceIn(0f, imageHeight)
        if (nMaxX - nMinX < minW || nMaxY - nMinY < minW) return
        points = listOf(
            CropPoint(nMinX, nMinY),
            CropPoint(nMaxX, nMinY),
            CropPoint(nMaxX, nMaxY),
            CropPoint(nMinX, nMaxY),
        )
    }

    private fun moveEdge(hit: HitTarget, touch: CropPoint, last: CropPoint) {
        val (i0, i1) = edgeIndices(hit) ?: return
        var dx = touch.x - last.x
        var dy = touch.y - last.y

        if (config.rectMode) {
            // 轴对齐：只沿法向移动
            when (hit) {
                HitTarget.EdgeTop, HitTarget.MidTop, HitTarget.EdgeBottom, HitTarget.MidBottom -> dx = 0f
                HitTarget.EdgeLeft, HitTarget.MidLeft, HitTarget.EdgeRight, HitTarget.MidRight -> dy = 0f
                else -> Unit
            }
            if (config.edgeSnapToTouchEnabled) {
                when (hit) {
                    HitTarget.EdgeTop, HitTarget.MidTop -> {
                        val y = touch.y.coerceIn(0f, points[2].y - config.minCropWidth)
                        applyRectEdgeY(top = true, y = y)
                        return
                    }
                    HitTarget.EdgeBottom, HitTarget.MidBottom -> {
                        val y = touch.y.coerceIn(points[0].y + config.minCropWidth, imageHeight)
                        applyRectEdgeY(top = false, y = y)
                        return
                    }
                    HitTarget.EdgeLeft, HitTarget.MidLeft -> {
                        val x = touch.x.coerceIn(0f, points[1].x - config.minCropWidth)
                        applyRectEdgeX(left = true, x = x)
                        return
                    }
                    HitTarget.EdgeRight, HitTarget.MidRight -> {
                        val x = touch.x.coerceIn(points[0].x + config.minCropWidth, imageWidth)
                        applyRectEdgeX(left = false, x = x)
                        return
                    }
                    else -> Unit
                }
            }
        }

        val candidate = points.toMutableList()
        candidate[i0] = (candidate[i0] + CropPoint(dx, dy)).coerceIn(0f, 0f, imageWidth, imageHeight)
        candidate[i1] = (candidate[i1] + CropPoint(dx, dy)).coerceIn(0f, 0f, imageWidth, imageHeight)
        if (config.rectMode) {
            points = CropGeometry.axisAlignedRect(candidate)
            if (config.edgeClampToBoundsEnabled) {
                points = points.map { it.coerceIn(0f, 0f, imageWidth, imageHeight) }
                points = CropGeometry.axisAlignedRect(points)
            }
            return
        }
        if (isValidCandidate(candidate)) {
            points = candidate
        }
    }

    private fun applyRectEdgeY(top: Boolean, y: Float) {
        val cur = CropGeometry.axisAlignedRect(points)
        val minX = cur[0].x
        val maxX = cur[1].x
        val minY = if (top) y else cur[0].y
        val maxY = if (top) cur[2].y else y
        if (maxY - minY < config.minCropWidth) return
        var ny0 = minY
        var ny1 = maxY
        if (config.edgeClampToBoundsEnabled) {
            ny0 = ny0.coerceIn(0f, imageHeight)
            ny1 = ny1.coerceIn(0f, imageHeight)
        }
        points = listOf(
            CropPoint(minX, ny0),
            CropPoint(maxX, ny0),
            CropPoint(maxX, ny1),
            CropPoint(minX, ny1),
        )
    }

    private fun applyRectEdgeX(left: Boolean, x: Float) {
        val cur = CropGeometry.axisAlignedRect(points)
        val minY = cur[0].y
        val maxY = cur[2].y
        val minX = if (left) x else cur[0].x
        val maxX = if (left) cur[1].x else x
        if (maxX - minX < config.minCropWidth) return
        var nx0 = minX
        var nx1 = maxX
        if (config.edgeClampToBoundsEnabled) {
            nx0 = nx0.coerceIn(0f, imageWidth)
            nx1 = nx1.coerceIn(0f, imageWidth)
        }
        points = listOf(
            CropPoint(nx0, minY),
            CropPoint(nx1, minY),
            CropPoint(nx1, maxY),
            CropPoint(nx0, maxY),
        )
    }

    private fun moveInside(touch: CropPoint, last: CropPoint) {
        val dx = touch.x - last.x
        val dy = touch.y - last.y
        val moved = points.map { it + CropPoint(dx, dy) }
        val minX = moved.minOf { it.x }
        val maxX = moved.maxOf { it.x }
        val minY = moved.minOf { it.y }
        val maxY = moved.maxOf { it.y }
        var ox = 0f
        var oy = 0f
        if (minX < 0f) ox = -minX
        if (maxX > imageWidth) ox = imageWidth - maxX
        if (minY < 0f) oy = -minY
        if (maxY > imageHeight) oy = imageHeight - maxY
        val shifted = moved.map { CropPoint(it.x + ox, it.y + oy) }
        if (isValidCandidate(shifted) || config.rectMode) {
            points = if (config.rectMode) CropGeometry.axisAlignedRect(shifted) else shifted
        }
    }

    private fun edgeIndices(hit: HitTarget): Pair<Int, Int>? = when (hit) {
        HitTarget.EdgeTop, HitTarget.MidTop -> 0 to 1
        HitTarget.EdgeRight, HitTarget.MidRight -> 1 to 2
        HitTarget.EdgeBottom, HitTarget.MidBottom -> 2 to 3
        HitTarget.EdgeLeft, HitTarget.MidLeft -> 3 to 0
        else -> null
    }
}
