# ADR 0009: 出图双引擎（Config.cropEngine）

## Status

Accepted（2026-11-09）

## Context

Android 用 `setPolyToPoly` 出图；H5 用 CPU 单应（ADR 0005）。鸿蒙无 Canvas 等价 API，此前 `CropProcessor` stub。产品需要鸿蒙真出图，并希望 Android 也能切换到与 H5 同算法的 CPU 路径做对比。

## Decision

1. **Config** 增加 `cropEngine: CropEngine = Auto`（`Auto` | `Native` | `Common`）。
2. **Common**：将原 `JsHomography` 上移 [`PerspectiveHomography.kt`](../../KuiklyPerspectiveCropCore/src/commonMain/kotlin/com/jlj/kuiklybase/perspectivecrop/PerspectiveHomography.kt)（`CROP_MAX_EDGE=4096`）。
3. **Android**：`Auto`/`Native` → 现有 `setPolyToPoly`；`Common` → ARGB + `perspectiveWarpArgb`。
4. **鸿蒙**：`Auto`/`Common` → OH Image 解码 + common 单应 + **软件 PNG** 落盘 `file://`（不调用 `OH_ImagePackerNative_*`，SDK/真机该符号为不可执行桩会导致 SIGSEGV）；显式 `Native` → `UNSUPPORTED_PLATFORM`（本期无原生引擎）。
5. **H5**：始终 Common；**iOS** 仍整端 stub。
6. **Init**：`PerspectiveCropOhos.init(resfileDir, cacheDir)`；壳 NAPI `setPerspectiveCropOhosDirs`。

## Consequences

- 默认行为：Android 不变（Auto→Native）；鸿蒙 Auto→Common 可出图。
- Lab 可循环切换引擎做对比。
- 全文台账见 [docs/decisions.md](../decisions.md) Q40。
