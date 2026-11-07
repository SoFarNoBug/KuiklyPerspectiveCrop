# M4 — H5 真像素放大镜 + 透视裁剪

> **post-v0.1**。依赖 [M3](./M3-checklist.md) 工程面；**不**改写 v0.1 / Q1 / Q23。  
> 已定：**Q33–Q37**（见 [decisions.md](../decisions.md) / [ADR 0005](../adr/0005-js-suspend-decode-cpu-warp-blob.md)）。  
> 分段验收：先 Sampler，再 Processor（Q15）。

## 非目标（本里程碑）

- 不实装 iOS 真采样 / 真出图（继续 stub）
- 不引入 WebGL / OpenCV.js
- 不回写 v0.1 完成定义
- 不改 common 公开 `Bitmap` / `ImageBitmap` 设图契约

## 文档

- [x] [decisions.md](../decisions.md) Q33–Q37 + 分期映射 M4
- [x] [ADR 0005](../adr/0005-js-suspend-decode-cpu-warp-blob.md)
- [x] [milestones/README](./README.md) 挂 M4
- [x] [kuikly-dev-h5/runbook](../specs/kuikly-dev-h5/runbook.md) 能力边界更新

## 工程 · 跨端签名（Q34）

- [x] `MagnifierSampler.warmCache` → `suspend`
- [x] `CropProcessor.process` → `suspend`
- [x] Android / iOS actual 仅加 `suspend`，行为不变
- [x] Stage / Controller 协程调用点适配

## 工程 · 分段 A：JS 放大镜（Q33/Q36）

- [x] `JsImageCodec`：`assets://` 映射 + Image 解码 + 旋转/降采样
- [x] `MagnifierSampler.js.kt`：`warmCache` 建缓存；`sample` → `MagnifierPatch`
- [x] 浏览器：拖角后 overlay canvas 出现多样像素（非空灰圆）

## 工程 · 分段 B：JS 裁剪（Q33/Q35/Q37）

- [x] `CropProcessor.js.kt`：CPU 单应出图；最长边 ≤4096
- [x] `PlatformImage.js.kt`：`saveToFile` → `blob:`
- [x] 浏览器：确认裁切成功；`blob:` 结果预览可显示（`ok · blob:…`）
- [x] 空图 / 非法四点仍抛 `CropException`（Q16/Q22；既有 Demo toast）

## 验收标准

1. `http://localhost:8080/?page_name=perspective_crop_demo`：拖角有真像素放大镜  
2. 确认裁切不再 `UNSUPPORTED_PLATFORM`；结果图可再显示  
3. iOS / 非 JS 目标行为与 M3 一致（stub）
