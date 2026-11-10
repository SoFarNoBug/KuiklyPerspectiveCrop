# iOS 壳冒烟（已验证 2026-11-09）

## 结论

- **已打通（macOS）**：`generateDummyFramework` → `podspec` → `pod install` → `iosApp.xcworkspace` → 模拟器 / 真机装机启动。
- **裁切**：`Auto`/`Native` → Core Image 四点透视（dest=上边/左边 hypot）；`Common` → CPU 单应；落盘 `file://`；须 `PerspectiveCropIos.init(cacheDir)`（壳 `iOSApp` → `PerspectiveCropIosBridge.configure`）。
- **放大镜**：KN `MagnifierBlitStore`（UIImage）+ 壳 `KRPerspectiveCropMagnifier`（`KRView`，按类名运行时解析）；采样 `MagnifierSampler.ios`。
- 真机表：[device-check-list/ios-native-crop.md](../../reviews/device-check-list/ios-native-crop.md)（步骤 1–7 OK）。

## 标准步骤

前置：Xcode（含 iOS Simulator SDK）；CocoaPods；本仓旁有可编译的 `KuiklyUI`（见下「OpenKuikly 依赖」）；JDK 供 Gradle 编 KN framework。

```bash
# 仓库根
./gradlew :perspectiveCropDemo:generateDummyFramework
./gradlew :perspectiveCropDemo:podspec

cd iosApp
pod install
open iosApp.xcworkspace
```

Xcode：选目标机（如 **iPhone Duo** 模拟器或真机）→ Run scheme **iosApp**。

命令行装模拟器示例（UUID 以 `xcrun simctl list devices` 为准）：

```bash
UDID=<simulator-udid>
cd iosApp
xcodebuild -workspace iosApp.xcworkspace -scheme iosApp -configuration Debug \
  -destination "platform=iOS Simulator,id=$UDID" \
  -derivedDataPath /tmp/KPC-ios-dd build
xcrun simctl install "$UDID" /tmp/KPC-ios-dd/Build/Products/Debug-iphonesimulator/iosApp.app
xcrun simctl launch "$UDID" com.jlj.kuiklyperspectivecropui.iosApp
```

| 改了什么 | 重跑什么 |
|---|---|
| Core / Demo `iosMain` 或 common 出图/放大镜 | Xcode Run（Pods 脚本会 `syncFramework`）；或清 DerivedData 后再编 |
| `Podfile` / podspec / 依赖版本 | `cd iosApp && pod install`，再开 **xcworkspace** |
| 壳 Swift/ObjC（含 `KRPerspectiveCropMagnifier`） | 仅 Xcode 重编 iosApp |
| `assets` 样例图 | 确认进 framework resources；必要时 Clean + Run |

## OpenKuikly 依赖

| 方式 | 说明 |
|---|---|
| **本仓默认** | `Podfile`：`pod 'OpenKuiklyIOSRender', :path => '../../KuiklyUI'`（邻仓；免 trunk 拉 GitHub tag 卡住） |
| Trunk | `pod 'OpenKuiklyIOSRender', '~> 2.30.0'`；首次可能慢/超时，需可访问 GitHub |

邻仓路径不存在时改回 trunk，或把 `KuiklyUI` clone 到 `../../KuiklyUI`。

## 工程注意

| 项 | 说明 |
|---|---|
| 打开工程 | **必须** `iosApp.xcworkspace`（勿开裸 `xcodeproj`） |
| Framework 名 | cocoapods `baseName` = `perspectiveCropDemo`；`KuiklyRenderViewController` `fetchContextCode` 回同名；Swift `import perspectiveCropDemo` |
| Deployment | **iOS 15.0+**（Xcode 27 / iOS 27 SDK 下限；`Podfile` `post_install` 抬齐 Pods） |
| `PerspectiveCropIos.init` | `iOSApp.init` → `PerspectiveCropIosBridge.configure(cacheDir:)`（Caches） |
| `assets://` | Bundle 搜索：main → `allBundles` → `allFrameworks`；样例图随 Demo pod resources 进包 |
| CG 位图 | 用 `kCGImageAlphaPremultipliedLast`；**勿** `kCGImageAlphaLast`（`CGBitmapContextCreate` 失败 → 裁切炸 / 黑镜） |
| 放大镜 View | 类名 **`KRPerspectiveCropMagnifier`**（与 `viewName()` 一致）；源在 `KuiklyPerspectiveCropIOS/`，Demo 经 `iosApp/.../Views/` **symlink**；iOS **无需** `registerExternalRenderView` |
| 放大镜桥 | Core `PerspectiveCropIosMagnifierBridge`（Demo framework **export** Core）；壳 `import perspectiveCropDemo` |
| 裁切引擎 | Lab 可切 Auto / Native / Common；默认 Auto→CI |

## 排障

| 现象 | 先查 | 处理 |
|---|---|---|
| `No podspec found for perspectiveCropDemo` | 是否只有陈旧 `shared.podspec` | `./gradlew :perspectiveCropDemo:podspec` + `generateDummyFramework` |
| `pod install` 卡在 Git clone OpenKuikly | trunk 拉 `Tencent-TDS/KuiklyUI` | 改用邻仓 `:path => '../../KuiklyUI'` |
| Deployment target 9.0/14.1 报错 | Xcode 27 SDK | `platform :ios, '15.0'` + `post_install` 抬齐 |
| 裁切 `DECODE_FAILED` / `CGBitmapContextCreate failed` | 位图 alpha | 确认 PremultipliedLast，勿 AlphaLast |
| 放大镜黑圆 / 无贴图 | `platformSupportsNativeMagnifier`、类是否进包 | 确认 native=true；`NSClassFromString(@"KRPerspectiveCropMagnifier")`；拖角前 warmCache |
| `NOT_INITIALIZED` / 落盘失败 | 未 inject cache | 确认 `PerspectiveCropIosBridge.configure` 在 `iOSApp.init` |
| `assets://` 解不出 | 资源未进 Bundle | Clean；查 Pods resources / framework 内 `crop_sample.webp` |
| 打开了 xcodeproj 链不上 KN | 未用 workspace | 关工程，开 `iosApp.xcworkspace` |

## 能力边界

- **可验**：home / Demo / Lab；Auto·Native·Common 出图；`file://` 再显示；旋源；原生圆镜拖动清晰；`CropException` 失败面。
- **非本 runbook**：H5 / 鸿蒙起服（各见对应 runbook）；Android device-check-list。
