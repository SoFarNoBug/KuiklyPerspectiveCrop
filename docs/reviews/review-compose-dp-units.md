# Review · compose-dp-units

- 结论：**有条件通过**
- 依据：Plan「Config 长度改 Compose Dp」（`compose_dp_units_7dfdbb42`）；changelog `docs/specs/corner-grab-catch-mincrop/changelog.md` 已 append ADR 0008 条

## P0

无。

## P1

### P1-device-verify · 1440p 上 dp 换算后的描边/命中须目视
- 证据：
  - Plan §5：`installDebug` 后看描边/角点/命中是否接近旧库 20dp 量级
  - `CropSession` `(dp×density)/fitScale`；Stage `toViewPx` 绘框（`PerspectiveCropStage.kt` viewConfig 路径）
- 影响：代码无法证明高密度机物理尺寸与旧库一致；须真机目视
- 处置：必修（放行条件）
- 缺口要点：见下「真机」；可执行档 → [device-check-list/compose-dp-units.md](device-check-list/compose-dp-units.md)

### P1-doc-drift · content-inset 真机表仍写「24 视图 px」
- 证据：`docs/reviews/device-check-list/content-inset.md` 步骤 3 期望「约 24 视图 px」；ADR 0008 后 Lab 示例 `24` 为 **Compose dp**
- 影响：按旧表验收会误判留白带宽
- 处置：建议（复审或跟进改表期望文案）

## P2

### P2-double-density-guard
- 证据：`toViewPx` 产出已是视图 px；`CropSession` 仍对 Config 字段再 × `_density`。Stage 现把 **dp** 写入 `controller.updateConfig`，绘制另用 `viewConfig`，路径正确；若调用方误把 `toViewPx` 结果再 `updateConfig` 会双重放大
- 影响：误用 API 时静默手感/绘制翻倍
- 处置：建议（KDoc/调用约定已部分说明；可选后续加 debug assert）

## 规范候选观察

- Config 存在「dp 真源 + toViewPx 视图副本」双形态时，须约定：**只允许 dp 进 Session/Controller**；`toViewPx` 结果仅限当帧绘制/Fit，禁止写回 `updateConfig`（可抄码农 L3 / 本仓 API 准则）

## 真机

- 可执行档：[device-check-list/compose-dp-units.md](device-check-list/compose-dp-units.md)
- 代码审不了：1440p 上角点半径/描边/catch 物理大小是否接近旧库 20dp；矩形边吸附在默认 catch 下是否可感

## 对照上轮

（首轮）
