# ADR 0006: 放大镜原生像素贴图（网格仅兜底）

## Status

Accepted（2026-11-07 M5）

## Context

M3 在 Kuikly Compose `DrawScope` 上用 N×N `drawRect` 铺圆镜（方案 B：拖动 12 / 停指 32）。Kuikly `ImageBitmap` 仅为 src/`ImageRef` 句柄，无法从 ARGB 构造后 `drawImage`，也无 Android `BitmapShader` 等价物。拖动全程马赛克，停指才清晰——放大镜最需要可读的时刻反而不可读。旧库用 `BitmapShader` + 椭圆一次填充。

## Decision

1. **主路径**：各端原生「`MagnifierPatch` → 圆镜」像素贴图；**拖动全程清晰**，不以停指升密为体验开关。
2. **Android**：`KRPerspectiveCropMagnifier` 原生 View + `BitmapShader`；像素经进程内 `MagnifierBlitStore`，避免 props 序列化 `IntArray`。
3. **H5（暂退）**：`platformSupportsNativeMagnifier()=false`，走 N×N 网格兜底。原 Canvas 贴图因 **h5App 壳与 Demo 业务包各有一份 `MagnifierBlitStore`** 会静默读空；打通单例后再开原生贴图。
4. **兜底**：平台 actual 未就绪，或业务注入了非 `DefaultMagnifierOverlayPainter` 的放大镜画家时，仍用 N×N 网格（可保留停指升密）。
5. **采样契约不变**（ADR 0004）；iOS / 鸿蒙本阶段继续网格兜底。

## Consequences

- Android 主路径关闭「拖动马赛克」体验债；`P2-magnifier-draw-cost` 在 Android 主路径上视为根因关闭。
- 需在 androidApp 注册外部 RenderView；H5 暂不注册放大镜自定义 View。
- 自定义放大镜 SPI 仍可用，但走网格而非原生 Shader。

全文台账见 [docs/decisions.md](../decisions.md) Q38。
