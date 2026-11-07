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
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.platform.LocalActivity
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page
import kotlinx.coroutines.launch

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 全能配置 Lab（Q30）：分组 Tab + Stage + 底栏 + 裁切结果/失败浮窗。
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

private enum class LabTab {
    Appearance,
    Gesture,
    Magnifier,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PerspectiveCropLabScreen() {
    val statusBarHeight = LocalActivity.current.pageData.statusBarHeight
    val controller = rememberPerspectiveCropController()
    val scope = rememberCoroutineScope()
    var cfg by remember { mutableStateOf(PerspectiveCropConfig()) }
    var altSkin by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(LabTab.Appearance) }
    var resultSrc by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var cropping by remember { mutableStateOf(false) }

    fun applyCfg(block: PerspectiveCropConfig.() -> PerspectiveCropConfig) {
        cfg = cfg.block()
        controller.updateConfig(cfg)
    }

    fun runCrop() {
        if (cropping) return
        cropping = true
        scope.launch {
            try {
                resultSrc = controller.cropToFile()
                errorText = null
            } catch (e: CropException) {
                resultSrc = null
                errorText = "${e.code}: ${e.message}"
            } catch (e: Exception) {
                resultSrc = null
                errorText = "ERROR: ${e.message}"
            } finally {
                cropping = false
            }
        }
    }

