# KuiklyPerspectiveCrop

斜拍照片上框选文档或物体，裁剪并做透视校正。基于 **Kuikly Compose** 的多端库。

| 项 | 值 |
|---|---|
| 发布模块 | `KuiklyPerspectiveCropCore` |
| 包名 | `com.jlj.kuiklybase.perspectivecrop` |
| Maven 坐标 | `io.github.sofarnobug:KuiklyPerspectiveCropCore:1.0.0-2.30.0` |
| Kuikly | `2.30.0-2.1.21`（经腾讯 Maven 镜像拉取） |

> 本文件给**接入方**。维护本仓（起服、里程碑、ADR）见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 前置条件

1. 已有 **Kuikly Compose** 宿主工程（本库不是独立 App SDK）。
2. 宿主能解析 **同一 Kuikly 版本**（`2.30.0-2.1.21`）；公开 API 经 `api` 传递 Kuikly Compose。
3. 出图前按端完成 **初始化**（见下；H5 除外）；否则抛 `CropException(code = NOT_INITIALIZED)`。

## 非目标

本库**不做** OCR、边缘检测或内置 autoScan。外部检测结果可通过 `writePoints` / `applyAutoScanPoints` 注入四点。

## 各端依赖清单

| 端 | 必加 | 原生放大镜另加 | 接完成功标准 |
|---|---|---|---|
| **Android** | Maven Core | 无（Core 内 `BitmapShader`） | `init` 后 `cropToFile()` 出路径；拖角见清晰圆镜 |
| **iOS** | Maven Core（进 KN framework） | CocoaPods `KuiklyPerspectiveCrop` **1.0.0** | `init(cacheDir)` 后可落盘；拖角见圆镜像素（非灰底） |
| **鸿蒙** | Maven Core（ohosArm64） | ohpm `@jlj/kuikly-perspective-crop-ohos@1.0.0` | `init` + 注册 View + blit；拖角见圆镜 |
| **H5** | Maven Core（JS 变体） | 无单独 npm/H5 包 | 无需 `PerspectiveCrop*.init`；`cropToFile()` 得 `blob:`；镜为网格兜底 |

版本后缀与 Kuikly 对齐：`1.0.0-2.30.0` ↔ Kuikly `2.30.0-2.1.21`。CocoaPods / ohpm 主版本与库主版本对齐为 `1.0.0`（无 `-2.30.0` 后缀）。

## 添加依赖（Maven Core · 全端）

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
}

dependencies {
    implementation("io.github.sofarnobug:KuiklyPerspectiveCropCore:1.0.0-2.30.0")
}
```

把 Core 编进宿主的 **Kotlin Multiplatform / Kuikly 共享模块**（或 Android 直接依赖）。不要只加原生 Pod/HAR 却不加 Core——裁切与 Stage 在 Core 里。

## 概念流

```text
相机 filePath / 相册 uri / assets
        ↓ imageSrc: String
PerspectiveCropStage   // Image + 叠层框选 + 放大镜
        ↓ suspend
crop() → PlatformImage  或  cropToFile() → String
```

## 各端初始化与原生镜

出图 / `saveToFile` 前必须按端初始化（H5 除外）。

### Android

**依赖**：仅 Maven Core。  
**落点**：`Application.onCreate`（或等价最早入口）。

```kotlin
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropAndroid

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PerspectiveCropAndroid.init(this)
    }
}
```

### iOS

**依赖**：

1. 共享 KN 模块：`implementation("…KuiklyPerspectiveCropCore:1.0.0-2.30.0")`，并在产生 framework 时 **export** Core（让壳运行时能取到 `PerspectiveCropIosMagnifierBridge`）。
2. App `Podfile` 另加原生镜：

```ruby
pod 'KuiklyPerspectiveCrop', '1.0.0'
```

**落点**：App 启动时注入可写缓存目录（`cropToFile` / `saveToFile` 用）：

```swift
// 推荐：KN 暴露的桥（本仓 Demo：PerspectiveCropIosBridge）
let caches = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first!
PerspectiveCropIosBridge.shared.configure(cacheDir: caches.path)
```

等价 Kotlin：`PerspectiveCropIos.init(cacheDir)`。

**说明**：

- 类名 `KRPerspectiveCropMagnifier` 由 Kuikly 按名解析，**无需** `registerExternalRenderView`。
- 壳经 ObjC 名 `PerspectiveCropIosMagnifierBridge` 读 blit；若未 link / 未 export Core，圆镜会灰底无像素。
- `assets://` 在 main Bundle 与已加载 Bundle 中按资源名查找。

