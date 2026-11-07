# ADR 0001: 预览用 src + Compose，出图双 API

## Status

Accepted（2026-11-06）

## Context

若用 Android `Bitmap` + 自定义 View，无法直接落到四端 Compose 契约。Kuikly 的 `ImageBitmap` 实质是缓存 src 句柄，缺少 `readPixels` / 离屏写回，无法作为透视出图契约。公开相机/相册组件（如 [KuiklyCamera](https://github.com/Kuikly-contrib/KuiklyCamera) / [KuiklyPhotoSelector](https://github.com/Kuikly-contrib/KuiklyPhotoSelector)）交出的也是 `filePath` / `uri` 字符串。需要同时服务 Android / iOS / 鸿蒙 / H5。

## Decision

1. 预览：`PerspectiveCropStage` 使用 Compose `Image(imageSrc)` + 叠层画框。
2. 设图公开类型：`String` src。
3. 出图：`suspend crop(): PlatformImage` 与 `suspend cropToFile(): String` 并存；`PlatformImage` 为 expect/actual，common 不暴露 `Bitmap`。
4. 透视实现：`CropProcessor` Android 先实装，其它端 stub。

## Consequences

- 与相机/相册 `imageSrc` 对接顺畅；四端公开契约从第一天一致。
- 放大镜需自建降采样采样缓存（不依赖 `ImageBitmap.readPixels`）；真像素实现属 **v0.1 / M3**（见 [decisions.md](../decisions.md) Q14）。
- 蒙版挖洞不能依赖 Kuikly DrawScope 的 `blendMode`/`DST_OUT`，需用路径方案。
- 完整 Q1–Q15 台账以 [docs/decisions.md](../decisions.md) 为准；本 ADR 只固定预览/出图契约。
