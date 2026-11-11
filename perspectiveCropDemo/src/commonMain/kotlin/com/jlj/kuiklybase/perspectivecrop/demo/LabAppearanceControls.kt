package com.jlj.kuiklybase.perspectivecrop.demo

import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropConfig

/**
 * @CreateDate: 2026/11/10
 * @Author: jlj
 * @Description: Lab 外观预设与色循环（只改 Config；不进 Core）。
 */

internal enum class LabAppearancePreset {
    AccentRing,
    SolidRed,
    WhiteHandles,
    BlueRing,
}

internal object LabAppearancePalettes {
    const val RED = 0xFFFF3B30L
    const val WHITE = 0xFFFFFFFFL
    const val BLUE = 0xFF2979FFL
    const val GREEN = 0xFF00C853L
    const val FAINT_BLACK = 0x33000000L

    /** 框线 / 描边循环（含淡黑）。 */
    val strokeColors: LongArray = longArrayOf(RED, WHITE, BLUE, GREEN, FAINT_BLACK)

    /** 角点填充只用实色。 */
    val fillColors: LongArray = longArrayOf(RED, WHITE, BLUE, GREEN)
}

internal fun LabAppearancePreset.chipLabel(): String = when (this) {
    LabAppearancePreset.AccentRing -> "外观·默认环"
    LabAppearancePreset.SolidRed -> "外观·红实心"
    LabAppearancePreset.WhiteHandles -> "外观·白框白柄"
    LabAppearancePreset.BlueRing -> "外观·白芯蓝圈"
}

internal fun PerspectiveCropConfig.withAppearancePreset(
    preset: LabAppearancePreset,
): PerspectiveCropConfig = when (preset) {
    LabAppearancePreset.AccentRing -> copy(
        borderColor = LabAppearancePalettes.RED,
        cornerFillColor = LabAppearancePalettes.WHITE,
        cornerFillAlpha = 1f,
        showCornerBorder = true,
        cornerBorderWidth = 2f,
        cornerBorderColor = LabAppearancePalettes.RED,
    )
    LabAppearancePreset.SolidRed -> copy(
        borderColor = LabAppearancePalettes.RED,
        cornerFillColor = LabAppearancePalettes.RED,
        cornerFillAlpha = 1f,
        showCornerBorder = false,
        cornerBorderWidth = 2f,
        cornerBorderColor = LabAppearancePalettes.RED,
    )
    LabAppearancePreset.WhiteHandles -> copy(
        borderColor = LabAppearancePalettes.WHITE,
        cornerFillColor = LabAppearancePalettes.WHITE,
        cornerFillAlpha = 1f,
        showCornerBorder = true,
        cornerBorderWidth = 1.5f,
        cornerBorderColor = LabAppearancePalettes.FAINT_BLACK,
    )
    LabAppearancePreset.BlueRing -> copy(
        borderColor = LabAppearancePalettes.BLUE,
        cornerFillColor = LabAppearancePalettes.WHITE,
        cornerFillAlpha = 1f,
        showCornerBorder = true,
        cornerBorderWidth = 2f,
        cornerBorderColor = LabAppearancePalettes.BLUE,
    )
}

internal fun nextPaletteColor(current: Long, palette: LongArray): Long {
    if (palette.isEmpty()) return current
    val idx = palette.indexOfFirst { it == current }
    return if (idx < 0) palette[0] else palette[(idx + 1) % palette.size]
}

internal fun paletteColorShortName(argb: Long): String = when (argb) {
    LabAppearancePalettes.RED -> "红"
    LabAppearancePalettes.WHITE -> "白"
    LabAppearancePalettes.BLUE -> "蓝"
    LabAppearancePalettes.GREEN -> "绿"
    LabAppearancePalettes.FAINT_BLACK -> "淡黑"
    else -> "其它"
}

internal fun PerspectiveCropConfig.matchesAppearancePreset(preset: LabAppearancePreset): Boolean {
    val applied = withAppearancePreset(preset)
    return borderColor == applied.borderColor &&
        cornerFillColor == applied.cornerFillColor &&
        cornerFillAlpha == applied.cornerFillAlpha &&
        showCornerBorder == applied.showCornerBorder &&
        cornerBorderWidth == applied.cornerBorderWidth &&
        cornerBorderColor == applied.cornerBorderColor
}
