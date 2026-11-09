# ios-native-crop · feature changelog

## 2026-11-09（沉淀 iOS runbook）

- 起服经验写入 [`../kuikly-dev-ios/runbook.md`](../kuikly-dev-ios/runbook.md)
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：交叉索引｜待批 0（已达标 0）

## 2026-11-09（真机验证通过）

- 真机表步骤 1–7 用户确认 OK（含 Auto/Native/Common、`file://`、旋源、原生放大镜）
- 审查 r2：**通过**（关 `P1-device-verify`）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：真机闭环勾选｜待批 0（已达标 0）

## 2026-11-09（Duo 实测：裁剪炸 / 放大镜黑屏）

- CG 位图回退 `kCGImageAlphaPremultipliedLast`（`kCGImageAlphaLast` 在 iOS `CGBitmapContextCreate` 失败）
- `MagnifierSampler.ios`：对齐 Ohos/Android 真像素 warmCache + sample（网格兜底有 patch）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：模拟器阻断修复｜待批 0（已达标 0）

## 2026-11-09（关审查 P2：旋源 / 文件名 / 直通 alpha）

- `rotateUiImage`：UIGraphics+手动 CTM → 纯 `CGBitmapContext` + `CGContextRotateCTM`（关 `P2-rotate-uigfx`）
- `saveToFile()`：秒级 `time(null)` → 毫秒 + 原子序号 token（关 `P2-filename-collision`）
- CG 位图：`PremultipliedLast` → `kCGImageAlphaLast` 直通 RGBA（关 `P2-premul-alpha`；**已由上一节回退**）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：审查 P2 整改｜待批 0（已达标 0）

## 2026-11-09（iOS Core Image 双引擎出图）

- `cropEngine` Auto/Native → Core Image 四点透视（dest=上边/左边 hypot）；Common → CPU 单应
- 出图解码：旋源后最长边硬顶 `CROP_MAX_EDGE`；`assets://` 按 main → 已加载 Bundle 搜索
- `PerspectiveCropIos.init(cacheDir)`；`PlatformImage.saveToFile` → `file://`
- ADR 0009 / Q40 / CONTEXT 同步；放大镜仍网格兜底（本期非目标）
- 真机表：`docs/reviews/device-check-list/ios-native-crop.md`（步骤待真机勾选）
- 壳：`PerspectiveCropIosBridge` + `iOSApp.init` 注入 Caches；Kuikly framework 名改为 `perspectiveCropDemo`
- 治理收尾：门禁 跳过｜三问 是/是/否｜理由：新 iOS Native 引擎与 init/assets 口径入仓｜待批 0（已达标 0）
