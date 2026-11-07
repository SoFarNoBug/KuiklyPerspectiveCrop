doc_version: 20261107_2
layer: L3

# 透视裁剪 API 准则（L3 · 本仓）

> **分层**：本仓库契约全文真源。编码会话红线摘要见 `.cursor/rules/kuikly-dev-android.mdc`。  
> **依据**：根目录 `CONTEXT.md`、`AGENTS.md`、[docs/decisions.md](../../../decisions.md)、`docs/adr/`。

**级别**：核心规范（强制）。违反 = 该次编码不合格。

## 规则

1. **预览设图**：公开 API 使用 `imageSrc: String`（相机 path / 相册 uri / assets 等）。**禁止**把 Android `Bitmap` / Compose `ImageBitmap` 作为 common 公开设图类型。
2. **出图双 API**：必须同时提供 `crop()` 与 `cropToFile()`；返回 `PlatformImage`（或文件路径），**common 不暴露**平台位图类型。
3. **几何与 UI 分离**：四点 / 命中 / 凸性等放 `CropSession`（或等价纯逻辑）；**禁止**把几何判定堆进 Composable 闭包当唯一真源。
4. **换皮 SPI（六层可注入）**：蒙版 / 框线 / 角点 / 边中点 / 辅助线 / 放大镜须**可分别注入**；可有门面聚合，但**禁止**「只能整包换一个 Painter」当唯一扩展。业务皮肤**只进 Demo**，不进库默认实现。
5. **边拖默认（Q9）**：`edgeSnapToTouchEnabled` / `edgeClampToBoundsEnabled` 默认 **`true`**，保留开关；不得写成仅 rectMode 才有或推迟暴露。
6. **平台实现**：`CropProcessor` Android 先实装；其它端可 stub，但不要在 common 假装已出图成功。
7. **命名**：对外统一 `PerspectiveCrop*` / 本仓既定类型名；禁止把历史 View / Drawer 类名带进公开 API。
8. **出图失败（Q16）**：抛 `CropException(code, message, cause?)`；**禁止** null / 改 `Result` 签名 / 非法点或 stub 时静默返回原图。
9. **旋转对齐（Q17）**：出图先按 `uiRotationSteps` 旋源图，再对当前显示坐标四点透视；**禁止**对结果再叠一遍同等 degrees。
10. **Android init（Q20）**：宿主调用 `PerspectiveCropAndroid.init(application)`；未 init 须明确失败（带 code）。

## 判据速查

| 场景 | 结论 |
|---|---|
| common 公开 API 出现 `Bitmap` / `ImageBitmap` 设图或出图 | **FAIL** |
| 只做 `crop()` 不做 `cropToFile()`（或反过来作为唯一入口） | **FAIL** |
| 业务皮肤写进库模块默认 Painter | **FAIL** → 挪 Demo |
| 换皮只提供整包单一 Painter、无法按层注入 | **FAIL** → 六层 SPI（Q13） |
| 将 Q9 两开关默认改 `false` 或标成「仅 rect」 | **FAIL** |
| 几何逻辑仅存在于 Composable 内联、无会话对象 | **FAIL** → 抽 `CropSession` |
| 出图失败返回 null / 静默原图 / 无 code 可区分 stub | **FAIL** → `CropException`（Q16） |
| 出图对结果重复旋转导致双旋 | **FAIL** → 只旋源图（Q17） |
| 仅改文档 / 纯 Demo 换皮且未改库契约 | 相关条可 `N/A` |
