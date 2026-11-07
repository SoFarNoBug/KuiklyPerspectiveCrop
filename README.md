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
PerspectiveCropStage   // Image + 叠层框选
        ↓ suspend
crop() → PlatformImage  或  cropToFile() → String
```

相机 / 相册对接（不强制依赖）：

- [KuiklyCamera](https://github.com/Kuikly-contrib/KuiklyCamera) → `onPhotoCaptured { filePath -> }`
- [KuiklyPhotoSelector](https://github.com/Kuikly-contrib/KuiklyPhotoSelector) → `KRAlbumImage.uri`

## 里程碑

按清单推进，验证完一步再下一步。总览与非目标 / parity：[docs/milestones/README.md](docs/milestones/README.md)

| 里程碑 | 说明 | 清单 |
|---|---|---|
| **M0** | 空架 + 文档 | [docs/milestones/M0-checklist.md](docs/milestones/M0-checklist.md) |
| **M1** | 几何触控 + 默认叠层 | [docs/milestones/M1-checklist.md](docs/milestones/M1-checklist.md) |
| **M2** | Android 透视出图 | [docs/milestones/M2-checklist.md](docs/milestones/M2-checklist.md) |
| **M3** | 放大镜与剩余通用能力 | [docs/milestones/M3-checklist.md](docs/milestones/M3-checklist.md) |

领域术语见 [CONTEXT.md](CONTEXT.md)；**已定决策台账**见 [docs/decisions.md](docs/decisions.md)；难逆转子集见 [docs/adr/](docs/adr/)。  
**v0.1** = M0–M3 全部验收（含真像素放大镜）。

## 出图 API（M2）

```kotlin
PerspectiveCropAndroid.init(application) // Android Application.onCreate

suspend fun crop(): PlatformImage
suspend fun cropToFile(): String          // 默认写 cacheDir
suspend fun cropToFile(path: String): String
```

- 失败抛 `CropException(code, message, cause?)`（如 `ILLEGAL_POINTS` / `UNSUPPORTED_PLATFORM` / `NOT_INITIALIZED`）；**禁止**静默回原图。
- 导出格式：`PerspectiveCropConfig.outputFormat` / `jpegQuality`（默认 JPEG·92）。
- 出图时按 `uiRotationSteps` 先旋源图再透视；其它端 Processor 为 stub（明确失败）。
- 详见 [docs/decisions.md](docs/decisions.md) Q16–Q22、[ADR 0003](docs/adr/0003-crop-exception-init-rotate-then-poly.md)。

## 运行 Demo（Android）

```bash
# 建议 JDK 17（见 M0 备注）
./gradlew :androidApp:installDebug
```

入口 pageName：`home` → `perspective_crop_demo` →「确认裁切」。

## 工程结构

```text
KuiklyPerspectiveCrop/
├── KuiklyPerspectiveCropCore/   # 发布库
├── perspectiveCropDemo/         # Demo 共享页
├── androidApp / iosApp / h5App
├── CONTEXT.md
└── docs/milestones/  docs/adr/
```
