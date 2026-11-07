package com.jlj.kuiklybase.perspectivecrop

import android.content.Context
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import com.tencent.kuikly.core.render.android.expand.component.KRView

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Android 放大镜 BitmapShader 圆镜（Q38）；像素来自 [MagnifierBlitStore]。
 */
class KRPerspectiveCropMagnifierView(context: Context) : KRView(context) {

    private var active: Boolean = false
    private var cx: Float = 0f
    private var cy: Float = 0f
    private var radius: Float = 0f

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val crossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val shaderMatrix = Matrix()

    init {
        setWillNotDraw(false)
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        isClickable = false
        isFocusable = false
    }

    override fun setProp(propKey: String, propValue: Any): Boolean {
        return when (propKey) {
            PerspectiveCropMagnifierAttr.PROP_ACTIVE -> {
                active = propValue.toBool()
                invalidate()
                true
            }
            PerspectiveCropMagnifierAttr.PROP_CX -> {
                cx = propValue.toFloatValue()
                invalidate()
                true
            }
            PerspectiveCropMagnifierAttr.PROP_CY -> {
                cy = propValue.toFloatValue()
                invalidate()
                true
            }
            PerspectiveCropMagnifierAttr.PROP_RADIUS -> {
                radius = propValue.toFloatValue()
                invalidate()
                true
            }
            PerspectiveCropMagnifierAttr.PROP_GENERATION -> {
                // 仅作脏标记：generation 变化触发 invalidate，像素仍读 MagnifierBlitStore
                propValue.toIntValue()
                invalidate()
                true
            }
            else -> super.setProp(propKey, propValue)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!active || radius <= 0f) return
        val bmp = MagnifierBlitStore.bitmapOrNull()
        if (bmp != null) {
            val shader = BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            val diam = radius * 2f
            val sx = diam / bmp.width.coerceAtLeast(1)
            val sy = diam / bmp.height.coerceAtLeast(1)
            shaderMatrix.reset()
            shaderMatrix.setScale(sx, sy)
            shaderMatrix.postTranslate(cx - radius, cy - radius)
            shader.setLocalMatrix(shaderMatrix)
            fillPaint.shader = shader
            canvas.drawCircle(cx, cy, radius, fillPaint)
            fillPaint.shader = null
        } else {
            fillPaint.shader = null
            fillPaint.color = 0xFF222222.toInt()
            canvas.drawCircle(cx, cy, radius, fillPaint)
        }

        borderPaint.color = MagnifierBlitStore.borderColor
        borderPaint.strokeWidth = MagnifierBlitStore.borderWidth
        canvas.drawCircle(cx, cy, radius, borderPaint)

        if (MagnifierBlitStore.showCross) {
            val half = MagnifierBlitStore.crossLineLength / 2f
            crossPaint.color = MagnifierBlitStore.crossColor
            crossPaint.strokeWidth = MagnifierBlitStore.crossLineWidth
            canvas.drawLine(cx - half, cy, cx + half, cy, crossPaint)
            canvas.drawLine(cx, cy - half, cx, cy + half, crossPaint)
        }
    }

    companion object {
        const val VIEW_NAME = PerspectiveCropMagnifierView.VIEW_NAME
    }
}

private fun Any.toFloatValue(): Float = when (this) {
    is Number -> this.toFloat()
    is String -> this.toFloatOrNull() ?: 0f
    else -> toString().toFloatOrNull() ?: 0f
}

private fun Any.toIntValue(): Int = when (this) {
    is Number -> this.toInt()
    is String -> this.toIntOrNull() ?: 0
    else -> toString().toIntOrNull() ?: 0
}

private fun Any.toBool(): Boolean = when (this) {
    is Boolean -> this
    is Number -> this.toInt() != 0
    is String -> this == "1" || this.equals("true", ignoreCase = true)
    else -> false
}
