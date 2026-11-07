# Review · m3-android-magnifier

- 结论：有条件通过
- 依据：Plan `m3_magnifier_parity_bd100a3a`；decisions Q23–Q31；M3-checklist；ADR 0004

## P0

无。

## P1

### P1-main-thread-io 放大镜首帧在手势路径同步解码
- 证据：`PerspectiveCropStage.kt:165-173` / `183-191` 在 `awaitEachGesture`/`drag` 回调内调用 `refreshMagnifier` → `MagnifierSampler.sample`；`MagnifierSampler.android.kt:89-103` 首次键未命中时 `BitmapFactory.decodeStream` + rotate/scale
- 影响：大图首次拖角可能掉帧/短暂卡顿；未证必 ANR。缓存命中后仅 `getPixels`，风险下降
- 处置：必修（改为后台预热缓存或首帧异步；手势线程禁止全图 decode）

### P1-device-verify 放大镜/透传/Toggle 须真机闭环
- 证据：代码无法确认圆镜像素观感、passthrough 与父容器协作、首帧卡顿是否可接受
- 影响：M3 验收标准 1–2、清单真机项依赖实机
- 处置：必修（可执行档 → [device-check-list/m3-android-magnifier.md](device-check-list/m3-android-magnifier.md)）
- 缺口要点：见该表「覆盖对照」

## P2

### P2-new-file-header
- 证据：新建 `MagnifierSampler*.kt` / `MagnifierPatch.kt` / Demo Lab 等缺 `@CreateDate/@Author/@Description`（L3 `new_code_file_guidelines`）
- 影响：规范不合；不影响运行
- 处置：建议补文件头

### P2-magnifier-draw-cost
- 证据：`DefaultCropOverlayPainters.kt:135-149` 每帧多 `drawRect` 填镜
- 影响：低端机拖动可能额外掉帧；非功能错误
- 处置：可选（后续改平台位图绘制或降低 patch 分辨率）

### P2-private-underscore
- 证据：`MagnifierSampler.android.kt:16-18` 私有字段未用 `_` 前缀（对照码农自检 K7）
- 影响：命名口径
- 处置：可选

## 规范候选观察

- 手势/Compose 回调路径禁止同步全图解码（放大镜/缩略图预热须后台）｜可抄码农池强化 `platform_android` A1
- Plan 确认后编码须先走 `kuikly-android-coding`｜本仓已用 Rule `plan-exec-coding-skill.mdc` 小结级落地，**不入审查池重复**

## 真机

- 可执行档：[device-check-list/m3-android-magnifier.md](device-check-list/m3-android-magnifier.md)
- 代码审不了：真像素观感、Toggle 手感、passthrough 与外层滑动、首帧卡顿是否可接受、换皮视觉

## 对照上轮

（M3 首轮审查，无）
