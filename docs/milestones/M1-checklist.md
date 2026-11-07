# M1 — 几何触控 + 默认叠层

> 依赖 [M0](./M0-checklist.md) 验收通过。总览见 [README](./README.md)。

## 非目标（本里程碑）

- 不做 `crop` / `cropToFile` / 像素级旋转出图（→ M2）
- 不做真像素放大镜、辅助线精调（→ M3；放大镜位可留空 Painter）
- 不把业务皮肤写进库默认
- 不做相机/相册原生对接（可用 assets）

## 文档

- [x] 若公开 API 相对占位有命名/签名变更 → 更新 `CONTEXT.md` / [decisions.md](../decisions.md) / ADR（仅当符合 ADR 门槛）
- [x] 本清单一项项勾完（真机验收后勾）

## 工程 · 几何与会话

- [x] `CropSession`（或等价）：四点状态、命中（角 / 边 / 内部）、凸性约束
- [x] `rectMode`：角拖保持轴对齐矩形；默认**关**（自由凸四边形主路径，Q7）
- [x] 边拖：平移对边；**本里程碑接线** `edgeSnapToTouchEnabled` / `edgeClampToBoundsEnabled`，默认均为 **`true`（Q9）**，保留开关
- [x] `minCropWidth` / 防共线坍塌等基础限制（有默认即可）
- [x] `insideDragEnabled` 行为明确（默认值写进 Config 注释）

## 工程 · 坐标与 Stage

- [x] 底图：`Image(imageSrc)` + `ContentScale.Fit`（Q8）
- [x] 图域 ↔ 视图域坐标映射（选区与底图对齐）
- [x] `PerspectiveCropStage`：Image + 叠层 + 手势接到 Session
- [x] `pointerInput` 等手势遵守 Kuikly Compose 规范（可变状态进 key）

## 工程 · 默认叠层（六层 Painter SPI，Q13）

- [x] **六层可分别注入**：蒙版 / 框线 / 角点 / 边中点 / 辅助线 / 放大镜；库提供默认实现（可有门面聚合，但禁止「只能整包换一个」当唯一扩展）
- [x] **蒙版**：路径挖洞（**不用** `blendMode` / `DST_OUT`，见 ADR 0001）
- [x] **框线** + **角点**默认绘制
- [x] **边中点**：绘制开关 + 命中接线（默认**关**；与 Config 字段对齐）
- [x] 放大镜 / 辅助线：**接口位可空实现**；真像素放大镜 → M3（仍属 v0.1，见 [decisions.md](../decisions.md)）

## 工程 · Controller（预览侧）

- [x] 设 `imageSrc` / 读写四点 / 全图重置
- [x] UI 旋转 90°：**只更新预览与选区状态**（像素变换留给 M2）
- [x] 注入 Config / **六层 Painters**（Processor 可仍为占位）

## Demo

- [x] commonMain assets 样例图（`assets://crop_sample.webp`）
- [x] 演示页可拖四角/边；可见蒙版+框+角点（真机已验）
- [x] 可选开关试用 `rectMode`（不必漂亮）

## 验收命令

```bash
./gradlew :KuiklyPerspectiveCropCore:compileDebugKotlinAndroid :perspectiveCropDemo:compileDebugKotlinAndroid :androidApp:compileDebugKotlin
```

## 验收标准

1. assets 图可拖四角/边；凸性（或 rectMode）限制正确，选区不非法坍塌
2. Fit 下选区与底图对齐，无明显漂移
3. 蒙版为路径挖洞方案，编译与真机预览正常
4. 上述 Gradle 任务通过；Demo 可操作验收

## 备注

- 本仓默认以自由四边形为主，边中点默认关更干净
- Q9 两开关是**边拖手感默认**，不是「仅 rectMode」；rect 模式下行为仍受这两开关约束
- 编译已通过（2026-11-07）；真机拖框/对齐/旋转已验（同日）：拖角·边·框内正常；rotate 底图+选区同步；过程中修了 pointerInput key 误绑 points、预览缺 graphicsLayer 旋转
