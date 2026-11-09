# ADR 0006: 放大镜原生像素贴图（网格仅兜底）

## Status

Accepted（2026-11-07 M5；2026-11-09 鸿蒙策略 A）

## Context

M3 在 Kuikly Compose `DrawScope` 上用 N×N `drawRect` 铺圆镜（方案 B：拖动粗 / 停指密）。Kuikly `ImageBitmap` 仅为 src/`ImageRef` 句柄，无法从 ARGB 构造后 `drawImage`，也无 Android `BitmapShader` 等价物。拖动全程马赛克，停指才清晰——放大镜最需要可读的时刻反而不可读。旧库用 `BitmapShader` + 椭圆一次填充。鸿蒙网格路径在 KN Compose 上实测卡顿，需原生贴图。

## Decision

1. **主路径**：各端原生「`MagnifierPatch` → 圆镜」像素贴图；**拖动全程清晰**，不以停指升密为体验开关。
2. **Android**：`KRPerspectiveCropMagnifier` 原生 View + `BitmapShader`；像素经进程内 `MagnifierBlitStore`，避免 props 序列化 `IntArray`。
3. **鸿蒙**：KN 侧 `MagnifierBlitStore`（`IntArray` 像素仓）+ `PlatformMagnifierOverlay` 只传几何/`magGeneration`；壳注册 ArkTS `KRPerspectiveCropMagnifier`，在 generation 变化时 **NAPI `getMagnifierBlit`** 同步拉 RGBA + 装饰元数据，Canvas 圆裁绘制（边框/十字/裁线镜像）。`@CName` 快照导出挂在 **Demo shared**（Core klib 的 `@CName` 不进最终 so）。失败画深灰圆，**不**中途切回网格。
4. **H5（暂退）**：`platformSupportsNativeMagnifier()=false`，走 N×N 网格兜底。原 Canvas 贴图因 **h5App 壳与 Demo 业务包各有一份 `MagnifierBlitStore`** 会静默读空；打通单例后再开原生贴图。鸿蒙用「单进程 KN Store + NAPI 拉块」规避同类双 Store。
5. **兜底**：`platformSupportsNativeMagnifier=false`，或业务注入了非 `DefaultMagnifierOverlayPainter` 的放大镜画家时，仍用 N×N 网格（可保留停指升密）。
6. **采样契约不变**（ADR 0004）。鸿蒙采样边长与 Android/JS 对齐（约 96）。**绘制**：iOS / H5 本阶段继续网格；鸿蒙 / Android 走原生贴图。

## Consequences

- Android / 鸿蒙主路径关闭「拖动马赛克」体验债。
- 需在 androidApp / ohosApp 注册外部 RenderView；H5 暂不注册放大镜自定义 View。
- 自定义放大镜 SPI 仍可用，但走网格而非原生贴图。
- 鸿蒙宿主须暴露 `getMagnifierBlit` NAPI，并在 `KuiklyViewDelegate` 注册 `KRPerspectiveCropMagnifier`。

全文台账见 [docs/decisions.md](../decisions.md) Q38。
