package com.jlj.kuiklybase.perspectivecrop.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.jlj.kuiklybase.perspectivecrop.CropEngine
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

/** 手势 Tab：同时只展开一根 catch 滑杆。 */
private enum class CatchSliderKind {
    None,
    Corner,
    Edge,
    Mid,
}

/** 外观 Tab：同时只展开一根视觉滑杆。 */
private enum class AppearanceSliderKind {
    None,
    CornerRadius,
    BorderWidth,
    CornerBorderWidth,
    CornerFillAlpha,
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
    var catchSlider by remember { mutableStateOf(CatchSliderKind.None) }
    var appearanceSlider by remember { mutableStateOf(AppearanceSliderKind.None) }
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
                onClick = {
                    tab = LabTab.Appearance
                    catchSlider = CatchSliderKind.None
                },
            )
            DemoTabChip(
                label = DemoCopy.TAB_GESTURE,
                selected = tab == LabTab.Gesture,
                onClick = { tab = LabTab.Gesture },
            )
            DemoTabChip(
                label = DemoCopy.TAB_MAGNIFIER,
                selected = tab == LabTab.Magnifier,
                onClick = {
                    tab = LabTab.Magnifier
                    catchSlider = CatchSliderKind.None
                },
            )
        }
        Text(
            text = when (tab) {
                LabTab.Appearance ->
                    "色板/滑杆改 Config；「改变样式」换 Painter。快捷预设只动框/角颜色与描边。"
                LabTab.Gesture ->
                    "手感类默认已按文档/快递单裁常用打开。觉得难点/拖边怪时再改下面开关。"
                LabTab.Magnifier ->
                    "默认圆镜 + 镜内框线即可。低端卡顿再降清晰；镜挡内容再改摆放。"
            },
            color = DemoTheme.TextSecondary,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            when (tab) {
                LabTab.Appearance -> {
                    for (preset in LabAppearancePreset.entries) {
                        DemoToggleChip(
                            label = preset.chipLabel(),
                            active = cfg.matchesAppearancePreset(preset),
                            onClick = { applyCfg { withAppearancePreset(preset) } },
                        )
                    }
                    DemoToggleChip(
                        label = "框线色·${paletteColorShortName(cfg.borderColor)}",
                        active = true,
                        onClick = {
                            applyCfg {
                                copy(
                                    borderColor = nextPaletteColor(
                                        borderColor,
                                        LabAppearancePalettes.strokeColors,
                                    ),
                                )
                            }
                        },
                    )
                    DemoToggleChip(
                        label = "角点填充·${paletteColorShortName(cfg.cornerFillColor)}",
                        active = true,
                        onClick = {
                            applyCfg {
                                copy(
                                    cornerFillColor = nextPaletteColor(
                                        cornerFillColor,
                                        LabAppearancePalettes.fillColors,
                                    ),
                                )
                            }
                        },
                    )
                    DemoToggleChip(
                        label = "角点描边色·${paletteColorShortName(cfg.cornerBorderColor)}",
                        active = true,
                        onClick = {
                            applyCfg {
                                copy(
                                    cornerBorderColor = nextPaletteColor(
                                        cornerBorderColor,
                                        LabAppearancePalettes.strokeColors,
                                    ),
                                )
                            }
                        },
                    )
                    val maskDeep = cfg.maskAlpha >= 0.45f
                    DemoToggleChip(
                        label = if (maskDeep) "蒙版·深" else "蒙版·浅",
                        active = maskDeep,
                        onClick = {
                            applyCfg {
                                copy(maskAlpha = if (maskAlpha >= 0.45f) 0.30f else 0.55f)
                            }
                        },
                    )
                    // chip = 接产品时的痛点/要不要开；非字段名。
                    DemoToggleChip(
                        label = if (cfg.showMagnifier) "绘制放大镜·开" else "绘制放大镜·关",
                        active = cfg.showMagnifier,
                        onClick = { applyCfg { copy(showMagnifier = !showMagnifier) } },
                    )
                    DemoToggleChip(
                        label = if (cfg.showGuideLines) "绘制九宫格·开" else "绘制九宫格·关",
                        active = cfg.showGuideLines,
                        onClick = { applyCfg { copy(showGuideLines = !showGuideLines) } },
                    )
                    DemoToggleChip(
                        label = if (cfg.showEdgeMidPoints) {
                            "绘制边线中点·开"
                        } else {
                            "绘制边线中点·关"
                        },
                        active = cfg.showEdgeMidPoints,
                        onClick = { applyCfg { copy(showEdgeMidPoints = !showEdgeMidPoints) } },
                    )
                    DemoToggleChip(
                        label = if (cfg.showCornerBorder) "角点描边·开" else "角点描边·关",
                        active = cfg.showCornerBorder,
                        onClick = { applyCfg { copy(showCornerBorder = !showCornerBorder) } },
                    )
                    DemoToggleChip(
                        label = "角点半径·${cfg.cornerRadiusPx.toInt()}dp",
                        active = appearanceSlider == AppearanceSliderKind.CornerRadius,
                        onClick = {
                            appearanceSlider =
                                if (appearanceSlider == AppearanceSliderKind.CornerRadius) {
                                    AppearanceSliderKind.None
                                } else {
                                    AppearanceSliderKind.CornerRadius
                                }
                        },
                    )
                    DemoToggleChip(
                        label = "框线宽·${cfg.borderWidthPx.toInt()}dp",
                        active = appearanceSlider == AppearanceSliderKind.BorderWidth,
                        onClick = {
                            appearanceSlider =
                                if (appearanceSlider == AppearanceSliderKind.BorderWidth) {
                                    AppearanceSliderKind.None
                                } else {
                                    AppearanceSliderKind.BorderWidth
                                }
                        },
                    )
                    DemoToggleChip(
                        label = "角描边宽·${cornerBorderWidthLabel(cfg.cornerBorderWidth)}dp",
                        active = appearanceSlider == AppearanceSliderKind.CornerBorderWidth,
                        onClick = {
                            appearanceSlider =
                                if (appearanceSlider == AppearanceSliderKind.CornerBorderWidth) {
                                    AppearanceSliderKind.None
                                } else {
                                    AppearanceSliderKind.CornerBorderWidth
                                }
                        },
                    )
                    DemoToggleChip(
                        label = "角填充透明·${(cfg.cornerFillAlpha * 100).toInt()}%",
                        active = appearanceSlider == AppearanceSliderKind.CornerFillAlpha,
                        onClick = {
                            appearanceSlider =
                                if (appearanceSlider == AppearanceSliderKind.CornerFillAlpha) {
                                    AppearanceSliderKind.None
                                } else {
                                    AppearanceSliderKind.CornerFillAlpha
                                }
                        },
                    )
                    DemoToggleChip(
                        label = if (altSkin) "改变样式·开" else "改变样式·关",
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
                        label = if (insetOn) "内缩显示开关·开" else "内缩显示开关·关",
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
                    DemoToggleChip(
                        label = "出图引擎·${cropEngineLabel(cfg.cropEngine)}",
                        active = true,
                        onClick = {
                            applyCfg {
                                val next = CropEngine.entries.let { all ->
                                    all[(all.indexOf(cropEngine) + 1) % all.size]
                                }
                                copy(cropEngine = next)
                            }
                        },
                    )
                }
                LabTab.Gesture -> {
                    DemoToggleChip(
                        label = if (cfg.edgeSnapToTouchEnabled) {
                            "矩形模式触点吸附·开"
                        } else {
                            "矩形模式触点吸附·关"
                        },
                        active = cfg.edgeSnapToTouchEnabled,
                        onClick = {
                            applyCfg { copy(edgeSnapToTouchEnabled = !edgeSnapToTouchEnabled) }
                        },
                    )
                    // Lab 不暴露：edgeClamp（拉不到底，单拨无感）、cornerGrabOffset（少用）、
                    // unhitPassthrough（触摸事件穿透，嵌横滑时由产品 Config 开）
                    val projOn = cfg.touchEdgeProjectionError > 0f
                    DemoToggleChip(
                        // 严谨·开 = 须贴边线段；严谨·关 = 宽松（角外延长也可抓边）。非「仅中点」。
                        label = if (projOn) "边线判定严谨·关" else "边线判定严谨·开",
                        active = !projOn,
                        onClick = {
                            applyCfg {
                                copy(touchEdgeProjectionError = if (touchEdgeProjectionError > 0f) 0f else 100f)
                            }
                        },
                    )
                    DemoToggleChip(
                        label = "角点命中·${cfg.touchCornerCatchDistance.toInt()}dp",
                        active = catchSlider == CatchSliderKind.Corner,
                        onClick = {
                            catchSlider =
                                if (catchSlider == CatchSliderKind.Corner) {
                                    CatchSliderKind.None
                                } else {
                                    CatchSliderKind.Corner
                                }
                        },
                    )
                    DemoToggleChip(
                        label = "边线命中·${cfg.touchEdgeCatchDistance.toInt()}dp",
                        active = catchSlider == CatchSliderKind.Edge,
                        onClick = {
                            catchSlider =
                                if (catchSlider == CatchSliderKind.Edge) {
                                    CatchSliderKind.None
                                } else {
                                    CatchSliderKind.Edge
                                }
                        },
                    )
                    DemoToggleChip(
                        label = "中点命中·${cfg.touchMidCatchDistance.toInt()}dp",
                        active = catchSlider == CatchSliderKind.Mid,
                        onClick = {
                            catchSlider =
                                if (catchSlider == CatchSliderKind.Mid) {
                                    CatchSliderKind.None
                                } else {
                                    CatchSliderKind.Mid
                                }
                        },
                    )
                }
                LabTab.Magnifier -> {
                    DemoToggleChip(
                        label = "圆镜挡内容·摆放·${placementLabel(cfg.magnifierPlacement)}",
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
                        label = if (cache2k) "镜清晰(默认)" else "低端卡·降清晰省内存",
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
                        label = if (cfg.showMagnifierCenterCross) {
                            "对准像素·镜心十字·开"
                        } else {
                            "镜心无十字"
                        },
                        active = cfg.showMagnifierCenterCross,
                        onClick = {
                            applyCfg { copy(showMagnifierCenterCross = !showMagnifierCenterCross) }
                        },
                    )
                    DemoToggleChip(
                        label = if (cfg.showMagnifierCropLineMirror) {
                            "拖角对齐框边·镜内画线·开"
                        } else {
                            "镜内只放大图"
                        },
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
        if (tab == LabTab.Appearance) {
            when (appearanceSlider) {
                AppearanceSliderKind.None -> Unit
                AppearanceSliderKind.CornerRadius -> DemoDpSlider(
                    label = "角点半径",
                    value = cfg.cornerRadiusPx,
                    valueRange = 4f..20f,
                    onValueChange = { v -> applyCfg { copy(cornerRadiusPx = v) } },
                )
                AppearanceSliderKind.BorderWidth -> DemoDpSlider(
                    label = "框线宽",
                    value = cfg.borderWidthPx,
                    valueRange = 1f..6f,
                    onValueChange = { v -> applyCfg { copy(borderWidthPx = v) } },
                )
                AppearanceSliderKind.CornerBorderWidth -> DemoDpSlider(
                    label = "角点描边宽",
                    value = cfg.cornerBorderWidth,
                    valueRange = 0.5f..6f,
                    snapToInt = false,
                    steps = 10,
                    formatValue = { "${cornerBorderWidthLabel(it)} dp" },
                    onValueChange = { v ->
                        val snapped = (kotlin.math.round(v * 2f) / 2f).coerceIn(0.5f, 6f)
                        applyCfg { copy(cornerBorderWidth = snapped) }
                    },
                )
                AppearanceSliderKind.CornerFillAlpha -> DemoDpSlider(
                    label = "角点填充透明",
                    value = cfg.cornerFillAlpha,
                    valueRange = 0.2f..1f,
                    snapToInt = false,
                    steps = 15,
                    formatValue = { "${(it * 100).toInt()}%" },
                    onValueChange = { v ->
                        applyCfg { copy(cornerFillAlpha = v.coerceIn(0.2f, 1f)) }
                    },
                )
            }
        }
        if (tab == LabTab.Gesture) {
            when (catchSlider) {
                CatchSliderKind.None -> Unit
                CatchSliderKind.Corner -> DemoDpSlider(
                    label = "角点命中范围",
                    value = cfg.touchCornerCatchDistance,
                    onValueChange = { v -> applyCfg { copy(touchCornerCatchDistance = v) } },
                )
                CatchSliderKind.Edge -> DemoDpSlider(
                    label = "边线命中范围",
                    value = cfg.touchEdgeCatchDistance,
                    onValueChange = { v -> applyCfg { copy(touchEdgeCatchDistance = v) } },
                )
                CatchSliderKind.Mid -> DemoDpSlider(
                    label = "边线中点命中范围",
                    value = cfg.touchMidCatchDistance,
                    onValueChange = { v -> applyCfg { copy(touchMidCatchDistance = v) } },
                )
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
    MagnifierPlacement.TopAutoFlip -> "自动躲开手指"
    MagnifierPlacement.TopLeft -> "固定左上"
    MagnifierPlacement.TopRight -> "固定右上"
    MagnifierPlacement.AboveTouch -> "跟着指尖上方"
}

private fun cropEngineLabel(e: CropEngine): String = when (e) {
    CropEngine.Auto -> "Auto"
    CropEngine.Native -> "Native"
    CropEngine.Common -> "Common"
}

private fun cornerBorderWidthLabel(w: Float): String {
    val tenths = kotlin.math.round(w * 10f).toInt()
    return if (tenths % 10 == 0) "${tenths / 10}" else "${tenths / 10}.${tenths % 10}"
}
