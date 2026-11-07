# 开发决策台账（已锁定）

> **SoT**：本文件锁定已定结论（Q1–Q15 @ 2026-11-06；**Q16–Q22 @ 2026-11-07 M2**）。  
> ADR 只收「难逆转 + 需后果说明」的子集；里程碑是**分期施工单**，不得改写本表已定项。  
> 冲突时：**本表 > ADR 摘要 > milestones 措辞 > 代码注释**。

## 范围与版本

| 项 | 已定 |
|---|---|
| 库名 / 路径 | 仓 `KuiklyPerspectiveCrop`；发布库模块 `:KuiklyPerspectiveCropCore` |
| 能力范围 | 通用透视框选 / 出图能力；业务皮肤只进 Demo |
| 工程形态 | KMP 多端壳：库模块 + Demo 共享页 + 各端 App |
| **v0.1 定义** | **M0 + M1 + M2 + M3 全部验收通过**（含真像素放大镜）。中间里程碑可 Demo 冒烟，但不算 v0.1 交付完成 |

## Q1–Q15

| ID | 议题 | 已定 | 落点 |
|---|---|---|---|
| **Q1** | v0.1 平台范围 | 四端工程壳；交互/叠层 `commonMain`；透视出图 **Android 实装**，其它端 `CropProcessor` **stub（明确失败）** | ADR 0001；M2 |
| **Q2** | UI / 图抽象 | Compose `PerspectiveCropStage`；出图像素用 `PlatformImage`（expect/actual）；**不**绑残缺 `ImageBitmap` | CONTEXT；ADR 0001 |
| **Q3** | 功能对等 | 通用能力全量进库；业务皮肤 **只 Demo 换皮** | milestones 非目标；M3 Demo |
| **Q4** | 预览策略 | 预览 = Compose `Image(imageSrc)` + 叠层画框；透视解码只在 `crop` / `cropToFile` 时发生 | ADR 0001；M1/M2 |
| **Q5** | 设图 | 公开设图：`imageSrc: String`（相机 `filePath` / 相册 `uri` / assets） | ADR 0001 |
| **Q5′** | 出图 | **双 API**：`suspend crop(): PlatformImage` **与** `suspend cropToFile(): String`；`PlatformImage` common 不暴露 `Bitmap` | ADR 0001；M2 |
| **Q6** | 调用形态 | `crop` / `cropToFile` 均为 **`suspend`**；库内后台解码+透视，回 Kuikly 线程再交 UI | M2；L2 线程条款 |
| **Q7** | 默认选区 | 默认 **自由凸四边形**（`rectMode = false`） | Config；M1 |
| **Q8** | 底图适配 | 默认 **`ContentScale.Fit`** | Stage；M1 |
| **Q9** | 边拖手感 | `edgeSnapToTouchEnabled = true`、`edgeClampToBoundsEnabled = true`（**新库默认开**，保留开关；**不是**「仅 rect / 拖到 M3 再暴露」） | Config；**M1 必须接线** |
| **Q10** | 脚手架 | KMP + Kuikly Compose 多端壳；Kuikly **2.30.0**（坐标 `2.30.0-2.1.21`）；Demo **不叫** `shared` | ADR 0002 |
| **Q11** | Demo 模块名 | **`perspectiveCropDemo`** | ADR 0002 |
| **Q12** | Demo 图源 | 内置 assets 走通；Camera/Album **只写对接说明**，Demo 不强制依赖 | M0/M1 Demo；M3 README |
| **Q13** | 换皮 SPI | **六层可分别注入**的 Painter SPI：蒙版 / 框线 / 角点 / 边中点 / 辅助线 / 放大镜 + 库默认实现；业务皮肤只 Demo。**不是**单一整包 `CropOverlayPainter` 替换（可有门面聚合，但分层必须可注入） | CONTEXT；M1 接口 / M3 补齐 |
| **Q14** | 放大镜 | **真像素** + **降采样采样缓存**；禁止每帧全分辨率解码。实现落在 **M3**，但是 **v0.1 必达**（见上「v0.1 定义」） | ADR 0001；M3 |
| **Q15** | 交付节奏 | 分里程碑，**先验一步再下一步**；清单一开始写好 | `docs/milestones/` |

## Q16–Q22（M2 出图）

| ID | 议题 | 已定 | 落点 |
|---|---|---|---|
| **Q16** | 出图失败面 | 抛 **`CropException(code, message, cause?)`**；禁止 null / 改 `Result` 签名 / 静默回原图 | ADR 0003；M2 |
| **Q17** | 旋转与透视对齐 | 出图：先按 `uiRotationSteps` **旋转源 Bitmap**，再对当前显示坐标四点 `setPolyToPoly`（勿对结果再加一遍 degrees） | ADR 0003；M2 |
| **Q18** | `cropToFile` 路径 | **`cropToFile()`** 默认写 App 缓存；另提供 **`cropToFile(path)`** | Controller；M2 |
| **Q19** | 导出格式 | 默认 **JPEG**；`outputFormat` / `jpegQuality` 挂 **`PerspectiveCropConfig`** | Config；M2 |
| **Q20** | Android Context | 宿主 **`PerspectiveCropAndroid.init(application)`** 一次；未 init 明确失败 | ADR 0003；M2 |
| **Q21** | 结果尺寸 | 宽≈上边长、高≈左边长 | Processor；M2 |
| **Q22** | Demo 确认裁切 | 按钮始终可点；失败 **toast/文案** 展示 `CropException` 的 code/message | Demo；M2 |

## 分期映射（不得挪已定能力出 v0.1）

```text
M0  空架 + 文档
M1  CropSession / 触控 / 默认叠层（蒙版路径挖洞+框+角；六层 SPI 接口；Q9 边拖默认）
M2  Android crop / cropToFile / canCrop；其它端 stub
M3  真像素放大镜 + 剩余旋钮 + Demo 换皮 + Camera/Album 对接说明
     ↑
  v0.1 完成线
```

## 明确非目标（任意里程碑）

- OCR / 边缘检测 / 内置 `autoScan` 算法（可留注入钩子）
- 业务皮肤进库默认
- common 公开 API 暴露 `Bitmap` / `ImageBitmap`
- 非 Android 真实透视出图（stub 即可）

## 维护

- 改已定项：先改本文件，再改 ADR / milestones / CONTEXT / L3，避免只改施工单。
- 新增大决策：优先追加本表一行；若难逆转再开 ADR。
