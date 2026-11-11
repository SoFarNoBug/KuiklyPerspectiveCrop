package com.jlj.kuiklybase.perspectivecrop.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.pager.ReservedRegion
import com.tencent.kuikly.core.pager.ReservedRegionKind
import com.tencent.kuikly.core.views.DivView
import kotlin.math.max

/**
 * @CreateDate: 2026/11/11
 * @Author: jlj
 * @Description: Demo 侧订阅 iOS Duo reservedRegions（OCCLUSION）；只给顶栏/Chip 算左右垫，Stage 不垫。
 */

/** 相对页面根视图的避让边距（点）。 */
internal data class LabOcclusionInsets(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

/**
 * 桥接 Kuikly observable：`reservedRegions` 首帧晚到且宽高可能不变，
 * 仅靠 pageViewWidth 不会重组；0dp 节点订阅后触发 Compose 刷新。
 */
@Composable
internal fun rememberReservedRegionsTick(): Int {
    var tick by remember { mutableStateOf(0) }
    MakeKuiklyComposeNode<DivView>(
        factory = { DivView() },
        modifier = Modifier.size(0.dp),
        viewInit = {
            attr {
                pagerData.reservedRegions
                pagerData.safeAreaInsets
                pagerData.hingeStatus
                tick++
            }
        },
    )
    return tick
}

/**
 * 对齐 FoldAvoid / FoldChrome：OCCLUSION 只垫左右（右上挖孔 / 侧栏）。
 * 竖条若再垫 top/bottom 会把整页挤没。顶边只用 statusBar / safeTop。
 */
internal fun computeOcclusionInsets(
    pageWidth: Float,
    regions: List<ReservedRegion>,
    statusBarHeight: Float,
    safeTop: Float,
): LabOcclusionInsets {
    val w = pageWidth.coerceAtLeast(1f)
    var left = 0f
    var right = 0f
    for (region in regions) {
        if (!region.active || region.kind != ReservedRegionKind.OCCLUSION) continue
        if (region.width <= 1f || region.height <= 1f) continue
        val cx = region.x + region.width / 2f
        if (cx >= w / 2f) {
            right = max(right, w - region.x)
        } else {
            left = max(left, region.x + region.width)
        }
    }
    val top = max(statusBarHeight, safeTop.coerceAtLeast(0f))
    return LabOcclusionInsets(left = left, top = top, right = right, bottom = 0f)
}

/** 订阅区域后计算避让；[avoid] 为 false 时仅保留 statusBar 顶垫（与改前 Lab 一致）。 */
@Composable
internal fun rememberLabOcclusionInsets(avoid: Boolean): LabOcclusionInsets {
    rememberReservedRegionsTick()
    val configuration = LocalConfiguration.current
    val pageData = configuration.pageData
    val statusBar = pageData.statusBarHeight
    if (!avoid) {
        return LabOcclusionInsets(
            left = 0f,
            top = statusBar,
            right = 0f,
            bottom = 0f,
        )
    }
    return computeOcclusionInsets(
        pageWidth = configuration.pageViewWidth,
        regions = pageData.reservedRegions,
        statusBarHeight = statusBar,
        safeTop = pageData.safeAreaInsets.top,
    )
}
