# Review · m2-android-crop

- 结论：有条件通过
- 依据：Plan `m2_crop_pipeline_a7403fd9`；decisions Q16–Q22；M2-checklist

## P0

无。

## P1

### P1-doc-drift CropPoint 坐标系口径过时
- 证据：`CropPoint.kt:3-5` 写「原图像素空间（未旋转；UI 旋转由 Stage 映射）」；实现上 `CropSession.rotateClockwiseUi` 已变换四点并对调宽高，`CropProcessor` 按显示坐标 + 先旋源图（Q17）出图
- 影响：调用方/后续 M3 若按注释理解会算错坐标
- 处置：必修（改 KDoc 与 SESSION/Stage 口径一致）

### P1-device-verify 出图主路径须真机闭环
- 证据：代码审无法确认 `assets://` 解码、旋转后朝向、绝对路径结果图能否被 `Image`/`Coil` 显示
- 影响：M2 验收标准 1–2 依赖真机
- 处置：必修（可执行档 → [device-check-list/m2-android-crop.md](device-check-list/m2-android-crop.md)）
- 缺口要点：见该表「覆盖对照」

## P2

### P2-dispatcher-io
- 证据：`PerspectiveCropController.kt:119` / `127` 用 `Dispatchers.Default` 做解码与写盘
- 影响：大图时争用 CPU 池；未证 ANR（已在后台）
- 处置：建议改为 `Dispatchers.IO`（若各端可用）

### P2-demo-config-reset
- 证据：Demo `PerspectiveCropConfig(rectMode = rectMode)` 每次只带 rectMode，会把其它 Config 字段打回默认
- 影响：M2 Demo 可接受；业务若复用该写法易丢 `jpegQuality` 等
- 处置：可选（Demo 改为基于 `controller.config.copy(rectMode=…)`）

## 规范候选观察

无。

## 真机

- 可执行档：[device-check-list/m2-android-crop.md](device-check-list/m2-android-crop.md)
- 代码审不了：assets 解码、旋转出图朝向、结果 path 再显示、非法四点文案

## 对照上轮

（首轮审查，无）
