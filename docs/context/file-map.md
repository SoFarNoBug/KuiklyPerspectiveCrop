# 文件地图（KuiklyPerspectiveCrop）

| 路径 | 说明 |
|---|---|
| `README.md` | **接入者**文档：依赖 / init / 裁图 / Config / SPI |
| `CONTRIBUTING.md` | **维护者**入口：Demo 起服摘要 + 链 CONTEXT / milestones / runbook |
| `KuiklyPerspectiveCropCore/` | 库模块（common + android/ios/js） |
| `…/CropProcessor*.kt` / `PlatformImage*.kt` / `PerspectiveCropAndroid.kt` / `PerspectiveCropOhos.kt` / `PerspectiveCropIos.kt` | 出图 expect/actual + 各端 init |
| `…/MagnifierSampler*.kt` / `MagnifierPatch.kt` | M3 放大镜采样 expect/actual + Patch |
| `…/CropException.kt` / `CropOutputFormat.kt` | 出图失败与导出格式 |
| `perspectiveCropDemo/` | Demo：`perspective_crop_demo` 产品演示 + `perspective_crop_lab` 全能配置 |
| `…/demo/DemoChrome.kt` | Demo 共用顶栏/底栏/Toggle/裁切结果·失败浮窗 |
| `androidApp/` `iosApp/` `h5App/` `ohosApp/` | 各端壳（Android `KRApplication` 调 init） |
| `h5App/webpack.config.d/output.js` | H5：IIFE，防 UMD 覆盖 `window.com`（必留） |
| `static_server/serve/` + `fetch-yarn-ivy.js` | H5：8083 业务包静态服；Yarn ivy 镜像兜底 |
| `docs/milestones/` | M0–M3 清单 |
| `docs/adr/` | 架构决策（含 0003 出图契约） |
| `docs/specs/m2-android-crop/` | M2 feature changelog |
| `docs/specs/kuikly-dev-h5/` | H5 冒烟 runbook / changelog |
| `docs/specs/kuikly-dev-ohos/` | 鸿蒙冒烟 runbook / changelog |
| `docs/specs/kuikly-dev-ios/` | iOS 冒烟 runbook / changelog |
| `docs/specs/ios-native-crop/` | iOS Core Image 出图 + 原生放大镜 feature changelog |
| `docs/milestones/M4-checklist.md` | H5/JS 放大镜+裁剪（post-v0.1） |
| `docs/adr/0005-js-suspend-decode-cpu-warp-blob.md` | JS suspend / CPU 单应 / blob: |
| `docs/adr/0008-config-compose-dp.md` | Config UI 长度 Compose dp + LocalDensity |
| `docs/skill-references/` | AI 规范素材 |
| `.cursor/` | Cursor rules / skills / hooks |
