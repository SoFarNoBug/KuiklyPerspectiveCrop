package com.jlj.kuiklybase.perspectivecrop

import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

/** 输出最长边硬顶（Q35）。 */
internal const val JS_CROP_MAX_EDGE = 4096

/**
 * 计算将目标矩形像素映射到源四边形的 3×3 单应（行优先）。
 * dest: (0,0)-(w,0)-(w,h)-(0,h) → src points[0..3]
 */
internal fun homographyDestToSrc(
    src: List<CropPoint>,
    destW: Int,
    destH: Int,
): DoubleArray {
    require(src.size == 4)
    val dx = doubleArrayOf(0.0, destW.toDouble(), destW.toDouble(), 0.0)
    val dy = doubleArrayOf(0.0, 0.0, destH.toDouble(), destH.toDouble())
    val sx = DoubleArray(4) { src[it].x.toDouble() }
    val sy = DoubleArray(4) { src[it].y.toDouble() }
    return solveHomography(dx, dy, sx, sy)
}

/**
 * 8 点对应求解 H（映射 (x,y)→(u,v)），返回 9 元行优先（h22=1）。
 */
private fun solveHomography(
    x: DoubleArray,
    y: DoubleArray,
    u: DoubleArray,
    v: DoubleArray,
): DoubleArray {
    // 8x9 增广：对每点两行
    val a = Array(8) { DoubleArray(9) }
    for (i in 0 until 4) {
        val xi = x[i]
        val yi = y[i]
        val ui = u[i]
        val vi = v[i]
        val r0 = i * 2
        val r1 = r0 + 1
        a[r0][0] = xi
        a[r0][1] = yi
        a[r0][2] = 1.0
        a[r0][3] = 0.0
        a[r0][4] = 0.0
        a[r0][5] = 0.0
        a[r0][6] = -ui * xi
        a[r0][7] = -ui * yi
        a[r0][8] = -ui
        a[r1][0] = 0.0
        a[r1][1] = 0.0
        a[r1][2] = 0.0
        a[r1][3] = xi
        a[r1][4] = yi
        a[r1][5] = 1.0
        a[r1][6] = -vi * xi
        a[r1][7] = -vi * yi
        a[r1][8] = -vi
    }
    // 高斯消元求 null space（最后一列为 rhs 形式：把 h8 移到右边 → 解 8 元，h8=1）
    // 改写为 8x8 * h0..h7 = -col8（当 h22=1 时 col8 是常数项）
    val m = Array(8) { DoubleArray(9) }
    for (r in 0 until 8) {
        for (c in 0 until 9) m[r][c] = a[r][c]
    }
    // 将方程 A[:,0:8] * h[0:8] = -A[:,8] * h8，设 h8=1
    for (r in 0 until 8) {
        m[r][8] = -m[r][8]
    }
    gaussianSolve(m)
    val h = DoubleArray(9)
    for (i in 0 until 8) h[i] = m[i][8]
    h[8] = 1.0
    return h
}

/** 原地解 8x9 增广矩阵（最后一列为 RHS）。 */
private fun gaussianSolve(m: Array<DoubleArray>) {
    val n = 8
    for (col in 0 until n) {
        var pivot = col
        var best = kotlin.math.abs(m[col][col])
        for (r in col + 1 until n) {
            val v = kotlin.math.abs(m[r][col])
            if (v > best) {
                best = v
                pivot = r
            }
        }
        if (best < 1e-12) {
            throw CropException(CropException.TRANSFORM_FAILED, "homography matrix singular")
        }
        if (pivot != col) {
            val tmp = m[col]
            m[col] = m[pivot]
            m[pivot] = tmp
        }
        val div = m[col][col]
        for (c in col until n + 1) m[col][c] /= div
        for (r in 0 until n) {
            if (r == col) continue
            val f = m[r][col]
            if (f == 0.0) continue
            for (c in col until n + 1) m[r][c] -= f * m[col][c]
        }
    }
}

internal fun applyHomography(h: DoubleArray, x: Double, y: Double): Pair<Double, Double> {
    val w = h[6] * x + h[7] * y + h[8]
    if (kotlin.math.abs(w) < 1e-12) {
        return Pair(Double.NaN, Double.NaN)
    }
    val u = (h[0] * x + h[1] * y + h[2]) / w
    val v = (h[3] * x + h[4] * y + h[5]) / w
    return Pair(u, v)
}

internal fun perspectiveWarpArgb(
    srcArgb: IntArray,
    srcW: Int,
    srcH: Int,
    pointsInSrcSpace: List<CropPoint>,
): Triple<IntArray, Int, Int> {
    val width = max(
        1,
        hypot(
            (pointsInSrcSpace[1].x - pointsInSrcSpace[0].x).toDouble(),
            (pointsInSrcSpace[1].y - pointsInSrcSpace[0].y).toDouble(),
        ).roundToInt(),
    )
    val height = max(
        1,
        hypot(
            (pointsInSrcSpace[3].x - pointsInSrcSpace[0].x).toDouble(),
            (pointsInSrcSpace[3].y - pointsInSrcSpace[0].y).toDouble(),
        ).roundToInt(),
    )
    if (max(width, height) > JS_CROP_MAX_EDGE) {
        throw CropException(
            CropException.TRANSFORM_FAILED,
            "output edge ${max(width, height)} exceeds hard cap $JS_CROP_MAX_EDGE",
        )
    }
    val h = homographyDestToSrc(pointsInSrcSpace, width, height)
    val out = IntArray(width * height)
    var i = 0
    for (y in 0 until height) {
        for (x in 0 until width) {
            val (sx, sy) = applyHomography(h, x + 0.5, y + 0.5)
            out[i++] = if (sx.isNaN() || sy.isNaN()) {
                0
            } else {
                sampleBilinearArgb(srcArgb, srcW, srcH, sx, sy)
            }
        }
    }
    return Triple(out, width, height)
}

internal fun sampleBilinearArgb(argb: IntArray, w: Int, h: Int, x: Double, y: Double): Int {
    if (x < 0 || y < 0 || x >= w - 1 || y >= h - 1) {
        val ix = x.toInt().coerceIn(0, w - 1)
        val iy = y.toInt().coerceIn(0, h - 1)
        return argb[iy * w + ix]
    }
    val x0 = floor(x).toInt()
    val y0 = floor(y).toInt()
    val x1 = x0 + 1
    val y1 = y0 + 1
    val fx = x - x0
    val fy = y - y0
    val c00 = argb[y0 * w + x0]
    val c10 = argb[y0 * w + x1]
    val c01 = argb[y1 * w + x0]
    val c11 = argb[y1 * w + x1]
    return lerpArgb(lerpArgb(c00, c10, fx), lerpArgb(c01, c11, fx), fy)
}

private fun lerpArgb(a: Int, b: Int, t: Double): Int {
    val ar = (a ushr 16) and 0xFF
    val ag = (a ushr 8) and 0xFF
    val ab = a and 0xFF
    val aa = (a ushr 24) and 0xFF
    val br = (b ushr 16) and 0xFF
    val bg = (b ushr 8) and 0xFF
    val bb = b and 0xFF
    val ba = (b ushr 24) and 0xFF
    val r = (ar + (br - ar) * t).roundToInt().coerceIn(0, 255)
    val g = (ag + (bg - ag) * t).roundToInt().coerceIn(0, 255)
    val bl = (ab + (bb - ab) * t).roundToInt().coerceIn(0, 255)
    val al = (aa + (ba - aa) * t).roundToInt().coerceIn(0, 255)
    return (al shl 24) or (r shl 16) or (g shl 8) or bl
}
