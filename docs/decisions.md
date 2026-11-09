# 开发决策台账（已锁定）

> **SoT**：本文件锁定已定结论（Q1–Q15 @ 2026-11-06；Q16–Q22 @ 2026-11-07 M2；Q23–Q32 @ 2026-11-07 M3；**Q33–Q37 @ 2026-11-07 M4**）。  
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
| **Q9** | 边拖手感 | `edgeSnapToTouchEnabled = true`（矩形模式触点吸附）、`edgeClampToBoundsEnabled = true`（**新库默认开**，保留开关；**不是**「仅 rect / 拖到 M3 再暴露」）。**自由四边形模式**钳制=两端位移取交集齐步贴边；关=越界整帧丢弃（不各自 coerceIn 斜边）。snap 仍仅 rectMode 有意义 | Config；**M1 必须接线** |
| **Q10** | 脚手架 | KMP + Kuikly Compose 多端壳；Kuikly **2.30.0**（坐标 `2.30.0-2.1.21`）；Demo **不叫** `shared` | ADR 0002 |
| **Q11** | Demo 模块名 | **`perspectiveCropDemo`** | ADR 0002 |
| **Q12** | Demo 图源 | 内置 assets 走通；Camera/Album **只写对接说明**，Demo 不强制依赖 | M0/M1 Demo；M3 README |
| **Q13** | 换皮 SPI | **六层可分别注入**的 Painter SPI：蒙版 / 框线 / 角点 / 边线中点 / 九宫格辅助线 / 放大镜 + 库默认实现；业务皮肤只 Demo（Lab「改变样式」）。**不是**单一整包 `CropOverlayPainter` 替换（可有门面聚合，但分层必须可注入） | CONTEXT；M1 接口 / M3 补齐 |
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

## Q23–Q31（M3 放大镜 / 旋钮 / Demo）

| ID | 议题 | 已定 | 落点 |
|---|---|---|---|
| **Q23** | 放大镜采样 | `MagnifierSampler` **expect/actual**；Android 真采样；iOS/JS stub | ADR 0004；M3 |
| **Q24** | 采样缓存键 | 键含 `imageSrc` + **`uiRotationSteps`**（及 maxEdge）；旋转/换图失效重建 | ADR 0004；M3 |
| **Q25** | 放大镜绘制输入 | Stage 组 **`MagnifierPatch`**（像素块 + 几何）→ 默认放大镜 Painter | ADR 0004；SPI |
| **Q26** | 触摸事件穿透 | **`unhitPassthrough` 默认 false**；未命中手柄时不吞父容器手势 | Config；Stage |
| **Q27** | autoScan 钩子 | 复用 / 别名现有 **`writePoints`**（可注入四点）；库内无算法 | Controller |
| **Q28** | 拖动手势回调 | Stage 暴露 **`onDragActiveChanged`**（开始/结束） | Stage |
| **Q29** | 放大镜外观 | Config 对齐旧库量级：半径比、边框、十字开关/色/线宽线长等 | Config |
| **Q30** | Demo 结构 | **双页**：`perspective_crop_demo` 最小实现 + `perspective_crop_lab` 全能全配置 | Demo |
| **Q31** | 降采样上限 | **`magnifierCacheMaxEdgePx` Config 可配，默认 2048** | Config；ADR 0004 |
| **Q32** | 放大镜摆放 | **`magnifierPlacement`**：`TopAutoFlip`（默认，对齐旧库）/ `TopLeft` / `TopRight` / `AboveTouch` | Config；Painter |

## Q33–Q37（M4 · H5 / JS 实装，post-v0.1）

