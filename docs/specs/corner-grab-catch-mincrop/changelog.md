# corner-grab-catch-mincrop · changelog

## 2026-11-08 · 防双重 density（P2-double-density-guard）

- [MODIFY] `PerspectiveCropConfig.lengthsAreViewPx`；`toViewPx` 置位且幂等
- [MODIFY] `CropSession.viewPx`：已是视图 px 时跳过再 × density
- 本单无真机验证
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：关审查 P2｜待批 0（已达标 0）

## 2026-11-08 · Config UI 长度改 Compose dp（ADR 0008）

- [MODIFY] `PerspectiveCropConfig`：UI 长度语义为 Compose dp；catch 默认回 20/20/30；`toViewPx(density)`
- [MODIFY] `CropSession`：`updateDensity`；命中/minCrop = `(dp×density)/fitScale`
- [MODIFY] `PerspectiveCropStage`：推 density；Fit/绘制/镜用 viewConfig
- [MODIFY] Lab catch 恢复 20/20/30；`CONTEXT` / `README` / `decisions` Q39；ADR 0007 修订 + 新增 0008
- 本单无真机验证（装包自测描边/命中；正式表由审核产出）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：单位口径对齐旧库 dp｜待批 0（已达标 0）

## 2026-11-08 · catch 默认临时改 100 试手感

- [MODIFY] `PerspectiveCropConfig`：角/边/中 catch 默认 `100`（1440p 真机放大命中，便于验吸附差；**非最终对齐值**）
- [MODIFY] Lab 恢复 catch 同步 `100`
- [MODIFY] `README` 表注明临时
- 本单无真机验证
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：临时试参｜待批 0（已达标 0）

## 2026-11-08 · 边中点 chip 文案纠偏

- [MODIFY] Lab：`显示边中点手柄·开` / `不显示边中点手柄`（去掉易误解的「只要四角」）
- [MODIFY] `README`：注明关中点后仍可拖边本身
- 本单无真机验证
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：文案纠偏｜待批 0（已达标 0）

## 2026-11-08 · Lab/README 痛点决策文案

- [MODIFY] Lab tip/chip：按接入方「痛点 → 要不要开」重写（非字段名/黑话）
- [MODIFY] `DemoCopy` 矩形开关；`README`「何时用」同口径
- 本单无真机验证
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：Demo 文案口径校正｜待批 0（已达标 0）

## 2026-11-08 · Lab 外观/放大镜场景文案 + Tab 提示

- [MODIFY] Lab：外观/放大镜 chip 场景话；各 Tab 顶一行「何时用」提示
- [MODIFY] `README` 补外观/放大镜「何时用」口径（内缩/缓存/镜线等）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：Demo/引用者可懂性｜待批 0（已达标 0）

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
