# config-corner-border-operation-ui · changelog

## 2026-11-10 · 关操作 UI 时拖动手势跳过放大镜采样

- [MODIFY] `PerspectiveCropStage`：DOWN/MOVE 仅在 `showOperationUI && showMagnifier` 时调 `refreshMagnifier`（关 P2-mag-sample-when-ui-hidden）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：审查 P2 空转调用｜待批 0（已达标 0）
- 本单无真机验证

## 2026-11-10 · Q41 showOperationUI + 角点 fill/border（Accent Ring）

- [MODIFY] `PerspectiveCropConfig`：删 `cornerColor`；加 `showOperationUI`、`cornerFillColor` / `cornerFillAlpha`、`showCornerBorder` / `cornerBorderWidth` / `cornerBorderColor`；`toViewPx` 乘 `cornerBorderWidth`
- [MODIFY] `DefaultCornerOverlayPainter` / `DefaultEdgeMidOverlayPainter`：消费 fill/alpha/border；中点 α = `cornerFillAlpha * 0.85`
- [MODIFY] `PerspectiveCropStage`：`showOperationUI=false` 整包跳过叠层绘制与原生镜/预热；手势不变
- [MODIFY] Lab 外观 Tab：`showCornerBorder` Toggle（不演示 `showOperationUI`）
- [MODIFY] `docs/decisions.md` Q41；`CONTEXT.md` 术语；`README` 旋钮表
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：通用 Config 补齐与命名澄清，无新规范/反例/冲突写法｜待批 0（已达标 0）
- 本单无真机验证（码农不写 device-check-list；验收建议见实现回报）
