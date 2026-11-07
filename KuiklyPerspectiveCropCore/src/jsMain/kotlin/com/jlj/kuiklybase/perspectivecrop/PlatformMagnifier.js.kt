package com.jlj.kuiklybase.perspectivecrop

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: H5 暂退回网格兜底（Q38 审查 P0：壳与 Demo 双 MagnifierBlitStore）；打通单例后再开 Canvas 贴图。
 */
actual fun platformSupportsNativeMagnifier(): Boolean = false

@Composable
actual fun PlatformMagnifierOverlay(
    modifier: Modifier,
    patch: MagnifierPatch?,
    touchViewX: Float,
    touchViewY: Float,
    containerW: Float,
    containerH: Float,
    config: PerspectiveCropConfig,
) {
    // H5：空实现，Stage 使用 DefaultMagnifierOverlayPainter 网格兜底
}
