# KuiklyPerspectiveCrop · AI 协作路由

## 项目概况

- 库：斜拍透视裁剪（Kuikly Compose）
- 模块：`:KuiklyPerspectiveCropCore`（库）+ `:perspectiveCropDemo`（Demo）+ 各端 App
- 包名：`com.jlj.kuiklybase.perspectivecrop.*` / `com.jlj.kuiklyperspectivecropui.*`
- 节奏：**分里程碑**，见 `docs/milestones/`；验完一步再下一步

## 接手阅读顺序

1. 本文件
2. [CONTEXT.md](CONTEXT.md)
3. [docs/decisions.md](docs/decisions.md)（已定决策 SoT；与清单冲突时改清单）
4. [docs/milestones/](docs/milestones/)（当前里程碑清单）
5. [docs/adr/](docs/adr/)
6. Cursor 协作：[.cursor/agents-guide.md](.cursor/agents-guide.md)

## 硬规则

1. 预览用 `imageSrc: String` + Compose；不要把 Android `Bitmap` 写进 common 公开设图 API。
2. 出图：`crop()` / `cropToFile()` 都要；`PlatformImage` common 不暴露 Bitmap。
3. 业务皮肤不进库默认实现；换皮走**六层可注入** Painter SPI，示例放 Demo。
4. `edgeSnapToTouch` / `edgeClampToBounds` 默认开（Q9）；改已定项先改 `docs/decisions.md`。
5. 改库行为前先更新对应里程碑清单与（若符合门槛）ADR。

## Cursor 配置（本仓）

- Rules / Skills / Hooks：`.cursor/`
- 规范素材：`docs/skill-references/`
- 编码前确认闸：`.cursor/rules/coding-confirm-gate.mdc` + `node .cursor/hooks/gate-unlock.mjs`
- Kuikly Compose DSL：`.cursor/rules/kuiklyComposeDSL.mdc`
