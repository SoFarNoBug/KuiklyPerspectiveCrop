package com.jlj.kuiklybase.perspectivecrop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.jlj.kuiklybase.perspectivecrop.overlay.CropOverlayPainters
import com.jlj.kuiklybase.perspectivecrop.overlay.DefaultMagnifierOverlayPainter
import com.jlj.kuiklybase.perspectivecrop.overlay.midpointsInView
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.gestures.awaitEachGesture
import com.tencent.kuikly.compose.foundation.gestures.awaitFirstDown
import com.tencent.kuikly.compose.foundation.gestures.drag
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxWithConstraints
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.geometry.isSpecified
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.graphicsLayer
import com.tencent.kuikly.compose.ui.input.pointer.pointerInput
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.min

/**
 * 透视裁剪主入口（Compose）：`Image(imageSrc)` + 六层叠层 + 手势 → [CropSession]。
 *
 * @param onDragActiveChanged 拖动手势开始/结束（Q28）；未命中且 [PerspectiveCropConfig.unhitPassthrough] 时不回调。
 */
@Composable
fun PerspectiveCropStage(
    modifier: Modifier = Modifier,
    controller: PerspectiveCropController = rememberPerspectiveCropController(),
    imageSrc: String = controller.imageSrc,
    config: PerspectiveCropConfig = controller.config,
    contentScale: ContentScale = ContentScale.Fit,
    painters: CropOverlayPainters = controller.painters,
    onDragActiveChanged: ((Boolean) -> Unit)? = null,
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
    var magnifierPatch by remember { mutableStateOf<MagnifierPatch?>(null) }
    val sampler = remember { MagnifierSampler() }
    DisposableEffect(Unit) {
        onDispose { sampler.clear() }
    }

    // 后台预热降采样缓存；手势路径 sample 只读缓存（关 P1-main-thread-io）
    LaunchedEffect(
        src,
        rotationSteps,
        imageW,
        activeConfig.showMagnifier,
        activeConfig.magnifierCacheMaxEdgePx,
    ) {
        if (src.isEmpty() || imageW <= 0f || !activeConfig.showMagnifier) return@LaunchedEffect
        val warmSrc = src
        val warmRot = rotationSteps
        val warmW = imageW
        val warmCfg = activeConfig
        withContext(Dispatchers.Default) {
            sampler.warmCache(warmSrc, warmRot, warmW, warmCfg)
        }
    }

    // Q38：仅网格兜底路径保留停指升密；原生贴图主路径拖动全程清晰
    val useNativeMagnifier = platformSupportsNativeMagnifier() &&
        overlayPainters.magnifier === DefaultMagnifierOverlayPainter
    LaunchedEffect(touchView, magnifierPatch?.touchViewX, magnifierPatch?.touchViewY, useNativeMagnifier) {
        if (useNativeMagnifier) return@LaunchedEffect
        val patch = magnifierPatch ?: return@LaunchedEffect
        if (!touchView.isSpecified || !activeConfig.showMagnifier) return@LaunchedEffect
        if (patch.drawGrid >= MagnifierDrawGrid.SETTLED) return@LaunchedEffect
        delay(MagnifierDrawGrid.SETTLE_DELAY_MS)
        val latest = magnifierPatch ?: return@LaunchedEffect
        if (!touchView.isSpecified) return@LaunchedEffect
        if (latest.touchViewX != patch.touchViewX || latest.touchViewY != patch.touchViewY) {
            return@LaunchedEffect
        }
        magnifierPatch = latest.copy(drawGrid = MagnifierDrawGrid.SETTLED)
    }

    val onDragActiveChangedState = rememberUpdatedState(onDragActiveChanged)
    val configState = rememberUpdatedState(activeConfig)

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
            val density = LocalDensity.current
            val mapping = if (imageW > 0f && imageH > 0f) {
                ImageFitMapping(
                    containerW = containerW,
                    containerH = containerH,
                    imageW = imageW,
                    imageH = imageH,
                    insetLeft = activeConfig.contentInsetLeft,
                    insetTop = activeConfig.contentInsetTop,
                    insetRight = activeConfig.contentInsetRight,
                    insetBottom = activeConfig.contentInsetBottom,
                )
            } else {
                null
            }
            val insetL = mapping?.effectiveInsetLeft ?: 0f
            val insetT = mapping?.effectiveInsetTop ?: 0f
            val insetR = mapping?.effectiveInsetRight ?: 0f
            val insetB = mapping?.effectiveInsetBottom ?: 0f
            val availW = mapping?.availW ?: containerW
            val availH = mapping?.availH ?: containerH
            SideEffect {
                val map = mapping
                if (map != null) {
                    controller.sessionOrNull().updateFitScale(map.scale)
                }
            }

            // 奇次 90° 后 Fit 尺度与「宽高对调后再 Fit」不一致，需补 scale（用扣 inset 后的 avail）。
            Image(
                painter = painter,
                contentDescription = "crop source",
                modifier = Modifier
                    .padding(
                        start = with(density) { insetL.toDp() },
                        top = with(density) { insetT.toDp() },
                        end = with(density) { insetR.toDp() },
                        bottom = with(density) { insetB.toDp() },
                    )
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = rotationSteps * 90f
                        if (rotationSteps % 2 != 0 && naturalW > 0f && naturalH > 0f) {
                            val sNat = min(availW / naturalW, availH / naturalH)
                            val sDisp = min(availW / naturalH, availH / naturalW)
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
            val mappingState = rememberUpdatedState(mapping)
            val srcState = rememberUpdatedState(src)
            val rotationState = rememberUpdatedState(rotationSteps)
            val imageSizeState = rememberUpdatedState(imageW to imageH)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(imageW, imageH, containerW, containerH) {
                        val session = controller.sessionOrNull()
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val map = mappingState.value ?: return@awaitEachGesture
                            val downImg = map.viewToImageClamped(down.position.x, down.position.y)
                            val hit = session.hitTest(downImg.x, downImg.y, map.scale)
                            val cfg = configState.value
                            if (hit == HitTarget.None && cfg.unhitPassthrough) {
                                // Q26：未命中不 consume，透传父容器
                                return@awaitEachGesture
                            }
                            down.consume()
                            touchView = down.position
                            session.onDragStart(downImg.x, downImg.y, map.scale)
                            controller.publish()
                            onDragActiveChangedState.value?.invoke(true)
                            refreshMagnifier(
                                sampler = sampler,
                                src = srcState.value,
                                rotation = rotationState.value,
                                imageSize = imageSizeState.value,
                                map = map,
                                touch = down.position,
                                config = cfg,
                            )?.also { magnifierPatch = it }
                            var magTick = 0

                            drag(pointerId = down.id) { change ->
                                change.consume()
                                val m = mappingState.value ?: return@drag
                                val offset = change.position
                                touchView = offset
                                val img = m.viewToImageClamped(offset.x, offset.y)
                                session.onDrag(img.x, img.y)
                                controller.publish()
                                // 每 2 次 MOVE 采一次样，减轻 getPixels + 重绘压力
                                magTick++
                                if (magTick % 2 == 0) {
                                    refreshMagnifier(
                                        sampler = sampler,
                                        src = srcState.value,
                                        rotation = rotationState.value,
                                        imageSize = imageSizeState.value,
                                        map = m,
                                        touch = offset,
                                        config = configState.value,
                                    )?.also { magnifierPatch = it }
                                }
                            }

                            session.onDragEnd()
                            touchView = Offset.Unspecified
                            magnifierPatch = null
                            controller.publish()
                            onDragActiveChangedState.value?.invoke(false)
                        }
                    },
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (viewPoints.size == 4) {
                        with(overlayPainters.mask) { drawMask(viewPoints, activeConfig) }
                        val map = mapping
                        if (map != null) {
                            with(overlayPainters.guide) {
                                drawGuides(
                                    viewPoints,
                                    activeConfig,
                                    map.contentLeft,
                                    map.contentTop,
                                    map.contentW,
                                    map.contentH,
                                )
                            }
                        }
                        with(overlayPainters.border) { drawBorder(viewPoints, activeConfig) }
                        with(overlayPainters.corner) { drawCorners(viewPoints, activeConfig) }
                        with(overlayPainters.edgeMid) { drawEdgeMids(viewMids, activeConfig) }
                        // 网格兜底：非原生平台，或注入了自定义放大镜画家
                        if (!useNativeMagnifier &&
                            activeConfig.showMagnifier &&
                            touchView.isSpecified
                        ) {
                            with(overlayPainters.magnifier) {
                                drawMagnifier(
                                    viewPoints,
                                    touchView.x,
                                    touchView.y,
                                    activeConfig,
                                    magnifierPatch,
                                )
                            }
                        }
                    }
                }
                if (useNativeMagnifier &&
                    activeConfig.showMagnifier &&
                    touchView.isSpecified
                ) {
                    PlatformMagnifierOverlay(
                        modifier = Modifier.fillMaxSize(),
                        patch = magnifierPatch,
                        touchViewX = touchView.x,
                        touchViewY = touchView.y,
                        containerW = containerW,
                        containerH = containerH,
                        config = activeConfig,
                        viewPoints = viewPoints,
                    )
                }
            }
        }
    }
}

private fun refreshMagnifier(
    sampler: MagnifierSampler,
    src: String,
    rotation: Int,
    imageSize: Pair<Float, Float>,
    map: ImageFitMapping,
    touch: Offset,
    config: PerspectiveCropConfig,
): MagnifierPatch? {
    if (!config.showMagnifier) return null
    val (iw, ih) = imageSize
    val img = map.viewToImageClamped(touch.x, touch.y)
    return sampler.sample(
        imageSrc = src,
        uiRotationSteps = rotation,
        imageX = img.x,
        imageY = img.y,
        imageWidth = iw,
        imageHeight = ih,
        touchViewX = touch.x,
        touchViewY = touch.y,
        viewWidth = map.containerW,
        viewScale = map.scale,
        config = config,
    )
}
