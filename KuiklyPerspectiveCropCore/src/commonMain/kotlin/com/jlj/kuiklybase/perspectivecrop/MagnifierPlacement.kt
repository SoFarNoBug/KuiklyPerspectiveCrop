package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 放大镜圆镜在视图中的摆放策略（Q32）。
 */
enum class MagnifierPlacement {
    /** 顶左；触点靠近左上时翻到顶右（对齐旧库 DefaultQuadCropMagnifierDrawer）。 */
    TopAutoFlip,

    /** 固定顶左。 */
    TopLeft,

    /** 固定顶右。 */
    TopRight,

    /** 圆心在触点上方，并钳制在画布内。 */
    AboveTouch,
}
