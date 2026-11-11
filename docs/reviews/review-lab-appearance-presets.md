# Review · lab-appearance-presets

- 结论：有条件通过
- 依据：Plan `.cursor/plans/lab-appearance-presets.md`（丰富旋钮口径）+ changelog `docs/specs/lab-appearance-presets/changelog.md`
- 范围：`LabAppearanceControls.kt`、`DemoChrome.kt`（`DemoDpSlider`）、`PerspectiveCropLabPager.kt` 外观 Tab；不含 Core / `showOperationUI`

## P0

无。

## P1

### P1-device-verify · Lab 外观视觉与交互闭环

- 代码审不了：4 预设视觉、色循环文案与实色一致、蒙版深浅、4 滑杆展开/互斥、与「改变样式」Painter 并存、切 Tab 后滑杆状态。
- 可执行档：[`docs/reviews/device-check-list/lab-appearance-presets.md`](../device-check-list/lab-appearance-presets.md)

## P2

### P2-color-chip-always-active

- 框线色 / 角点填充 / 角点描边色 Chip 固定 `active = true`，无法区分「可点」与「选中态」；功能可达，仅交互反馈弱。
- 建议：与预设 Chip 一样用当前色是否落在色表、或恒 `active = false` 仅靠 label 短名反馈。

## 规范候选观察

无。

## 真机

见上 `P1-device-verify` + 可执行档链接。

## 对照上轮

首审（无上轮开放项）。

## 卡路由摘要

- 卡 B：预设 / 色循环 / 滑杆 Chip → `applyCfg` 均接线；Q41「Lab 不 demo `showOperationUI`」——Demo 无该字段引用。
- 卡 A/C/D/E：未开（无契约双源 / 机型 / 量纲跨模块 / 安全面）。
