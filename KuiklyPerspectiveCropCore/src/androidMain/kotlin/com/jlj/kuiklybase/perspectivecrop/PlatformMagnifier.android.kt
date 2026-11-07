package com.jlj.kuiklybase.perspectivecrop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.jlj.kuiklybase.perspectivecrop.overlay.magnifierCenter
import com.tencent.kuikly.compose.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.compose.ui.Modifier

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: Android 原生放大镜 Overlay（BitmapShader View）。
 */
actual fun platformSupportsNativeMagnifier(): Boolean = true

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
    DisposableEffect(Unit) {
        onDispose { MagnifierBlitStore.clear() }
    }

    val radius = patch?.displayRadiusPx
        ?: (containerW * config.magnifierRadiusRatio).coerceAtLeast(24f)
    val (cx, cy) = magnifierCenter(
        placement = config.magnifierPlacement,
        radius = radius,
        touchViewX = touchViewX,
        touchViewY = touchViewY,
        canvasW = containerW,
        canvasH = containerH,
    )
    val active = patch != null

    MakeKuiklyComposeNode<PerspectiveCropMagnifierView>(
        factory = { PerspectiveCropMagnifierView() },
        modifier = modifier,
        viewInit = {
            // publish 放在节点回调，避免 Composable 组合阶段写全局 Store（P2-compose-impure-publish）
            MagnifierBlitStore.publish(patch, config, viewPoints, touchViewX, touchViewY)
            getViewAttr().run {
                applyMagnifierGeometry(
                    this,
                    active,
                    cx,
                    cy,
                    radius,
                    MagnifierBlitStore.generation,
                )
            }
        },
        viewUpdate = { view ->
            MagnifierBlitStore.publish(patch, config, viewPoints, touchViewX, touchViewY)
            view.getViewAttr().run {
                applyMagnifierGeometry(
                    this,
                    active,
                    cx,
                    cy,
                    radius,
                    MagnifierBlitStore.generation,
                )
            }
        },
    )
}
