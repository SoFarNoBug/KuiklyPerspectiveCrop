# KuiklyPerspectiveCrop

斜拍照片上框选文档或物体，裁剪并做透视校正。基于 **Kuikly Compose** 的多端库。

| 项 | 值 |
|---|---|
| 发布模块 | `KuiklyPerspectiveCropCore` |
| 包名 | `com.jlj.kuiklybase.perspectivecrop` |
| Maven 坐标 | `io.github.sofarnobug:KuiklyPerspectiveCropCore:1.0.0-2.30.0` |
| Kuikly | `2.30.0-2.1.21`（经腾讯 Maven 镜像拉取） |

> 维护本仓（起服、里程碑、ADR）见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 非目标

本库**不做** OCR、边缘检测或内置 autoScan。外部检测结果可通过 `writePoints` / `applyAutoScanPoints` 注入四点。

## 添加依赖

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
}

dependencies {
    implementation("io.github.sofarnobug:KuiklyPerspectiveCropCore:1.0.0-2.30.0")
}
```

版本后缀与 Kuikly 对齐：库默认 `1.0.0-2.30.0` 对应 Kuikly `2.30.0-2.1.21`。本仓 Demo 使用 `implementation(project(":KuiklyPerspectiveCropCore"))`。

公开 API 通过 `api` 传递 Kuikly Compose；消费方需能解析同一 Kuikly 版本。

## 概念流

```text
相机 filePath / 相册 uri / assets
        ↓ imageSrc: String
PerspectiveCropStage   // Image + 叠层框选 + 放大镜
        ↓ suspend
crop() → PlatformImage  或  cropToFile() → String
```

## 各端初始化

出图 / `saveToFile` 前必须按端初始化（未 init 会抛 `CropException`，`code = NOT_INITIALIZED`）。

### Android

在 `Application.onCreate`（或等价入口）调用：

```kotlin
PerspectiveCropAndroid.init(application)
```

### iOS

注入可写缓存目录（如 `NSCachesDirectory`），供 `cropToFile()` / `PlatformImage.saveToFile()` 落盘：

```kotlin
PerspectiveCropIos.init(cacheDir)
```

`assets://` 默认在 main Bundle 与已加载 Bundle 中按资源名查找。

KMP 层用 Maven Core；**原生放大镜 View**另加 CocoaPods（类名 `KRPerspectiveCropMagnifier`，Kuikly 按名解析，无需 `registerExternalRenderView`）：

```ruby
pod 'KuiklyPerspectiveCrop', '1.0.0'
```

配套 Maven：`io.github.sofarnobug:KuiklyPerspectiveCropCore:1.0.0-2.30.0`。  
壳经 ObjC 名 `PerspectiveCropIosMagnifierBridge` 运行时取桥（宿主须 link 含该桥的 KN framework，并 `framework.export` Core）。

### 鸿蒙

注入 hap `resourceDir`（解码 `assets://`）与可写缓存目录：

```kotlin
PerspectiveCropOhos.init(resfileDir, cacheDir)
// cacheDir 可省略；为空时无参 saveToFile 会失败
```

原生放大镜 View 另加 ohpm（须在 `KuiklyViewDelegate` 注册，并注入 blit NAPI）：

```bash
ohpm install @jlj/kuikly-perspective-crop-ohos
```

```typescript
import {
  KRPerspectiveCropMagnifierView,
  PerspectiveCropOhosNative
} from '@jlj/kuikly-perspective-crop-ohos';
import Napi from 'libentry.so'; // 宿主 so，须导出 getMagnifierBlit

PerspectiveCropOhosNative.configure(() => Napi.getMagnifierBlit());
// map.set(KRPerspectiveCropMagnifierView.VIEW_NAME, () => new KRPerspectiveCropMagnifierView());
```

配套 Maven Core `1.0.0-2.30.0`。Demo 壳 NAPI 转发见 `perspectiveCropDemo/.../PerspectiveCropOhosNative.kt`。

### H5

无对等 `PerspectiveCrop*.init`。`cropToFile()` 成功时可能返回 `blob:` URL，可再作预览 `imageSrc`。

## 最小接入示例

```kotlin
val controller = rememberPerspectiveCropController()
val scope = rememberCoroutineScope()

PerspectiveCropStage(
    modifier = Modifier.fillMaxSize(),
    controller = controller,
    imageSrc = imageSrc, // 或先 controller.applyImageSrc(src)
)

// 确认裁切
scope.launch {
    try {
        val outSrc = controller.cropToFile()
        // 展示 outSrc
    } catch (e: CropException) {
        // "${e.code}: ${e.message}"
    }
}
```

常用命令：`controller.resetFullImage()`、`controller.rotateClockwise()`、`controller.canCrop()`、`controller.updateConfig(…)`。

完整最小页对照：`perspectiveCropDemo/.../demo/PerspectiveCropDemoPager.kt`。

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
