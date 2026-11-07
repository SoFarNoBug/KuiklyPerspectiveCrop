package com.jlj.kuiklybase.perspectivecrop

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier

/**
 * @CreateDate: 2026/11/07
 * @Author: jlj
 * @Description: 放大镜原生像素贴图入口（Q38）；不支持时 Stage 走网格兜底。
 */
expect fun platformSupportsNativeMagnifier(): Boolean

/**
 * 全屏透明原生放大镜层：Android BitmapShader / H5 Canvas 等。
 * 几何由调用方用 [com.jlj.kuiklybase.perspectivecrop.overlay.magnifierCenter] 算好后也可在 actual 内自算。
 */
@Composable
expect fun PlatformMagnifierOverlay(
    modifier: Modifier = Modifier,
    patch: MagnifierPatch?,
    touchViewX: Float,
    touchViewY: Float,
    containerW: Float,
    containerH: Float,
    config: PerspectiveCropConfig,
    /** 选区四点（视图坐标），供镜内框线镜像。 */
    viewPoints: List<CropPoint> = emptyList(),
)
