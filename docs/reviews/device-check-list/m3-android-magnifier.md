# 真机可执行档 · m3-android-magnifier

- 关联报告：`docs/reviews/review-m3-android-magnifier.md`
- 包名 / 入口：`androidApp` Debug；pageName `home`
- 前置条件：JDK 17；`PerspectiveCropAndroid.init` 已在 `KRApplication`；`./gradlew :androidApp:installDebug`

## 步骤

| # | 操作 | 类型 | 期望 | 结果 |
|---|---|---|---|---|
| 1 | 启动 App，确认进入 `home` | assert | 见「最小实现演示」「全能全配置 Lab」两入口 | |
| 2 | tap「最小实现演示」 | navigate | 进入 `perspective_crop_demo`，见 Stage 与确认裁切 | |
| 3 | 拖角 / 点确认裁切 | tap | 可选区；出图成功或状态行展示 CropException code | |
| 4 | 返回 home，tap「全能全配置 Lab」 | navigate | 进入 `perspective_crop_lab`，见 FlowRow 换行 Toggle（含 `pos:*`） | pass（2026-11-07 用户） |
| 5 | 拖四角之一 | tap | 圆镜出现，内容为源图像素放大（非纯灰/空心） | pass（流畅+顶角对齐，用户） |
| 5b | tap `pos:*` 循环 auto/TL/TR/above 并拖角 | assert | 圆镜摆放随档变化（Q32） | pass（2026-11-07 用户） |
| 6 | tap magnifier 关闭后再拖角 | tap | 无放大镜 | |
| 7 | tap guides、mids | tap | 三分线、边中点可见 | |
| 8 | tap passthrough，在选区外空白按住拖 | assert | 本层不拖框（透传；无外层滑动容器时至少不移动选区） | |
| 9 | tap snap / clamp 开关后拖边 | assert | 开关切换后边拖手感可区分（开=吸附/钳制） | |
| 10 | tap catch=0，再点角/边尝试拖 | assert | 无法命中手柄 | |
| 11 | tap catch=def 恢复；tap skin:alt | tap | 框线/角点样式变为蓝/绿配方 | |
| 12 | tap injectPts | assert | 四点跳到内缩注入位置 | |
| 13 | tap cache 在 2k↔1k 间切换后拖角 | assert | 仍见镜（1k 可略糊）；无崩溃 | |
| 14 | 首进 Lab：等约 1s 预热后再拖角；另试一进页立刻拖 | assert | 预热后拖角应出真像素且流畅；立刻拖允许短暂仅边框/无像素，随后应出镜；logcat 无崩溃 | |
| 15 | tap 裁切 | assert | ok·path 或 CropException 文案 | |

类型合法值：`tap` `assert` `scroll` `input` `wait` `screenshot` `track_logcat` `navigate`

## 覆盖对照

| 审查缺口 | 本表步骤 |
|---|---|
| 真像素镜是否可见、是否像源图 | #5 #6 #13 |
| 圆镜摆放 Q32 / FlowRow 可见 | #4 #5b |
| 首帧同步解码卡顿/ANR 观感 | #14 + track_logcat 可选 |
| Config Toggle 行为 | #7–#11 |
| unhitPassthrough | #8 |
| Demo 换皮不污染 Core | #11 |
| writePoints 钩子 | #12 |
| 双页入口与裁切 | #1–#4 #15 |
