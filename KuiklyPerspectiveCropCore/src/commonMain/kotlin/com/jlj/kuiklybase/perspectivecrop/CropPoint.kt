package com.jlj.kuiklybase.perspectivecrop

/**
 * 选区角点，坐标在**当前显示图像素空间**（与 [CropSession.imageWidth]/[CropSession.imageHeight] 一致）。
 *
 * UI 旋转后 Session 会变换四点并对调宽高；预览用 Stage `graphicsLayer`，出图按 Q17 先旋源图再透视。
 * 约定顺序：左上(0) → 右上(1) → 右下(2) → 左下(3)。
 */
data class CropPoint(val x: Float, val y: Float) {
    operator fun plus(other: CropPoint): CropPoint = CropPoint(x + other.x, y + other.y)
    operator fun minus(other: CropPoint): CropPoint = CropPoint(x - other.x, y - other.y)
    fun coerceIn(minX: Float, minY: Float, maxX: Float, maxY: Float): CropPoint =
        CropPoint(x.coerceIn(minX, maxX), y.coerceIn(minY, maxY))
}

enum class HitTarget {
    Corner0, Corner1, Corner2, Corner3,
    EdgeTop, EdgeRight, EdgeBottom, EdgeLeft,
    MidTop, MidRight, MidBottom, MidLeft,
    Inside,
    None,
}

fun HitTarget.isCorner(): Boolean = this == HitTarget.Corner0 || this == HitTarget.Corner1 ||
    this == HitTarget.Corner2 || this == HitTarget.Corner3

fun HitTarget.isEdge(): Boolean = this == HitTarget.EdgeTop || this == HitTarget.EdgeRight ||
    this == HitTarget.EdgeBottom || this == HitTarget.EdgeLeft

fun HitTarget.isMid(): Boolean = this == HitTarget.MidTop || this == HitTarget.MidRight ||
    this == HitTarget.MidBottom || this == HitTarget.MidLeft
