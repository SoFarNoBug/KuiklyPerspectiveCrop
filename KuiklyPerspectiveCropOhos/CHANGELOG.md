# Changelog

## 1.0.0 — 2026-11-11

- 版本升至 `1.0.0`；配套 Maven Core `KuiklyPerspectiveCropCore:1.0.0-2.30.0`。
- 行为对齐 Core 1.0（含 Accent Ring 角点描边 / `showOperationUI` 等；本 HAR 仍为原生放大镜 View）。

## 0.0.1 — 2026-11-10

- 初始鸿蒙 HAR：`KRPerspectiveCropMagnifier` Canvas 圆镜。
- 经 `PerspectiveCropOhosNative.configure` 注入宿主 `getMagnifierBlit`（不绑死 `libentry.so`）。
- 配套 Maven Core：`KuiklyPerspectiveCropCore:0.0.1-2.30.0`；render `@kuikly-open/render@2.30.0`。
