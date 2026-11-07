# M3 放大镜 / 旋钮 / 双页 Demo · changelog

## 2026-11-07（Lab 工具条 FlowRow 适配）

- Lab chip 区改 `FlowRow` 自动换行，避免窄屏/折叠外屏裁掉 `pos` 等按钮（不用横滑）
- 真机：用户确认 `pos` 可见 + Q32 摆放切换 OK（表 #4/#5/#5b）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：Demo 适配｜待批 0（已达标 0）

## 2026-11-07（Q32 圆镜摆放可配置）

- 新增 `MagnifierPlacement` + Config `magnifierPlacement`（默认 `TopAutoFlip`）
- `DefaultMagnifierOverlayPainter` 按枚举算圆心；Lab chip `pos:*` 循环四档
- decisions Q32；README 旋钮表
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：小增强按计划｜待批 0（已达标 0）

## 2026-11-07（放大镜位置对齐旧库）

- 圆镜改为顶部左/右摆放（触点近左上则翻到右上），对齐 `DefaultQuadCropMagnifierDrawer`
- 采样半径按镜面直径 / Fit scale 取 ~1:1 视图像素（非固定 24 强行放大）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：真机对齐旧库｜待批 0（已达标 0）

## 2026-11-07（放大镜卡顿速修）

- 关/缓解 `P2-magnifier-draw-cost`：默认镜改为 12×12 网格 drawRect；patch 边长 24；拖动采样 ~33ms 节流
- 说明：仍非旧库 BitmapShader；为 Kuikly 无像素贴图下的过渡方案
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：真机卡顿速修｜待批 0（已达标 0）

## 2026-11-07（审查整改 r1）

- 关 `P1-main-thread-io`：`MagnifierSampler.warmCache` + Stage `LaunchedEffect`/`Dispatchers.IO` 预热；`sample` 仅读缓存不解码
- 关 `P2-new-file-header`：补新建文件 `@CreateDate/@Author/@Description`
- 关 `P2-private-underscore`：Android Sampler 私有字段 `_` 前缀
- 未关 `P2-magnifier-draw-cost`（绘制管线另单）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：审查整改｜待批 0（已达标 0）

## 2026-11-07（编码 skill 补债）

- 补跑 `kuikly-android-coding` · 按计划行事收尾：规范自检三态表见对话 §实现回报
- 新增 alwaysApply Rule：`.cursor/rules/plan-exec-coding-skill.mdc`（防 Plan 执行跳过 skill）
- carried-over: 新建 `.kt` 缺 `@CreateDate/@Author/@Description` 文件头｜不修理由：补债回合只文档/规则，改源码须另开编码闸
- carried-over: `MagnifierSampler.sample` 首帧可能在手势线程同步解码｜风险：大图首次拖角卡顿；缓存命中后不再全图解码｜不修理由：M3 行为可接受，优化另单
- 流程债：码农越权起草了 `docs/reviews/device-check-list/m3-android-magnifier.md`；正式勾选仍归审核员
- 治理收尾：门禁 跳过｜三问 是/否/否｜理由：小结级——Plan 跳过 skill 事故已写入 Rule，未升 norms｜待批 0（已达标 0）

## 2026-11-07

- 落盘 decisions Q23–Q31、ADR 0004、CONTEXT（MagnifierSampler / Patch / unhitPassthrough / writePoints）、L3 §11–13
- `MagnifierSampler` expect/actual：Android 降采样缓存（键含 src/旋转/maxEdge）；ios/js stub
- `MagnifierPatch` + 默认真像素放大镜 Painter；辅助线三分线插值；Config 放大镜/透传/辅助线/缓存边长
- Stage：`unhitPassthrough`、`onDragActiveChanged`、采样喂放大镜层
- Controller：`applyAutoScanPoints` 别名 `writePoints`
- Demo 双页：`perspective_crop_demo` 最小 + `perspective_crop_lab` 全能换皮；Home 双入口
- README 旋钮表 + Camera/Album 对接；编译验收通过；真机表路径：`docs/reviews/device-check-list/m3-android-magnifier.md`
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：纯业务实现，契约已定｜待批 0（已达标 0）
