package com.jlj.kuiklybase.perspectivecrop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.jlj.kuiklybase.perspectivecrop.overlay.CropOverlayPainters
import com.jlj.kuiklybase.perspectivecrop.overlay.midpointsInView
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.gestures.detectDragGestures
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxWithConstraints
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.geometry.isSpecified
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.graphicsLayer
import com.tencent.kuikly.compose.ui.input.pointer.pointerInput
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.unit.sp
import kotlin.math.min

/**
 * 透视裁剪主入口（Compose）：`Image(imageSrc)` + 六层叠层 + 手势 → [CropSession]。
 */
@Composable
fun PerspectiveCropStage(
    modifier: Modifier = Modifier,
    controller: PerspectiveCropController = rememberPerspectiveCropController(),
    imageSrc: String = controller.imageSrc,
    config: PerspectiveCropConfig = controller.config,
    contentScale: ContentScale = ContentScale.Fit,
    painters: CropOverlayPainters = controller.painters,
) {
    SideEffect {
        if (imageSrc != controller.imageSrc) controller.applyImageSrc(imageSrc)
        if (config != controller.config) controller.updateConfig(config)
        if (painters != controller.painters) controller.updatePainters(painters)
    }

    val points = controller.points
    val imageW = controller.imageWidth
    val imageH = controller.imageHeight
    val naturalW = controller.naturalWidth
    val naturalH = controller.naturalHeight
    val rotationSteps = controller.uiRotationSteps
    val overlayPainters = controller.painters
    val activeConfig = controller.config
    val src = controller.imageSrc

    var touchView by remember { mutableStateOf(Offset.Unspecified) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A)),
    ) {
        if (src.isEmpty()) {
            Text(
                text = "PerspectiveCropStage\nset imageSrc to begin",
                color = Color(0xFFEEEEEE),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Center),
            )
            return@Box
        }

        val painter = rememberAsyncImagePainter(
            model = src,
            onSuccess = { state ->
                val size = state.painter.intrinsicSize
                if (size.width > 0f && size.height > 0f) {
                    controller.onImageSizeReady(size.width, size.height)
                }
            },
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val containerW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            val containerH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
            val mapping = if (imageW > 0f && imageH > 0f) {
                ImageFitMapping(containerW, containerH, imageW, imageH)
            } else {
                null
            }

            // M1：预览旋转用 graphicsLayer（像素级旋转出图留给 M2）。
            // 奇次 90° 后 Fit 尺度与「宽高对调后再 Fit」不一致，需补 scale。
            Image(
                painter = painter,
                contentDescription = "crop source",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = rotationSteps * 90f
                        if (rotationSteps % 2 != 0 && naturalW > 0f && naturalH > 0f) {
                            val sNat = min(containerW / naturalW, containerH / naturalH)
                            val sDisp = min(containerW / naturalH, containerH / naturalW)
                            if (sNat > 1e-6f) {
                                val fix = sDisp / sNat
                                scaleX = fix
                                scaleY = fix
                            }
                        }
                    },
                contentScale = contentScale,
                alignment = Alignment.Center,
            )

            val viewPoints = if (mapping != null && points.size == 4) {
                points.map { mapping.imageToView(it) }
            } else {
                emptyList()
            }
            val viewMids = midpointsInView(viewPoints)

            // 选区 points 每帧 publish 都会变，绝不能进 pointerInput key，否则拖动手势立刻被取消。
            // 映射/配置用 rememberUpdatedState 读最新值（L2：勿捕获过期闭包）。
            val mappingState = rememberUpdatedState(mapping)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(imageW, imageH, containerW, containerH) {
                        val session = controller.sessionOrNull()
                        detectDragGestures(
                            onDragStart = { offset ->
                                val map = mappingState.value
                                if (map != null) {
                                    touchView = offset
                                    val img = map.viewToImageClamped(offset.x, offset.y)
                                    session.onDragStart(img.x, img.y, map.scale)
                                    controller.publish()
                                }
                            },
                            onDrag = { change, _ ->
                                val map = mappingState.value
                                if (map != null) {
                                    val offset = change.position
                                    touchView = offset
                                    val img = map.viewToImageClamped(offset.x, offset.y)
                                    session.onDrag(img.x, img.y)
                                    controller.publish()
                                }
                            },
                            onDragEnd = {
                                session.onDragEnd()
                                touchView = Offset.Unspecified
                                controller.publish()
                            },
                            onDragCancel = {
                                session.onDragEnd()
                                touchView = Offset.Unspecified
                                controller.publish()
                            },
                        )
                    },
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (viewPoints.size == 4) {
                        with(overlayPainters.mask) { drawMask(viewPoints, activeConfig) }
                        with(overlayPainters.guide) { drawGuides(viewPoints, activeConfig) }
                        with(overlayPainters.border) { drawBorder(viewPoints, activeConfig) }
                        with(overlayPainters.corner) { drawCorners(viewPoints, activeConfig) }
                        with(overlayPainters.edgeMid) { drawEdgeMids(viewMids, activeConfig) }
                        if (activeConfig.showMagnifier && touchView.isSpecified) {
                            with(overlayPainters.magnifier) {
                                drawMagnifier(viewPoints, touchView.x, touchView.y, activeConfig)
                            }
                        }
                    }
                }
            }
        }
    }
}
