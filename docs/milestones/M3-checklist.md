# M3 — 放大镜、剩余旋钮与对接

> 依赖 [M2](./M2-checklist.md) 验收通过。总览见 [README](./README.md)。  
> 完成本清单后视为 **v0.1 / 通用能力 parity** 达成（业务皮肤仍仅 Demo）。真像素放大镜为本里程碑实现，但是 [Q14 / v0.1 必达](../decisions.md)。

## 非目标（本里程碑）

- 不内置 `autoScan` / 边缘检测 / OCR 算法（只留钩子）
- 不把业务皮肤设为库默认 Painter
- 不实现非 Android 真实出图
- **不重开** Q9：`edgeSnapToTouch` / `edgeClampToBounds` 已在 **M1** 接线且默认 `true`（本里程碑只做文档/样式对齐，不改已定默认）

## 文档

- [ ] README：Camera `filePath` / Album `uri` → `imageSrc` 对接示例（可链 KuiklyCamera / PhotoSelector；Demo 不强制依赖，Q12）
- [ ] README 或 KDoc：Config 旋钮表（与下列工程项一致；含 Q9 两开关默认开）
- [ ] 更新 [milestones/README](./README.md) 状态为 M3 完成（= v0.1）
- [ ] 本清单一项项勾完

## 工程 · 放大镜（Q14）

- [ ] 真像素放大镜（拖角/边时可见）
- [ ] **降采样采样缓存**：禁止每帧全分辨率解码（ADR 0001 / decisions Q14）
- [ ] 可通过 Config / **放大镜层 Painter** 关闭

## 工程 · 剩余 Config 旋钮

- [ ] 辅助线（如三分线）开关 + **辅助线层**默认 Painter
- [ ] 边中点：与 M1 接线对齐；样式/默认与文档一致（绘制默认仍关）
- [ ] 捕捉距离：`touch*CatchDistance ≤ 0` 关闭对应命中源
- [ ] `unhitPassthrough`（未命中透传，便于外层滑动容器）
- [ ] 绘制开关命名与 Config 对齐（如 `showMagnifier` / `showGuideLines` / `showEdgeMidPoints`）
- [ ] 核对 M1 已暴露的 `edgeSnapToTouchEnabled` / `edgeClampToBoundsEnabled`：**默认 true**，文档勿写成「仅 rect」
- [ ] 视觉项：线宽/颜色、点半径、maskAlpha 等（有合理默认即可）

## 工程 · 钩子

- [ ] `autoScan`（或等价）**接口钩子**：可注入结果四点；库内无算法实现
- [ ] 可选 touch 开始回调（按需）

## Demo

- [ ] 换皮示例：自定义 **单层或多层** Painter（**仅 Demo 模块**；证明六层可分别注入）
- [ ] 开关演示：放大镜 / 辅助线 / 中点 / passthrough / 边拖 snap·clamp（可用简单 Toggle）
- [ ] README 对接说明在 Demo 注释或独立「对接」段落可跳转

## 验收命令

```bash
./gradlew :KuiklyPerspectiveCropCore:compileDebugKotlinAndroid :perspectiveCropDemo:compileDebugKotlinAndroid :androidApp:compileDebugKotlin
```

## 验收标准

1. 拖角可见放大镜，且 profiling/日志或代码结构能说明未每帧全分辨率解码
2. 上表 Config 开关行为与文档一致（抽测 ≥ 放大镜 / 辅助线 / 中点 / passthrough / catch≤0）
3. Demo 换皮不污染 `:KuiklyPerspectiveCropCore` 默认实现
4. README 含相机/相册 → `imageSrc` 对接示例
5. 上述 Gradle 任务通过

## 备注

- 像素级视觉「完全一致」不是硬门禁；行为与可配置面到位优先
- 业务主题组合（如 rect + 中点 + 无放大镜 + passthrough + 渐变框）仅作为 Demo 配方，不写进库默认
