# Review · m5-magnifier-native-r3

- 结论：**有条件通过**
- 依据：对照 `review-m5-magnifier-native-r2.md`；码农 changelog「关审查 P2：publish 边界 + 删 dead generation」

## P0

无。

## P1

### P1-device-verify（未关 · 必修）
- 证据：Android BitmapShader 主路径仍待真机证拖动清晰 / 几何对齐
- 影响：唯一未关必修项
- 处置：必修（可执行档 → [device-check-list/m5-magnifier-native.md](device-check-list/m5-magnifier-native.md)）
- 缺口要点：拖动无马赛克、TopAutoFlip、预热前后

## P2

无。

## 规范候选观察

无。

## 真机

- 可执行档：[device-check-list/m5-magnifier-native.md](device-check-list/m5-magnifier-native.md)
- 代码审不了：拖动全程清晰度与 Compose↔原生坐标对齐

## 对照上轮

| 桶 ID | 状态 |
|---|---|
| P0-silent-fail | **已关**（上轮已关，本轮维持） |
| P1-device-verify | **未关** |
| P2-compose-impure-publish | **已关** — `publish` 在 `viewInit`/`viewUpdate`（`PlatformMagnifier.android.kt`） |
| P2-dead-generation-field | **已关** — 已删 `lastGeneration`；`magGeneration` 仅 invalidate |
| P2-h5-redraw-alloc | **已关**（更早轮次） |
| （本轮新开） | 无 |
