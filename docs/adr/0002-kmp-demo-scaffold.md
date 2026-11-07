# ADR 0002: KMP 多端脚手架与 Demo 模块名

## Status

Accepted（2026-11-06）

## Context

需要 Demo 包着库源码的多端工程（Android / iOS / 鸿蒙 / H5）。相机/相册类能力偏原生 Module 分包；本库是 Compose 叠层 + 几何 + 出图策略，宜采用「库模块 + Demo 共享页 + 各端 App」结构。

## Decision

1. 采用 KMP + Kuikly Compose 多端壳：库模块与 Demo 分仓内模块。
2. 发布模块：`:KuiklyPerspectiveCropCore`；Demo 共享模块：**`perspectiveCropDemo`**（不叫 `shared`）。
3. Kuikly 依赖：**2.30.0-2.1.21**（腾讯 Maven 镜像）。
4. 分里程碑推进，每步写清单并验收后再进入下一步。

## Consequences

- 可复用 androidApp / iosApp / h5App / ohos 壳与 Compose 插件配置。
- iOS Pod 路径与 h5 `businessPathName` 需与 `perspectiveCropDemo` 一致。
- 库模块只保留透视裁剪公开面，不含无关 Demo 业务样例源码。