### 鸿蒙

**依赖**：

1. KMP ohosArm64：Maven Core `1.0.0-2.30.0`。
2. 原生镜：

```bash
ohpm install @jlj/kuikly-perspective-crop-ohos@1.0.0
```

**落点（三处都要）**：

1. Kotlin：`PerspectiveCropOhos.init(resfileDir, cacheDir)`（`resfileDir` 解 `assets://`；`cacheDir` 可空，空则带 path 的 `saveToFile` 会失败）。本仓 Demo 经 `perspectiveCropDemo/.../PerspectiveCropOhosNative.kt` 从壳转发。
2. 注册 View（`KuiklyViewDelegate`）：

```typescript
import { KRPerspectiveCropMagnifierView } from '@jlj/kuikly-perspective-crop-ohos';

map.set(
  KRPerspectiveCropMagnifierView.VIEW_NAME,
  () => new KRPerspectiveCropMagnifierView(),
);
```

3. 注入 blit（宿主 so 须导出 `getMagnifierBlit`，可对照 Demo `libentry`）：

```typescript
import {
  KRPerspectiveCropMagnifierView,
  PerspectiveCropOhosNative,
} from '@jlj/kuikly-perspective-crop-ohos';
import Napi from 'libentry.so';

PerspectiveCropOhosNative.configure(() => Napi.getMagnifierBlit());
```

### H5

- **只依赖 Maven Core 的 JS 变体**；没有单独 npm / H5 壳包。
- **无** `PerspectiveCropAndroid/Ios/Ohos.init`。
- `cropToFile()` 成功时常返回 **`blob:`** ObjectURL，可再赋给预览 `imageSrc`；刷新或 revoke 后失效。
- 放大镜目前为采样缓存 + **网格绘制兜底**（非原生贴图）。

