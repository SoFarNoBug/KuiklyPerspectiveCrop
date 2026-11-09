# Review · ohos-native-crop

- 结论：**有条件通过**
- 依据：Plan「鸿蒙 Native 出图（Drawing SetPolyToPoly）」；放大镜含 2026-11-09 鸿蒙策略 A（`m5-magnifier-native` changelog）；码农 norms：`kuikly-ohos-coding` + 共用 `perspective_crop_api`

## P0

无。

## P1

### P1-device-verify 真机/Lab 闭环未书面勾选
- 证据：Plan §5 要求装机验 Auto 裁切与 Lab Native vs Common；用户口头「颜色正常、速度快」，但无勾选可执行档
- 影响：Native/Common 几何一致性、放大镜拖动帧、默认 Auto 出图未形成审查闭环
- 处置：必修（跑表）— 可执行档 → [device-check-list/ohos-native-crop.md](device-check-list/ohos-native-crop.md)

### P1-changelog-missing Native 出图缺 feature changelog
- 证据：ADR 0009 / Q40 / runbook / CONTEXT 已改；`docs/specs/perspective-crop/changelog.md` 与独立 feature changelog **无** Drawing Native 条目；`kuikly-dev-ohos/changelog.md` 仅为 skill 元变更
- 影响：按计划行事留痕不完整，后续复盘难对版本
- 处置：必修 — 码农 append feature changelog（建议 `perspective-crop` 或新建 `ohos-native-crop`）

### P1-concurrency MagnifierBlitStore 多字段无原子快照
- 证据：`MagnifierBlitStore.ohos.kt` `publish` / `snapshot` 多 `@Volatile` 字段分写分读，无锁/无单次不可变快照对象替换；Compose `viewUpdate` 与 NAPI `getMagnifierBlit` 可交错
- 影响：偶发 generation/宽高/像素撕裂（花屏一帧或空像素），未证必崩
- 处置：建议 — 发布时整包替换不可变快照，或 `synchronized`/`Mutex` 包住 publish/snapshot

## P2

### P2-file-header `OhosPerspectiveCropNative.kt` 缺文件头
- 证据：新建文件无 `@CreateDate` / `@Author` / `@Description`（对照 ohos L3 `new_code_file_guidelines`）
- 处置：建议

### P2-napi-unchecked `ReadUtf8Arg` / 部分 `napi_*` 未查状态与类型
- 证据：`napi_init.cpp` `ReadUtf8Arg` 未确认 `napi_string`；`napi_create_arraybuffer` 等未查 `napi_status`
- 处置：建议

### P2-deprecated-px2vp 放大镜 View 使用已弃用 `px2vp`
- 证据：`KRPerspectiveCropMagnifier.ets` 多处 `px2vp`（构建 WARN）
- 处置：可选 — 跟 DevEco 推荐 API 替换

## 规范候选观察

1. 审查 skill 强制检查表第 3 条仍只列 `kuikly-android-coding` norms；鸿蒙 diff 应以 `kuikly-ohos-coding`（`lang_arkts`/`lang_c`/`lang_cpp`/`platform_ohos`）为对照真源，android 包仅作 Kotlin/Compose/API 共用项。
2. Drawing 位图与 `GetArgbPixels` 字节序不同源 — 已有 ohos `misuse` 错误 1；可考虑晋升为审查启发式「像素布局」开卡条件（跨端出图/贴图）。

（已入本 skill `_backlog` 候选）

## 真机

- 可执行档：[device-check-list/ohos-native-crop.md](device-check-list/ohos-native-crop.md)
- 代码审不了：Native vs Common 透视观感/耗时、圆镜拖动连续性、紫蓝回归、落盘 `file://` 再显示

## 开放发现摘要（卡路由）

| 卡 | 结论 |
|---|---|
| B 可达性 | Auto/Native→Drawing、Common→CPU 已接线；删「Native 明确失败」；主路径可达 |
| A 契约 | 出图尺寸规则与 Android `hypot` 顶/左边一致；像素字节序已按 BGRA 修正（用户确认颜色正常） |
| C/E | 无 insets/WebView 命中 |

## 写权限自检

未改业务源码、未改 feature changelog、未改 severity。
