# ios-native-crop · feature changelog

## 2026-11-10（用户确认：放大镜+旋源四角 OK）

- Duo：#6 圆镜像素/触点、#7 0/90/180/270 裁切与放大镜均正常
- 收尾：去掉 `KPC-Mag` 排查日志；采样行翻转与 UIGraphics 旋源保留
- 真机表 [`device-check-list/ios-native-crop.md`](../../reviews/device-check-list/ios-native-crop.md) #6 #7 → OK
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：验收收口｜待批 0（已达标 0）

## 2026-11-10（旋源 90/270：UIGraphics 顺时针）

- 根因：`rotateUiImage` 把 UIKit CTM 套进 CGBitmapContext（Y 向上）→ 90°/270° 裁切与放大镜同坏；0°/180° 仍可看似正常
- 改回 `UIGraphicsBeginImageContextWithOptions` + `UIImage.drawInRect`，与 Stage `rotationZ` / Android `postRotate` 顺时针对齐
- 采样行翻转保留（红顶蓝底证据：绘制保序，缓冲相对预览 Y 颠倒）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：旋源坐标系｜待批 0（已达标 0）

## 2026-11-10（放大镜倒像：采样行翻转）

- 红顶蓝底标记：镜顶红 → 壳 `UIImage.draw` 保序；景物仍倒 → `IosCropBuffer` 相对 Stage 预览 Y 颠倒
- `MagnifierSampler.ios` 拷贝 patch 时 `srcY = (H-1)-(top+y)`；不改 decode/出图
- 去掉临时红蓝标记
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：倒镜像证据修复｜待批 0（已达标 0）

## 2026-11-10（放大镜倒像：去掉 CG Y 翻转）

- 贴图改 `UIImage(cgImage:).draw(in:)`；RGBA 顶左行优先，对齐 Android/鸿蒙，不再 `translate+scale(-1)` 二次翻转
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：倒像/触点｜待批 0（已达标 0）

## 2026-11-10（放大镜灰底：弃协议强转 + 修 CGDataProvider）

- 日志：`publish ok 96x96 rgbaLen=36864`，但 `as? PerspectiveCropIosMagnifierBridging` 失败 → `path=no-bridge` 只铺灰底
- 壳改 `MagBridge`：`method(for:)` / `perform`，不再 `as?` Swift 协议
- `argbToCgImage`：撤回 `NSData as CFDataRef`（KN ClassCast `_NSInlineData`→`CPointer`），恢复 BitmapContext+memcpy
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：灰镜根因修复｜待批 0（已达标 0）

## 2026-11-10（放大镜灰底：RGBA 直传 + 协议 optional）

- `argbToCgImage`：改 `CGImageCreate` + `NSData`/`CGDataProvider`（顶左 RGBA）；抽出 `argbToRgbaBytes` / `toNSDataCopy`
- `MagnifierBlitStore`：始终存 `_rgbaData`+宽高；CG/PNG 失败仍可供壳贴图
- Bridge：`rgbaDataOrNull` / `blitWidth` / `blitHeight`
- Swift：`pngDataOrNull`/`rgba*` 标 `@objc optional`，避免缺方法导致 `as?` 全失败只剩灰底；优先 RGBA→`CGImage`
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：灰镜像素通道｜待批 0（已达标 0）

## 2026-11-10（放大镜镜内黑屏：像素自持 + 壳贴图）

- `argbToCgImage`：`CGBitmapContextCreate(data=null)` + `memcpy`
- `MagnifierBlitStore.publish`：`UIImagePNGRepresentation` 往返，强制 UIImage 自持像素
- Swift `draw`：先铺灰底；有 `cgImage` 时用翻转后的 `CGContextDrawImage` 贴图
- 排障：Pods 链的是 `build/cocoapods/framework`，仅编 `bin/…/podDebugFramework` 不会进模拟器，须 `syncFramework` / Xcode 脚本同步后再装
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：黑镜缓冲+贴图｜待批 0（已达标 0）

## 2026-11-10（放大镜桥协议选择子 + px→pt）

- `PerspectiveCropIosMagnifierBridging.viewPointAt`：加 `@objc(viewPointAtIndex:)` 对齐 KN 头；否则 `as?` 失败 → `draw` 直接 return、拖角完全无镜
- 桥为 nil 时仍画深灰圆+白边（不再静默空白）
- 承接同日 px→pt：几何/线宽 `/ UIScreen.main.scale`
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：桥选择子个案｜待批 0（已达标 0）

## 2026-11-10（iOS 放大镜 px→pt）

- `KRPerspectiveCropMagnifier.draw`：Compose/BlitStore 的 cx/cy/radius、触点、框线点、边框/十字线宽线长统一 `/ UIScreen.main.scale` 再画（对齐鸿蒙 `px2vp`；prop 仍存原始 px）
- 真机表 [`device-check-list/ios-native-crop.md`](../../reviews/device-check-list/ios-native-crop.md) #6：补直径 ~14% Stage 宽期望；结果置「待复验」（Duo 展开 + 普通机）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：壳单位换算个案｜待批 0（已达标 0）

## 2026-11-10（CocoaPods 壳发布接线）

- 桥 `PerspectiveCropIosMagnifierBridge` 迁 Core；`@ObjCName(exact)`；`viewPointCount` / `viewPointAt`
- 新建 `KuiklyPerspectiveCropIOS/` + 根/子 podspec `0.0.1`；iosApp symlink 同 Swift
- Demo：`api` + `framework.export(Core)`；D3：壳改 ObjC 运行时取桥（不 import 宿主模块名，便于 `pod trunk push`）
- 模拟器：`xcodebuild` BUILD SUCCEEDED 并 launch home
- trunk：`KuiklyPerspectiveCrop 0.0.1` 已发布（2026-11-10）；壳 ObjC 运行时取桥；Xcode 27 下 lint 依赖 OpenKuikly 需临时将本地 CDN podspec ios→15
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：发布接线非规范晋升｜待批 0（已达标 0）

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