    fun injectPoints() {
        val w = controller.imageWidth
        val h = controller.imageHeight
        if (w <= 0f || h <= 0f) return
        val inset = minOf(w, h) * 0.15f
        controller.applyAutoScanPoints(
            listOf(
                CropPoint(inset, inset),
                CropPoint(w - inset, inset * 1.2f),
                CropPoint(w - inset * 1.1f, h - inset),
                CropPoint(inset * 1.2f, h - inset * 0.9f),
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DemoTheme.Background)
            .padding(top = statusBarHeight.dp),
    ) {
        DemoTopBar(title = DemoCopy.TITLE_LAB)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            DemoTabChip(
                label = DemoCopy.TAB_APPEARANCE,
                selected = tab == LabTab.Appearance,
                onClick = { tab = LabTab.Appearance },
            )
            DemoTabChip(
                label = DemoCopy.TAB_GESTURE,
                selected = tab == LabTab.Gesture,
                onClick = { tab = LabTab.Gesture },
            )
            DemoTabChip(
                label = DemoCopy.TAB_MAGNIFIER,
                selected = tab == LabTab.Magnifier,
                onClick = { tab = LabTab.Magnifier },
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            when (tab) {
                LabTab.Appearance -> {
                    DemoToggleChip(
                        label = if (cfg.showMagnifier) "放大镜·开" else "放大镜·关",
                        active = cfg.showMagnifier,
                        onClick = { applyCfg { copy(showMagnifier = !showMagnifier) } },
                    )
                    DemoToggleChip(
                        label = if (cfg.showGuideLines) "辅助线·开" else "辅助线·关",
                        active = cfg.showGuideLines,
                        onClick = { applyCfg { copy(showGuideLines = !showGuideLines) } },
                    )
                    DemoToggleChip(
                        label = if (cfg.showEdgeMidPoints) "中点·开" else "中点·关",
                        active = cfg.showEdgeMidPoints,
                        onClick = { applyCfg { copy(showEdgeMidPoints = !showEdgeMidPoints) } },
                    )
                    DemoToggleChip(
                        label = if (altSkin) "换皮·开" else "换皮·关",
                        active = altSkin,
                        onClick = {
                            altSkin = !altSkin
                            controller.updatePainters(
                                if (altSkin) altSkinPainters() else defaultLabPainters(),
                            )
                        },
                    )
                    DemoToggleChip(
                        label = if (cfg.rectMode) DemoCopy.RECT_ON else DemoCopy.RECT_OFF,
                        active = cfg.rectMode,
                        onClick = { applyCfg { copy(rectMode = !rectMode) } },
                    )
                    val insetOn = cfg.contentInsetLeft > 0f || cfg.contentInsetTop > 0f ||
                        cfg.contentInsetRight > 0f || cfg.contentInsetBottom > 0f
                    DemoToggleChip(
                        label = if (insetOn) "内缩·示例" else "内缩·关",
                        active = insetOn,
                        onClick = {
                            applyCfg {
                                if (insetOn) {
                                    copy(
                                        contentInsetLeft = 0f,
                                        contentInsetTop = 0f,
                                        contentInsetRight = 0f,
                                        contentInsetBottom = 0f,
                                    )
                                } else {
                                    copy(
                                        contentInsetLeft = 24f,
                                        contentInsetTop = 24f,
                                        contentInsetRight = 24f,
                                        contentInsetBottom = 24f,
                                    )
                                }
                            }
                        },
                    )
                }
                LabTab.Gesture -> {
                    // 文案用场景话，避免「吸附/钳制/抓取」黑话；默认态即库默认建议。
                    DemoToggleChip(
                        label = if (cfg.edgeSnapToTouchEnabled) "矩形边贴手指·开" else "矩形边贴手指·关",
                        active = cfg.edgeSnapToTouchEnabled,
                        onClick = {
                            applyCfg { copy(edgeSnapToTouchEnabled = !edgeSnapToTouchEnabled) }
                        },
                    )
                    DemoToggleChip(
                        label = if (cfg.edgeClampToBoundsEnabled) "拖边顶界齐步·开" else "拖边顶界齐步·关",
                        active = cfg.edgeClampToBoundsEnabled,
                        onClick = {
                            applyCfg { copy(edgeClampToBoundsEnabled = !edgeClampToBoundsEnabled) }
                        },
                    )
                    DemoToggleChip(
                        label = if (cfg.cornerGrabOffsetEnabled) "角按压偏移·少用开" else "角按压偏移·关(常用)",
                        active = cfg.cornerGrabOffsetEnabled,
                        onClick = {
                            applyCfg { copy(cornerGrabOffsetEnabled = !cornerGrabOffsetEnabled) }
                        },
                    )
                    val projOn = cfg.touchEdgeProjectionError > 0f
                    DemoToggleChip(
                        label = if (projOn) "抓边延长线·开" else "抓边延长线·关",
                        active = projOn,
                        onClick = {
                            applyCfg {
                                copy(touchEdgeProjectionError = if (touchEdgeProjectionError > 0f) 0f else 100f)
                            }
                        },
                    )
                    DemoToggleChip(
                        label = if (cfg.unhitPassthrough) "未点中交给外层·开" else "未点中交给外层·关",
                        active = cfg.unhitPassthrough,
                        onClick = { applyCfg { copy(unhitPassthrough = !unhitPassthrough) } },
                    )
                    val catchOff = cfg.touchCornerCatchDistance <= 0f
                    DemoToggleChip(
                        label = if (catchOff) "手柄好点·关" else "手柄好点·开",
                        active = !catchOff,
                        onClick = {
                            if (catchOff) {
                                applyCfg {
                                    copy(
                                        touchCornerCatchDistance = 20f,
                                        touchEdgeCatchDistance = 20f,
                                        touchMidCatchDistance = 30f,
                                    )
                                }
                            } else {
                                applyCfg {
                                    copy(
                                        touchCornerCatchDistance = 0f,
                                        touchEdgeCatchDistance = 0f,
                                        touchMidCatchDistance = 0f,
                                    )
                                }
                            }
                        },
                    )
                }
                LabTab.Magnifier -> {
                    DemoToggleChip(
                        label = "位置·${placementLabel(cfg.magnifierPlacement)}",
                        active = true,
                        onClick = {
                            applyCfg {
                                val next = MagnifierPlacement.entries.let { all ->
                                    all[(all.indexOf(magnifierPlacement) + 1) % all.size]
                                }
                                copy(magnifierPlacement = next)
                            }
                        },
                    )
                    val cache2k = cfg.magnifierCacheMaxEdgePx >= 2048
                    DemoToggleChip(
                        label = if (cache2k) "缓存·2k" else "缓存·1k",
                        active = cache2k,
                        onClick = {
                            applyCfg {
                                copy(
                                    magnifierCacheMaxEdgePx =
                                        if (magnifierCacheMaxEdgePx >= 2048) 1024 else 2048,
                                )
                            }
                        },
                    )
                    DemoToggleChip(
                        label = if (cfg.showMagnifierCenterCross) "十字·开" else "十字·关",
                        active = cfg.showMagnifierCenterCross,
                        onClick = {
                            applyCfg { copy(showMagnifierCenterCross = !showMagnifierCenterCross) }
                        },
                    )
                    DemoToggleChip(
                        label = if (cfg.showMagnifierCropLineMirror) "镜线·开" else "镜线·关",
                        active = cfg.showMagnifierCropLineMirror,
                        onClick = {
                            applyCfg {
                                copy(showMagnifierCropLineMirror = !showMagnifierCropLineMirror)
                            }
                        },
                    )
                }
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PerspectiveCropStage(
                modifier = Modifier.fillMaxSize(),
                controller = controller,
                imageSrc = SAMPLE_SRC,
                config = cfg,
                painters = if (altSkin) altSkinPainters() else defaultLabPainters(),
            )
        }
        DemoBottomBar {
            DemoBarButton(label = DemoCopy.RESET, onClick = { controller.resetFullImage() })
            DemoBarButton(label = DemoCopy.ROTATE, onClick = { controller.rotateClockwise() })
            DemoBarButton(label = DemoCopy.INJECT_POINTS, onClick = { injectPoints() })
            DemoBarButton(
                label = if (cropping) DemoCopy.CROP_BUSY else DemoCopy.CROP_SHORT,
                primary = true,
                enabled = !cropping,
                onClick = { runCrop() },
            )
        }
    }

    val preview = resultSrc
    if (preview != null) {
        DemoResultModal(
            imageSrc = preview,
            onAgain = { resultSrc = null },
            onDone = { resultSrc = null },
        )
    }
    val err = errorText
    if (err != null) {
        DemoErrorModal(
            message = err,
            onDismiss = { errorText = null },
        )
    }
}

private fun placementLabel(p: MagnifierPlacement): String = when (p) {
    MagnifierPlacement.TopAutoFlip -> "自动"
    MagnifierPlacement.TopLeft -> "左上"
    MagnifierPlacement.TopRight -> "右上"
    MagnifierPlacement.AboveTouch -> "触点上"
}
