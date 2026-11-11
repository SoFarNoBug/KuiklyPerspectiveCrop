# Lab 外观色板 / 丰富旋钮

> 与 IDE Plan「Lab appearance rich knobs」对齐（档 a：预设 + 色循环 + 滑杆）。

## 目标

`perspective_crop_lab` **外观** Tab：快捷预设 + 色循环 + 蒙版深浅 + 可展开滑杆；全部写 `PerspectiveCropConfig`；**不进 Core**。

## 已实现

| 能力 | 说明 |
|---|---|
| 4 预设 | 默认环 / 红实心 / 白框白柄 / 白芯蓝圈 |
| 色循环 | 框线色、角点填充（四实色）、角点描边色（五色含淡黑） |
| 蒙版 | 深 0.55 ↔ 浅 0.30 |
| 滑杆 | 角点半径 4…20、框线宽 1…6、角描边宽 0.5…6、填充 α 0.2…1 |
| 既有开关 | 放大镜/九宫格/中点/描边/改变样式/矩形/内缩/引擎 |

## 非目标

- 不演示 `showOperationUI`
- 无自由 ARGB 取色
- 「改变样式」仍为 Painter 换皮

## 文件

- `perspectiveCropDemo/.../LabAppearanceControls.kt`
- `perspectiveCropDemo/.../DemoChrome.kt`（`DemoDpSlider`）
- `perspectiveCropDemo/.../PerspectiveCropLabPager.kt`
- `docs/specs/lab-appearance-presets/changelog.md`
