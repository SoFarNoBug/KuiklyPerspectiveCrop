# Review · config-corner-border-operation-ui

- 结论：有条件通过
- 依据：Plan `config_border_operationui_86564e36`（审讯定稿 Q41）+ changelog `docs/specs/config-corner-border-operation-ui/changelog.md`

## P0

无。

## P1

### P1-device-verify · Accent Ring / showOperationUI 须真机

- 证据：`DefaultCropOverlayPainters.kt` 角点 fill+Stroke；`PerspectiveCropStage.kt` `showOperationUI` 门；Plan §6 真机验收
- 影响：代码无法证明默认白芯红圈观感、描边居中半内半外是否可接受、以及关操作 UI 后「无叠层仍可拖」手感
- 处置：必修（放行条件）；可执行档 → [`device-check-list/config-corner-border-operation-ui.md`](device-check-list/config-corner-border-operation-ui.md)
- 缺口要点：①默认 Accent Ring；②关 `showCornerBorder`→白实心；③`showOperationUI=false` 无叠层可拖（Lab 无入口，须临时代码）；④边中点无描边

## P2

### P2-mag-sample-when-ui-hidden

- 证据：`PerspectiveCropStage.kt` 拖动手势仍周期调 `refreshMagnifier`；函数内对 `showOperationUI` 早退
- 影响：关操作 UI 时仍有空转采样调用（立即 null），非功能错
- 处置：已关（DOWN/MOVE 前先判 `showOperationUI && showMagnifier` 再采样）

## 规范候选观察

无。

## 真机

可执行档：[`docs/reviews/device-check-list/config-corner-border-operation-ui.md`](device-check-list/config-corner-border-operation-ui.md)

代码审不了：默认主题像素观感、`showOperationUI=false` 真机手势与原生镜是否彻底消失。

## 对照上轮

（首轮，无）

## 对齐摘要（审查侧）

| Plan 项 | 代码 |
|---|---|
| 删 `cornerColor` + fill/border 字段 | OK Config |
| Accent Ring 默认 | OK 白芯/红边/描边默认开 |
| `showOperationUI` 整包跳过含原生镜/预热 | OK Stage + `refreshMagnifier` |
| 手势不改 | OK pointerInput 未按总开关早退 |
| Lab 仅 `showCornerBorder` | OK |
| changelog / decisions Q41 / CONTEXT / README | OK（changelog 已 append） |
