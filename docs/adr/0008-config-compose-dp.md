# ADR 0008: Config UI 长度用 Compose dp

## Status

Accepted（2026-11-08）

## Context

对齐 freequadcrop 时，旧库 UI 长度经 `dp2px` 再参与命中/绘制。本库曾把 catch / minCrop / inset / 描边等当作**裸视图 px**（ADR 0007 称「视图逻辑像素」且 Avoid Android dp），在 1440p 等高密度机上物理尺寸偏小，手感与观感相对旧库差一截。跨端不能引入 Android `Resources.density`。

## Decision

1. `PerspectiveCropConfig` 中 UI 长度字段语义为 **Compose dp 数字**（字段名如 `borderWidthPx` 历史遗留，仍表示 dp）。
2. Stage 经 `LocalDensity.density`：`CropSession.updateDensity`；绘制 / Fit inset 用 `toViewPx(density)` 副本。
3. 命中与 `minCropWidth`：`(dp × density) / Fit.scale`。
4. **不缩放**：`cornerMaxStepPx`（图像素）、`magnifierCacheMaxEdgePx`（采样缓冲 px）、比例/开关/颜色。
5. 默认 catch 回到 **20 / 20 / 30** dp；`touchEdgeProjectionError` 默认 **100 dp**。

## Consequences

- 高密度机触控圈与描边接近旧库 dp 量级；依赖各端 Kuikly `LocalDensity`（H5/iOS/鸿蒙须有正确 density）。
- 调用方若曾按「裸 px」传 catch/minCrop，须改为按 dp 传。
- 修订 ADR 0007：minCrop 为 dp，再 × density / Fit.scale。
- **防双重放大**：`toViewPx` 置 `lengthsAreViewPx=true` 且幂等；Session 对已是视图 px 的 Config 不再 × density。约定 Controller 只收 dp；误把 `toViewPx` 写回也不会连乘。

全文台账见 [docs/decisions.md](../decisions.md) Q39。
