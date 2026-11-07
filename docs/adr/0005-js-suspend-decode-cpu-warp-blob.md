# ADR 0005: JS suspend 解码 + CPU 单应出图 + blob: 落盘

## Status

Accepted（2026-11-07 M4）

## Context

M4 要在浏览器实装真像素放大镜与透视裁剪。Canvas/`createImageBitmap` 解码异步，而既有 `MagnifierSampler.warmCache` / `CropProcessor.process` 为同步 expect；Canvas 2D 无 `setPolyToPoly`；浏览器无 App 缓存目录可写 `file://`。

## Decision

1. **`warmCache` / `process` 改为 `suspend`**：Android/iOS actual 仍同步干活；JS actual `await` 解码。`sample` / `PlatformImage.saveToFile` 保持同步。
2. **透视用 CPU 单应 + 目标像素双线性反变换**（对齐 Q17/Q21）；不引入 WebGL / OpenCV.js。输出最长边硬顶 **4096**，超则 `TRANSFORM_FAILED`。
3. **H5 `saveToFile()` 返回 `blob:` ObjectURL**（`toDataURL`→Blob→`createObjectURL`）；`saveToFile(path)` 忽略本地路径语义，同样返回 `blob:`。非持久文件，刷新/revoke 失效。
4. **仅 JS actual 实装**；iOS 继续 stub。v0.1 / Q1 / Q23 不回写。

## Consequences

- 跨端 expect 签名有一次兼容性变更（调用点已在协程内）。
- Demo 可用同一 `assets://` 源在 H5 上验放大镜与裁切；CORS 失败抛 `DECODE_FAILED`。
- 大图 CPU 出图可能慢；硬顶避免卡死；日后可另开 WebGL 优化里程碑。

全文台账见 [docs/decisions.md](../decisions.md) Q33–Q37。