| ID | 议题 | 已定 | 落点 |
|---|---|---|---|
| **Q33** | M4 范围 | **post-v0.1**：JS 真放大镜 + 真裁剪；**不**回写 v0.1 / Q1 / Q23；**iOS 继续 stub** | M4；ADR 0005 |
| **Q34** | 异步解码签名 | `MagnifierSampler.warmCache`、`CropProcessor.process` → **`suspend`**；`sample` / `saveToFile` 仍同步 | expect；ADR 0005 |
| **Q35** | H5 透视算法 | **CPU 单应 + 双线性反变换**；输出最长边硬顶 **4096**；无 WebGL/OpenCV | `CropProcessor.js`；ADR 0005 |
| **Q36** | JS `imageSrc` 解码 | 库内自解码；`assets://`→可 fetch 路径；`http(s)`/`blob:`/`data:`/相对路径原样；CORS 失败 → `DECODE_FAILED` | `JsImageCodec` |
| **Q37** | H5 `saveToFile` | 返回 **`blob:`** ObjectURL；`saveToFile(path)` **忽略**本地路径语义；非持久文件 | `PlatformImage.js`；ADR 0005 |

## Q38（M5 · 放大镜原生像素贴图）

| ID | 议题 | 已定 | 落点 |
|---|---|---|---|
| **Q38** | 放大镜绘制主路径 | **主路径原生像素贴图**：**Android** `BitmapShader`；**鸿蒙** KN `MagnifierBlitStore` + NAPI 拉块 + ArkTS Canvas 圆镜；**H5 / iOS** 暂网格（H5 待 BlitStore 单例后再开）；自定义 `MagnifierOverlayPainter` 时强制网格逃生口；拖动全程清晰为目标 | ADR 0006；M5 |

## Q39（交互对齐 · minCrop / 角点 grab）

| ID | 议题 | 已定 | 落点 |
|---|---|---|---|
| **Q39** | minCrop / 自由角抓取 / 单位 | UI 长度（catch / minCrop / inset / 描边等）为 **Compose dp**（× `LocalDensity.density` 再 `/ Fit.scale` 或绘制）；`minCropWidth` 默认 100；catch 默认 20/20/30；自由角 **帧增量** + `cornerGrabOffsetEnabled`（默认关）+ `cornerMaxStepPx`（图像素，默认 0） | ADR 0007、**0008**；Config / Session / Stage |

## Q40（出图双引擎）

| ID | 议题 | 已定 | 落点 |
|---|---|---|---|
| **Q40** | 出图引擎可切换 | Config `cropEngine`：`Auto`（默认）/ `Native` / `Common`；Common = 上移自 H5 的 CPU 单应；Android Native=`setPolyToPoly`；鸿蒙本期仅 Common，显式 Native 明确失败；H5 始终 Common；iOS stub | ADR 0009；`PerspectiveHomography` / Processor actual |

## 分期映射（不得挪已定能力出 v0.1）

```text
M0  空架 + 文档
M1  CropSession / 触控 / 默认叠层（蒙版路径挖洞+框+角；六层 SPI 接口；Q9 边拖默认）
M2  Android crop / cropToFile / canCrop；其它端 stub
M3  真像素放大镜 + 剩余旋钮 + Demo 换皮 + Camera/Album 对接说明
     ↑
  v0.1 完成线
M4  JS 真放大镜 + CPU 透视裁剪 + blob: 落盘（post-v0.1；iOS 仍 stub）
M5  放大镜原生像素贴图（Android BitmapShader + H5 Canvas；网格仅兜底；iOS/鸿蒙暂网格）
```

## 明确非目标

**任意里程碑：**

- OCR / 边缘检测 / 内置 `autoScan` 算法（可留注入钩子）
- 业务皮肤进库默认
- common 公开 API 暴露 `Bitmap` / `ImageBitmap`

**v0.1（M0–M3）额外非目标：**

- 非 Android 真实透视出图 / 真像素放大镜（stub 即可）— **M4 起 JS 实装，见 Q33**

## 维护

- 改已定项：先改本文件，再改 ADR / milestones / CONTEXT / L3，避免只改施工单。
- 新增大决策：优先追加本表一行；若难逆转再开 ADR。
