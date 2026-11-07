# ADR 0007: minCropWidth 改为视图逻辑像素

## Status

Accepted（2026-11-08）

## Context

对齐 freequadcrop 时，旧库 `minCropWidth` 存视图 px（`dp2px(100)`），用时 `/ mScaleX` 进图像坐标。本库原先把 `minCropWidth` 当**图像素**直填（默认 48），与 catch / projection / contentInset 的「视图逻辑 px → `/ Fit.scale`」口径不一致；只改数字无法对齐物理门槛。

## Decision

1. `PerspectiveCropConfig.minCropWidth` 语义改为**视图逻辑像素**（默认 `100f`，不引入 Android `dp2px`）。
2. `CropSession` 校验与钳制使用 `minCropWidth / fitScale`（`fitScale` 来自 `ImageFitMapping.scale`）。
3. 公开字段名保持 `minCropWidth`（与旧库同名）；调用方若曾按图像素传值须自行换算。

## Consequences

- 默认最小框随 Fit 缩放与旧库量级对齐；未传自定义值的宿主手感变严。
- 与命中半径同一换算套路，文档 / CONTEXT 需标明单位，避免再当图像素。