## 最小接入示例

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.jlj.kuiklybase.perspectivecrop.CropException
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropStage
import com.jlj.kuiklybase.perspectivecrop.rememberPerspectiveCropController
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun CropPage(imageSrc: String) {
    val controller = rememberPerspectiveCropController()
    val scope = rememberCoroutineScope()

    PerspectiveCropStage(
        modifier = Modifier.fillMaxSize(),
        controller = controller,
        imageSrc = imageSrc, // 或先 controller.applyImageSrc(src)
    )

    // 确认裁切（按钮 onClick 里调用）
    fun onConfirm() {
        scope.launch {
            try {
                val outSrc = controller.cropToFile()
                // 展示 outSrc
            } catch (e: CropException) {
                // "${e.code}: ${e.message}"
            }
        }
    }
}
```

常用命令：`controller.resetFullImage()`、`controller.rotateClockwise()`、`controller.canCrop()`、`controller.updateConfig(…)`。

完整最小页：`perspectiveCropDemo/.../demo/PerspectiveCropDemoPager.kt`。

### 接入自检

1. 依赖：该端「必加」已进工程；需要镜的端「另加」也已进。
2. 启动后完成对应 `init` / 注册 / blit（H5 跳过）。
3. 设好 `imageSrc` → 拖角/边 → 点确认 → `cropToFile()` 有路径或 `blob:`，失败为带 `code` 的 `CropException`（不静默成功）。
4. Android / iOS / 鸿蒙：拖角圆镜应见贴图像素；H5 可为网格。

## 接入避坑（高频）

| 坑 | 典型现象 | 怎么避 |
|---|---|---|
| 只加 CocoaPods / ohpm，不加 Maven Core | 没有 Stage / 裁切 API | Core 必加；原生包只补放大镜壳 |
| 未按端 `init`（H5 除外） | `CropException(NOT_INITIALIZED)` / 落盘失败 | Android `Application`；iOS 启动注入 `cacheDir`；鸿蒙 `init` + 壳转发 |
| iOS 未 export Core / 桥链不上 | 圆镜灰底、无贴图像素 | KN framework **export** Core；开 **xcworkspace**；Pod `KuiklyPerspectiveCrop` |
| iOS 打开裸 `xcodeproj` | Pod / KN 链失败 | 必须 `*.xcworkspace` |
| 鸿蒙未 `map.set` 放大镜 View | 无镜或占位异常 | `KuiklyViewDelegate` 注册 `KRPerspectiveCropMagnifierView` |
| 鸿蒙未 `PerspectiveCropOhosNative.configure(blit)` | 黑镜 / 无像素 | 宿主 so 导出 `getMagnifierBlit` 并 configure |
| `showOperationUI = false` 当成「只藏手柄」 | 六层叠层 + 原生镜整包不画；手势仍可用 | 要隐藏操作 UI 才关；细控用 `showMagnifier` 等 |
| 把 `config.toViewPx(density)` 再 `updateConfig` | 框线/命中区被放大两次 | 公开 Config 保持 **dp**；`lengthsAreViewPx` 不是产品开关 |
| H5 当持久文件路径用 `cropToFile()` | 刷新后预览失效 | 返回常为 `blob:`，需立刻展示或自行转存 |
| 期望 H5 原生清晰镜 | 只有网格兜底 | 当前设计如此；清晰镜看 Android / iOS / 鸿蒙 |
| `imageSrc` 协议错 / H5 跨域无 CORS | `DECODE_FAILED` | 核对 `file://` / `content://` / `assets://` / 可访问 URL |
| 业务皮肤写进库默认 Painter | 升级被覆盖、污染默认主题 | 皮肤只放宿主；用六层 SPI 注入 |
| Android 折叠开合未声明 `configChanges` | Activity 重建 → Kuikly 页重挂，Compose `remember` 全丢（像回首页） | 宿主 Activity 对齐 KuiklyUI：`configChanges` 含 `screenSize` / `smallestScreenSize` / `screenLayout` / `orientation`；尺寸由 `rootViewSizeDidChanged` 刷新。本仓 Demo 已加；接入方勿漏 |
| iPhone Duo 摄像头挖孔压住顶栏/Chip | Lab「外观」Chip 或状态区落在黑圆上 | 读 `pageData.reservedRegions` 中生效的 `OCCLUSION` 自行垫开（非 Core Config）。本仓 Lab 默认开「遮挡避让」；最小 Demo 始终避让。宿主业务页须自己订阅，库不会代垫 |

Demo 起服级排障（白屏、webpack、UMD 等）见 [CONTRIBUTING.md](CONTRIBUTING.md) 与各端 runbook，不在本表展开。

## 相机 / 相册 → `imageSrc`

库不强制依赖相机/相册；把路径或 uri 交给 Controller 即可：

```kotlin
// 相机：KuiklyCamera → onPhotoCaptured { filePath -> … }
controller.applyImageSrc(filePath)           // 或 file://$filePath

// 相册：KuiklyPhotoSelector → KRAlbumImage.uri
controller.applyImageSrc(uri)                // content://… / file://…

PerspectiveCropStage(controller = controller, imageSrc = controller.imageSrc)
```

