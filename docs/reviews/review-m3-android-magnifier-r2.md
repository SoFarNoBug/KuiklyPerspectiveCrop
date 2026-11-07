# Review · m3-android-magnifier-r2

- 结论：**有条件通过**
- 依据：对照 `review-m3-android-magnifier.md`；码农 changelog「审查整改 r1」

## P0

无。

## P1

### P1-device-verify 放大镜/透传/Toggle 须真机闭环（未关）
- 证据：同基线；预热后手势不再同步解码，但像素观感/Toggle/passthrough 仍须实机
- 影响：M3 验收标准 1–2
- 处置：必修（可执行档 → [device-check-list/m3-android-magnifier.md](device-check-list/m3-android-magnifier.md)；#14 改为观察「预热完成前可能短暂无像素，完成后拖角应流畅出镜」）
- 缺口要点：见该表「覆盖对照」

## P2

### P2-magnifier-draw-cost（未关 · 可选）
- 证据：`DefaultCropOverlayPainters.kt` 仍多 `drawRect` 填镜
- 影响：低端机拖动微掉帧可能；非本轮必修
- 处置：可选（另单）

## 规范候选观察

无。

## 真机

- 可执行档：[device-check-list/m3-android-magnifier.md](device-check-list/m3-android-magnifier.md) — **未跑**
- 代码审不了：同基线；另：预热完成前拖角是否仅边框、预热后是否流畅

## 对照上轮

| 桶 ID | 状态 |
|---|---|
| P1-main-thread-io | **已关** — `warmCache` + Stage `LaunchedEffect`/`Dispatchers.IO`；`sample` 缓存未就绪则 return null，无 decode（`MagnifierSampler.android.kt` / `PerspectiveCropStage.kt`） |
| P2-new-file-header | **已关** — Sampler/Patch/Demo 等已补 `@CreateDate/@Author/@Description` |
| P2-private-underscore | **已关** — `_lock` / `_cacheKey` / `_cacheBitmap` / `_cacheScale` / `_patchEdge` |
| P1-device-verify | **未关** — 待真机表勾选 |
| P2-magnifier-draw-cost | **未关** — 可选，另单 |
| （本轮新开） | 无 |
