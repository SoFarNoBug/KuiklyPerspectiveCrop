package com.jlj.kuiklybase.perspectivecrop

import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.DeclarativeBaseView

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 放大镜原生贴图 Declarative 节点（Q38）；像素走进程内 BlitStore，props 只传几何与 generation。
 */
class PerspectiveCropMagnifierView :
    DeclarativeBaseView<PerspectiveCropMagnifierAttr, PerspectiveCropMagnifierEvent>() {

    override fun createAttr(): PerspectiveCropMagnifierAttr = PerspectiveCropMagnifierAttr()

    override fun createEvent(): PerspectiveCropMagnifierEvent = PerspectiveCropMagnifierEvent()

    override fun viewName(): String = VIEW_NAME

    companion object {
        const val VIEW_NAME = "KRPerspectiveCropMagnifier"
    }
}

class PerspectiveCropMagnifierAttr : ComposeAttr() {
    fun active(value: Boolean): PerspectiveCropMagnifierAttr {
        PROP_ACTIVE with if (value) 1 else 0
        return this
    }

    fun cx(value: Float): PerspectiveCropMagnifierAttr {
        PROP_CX with value
        return this
    }

    fun cy(value: Float): PerspectiveCropMagnifierAttr {
        PROP_CY with value
        return this
    }

    fun radius(value: Float): PerspectiveCropMagnifierAttr {
        PROP_RADIUS with value
        return this
    }

    fun generation(value: Int): PerspectiveCropMagnifierAttr {
        PROP_GENERATION with value
        return this
    }

    companion object {
        const val PROP_ACTIVE = "magActive"
        const val PROP_CX = "magCx"
        const val PROP_CY = "magCy"
        const val PROP_RADIUS = "magRadius"
        const val PROP_GENERATION = "magGeneration"
    }
}

class PerspectiveCropMagnifierEvent : ComposeEvent()

internal fun applyMagnifierGeometry(
    attr: PerspectiveCropMagnifierAttr,
    active: Boolean,
    cx: Float,
    cy: Float,
    radius: Float,
    generation: Int,
) {
    attr.touchEnable(false)
    attr.active(active)
    attr.cx(cx)
    attr.cy(cy)
    attr.radius(radius)
    attr.generation(generation)
}
