# KuiklyPerspectiveCrop

斜拍照片上框选文档或物体，裁剪并做透视校正。基于 **Kuikly Compose**（库 + Demo 多端工程）。

| 项 | 值 |
|---|---|
| 库模块 | `:KuiklyPerspectiveCropCore` |
| Demo 模块 | `:perspectiveCropDemo`（不叫 shared） |
| 包名 | `com.jlj.kuiklybase.perspectivecrop` |
| Kuikly | `2.30.0-2.1.21`（腾讯 Maven 镜像） |
| 坐标示例 | `io.github.sofarnobug:KuiklyPerspectiveCropCore:0.0.1-2.30.0` |

## 快速概念

```text
相机 filePath / 相册 uri
        ↓ imageSrc: String
PerspectiveCropStage   // Image + 叠层框选 + 放大镜
        ↓ suspend
crop() → PlatformImage  或  cropToFile() → String
```

## 相机 / 相册 → `imageSrc`（Q12）

Demo **不强制依赖**相机/相册库；对接示例：

```kotlin
// 相机：KuiklyCamera → onPhotoCaptured { filePath -> … }
controller.applyImageSrc(filePath)           // 或 file://$filePath

// 相册：KuiklyPhotoSelector → KRAlbumImage.uri
controller.applyImageSrc(uri)                // content://… / file://…

// Stage
PerspectiveCropStage(controller = controller, imageSrc = controller.imageSrc)
```

