# M5 changelog

## 2026-11-09（鸿蒙策略 A：原生圆镜）

- `PlatformMagnifier.ohos`：`platformSupportsNativeMagnifier=true`；`MagnifierBlitStore` + `MakeKuiklyComposeNode`
- Demo `@CName` `perspective_crop_magnifier_blit_snapshot`；NAPI `getMagnifierBlit`；ArkTS `KRPerspectiveCropMagnifier` Canvas
- `MagnifierSampler.ohos`：`maxEdge` 回 96；Q38 / ADR 0006 / CONTEXT / ohos runbook 同步
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：鸿蒙网格卡顿根因修复｜待批 0

## 2026-11-07（关审查 P2：publish 边界 + 删 dead generation）

- `PlatformMagnifier.android`：`MagnifierBlitStore.publish` 移入 `viewInit`/`viewUpdate`（关 `P2-compose-impure-publish`）
- `KRPerspectiveCropMagnifierView`：去掉无用 `lastGeneration` 字段（关 `P2-dead-generation-field`）
- 未改：`P1-device-verify`（真机表仍待跑）
- 真机表登记：`docs/reviews/device-check-list/m5-magnifier-native.md`
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：审查 P2 整改｜待批 0（已达标 0）

## 2026-11-07（H5 退回网格 · 关 P0 双 Store）

- `PlatformMagnifier.js`：`platformSupportsNativeMagnifier=false`；删 `MagnifierBlitStore.js`、h5App `KRPerspectiveCropMagnifier` 注册与 Core 依赖
- 口径：H5/iOS/鸿蒙网格兜底；Android BitmapShader 主路径不变（审查 `P0-silent-fail`）
- 真机表登记：`docs/reviews/device-check-list/m5-magnifier-native.md`（审核员产出；本单整改无新增真机项）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：关 H5 假主路径｜待批 0（已达标 0）

## 2026-11-07（放大镜原生像素贴图）

- Q38 / ADR 0006：主路径原生贴图；N×N 网格仅兜底
- Android：`MagnifierBlitStore` + `KRPerspectiveCropMagnifierView`（`BitmapShader`）；Stage 拆出 `PlatformMagnifierOverlay`
- H5：同名自定义 View + Canvas2D 圆裁像素贴图
- iOS / 鸿蒙：暂网格兜底
- 编译验收：`:KuiklyPerspectiveCropCore:compileDebugKotlinAndroid` / `:androidApp:compileDebugKotlin` / `:compileKotlinJs` / `:h5App:compileKotlinJs` 通过
- 真机表：`docs/reviews/device-check-list/m5-android-magnifier-native.md`（拖动全程无马赛克）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：拖动清晰根因修复｜待批 0
