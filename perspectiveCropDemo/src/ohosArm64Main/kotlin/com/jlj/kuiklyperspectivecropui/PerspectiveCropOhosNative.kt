package com.jlj.kuiklyperspectivecropui

import com.jlj.kuiklybase.perspectivecrop.MagnifierBlitStore
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropOhos
import kotlin.experimental.ExperimentalNativeApi
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.posix.memcpy

/**
 * NAPI 入口（须在 shared 产物模块导出，Core klib 的 @CName 不会进最终 so）。
 * @param resfileDir hap resourceDir；@param cacheDir 可写缓存（可为 null/空）
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("perspective_crop_set_ohos_dirs")
fun perspectiveCropSetOhosDirs(resfileDir: CPointer<ByteVar>?, cacheDir: CPointer<ByteVar>?) {
    val res = resfileDir?.toKString().orEmpty()
    val cache = cacheDir?.toKString().orEmpty()
    if (res.isEmpty() && cache.isEmpty()) return
    PerspectiveCropOhos.init(res, cache)
}

/** 兼容旧 NAPI：仅注入 resfile（不改已有 cacheDir） */
@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("perspective_crop_set_resfile_dir")
fun perspectiveCropSetResfileDir(dir: CPointer<ByteVar>?) {
    if (dir == null) return
    PerspectiveCropOhos.init(dir.toKString())
}

/**
 * 将 [MagnifierBlitStore] 快照写入调用方缓冲。
 *
 * @return 1=有完整像素，0=无像素或缓冲不足（仍写 generation / meta）。
 * rgbaLen / viewPointsLen：入参容量，出参实际写入量。
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("perspective_crop_magnifier_blit_snapshot")
fun perspectiveCropMagnifierBlitSnapshot(
    generation: CPointer<IntVar>?,
    width: CPointer<IntVar>?,
    height: CPointer<IntVar>?,
    rgba: CPointer<UByteVar>?,
    rgbaLen: CPointer<IntVar>?,
    borderWidth: CPointer<FloatVar>?,
    borderColor: CPointer<IntVar>?,
    showCross: CPointer<IntVar>?,
    crossColor: CPointer<IntVar>?,
    crossLineWidth: CPointer<FloatVar>?,
    crossLineLength: CPointer<FloatVar>?,
    showCropLineMirror: CPointer<IntVar>?,
    cropLineColor: CPointer<IntVar>?,
    cropLineWidth: CPointer<FloatVar>?,
    touchViewX: CPointer<FloatVar>?,
    touchViewY: CPointer<FloatVar>?,
    viewPointsXy: CPointer<FloatVar>?,
    viewPointsLen: CPointer<IntVar>?,
): Int {
    val snap = MagnifierBlitStore.snapshot()
    if (generation != null) generation.pointed.value = snap.generation
    if (borderWidth != null) borderWidth.pointed.value = snap.borderWidth
    if (borderColor != null) borderColor.pointed.value = snap.borderColor
    if (showCross != null) showCross.pointed.value = if (snap.showCross) 1 else 0
    if (crossColor != null) crossColor.pointed.value = snap.crossColor
    if (crossLineWidth != null) crossLineWidth.pointed.value = snap.crossLineWidth
    if (crossLineLength != null) crossLineLength.pointed.value = snap.crossLineLength
    if (showCropLineMirror != null) {
        showCropLineMirror.pointed.value = if (snap.showCropLineMirror) 1 else 0
    }
    if (cropLineColor != null) cropLineColor.pointed.value = snap.cropLineColor
    if (cropLineWidth != null) cropLineWidth.pointed.value = snap.cropLineWidth
    if (touchViewX != null) touchViewX.pointed.value = snap.touchViewX
    if (touchViewY != null) touchViewY.pointed.value = snap.touchViewY

    val pts = snap.viewPointsXy
    val ptsCap = viewPointsLen?.pointed?.value ?: 0
    val ptsWrite = minOf(pts.size, ptsCap.coerceAtLeast(0))
    if (viewPointsXy != null && ptsWrite > 0) {
        pts.usePinned { pinned ->
            memcpy(
                viewPointsXy as COpaquePointer,
                pinned.addressOf(0),
                (ptsWrite * 4).toULong(),
            )
        }
    }
    if (viewPointsLen != null) viewPointsLen.pointed.value = ptsWrite

    val rgbaBytes = snap.toRgbaBytes()
    val cap = rgbaLen?.pointed?.value ?: 0
    if (rgbaBytes.isEmpty() || snap.width <= 0 || snap.height <= 0) {
        if (width != null) width.pointed.value = 0
        if (height != null) height.pointed.value = 0
        if (rgbaLen != null) rgbaLen.pointed.value = 0
        return 0
    }
    if (width != null) width.pointed.value = snap.width
    if (height != null) height.pointed.value = snap.height
    val write = minOf(rgbaBytes.size, cap.coerceAtLeast(0))
    if (rgba != null && write > 0) {
        rgbaBytes.usePinned { pinned ->
            memcpy(
                rgba as COpaquePointer,
                pinned.addressOf(0),
                write.toULong(),
            )
        }
    }
    if (rgbaLen != null) rgbaLen.pointed.value = write
    return if (write == rgbaBytes.size && write > 0) 1 else 0
}
