# ADR 0004: MagnifierSampler + MagnifierPatch + 降采样缓存

## Status

Accepted（2026-11-07 M3）

## Context

Q14 要求真像素放大镜且禁止每帧全分辨率解码。Kuikly `ImageBitmap` 缺少可靠 `readPixels`，不能作为采样源；出图侧已有 Android 解码管线可复用思路。

## Decision

1. `MagnifierSampler` 为 **expect/actual**：Android 建降采样缓存并裁 patch；iOS/JS stub（返回 null）。
2. 缓存键含 **`imageSrc` + `uiRotationSteps` + `magnifierCacheMaxEdgePx`**；换图 / UI 旋转 / 改上限则丢弃重建。最长边默认 **2048**，经 Config 可配（Q31）。
3. Stage 将采样结果打成 **`MagnifierPatch`**（ARGB 像素块 + 触点/半径等几何）交给放大镜层 Painter；默认实现圆镜 + 可选十字/边框（Q25/Q29）。
4. 不在 common 公开 API 暴露平台 `Bitmap` / 残缺 `ImageBitmap` 设图契约。

## Consequences

- 拖角可见真像素放大镜；profiling/结构可说明未每帧全分辨率解码。
- 非 Android 端放大镜为空 patch（仍可关 `showMagnifier`）；真采样属后续平台里程碑。
- Demo lab 页可拨 `magnifierCacheMaxEdgePx` 验证可配。

全文台账见 [docs/decisions.md](../decisions.md) Q14、Q23–Q31。
