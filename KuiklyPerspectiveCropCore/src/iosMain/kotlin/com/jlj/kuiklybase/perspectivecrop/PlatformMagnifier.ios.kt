package com.jlj.kuiklybase.perspectivecrop

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: iOS 暂无原生放大镜贴图；Stage 走网格兜底。
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
    viewPoints: List<CropPoint>,
) {
    // iOS：空实现，Stage 使用 DefaultMagnifierOverlayPainter 网格兜底
}
