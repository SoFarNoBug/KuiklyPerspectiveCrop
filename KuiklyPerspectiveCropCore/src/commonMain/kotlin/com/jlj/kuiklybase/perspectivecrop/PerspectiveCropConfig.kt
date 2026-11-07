package com.jlj.kuiklybase.perspectivecrop

/**
 * 样式与行为旋钮。默认值对齐 [docs/decisions.md]（Q7/Q9/Q14/Q19）。
 *
 * Q9：`edgeSnapToTouchEnabled` / `edgeClampToBoundsEnabled` 默认开，**M1 必须接线**，
 * 不是「仅 rectMode」或拖到 M3 才暴露。
 */
data class PerspectiveCropConfig(
    /** 自由凸四边形为默认；true 时为矩形模式（Q7）。 */
    val rectMode: Boolean = false,
    /** 边拖吸附手指（默认开，Q9；M1 接线）。 */
    val edgeSnapToTouchEnabled: Boolean = true,
    /** 边拖越界钳制（默认开，Q9；M1 接线）。 */
    val edgeClampToBoundsEnabled: Boolean = true,
    /** 框内拖动整框。 */
    val insideDragEnabled: Boolean = true,
    /** 角点命中半径（**视图像素**，内部按 Fit scale 换算）；≤0 关闭。 */
    val touchCornerCatchDistance: Float = 48f,
    /** 边命中带宽（视图像素）；≤0 关闭。 */
    val touchEdgeCatchDistance: Float = 28f,
    /** 边中点命中半径（视图像素）；≤0 关闭。 */
    val touchMidCatchDistance: Float = 36f,
    /** 最小边长（图像素）。 */
    val minCropWidth: Float = 48f,
    /** 最小内角（度）；0 表示不校验。 */
    val minAngleLimit: Float = 20f,
    /** 是否绘制放大镜（真像素实现在 M3，属 v0.1 / Q14）。 */
    val showMagnifier: Boolean = true,
    /** 是否绘制边中点（默认关）。 */
    val showEdgeMidPoints: Boolean = false,
    /** 是否绘制九宫格辅助线。 */
    val showGuideLines: Boolean = false,
    val maskAlpha: Float = 0.55f,
    val borderWidthPx: Float = 2f,
    val borderColor: Long = 0xFFFF3B30,
    val cornerRadiusPx: Float = 8f,
    val cornerColor: Long = 0xFFFF3B30,
    /** 出图落盘格式（Q19）；默认 JPEG。 */
    val outputFormat: CropOutputFormat = CropOutputFormat.Jpeg,
    /** JPEG 质量 0–100（仅 [CropOutputFormat.Jpeg] 生效）；默认 92。 */
    val jpegQuality: Int = 92,
)
