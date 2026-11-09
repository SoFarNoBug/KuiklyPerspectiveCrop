package com.jlj.kuiklybase.perspectivecrop

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 放大镜绘制输入：降采样缓存裁出的 ARGB 像素块 + 视口几何（Q25）。
 */
data class MagnifierPatch(
    /** 行优先 ARGB8888（非预乘）。 */
    val argbPixels: IntArray,
    val width: Int,
    val height: Int,
    /** 触点（视图坐标）。 */
    val touchViewX: Float,
    val touchViewY: Float,
    /** 圆镜半径（视图 px）。 */
    val displayRadiusPx: Float,
    /**
     * 圆镜内 drawRect 网格边长（拖动粗 / 停指密，方案 B）。
     * 仅影响绘制密度，不改变 [argbPixels]。
     */
    val drawGrid: Int = MagnifierDrawGrid.DRAG,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as MagnifierPatch
        if (width != other.width) return false
        if (height != other.height) return false
        if (touchViewX != other.touchViewX) return false
        if (touchViewY != other.touchViewY) return false
        if (displayRadiusPx != other.displayRadiusPx) return false
        if (drawGrid != other.drawGrid) return false
        if (!argbPixels.contentEquals(other.argbPixels)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = argbPixels.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + touchViewX.hashCode()
        result = 31 * result + touchViewY.hashCode()
        result = 31 * result + displayRadiusPx.hashCode()
        result = 31 * result + drawGrid
        return result
    }
}

/** 放大镜网格密度：拖动保帧，停指提清（仅网格兜底路径；Android 原生贴图不用）。 */
object MagnifierDrawGrid {
    /**
     * 拖动网格边长。12 在鸿蒙 KN Compose 上仍卡；6≈36 次 drawRect。
     * Android 主路径走 BitmapShader，不受此影响。
     */
    const val DRAG: Int = 6
    /** 停指升密；32 在鸿蒙上过重，16 折中。 */
    const val SETTLED: Int = 16
    const val SETTLE_DELAY_MS: Long = 160L
}
