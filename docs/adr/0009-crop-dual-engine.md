# ADR 0009: 出图双引擎（Config.cropEngine）

## Status

Accepted（2026-11-09；2026-11-09 补 iOS）

## Context

Android 用 `setPolyToPoly` 出图；H5 用 CPU 单应（ADR 0005）。产品需要鸿蒙真出图，并希望 Android 也能切换到与 H5 同算法的 CPU 路径做对比。鸿蒙侧现已有 `libnative_drawing` 的 `OH_Drawing_MatrixSetPolyToPoly`，可对齐 Android Native。iOS 需同等双引擎：平台 Native + Common 对比。

## Decision

1. **Config** 增加 `cropEngine: CropEngine = Auto`（`Auto` | `Native` | `Common`）。
2. **Common**：将原 `JsHomography` 上移 [`PerspectiveHomography.kt`](../../KuiklyPerspectiveCropCore/src/commonMain/kotlin/com/jlj/kuiklybase/perspectivecrop/PerspectiveHomography.kt)（`CROP_MAX_EDGE=4096`）。
3. **Android**：`Auto`/`Native` → 现有 `setPolyToPoly`；`Common` → ARGB + `perspectiveWarpArgb`。
4. **鸿蒙**：`Auto`/`Native` → `OH_Drawing_MatrixSetPolyToPoly` + Canvas `DrawPixelMapRect` 出 ARGB；`Common` → OH Image 解码抽 ARGB + common 单应。两端落盘均用 **软件 PNG** 写 `file://`（不调用 `OH_ImagePackerNative_*`，SDK/真机该符号为不可执行桩会导致 SIGSEGV）。
5. **H5**：始终 Common。
6. **iOS**：`Auto`/`Native` → Core Image 四点透视，dest 尺寸与 Android/Ohos 一致（上边/左边 hypot）；`Common` → ARGB + `perspectiveWarpArgb`。出图解码旋源后最长边硬顶 `CROP_MAX_EDGE`。落盘 PNG/JPEG → `file://`。宿主 `PerspectiveCropIos.init(cacheDir)`；`assets://` 按 `main` → 已加载 Bundle 搜索。
7. **Init（鸿蒙）**：`PerspectiveCropOhos.init(resfileDir, cacheDir)`；壳 NAPI `setPerspectiveCropOhosDirs`。

## Consequences

- 默认行为：Android / 鸿蒙 / iOS 均为 Auto→Native；Common 可 Lab 对比。
- Lab 可循环切换引擎做对比。
- 全文台账见 [docs/decisions.md](../decisions.md) Q40。