- [KuiklyCamera](https://github.com/Kuikly-contrib/KuiklyCamera)
- [KuiklyPhotoSelector](https://github.com/Kuikly-contrib/KuiklyPhotoSelector)

## 出图 API 与错误码

```kotlin
suspend fun crop(): PlatformImage
suspend fun cropToFile(): String
suspend fun cropToFile(path: String): String
```

失败抛 `CropException(code, message, cause?)`，**不会**静默回原图或返回可空假成功。

| `code` | 含义 |
|---|---|
| `EMPTY_IMAGE_SRC` | 未设 `imageSrc` |
| `ILLEGAL_POINTS` | 四点非法（可先查 `canCrop()`） |
| `NOT_INITIALIZED` | 未调用对应端 `init` |
| `DECODE_FAILED` | 原图解码失败 |
| `TRANSFORM_FAILED` | 透视变换失败 |
| `WRITE_FAILED` | 落盘失败（含空 path） |
| `UNSUPPORTED_PLATFORM` | 当前端/引擎组合不支持 |

## 能力矩阵

| 端 | `cropEngine`（默认 Auto） | 放大镜 |
|---|---|---|
| **Android** | Auto/Native → `setPolyToPoly`；Common → CPU 单应 | 原生贴图（`BitmapShader`） |
| **iOS** | Auto/Native → Core Image 四点透视；Common → CPU 单应 | 原生贴图（`MagnifierBlitStore` + 壳桥） |
| **鸿蒙** | Auto/Native → Drawing `SetPolyToPoly`；Common → CPU 单应 | 原生贴图（Blit + NAPI 快照） |
| **H5** | 始终 Common（CPU 单应）；`cropToFile` 可返回 `blob:` | 采样缓存 + 网格绘制兜底 |

`CropEngine.Native` 在无原生路径的端上会明确失败（带 `CropException`），不会假装成功。

## Config 旋钮表

产品页一般用默认即可。UI 长度字段为 **Compose dp**（Stage 内经 density 换算）；`cornerMaxStepPx`、`magnifierCacheMaxEdgePx` 为像素语义，不按 dp 缩放。

| 字段 | 默认 | 说明 | 何时用 |
|---|---|---|---|
| `rectMode` | `false` | 自由四边形 ↔ 轴对齐矩形 | 要矩形裁剪时开 |
| `edgeSnapToTouchEnabled` | **`true`** | 矩形模式拖边吸到触点 | **仅 rectMode 有感**；边要跟指尖走时保持开 |
| `edgeClampToBoundsEnabled` | **`true`** | 拖边顶到图边时两端齐步、不斜边（自由/矩形均生效） | 不要到边斜掉 → 保持开 |
| `insideDragEnabled` | `true` | 框内拖整框 | 不要框内平移时关 |
| `unhitPassthrough` | **`false`** | DOWN 未命中手柄时交给父容器 | 嵌横滑页、要点空白翻页时开 |
| `touchCornerCatchDistance` | `20` | 角命中半径（dp；≤0 关闭） | 角难点 → 加大；禁角拖 → 0 |
| `touchEdgeCatchDistance` | `20` | 边命中带宽（dp；≤0 关闭） | 边难抓 → 加大 |
| `touchMidCatchDistance` | `30` | 边中点命中半径（dp；≤0 关闭） | 开了中点绘制后再调 |
| `touchEdgeProjectionError` | **`100`** | 抓边沿边外延（dp） | 宽松默认；严谨须贴边线段 → 0（仍可抓边，非仅中点） |
| `minCropWidth` | **`100`** | 最小边长（dp） | 允许更小框 → 调小；防缩没 → 调大 |
| `minAngleLimit` | `20` | 最小内角（度）；`0` 不校验 | 要更严/更松的凸性约束时调 |
| `cornerGrabOffsetEnabled` | `false` | 自由角保留「手指−角点」按压偏移 | 多数不用；默认帧增量即可 |
| `cornerMaxStepPx` | `0` | 自由角单帧最大位移（**图像素**；≤0 不限） | 甩动大跳时再限 |
| `contentInsetLeft/Top/Right/Bottom` | `0` | 内容区内缩（dp；蒙版仍满屏） | 裁剪区外还要摆控件时开 |
| `showOperationUI` | **`true`** | 绘制选区操作 UI（六层叠层 + 原生镜） | 只要底图/仍可拖 → 关（对齐旧库 hideOperationUI 取反） |
| `showMagnifier` | **`true`** | 绘制放大镜 | 不要镜 → 关 |
| `showGuideLines` | `false` | 九宫格（整图 Fit） | 要构图辅助线 → 开 |
| `showEdgeMidPoints` | `false` | 绘制边线中点 | 要边上圆点 → 开；关后仍可拖边 |
| `showCornerBorder` | **`true`** | 角点描边 | 白实心无红圈 → 关 |
| `showMagnifierCropLineMirror` | **`true`** | 镜内再画裁剪框线段 | 镜里只要图 → 关 |
| `showMagnifierCenterCross` | `false` | 镜心十字 | 要对准像素 → 开 |
| `magnifierRadiusRatio` | `0.07` | 镜半径占视图宽比 | 调镜大小 |
| `magnifierBorderWidth` / `magnifierBorderColor` | `2` / 白 | 镜边框 | 换皮时调 |
| `magnifierCrossColor` / `magnifierCrossLineWidth` / `magnifierCrossLineLength` | 白 / `3` / `15` | 十字样式（dp） | 开了十字再调 |
| `magnifierPlacement` | **`TopAutoFlip`** | 圆镜摆放 | 挡内容时改 `TopLeft` / `TopRight` / `AboveTouch` |
| `magnifierCacheMaxEdgePx` | **`2048`** | 采样缓存最长边（**真实 px**） | 低端卡/省内存 → `1024` |
| `maskAlpha` | `0.55` | 蒙版透明度 | 换品牌皮 |
| `borderWidthPx` / `borderColor` | `2` / 红 | 框线（字段名历史遗留，语义为 dp） | 换皮 |
| `cornerRadiusPx` | `8` | 角点半径（dp） | 换皮 |
| `cornerFillColor` / `cornerFillAlpha` | 白 / `1` | 角点填充（ARGB + 0–1 α） | Accent Ring 默认白芯 |
| `cornerBorderWidth` / `cornerBorderColor` | `2` / 红 | 角点描边（dp；须 `showCornerBorder`） | 换皮 |
| `guideLineWidth` / `guideLineColor` | `1` / 半透明白 | 辅助线 | 开了九宫格再调 |
| `outputFormat` / `jpegQuality` | `Jpeg` / `92` | 导出格式与 JPEG 质量 | 要 PNG 或调质量时 |
| `cropEngine` | **`Auto`** | 出图引擎：`Auto` / `Native` / `Common` | 强制原生或强制 CPU 时改 |

**警告**：不要把 `config.toViewPx(density)` 的结果再传给 `updateConfig` / Session；公开配置保持 **dp**。`lengthsAreViewPx` 是库内防双重放大标记，不是产品开关。

```kotlin
controller.updateConfig(
    controller.config.copy(rectMode = true, showGuideLines = true),
)
```

## 外部四点注入

四点为**图像素**，顺序 TL → TR → BR → BL：

```kotlin
controller.writePoints(listOf(tl, tr, br, bl))
// 或
controller.applyAutoScanPoints(listOf(tl, tr, br, bl)) // 等价别名
```

## 换皮 SPI（六层可分别注入）

叠层分为蒙版 / 框线 / 角点 / 边中点 / 辅助线 / 放大镜，可单独替换；门面：

```kotlin
import com.jlj.kuiklybase.perspectivecrop.overlay.CropOverlayPainters
import com.jlj.kuiklybase.perspectivecrop.overlay.BorderOverlayPainter
// Mask / Corner / EdgeMid / Guide / Magnifier 同理

val painters = CropOverlayPainters(
    border = BorderOverlayPainter { viewPoints, config ->
        // DrawScope 内自绘框线
    },
    // 其余层省略则用 Default*OverlayPainter
)
controller.updatePainters(painters)
// 或 PerspectiveCropStage(..., painters = painters)
// 或 rememberPerspectiveCropController(painters = painters)
```

接口：`MaskOverlayPainter`、`BorderOverlayPainter`、`CornerOverlayPainter`、`EdgeMidOverlayPainter`、`GuideOverlayPainter`、`MagnifierOverlayPainter`。

完整换皮示例见 Demo Lab：`perspectiveCropDemo/.../demo/LabSkinPainters.kt`、`PerspectiveCropLabPager.kt`。业务皮肤放在宿主/Demo，不要改库默认实现。

## 参考 Demo

| pageName | 角色 |
|---|---|
| `perspective_crop_demo` | 最小实现（框选 + 确认裁切） |
| `perspective_crop_lab` | 全能 Lab（Toggle / 换皮 / 缓存边长 / writePoints） |

源码目录：`perspectiveCropDemo/src/commonMain/kotlin/.../demo/`。

本仓快速看一眼（需 JDK 17）：

```bash
./gradlew :androidApp:installDebug
```

H5 / 鸿蒙 / iOS 起服与排障见 [CONTRIBUTING.md](CONTRIBUTING.md)。
