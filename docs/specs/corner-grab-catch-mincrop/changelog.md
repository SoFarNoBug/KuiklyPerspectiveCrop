# corner-grab-catch-mincrop · changelog

## 2026-11-08 · Lab/README 手势场景文案

- [MODIFY] Lab Gesture chip：场景话（矩形边贴手指 / 拖边顶界齐步 / 角按压偏移·少用 / 抓边延长线 / 未点中交给外层 / 手柄好点）
- [MODIFY] `README` Config 表加「何时用」列；`PerspectiveCropConfig` 手势字段 KDoc 补场景
- [MODIFY] 真机表文案与 Lab 对齐
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：文档与 Demo 可懂性｜待批 0（已达标 0）

## 2026-11-08 · P2 fitScale 未就绪勿当 1

- [MODIFY] `CropSession`：`_fitScale` 初值/`clearImage` 置 `0`；未推入前 `canCrop()=false`，`minCropImage` 返回 `MAX_VALUE`（偏严不偏松）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：关 P2-fitscale-default-one｜待批 0（已达标 0）

## 2026-11-08 · 角点 grab / catch·minCrop 默认对齐

- [MODIFY] `PerspectiveCropConfig`：`cornerGrabOffsetEnabled`（默认 false）、`cornerMaxStepPx`（默认 0）；catch 默认 20/20/30；`minCropWidth` 默认 100 且语义改为视图逻辑 px（ADR 0007）
- [MODIFY] `CropSession`：缓存 `fitScale`；`minCropImage=minCropWidth/scale`；自由角帧增量 + grab/maxStep（对齐 freequadcrop `applyCornerMoveSoft`）
- [MODIFY] `PerspectiveCropStage`：`SideEffect` 推入 Fit scale
- [MODIFY] Lab Gesture：「抓取·关/开」；命中恢复 20/20/30
- [MODIFY] `CONTEXT.md` / `README.md`；新增 `docs/adr/0007-min-crop-view-px.md`
- 真机验证表路径（审核产出）：`docs/reviews/device-check-list/corner-grab-catch-mincrop.md`
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：对齐旧库手感与单位口径，无新规范/反例/冲突写法｜待批 0（已达标 0）
