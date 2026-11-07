# ADR 0003: CropException + Android init + 先旋源图再透视

## Status

Accepted（2026-11-07 M2）

## Context

M2 需钉死出图失败面、Android Context 获取方式，以及 M1「仅 UI 旋转」与出图像素的对齐。非法点时禁止静默返回原图。

## Decision

1. 公开失败面：抛单一类型 `CropException(code, message, cause?)`，用 code 细分；不改为 `Result` / 可空返回。
2. Android 宿主启动时调用 `PerspectiveCropAndroid.init(application)`；未 init 明确失败。
3. 出图时按 `uiRotationSteps` **先旋转源 Bitmap**，再在当前显示坐标上 `setPolyToPoly`（不再对结果重复加 degrees）。

## Consequences

- 调用方须 `try/catch` 或先 `canCrop()`；Demo 用文案展示 code/message。
- 库不依赖业务 Application 类名；未 init 即可测失败路径。
- 预览继续只用 `graphicsLayer`，解码仍只在 crop 时发生（与 ADR 0001 / Q4 一致）。

全文台账见 [docs/decisions.md](../decisions.md) Q16–Q22。
