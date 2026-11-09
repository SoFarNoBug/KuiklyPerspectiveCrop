package com.jlj.kuiklybase.perspectivecrop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.jlj.kuiklybase.perspectivecrop.overlay.magnifierCenter
import com.tencent.kuikly.compose.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.compose.ui.Modifier

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 鸿蒙原生放大镜 Overlay（BlitStore + ArkTS Canvas View）。
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
