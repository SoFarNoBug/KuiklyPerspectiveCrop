# ohos-native-crop · feature changelog

## 2026-11-09（关审查 P2：文件头 / NAPI 校验 / UIContext.px2vp）

- `OhosPerspectiveCropNative`：补 `@CreateDate` / `@Author` / `@Description`（关 `P2-file-header`）
- `napi_init.cpp`：`ReadUtf8Arg` 校验 string + `napi_status`；关键 create 失败早退（关 `P2-napi-unchecked`）
- `KRPerspectiveCropMagnifier`：全局 `px2vp` → `getUIContext().px2vp`（关 `P2-deprecated-px2vp`）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：审查 P2 整改｜待批 0（已达标 0）

## 2026-11-09（关审查 P1：changelog + BlitStore 并发）

- 补本 feature changelog（关 `P1-changelog-missing`）
- `MagnifierBlitStore.ohos`：整包不可变 `MagnifierBlitSnapshot` + `@Volatile` 引用替换；禁止原地改旧像素缓冲（关 `P1-concurrency`）
- 真机表登记：`docs/reviews/device-check-list/ohos-native-crop.md`（`P1-device-verify` 仍待勾）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：审查 P1 整改｜待批 0（已达标 0）

## 2026-11-09（鸿蒙 Native Drawing 出图）

- `cropEngine` Auto/Native → `OH_Drawing_MatrixSetPolyToPoly` + Canvas `DrawPixelMapRect`；Common → CPU 单应
- 解码改为保留 `OH_PixelmapNative`；落盘仍软件 PNG（禁 ImagePacker Native）
- 像素：Drawing `BGRA_8888` 按 B,G,R,A 解包为 ARGB（修紫蓝）
- ADR 0009 / Q40 / CONTEXT / ohos runbook 同步；链接 `-lnative_drawing`
- 真机表：`docs/reviews/device-check-list/ohos-native-crop.md`
- 治理收尾：门禁 跳过｜三问 是/是/否｜理由：新 Native 引擎与字节序反例入仓口径｜待批 0（已达标 0）
