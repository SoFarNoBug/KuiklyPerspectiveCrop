package com.jlj.kuiklybase.perspectivecrop.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.jlj.kuiklybase.perspectivecrop.CropException
import com.jlj.kuiklybase.perspectivecrop.CropPoint
import com.jlj.kuiklybase.perspectivecrop.MagnifierPlacement
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropConfig
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropStage
import com.jlj.kuiklybase.perspectivecrop.rememberPerspectiveCropController
import com.jlj.kuiklyperspectivecropui.DemoTheme
import com.jlj.kuiklyperspectivecropui.base.BasePager
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.ExperimentalLayoutApi
import com.tencent.kuikly.compose.foundation.layout.FlowRow
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.platform.LocalActivity
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import kotlinx.coroutines.launch

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 全能全配置演示（Q30）：Toggle + 换皮；工具条 FlowRow 适配窄屏/折叠屏。
 */
@Page("perspective_crop_lab", supportInLocal = true)
internal class PerspectiveCropLabPager : BasePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            PerspectiveCropLabScreen()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PerspectiveCropLabScreen() {
    val statusBarHeight = LocalActivity.current.pageData.statusBarHeight
    val controller = rememberPerspectiveCropController()
    val scope = rememberCoroutineScope()
    var cfg by remember { mutableStateOf(PerspectiveCropConfig()) }
    var altSkin by remember { mutableStateOf(false) }
    var dragActive by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var cropping by remember { mutableStateOf(false) }

    fun applyCfg(block: PerspectiveCropConfig.() -> PerspectiveCropConfig) {
        cfg = cfg.block()
        controller.updateConfig(cfg)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DemoTheme.Background)
            .padding(top = statusBarHeight.dp),
    ) {
        Text(
            text = "全能 Lab · drag=${if (dragActive) "ON" else "off"}",
            color = DemoTheme.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
        // FlowRow：窄屏/折叠外屏自动换行，避免 chip 被裁掉（勿依赖横滑）
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            DemoChip("magnifier", onClick = { applyCfg { copy(showMagnifier = !showMagnifier) } })
            DemoChip("guides", onClick = { applyCfg { copy(showGuideLines = !showGuideLines) } })
            DemoChip("mids", onClick = { applyCfg { copy(showEdgeMidPoints = !showEdgeMidPoints) } })
            DemoChip("passthrough", onClick = { applyCfg { copy(unhitPassthrough = !unhitPassthrough) } })
            DemoChip("snap", onClick = { applyCfg { copy(edgeSnapToTouchEnabled = !edgeSnapToTouchEnabled) } })
            DemoChip("clamp", onClick = { applyCfg { copy(edgeClampToBoundsEnabled = !edgeClampToBoundsEnabled) } })
            DemoChip("cross", onClick = { applyCfg { copy(showMagnifierCenterCross = !showMagnifierCenterCross) } })
            DemoChip(
                "pos:${shortPlacement(cfg.magnifierPlacement)}",
                onClick = {
                    applyCfg {
                        val next = MagnifierPlacement.entries.let { all ->
                            all[(all.indexOf(magnifierPlacement) + 1) % all.size]
                        }
                        copy(magnifierPlacement = next)
                    }
                },
            )
            DemoChip(
                "catch=0",
                onClick = {
                    applyCfg {
                        copy(
                            touchCornerCatchDistance = 0f,
                            touchEdgeCatchDistance = 0f,
                            touchMidCatchDistance = 0f,
                        )
                    }
                },
            )
            DemoChip(
                "catch=def",
                onClick = {
                    applyCfg {
                        copy(
                            touchCornerCatchDistance = 48f,
                            touchEdgeCatchDistance = 28f,
                            touchMidCatchDistance = 36f,
                        )
                    }
                },
            )
            DemoChip(
                if (cfg.magnifierCacheMaxEdgePx >= 2048) "cache:2k" else "cache:1k",
                onClick = {
                    applyCfg {
                        copy(
                            magnifierCacheMaxEdgePx = if (magnifierCacheMaxEdgePx >= 2048) 1024 else 2048,
                        )
                    }
                },
            )
            DemoChip(
                if (altSkin) "skin:alt" else "skin:def",
                onClick = {
                    altSkin = !altSkin
                    controller.updatePainters(if (altSkin) altSkinPainters() else defaultLabPainters())
                },
            )
            DemoChip(
                "injectPts",
                onClick = {
                    val w = controller.imageWidth
                    val h = controller.imageHeight
                    if (w > 0f && h > 0f) {
                        val inset = minOf(w, h) * 0.15f
                        controller.applyAutoScanPoints(
                            listOf(
                                CropPoint(inset, inset),
                                CropPoint(w - inset, inset * 1.2f),
                                CropPoint(w - inset * 1.1f, h - inset),
                                CropPoint(inset * 1.2f, h - inset * 0.9f),
                            ),
                        )
                        statusText = "ok · writePoints injected"
                    }
                },
            )
            DemoChip("rect", onClick = { applyCfg { copy(rectMode = !rectMode) } })
            DemoChip("rotate", onClick = { controller.rotateClockwise() })
            DemoChip("reset", onClick = { controller.resetFullImage() })
            DemoChip(
                if (cropping) "…" else "裁切",
                onClick = {
                    if (cropping) return@DemoChip
                    cropping = true
                    scope.launch {
                        try {
                            val path = controller.cropToFile()
                            statusText = "ok · $path"
                        } catch (e: CropException) {
                            statusText = "${e.code}: ${e.message}"
                        } catch (e: Exception) {
                            statusText = "ERROR: ${e.message}"
                        } finally {
                            cropping = false
                        }
                    }
                },
            )
        }
        DemoStatusLine(statusText)
        Text(
            text = "mag=${cfg.showMagnifier} guide=${cfg.showGuideLines} mid=${cfg.showEdgeMidPoints} " +
                "pass=${cfg.unhitPassthrough} snap=${cfg.edgeSnapToTouchEnabled} " +
                "clamp=${cfg.edgeClampToBoundsEnabled} edge=${cfg.magnifierCacheMaxEdgePx} " +
                "pos=${cfg.magnifierPlacement}",
            color = DemoTheme.TextSecondary,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PerspectiveCropStage(
                modifier = Modifier.fillMaxSize(),
                controller = controller,
                imageSrc = SAMPLE_SRC,
                config = cfg,
                painters = if (altSkin) altSkinPainters() else defaultLabPainters(),
                onDragActiveChanged = { dragActive = it },
            )
        }
    }
}

private fun shortPlacement(p: MagnifierPlacement): String = when (p) {
    MagnifierPlacement.TopAutoFlip -> "auto"
    MagnifierPlacement.TopLeft -> "TL"
    MagnifierPlacement.TopRight -> "TR"
    MagnifierPlacement.AboveTouch -> "above"
}
