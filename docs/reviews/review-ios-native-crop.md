# Review · ios-native-crop

- 结论：**有条件通过** → 真机关闭见 [review-ios-native-crop-r2.md](review-ios-native-crop-r2.md)（**通过**）
- 依据：Plan「iOS 透视裁剪双引擎」；码农 norms：`kuikly-ios-coding`（`lang_swift`/`lang_objc`/`platform_ios`/`lang_kotlin`/`new_code_file_guidelines`）+ 共用 `perspective_crop_api`

## P0

无。

## P1

### P1-device-verify Lab/真机闭环未勾选
- 证据：Plan §验证要求 Auto/Native/Common 出图、`file://` 预览、`assets://` 解码；`linkPodDebugFrameworkIosSimulatorArm64` 已过，真机表步骤仍空
- 影响：CI 坐标翻转 + hypot 缩放后的几何/色彩、旋源后出图、framework 资源解析无法仅靠静态审查闭环
- 处置：必修（跑表）— 可执行档 → [device-check-list/ios-native-crop.md](device-check-list/ios-native-crop.md)

## P2

### P2-rotate-uigfx `rotateUiImage` 在 UIGraphics 上下文上手动 CTM
- 证据：`IosImageCodec.kt` `UIGraphicsBeginImageContextWithOptions` 后 `CGContextTranslateCTM` + `CGAffineTransformMakeRotation` 再 `drawInRect`（UIKit 上下文本身已翻转）
- 影响：90°/270° 旋源可能方向异常（未证必错）
- 处置：建议 — 真机表步骤 #6 重点验；若错改用 `UIImage` 绘制惯例或 CI 旋转

### P2-filename-collision 落盘文件名仅秒级 `time(null)`
- 证据：`PlatformImage.ios.kt` `perspective_crop_${time(null)}.$ext`
- 影响：同一秒两次裁切可能覆盖
- 处置：可选 — 换毫秒/UUID

### P2-premul-alpha CG 使用 PremultipliedLast 再按直通 RGBA 解包
- 证据：`cgImageToArgb` / `argbToCgImage` `kCGImageAlphaPremultipliedLast` + 按 R,G,B,A 直通打包供 Common/`perspectiveWarpArgb`
- 影响：半透明边缘可能与 Android/Ohos 直通 ARGB 观感略差（样例图多为不透明）
- 处置：可选

## 规范候选观察

1. 审查 skill 强制检查表第 3 条须按改动面路由：iOS `.swift`/`.m`/`iosMain` 对照 `kuikly-ios-coding` norms（与既有「鸿蒙路由 ohos 包」候选同族）。

（已入本 skill `_backlog` 候选）

## 真机

- 可执行档：[device-check-list/ios-native-crop.md](device-check-list/ios-native-crop.md)
- 代码审不了：Native vs Common 透视观感/耗时、旋源方向、`assets://crop_sample.webp` 经 framework 解码、`file://` 再显示、CI Y 翻转后是否上下颠倒

## 开放发现摘要（卡路由）

| 卡 | 结论 |
|---|---|
| B 可达性 | stub 已撤；Auto/Native→CI、Common→Homography；壳 `PerspectiveCropIosBridge.configure` + framework 名 `perspectiveCropDemo` 已接线 |
| A 契约 | dest 宽高与 Android/Ohos hypot 同公式；落盘 `file://`；changelog 已有 `ios-native-crop` |
| C/E | 无命中 |

## 写权限自检

未改业务源码、未改 feature changelog、未改 severity。
