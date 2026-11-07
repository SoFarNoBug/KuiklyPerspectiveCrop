# content-inset · changelog

## 2026-11-07 · contentInset* 接 ImageFitMapping

- [MODIFY] `PerspectiveCropConfig`：`contentInsetLeft/Top/Right/Bottom`（视图逻辑像素，默认 0）
- [MODIFY] `ImageFitMapping`：扣 inset 后 avail 再 Fit；过大钳到可用区 ≥ 1
- [MODIFY] `PerspectiveCropStage`：mapping 传 inset；`Image.padding` 同源；奇次旋转补 scale 用 avail
- [MODIFY] Lab Appearance：「内缩·关 / 示例」(四边 24)
- [MODIFY] `CONTEXT.md`：contentInset 术语；外扩不进库
- 非本单：角点 grab、catch/minCrop 默认、库内绘制外扩
- 真机验证表路径（审核产出）：`docs/reviews/device-check-list/content-inset.md`
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：对齐旧库 inset 单点接线｜待批 0（已达标 0）
