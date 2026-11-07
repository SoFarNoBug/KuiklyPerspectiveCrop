# 文件地图（KuiklyPerspectiveCrop）

| 路径 | 说明 |
|---|---|
| `KuiklyPerspectiveCropCore/` | 库模块（common + android/ios/js） |
| `…/CropProcessor*.kt` / `PlatformImage*.kt` / `PerspectiveCropAndroid.kt` | M2 出图 expect/actual + Android init |
| `…/MagnifierSampler*.kt` / `MagnifierPatch.kt` | M3 放大镜采样 expect/actual + Patch |
| `…/CropException.kt` / `CropOutputFormat.kt` | 出图失败与导出格式 |
| `perspectiveCropDemo/` | Demo：`perspective_crop_demo` 最小 + `perspective_crop_lab` 全能 |
| `androidApp/` `iosApp/` `h5App/` `ohosApp/` | 各端壳（Android `KRApplication` 调 init） |
| `docs/milestones/` | M0–M3 清单 |
| `docs/adr/` | 架构决策（含 0003 出图契约） |
| `docs/specs/m2-android-crop/` | M2 feature changelog |
| `docs/skill-references/` | AI 规范素材 |
| `.cursor/` | Cursor rules / skills / hooks |