- [KuiklyCamera](https://github.com/Kuikly-contrib/KuiklyCamera)
- [KuiklyPhotoSelector](https://github.com/Kuikly-contrib/KuiklyPhotoSelector)

外部边缘检测结果可用 `controller.writePoints(…)` / `applyAutoScanPoints(…)` 注入四点（库内无 autoScan 算法，Q27）。

## Config 旋钮表（M3）

产品页一般用库默认即可。下表「何时用」= **接产品时的痛点决策**（Lab chip 同口径：痛点 → 要不要开）。

| 字段 | 默认 | 说明 | 何时用 |
|---|---|---|---|
| `rectMode` | `false` | 自由四边形模式 ↔ 矩形模式 | 要轴对齐矩形裁剪时开 |
| `edgeSnapToTouchEnabled` | **`true`** | 矩形模式触点吸附（Q9） | **仅 rectMode**；拖边要跟触点走时保持开 |
| `edgeClampToBoundsEnabled` | **`true`** | 拖边顶到图边时两端齐步、不斜边（Q9） | 不要到边斜掉/顿挫 → 保持开 |
| `insideDragEnabled` | `true` | 框内拖整框 | 不要框内平移时关 |
| `unhitPassthrough` | **`false`** | 触摸事件穿透（Q26）：未命中手柄时交给外层 | 嵌横滑页、要外层翻页时开（Lab 不暴露） |
| `touch*CatchDistance` | 角 20 / 边 20 / 中 30 | 手柄好不好点到（**Compose dp**；× density；≤0 关闭） | 手柄难点 → 加大；只要看不能拖 → 置 0 |
| `touchEdgeProjectionError` | **100** | 边线判定外延（**Compose dp**） | 宽松可抓角外延长 → 保持默认；严谨须贴边线段 → 置 0（非仅中点） |
| `minCropWidth` | **100** | 最小边长（**Compose dp**；× density `/ Fit.scale`，ADR 0007/0008） | 要允许更小框 → 调小；要更难缩没 → 调大 |
| `cornerGrabOffsetEnabled` | `false` | 自由角保留「手指−角点」按压偏移 | 多数不用；默认帧增量即可（Lab 不暴露） |
| `cornerMaxStepPx` | `0` | 自由角单帧最大位移（图像素；≤0 不限） | 甩动过大跳时再限；默认不用 |
| `contentInset*` | 0 | 内缩显示（**Compose dp**；蒙版仍满屏） | 裁剪区外还要摆控件、图需往里缩时开；普通裁图页保持 0 |
| `showMagnifier` | **true** | 绘制放大镜 | 要拖角精细对边 → 开；不要镜 → 关 |
| `showGuideLines` | `false` | 绘制九宫格（整图 Fit） | 要构图辅助线 → 开 |
| `showEdgeMidPoints` | `false` | 绘制边线中点 | 要边上多圆点可拖 → 开；关后仍可拖边本身，只是不画中点 |
| `showMagnifierCropLineMirror` | **true** | 圆镜内再画当前裁剪框线段 | 拖角要对齐框边 → 开；镜里只要图 → 关 |
| `magnifierRadiusRatio` 等 | 见 Config | 放大镜外观（Q29） | 调镜大小/边框；十字见 `showMagnifierCenterCross` |
| `showMagnifierCenterCross` | `false` | 镜心十字准星 | 要对准某像素 → 开 |
| `magnifierPlacement` | **`TopAutoFlip`** | 圆镜出现位置（Q32） | 镜挡内容再改固定角 / 指尖上；默认自动躲开手指 |
| `magnifierCacheMaxEdgePx` | **2048** | 镜像素缓存最长边（Q31） | 低端卡 / 内存紧 → 1024（略糊）；默认 2048 清晰 |
| `guideLineWidth` / `guideLineColor` | 1 / 半透明白 | 辅助线样式 | 开了九宫格再调样式 |
| `maskAlpha` / `border*` / `corner*` | … | 蒙版与描边 | 换品牌皮时自注入 Painter / 调色 |
| `outputFormat` / `jpegQuality` | JPEG / 92 | 出图导出（Q19） | 要 PNG 无损或调 JPEG 质量时 |

## 里程碑

| 里程碑 | 说明 | 清单 |
|---|---|---|
| **M0–M2** | 空架 / 触控叠层 / Android 出图 | 见 [docs/milestones](docs/milestones/README.md) |
| **M3** | 放大镜 + 剩余旋钮 + 双页 Demo + 对接说明 | [M3-checklist.md](docs/milestones/M3-checklist.md) |

**v0.1** = M0–M3 全部验收（含真像素放大镜）。决策台账：[docs/decisions.md](docs/decisions.md)（含 Q23–Q31）；放大镜 ADR：[0004](docs/adr/0004-magnifier-sampler-patch.md)。

## 出图 API

```kotlin
PerspectiveCropAndroid.init(application) // Android Application.onCreate

suspend fun crop(): PlatformImage
suspend fun cropToFile(): String
suspend fun cropToFile(path: String): String
```

失败抛 `CropException(code, message, cause?)`。

## Demo

### Android

```bash
# 建议 JDK 17（见 M0 备注；可用 .tools/jdk-17.0.2）
./gradlew :androidApp:installDebug
```

### H5（浏览器冒烟）

本仓用**精简 8083 静态服**提供业务包 `nativevue2.js`（**无** Kuikly 官方 whistle）。宿主 webpack 仍在 8080。  
**M4**：JS 真像素放大镜 + CPU 透视裁剪；`cropToFile` 返回 `blob:`（见 [M4-checklist](docs/milestones/M4-checklist.md)）。

**M5**：放大镜原生像素贴图（**Android** `BitmapShader`；H5/iOS/鸿蒙暂网格兜底；Q38 / [ADR 0006](docs/adr/0006-magnifier-native-blit.md)）；真机表：[m5-magnifier-native](docs/reviews/device-check-list/m5-magnifier-native.md)。

完整拓扑、白屏排障与工程约定见 **[docs/specs/kuikly-dev-h5/runbook.md](docs/specs/kuikly-dev-h5/runbook.md)**（已验证）。

前置：JDK 17；`node` 在 PATH。若 `packLocalJsBundleDebug` 因 GitHub 拉 Yarn 超时：先跑 `node static_server/fetch-yarn-ivy.js`（腾讯镜像写入 `%USERPROFILE%\.gradle\caches\manual-yarn\ivy`；根 `build.gradle.kts` 已改用该地址）。

必留：`h5App/webpack.config.d/output.js`（IIFE，防 `h5App.js` UMD 覆盖 `window.com`）；`index.html` **不要**默认同步挂会超时的 libpag CDN。

```bash
# 终端 1：业务 JS 包 + 8083 静态服
./gradlew :perspectiveCropDemo:packLocalJsBundleDebug
node static_server/serve/index.js

# 终端 2：样例图 + 宿主 webpack（持续跑）
./gradlew :h5App:copyAssetsToWebpackDevServer
./gradlew :h5App:jsBrowserDevelopmentRun
```

打开：`http://localhost:8080/?page_name=home` →「透视裁剪」入口。  
改 Demo 后重跑 `packLocalJsBundleDebug` 并重启 8083；改 `webpack.config.d` 后必须重启 webpack；改 `assets` 后重跑 `copyAssetsToWebpackDevServer`。

| pageName | 角色 |
|---|---|
| `home` | 入口 |
| `perspective_crop_demo` | **最小实现**（框选 + 确认裁切） |
| `perspective_crop_lab` | **全能 Lab**（Toggle / 换皮 / 缓存边长 / writePoints） |

## 工程结构

```text
KuiklyPerspectiveCrop/
├── KuiklyPerspectiveCropCore/   # 发布库
├── perspectiveCropDemo/         # Demo 共享页
├── androidApp / iosApp / h5App
├── CONTEXT.md
└── docs/milestones/  docs/adr/
```
