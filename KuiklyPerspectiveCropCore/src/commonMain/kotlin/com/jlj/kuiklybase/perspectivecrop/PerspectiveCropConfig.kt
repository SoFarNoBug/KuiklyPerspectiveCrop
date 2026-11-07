package com.jlj.kuiklybase.perspectivecrop

/**
 * 样式与行为旋钮。默认值对齐 [docs/decisions.md]（Q7/Q9/Q14/Q19/Q26/Q29/Q31）。
 *
 * Q9：`edgeSnapToTouchEnabled` / `edgeClampToBoundsEnabled` 默认开，**M1 必须接线**，
 * 不是「仅 rectMode」或拖到 M3 才暴露。
 */
data class PerspectiveCropConfig(
    /** 自由凸四边形为默认；true 时为矩形模式（Q7）。 */
    val rectMode: Boolean = false,
    /** 边拖吸附手指（默认开，Q9；M1 接线）。 */
    val edgeSnapToTouchEnabled: Boolean = true,
    /**
     * 边拖越界钳制（默认开，Q9；自由 / 矩形均生效）。
     * 开：两端位移取交集饱和（齐步贴边、不斜边）；关（自由）：越界整帧丢弃。
     */
    val edgeClampToBoundsEnabled: Boolean = true,
    /** 框内拖动整框。 */
    val insideDragEnabled: Boolean = true,
    /**
     * DOWN 未命中锚点时透传父容器（Q26）。默认 false。
     */
    val unhitPassthrough: Boolean = false,
    /** 角点命中半径（**视图像素**，内部按 Fit scale 换算）；≤0 关闭。 */
    val touchCornerCatchDistance: Float = 48f,
    /** 边命中带宽（视图像素）；≤0 关闭。 */
    val touchEdgeCatchDistance: Float = 28f,
    /**
     * 边命中投影外延（视图逻辑像素，默认 100；对齐 freequadcrop）。
     * 命中时除以 Fit scale 进图像坐标；≤0 为严格线段（无外延）。
     */
    val touchEdgeProjectionError: Float = 100f,
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
    /** 是否绘制九宫格辅助线（整图 Fit 矩形；对齐 freequadcrop）。 */
    val showGuideLines: Boolean = false,
    val maskAlpha: Float = 0.55f,
    val borderWidthPx: Float = 2f,
    val borderColor: Long = 0xFFFF3B30,
    val cornerRadiusPx: Float = 8f,
    val cornerColor: Long = 0xFFFF3B30,
    /** 辅助线宽度（视图 px）。 */
    val guideLineWidth: Float = 1f,
    /** 辅助线颜色 ARGB。 */
    val guideLineColor: Long = 0x59FFFFFF,
    /** 放大镜半径占视图宽度比例（Q29）。 */
    val magnifierRadiusRatio: Float = 0.07f,
    /** 放大镜边框宽度（视图 px）。 */
    val magnifierBorderWidth: Float = 2f,
    /** 放大镜边框颜色 ARGB。 */
    val magnifierBorderColor: Long = 0xFFFFFFFF,
    /** 是否绘制放大镜中心十字。 */
    val showMagnifierCenterCross: Boolean = false,
    /**
     * 放大镜圆内绘制裁剪框线镜像（默认开；对齐 freequadcrop need2DrawInsideAreaLineMirror）。
     * 样式复用 [borderColor] / [borderWidthPx]。
     */
    val showMagnifierCropLineMirror: Boolean = true,
    val magnifierCrossColor: Long = 0xFFFFFFFF,
    val magnifierCrossLineWidth: Float = 3f,
    val magnifierCrossLineLength: Float = 15f,
    /**
     * 圆镜摆放（Q32）；默认 [MagnifierPlacement.TopAutoFlip] 对齐旧库顶角自动翻转。
     */
    val magnifierPlacement: MagnifierPlacement = MagnifierPlacement.TopAutoFlip,
    /**
     * 降采样采样缓存最长边（px）；默认 2048（Q31）。
     * ≤0 时按 2048 处理。
     */
    val magnifierCacheMaxEdgePx: Int = 2048,
    /** 出图落盘格式（Q19）；默认 JPEG。 */
    val outputFormat: CropOutputFormat = CropOutputFormat.Jpeg,
    /** JPEG 质量 0–100（仅 [CropOutputFormat.Jpeg] 生效）；默认 92。 */
    val jpegQuality: Int = 92,
)
