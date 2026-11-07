package com.jlj.kuiklybase.perspectivecrop

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 鸿蒙暂无原生放大镜贴图；Stage 走网格兜底。
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
    // Ohos：空实现，Stage 使用网格兜底
}
