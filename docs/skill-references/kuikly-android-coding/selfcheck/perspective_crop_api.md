# 自检清单 · perspective_crop_api（L3）

> 全文 SoT：`docs/skill-references/kuikly-android-coding/project/perspective_crop_api.md`。

| # | 条款 | 什么算 `OK`（判据） |
|---|---|---|
| P1 | imageSrc 设图 | 公开设图仍为 `String`；未新增 Bitmap/ImageBitmap 公开参数 |
| P2 | crop + cropToFile | 双 API 仍在或本轮未削弱任一端；`PlatformImage` 未在 common 暴露位图 |
| P3 | CropSession 几何 | 新增几何判定落在会话/纯逻辑层，非仅 Composable 内联 |
| P4 | 六层 Painter SPI / 皮肤边界 | 六层可分别注入（或本轮未削弱）；业务皮肤仅 Demo；库默认无业务绑死 |
| P5 | Q9 边拖默认 | `edgeSnapToTouch`/`edgeClampToBounds` 仍默认 true（或本轮未改默认/语义） |
| P6 | 命名与模块边界 | 公开 API 使用 `PerspectiveCrop*` 前缀；processor 平台边界清晰 |

## 说明

命中条件：改 `KuiklyPerspectiveCropCore/` 库模块公开 API 或 Stage/Controller/Session/Processor/Painter。纯 Demo UI 文案/布局且未改库契约 → 全表 `N/A` +「未改库」。
