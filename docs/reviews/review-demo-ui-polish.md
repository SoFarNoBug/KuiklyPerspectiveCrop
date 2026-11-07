# Review · demo-ui-polish

- 结论：**有条件通过**
- 依据：Plan「Demo 页布局改造」（顶栏+Stage+底栏+成功/失败浮窗；Lab 分组 Tab；Home 文案）

## P0

无。

## P1

### P1-device-verify · 浮窗/底栏/分组 Tab 须真机或浏览器点验
- 证据：
  - `DemoChrome.kt`：`DemoResultModal` / `DemoErrorModal` / `DemoBottomBar`
  - `PerspectiveCropDemoPager.kt` / `PerspectiveCropLabPager.kt`：裁切 → 设 `resultSrc`/`errorText` 弹窗
  - `HomePager.kt`：入口「产品演示」「全能配置」
- 影响：代码可证接线，无法证窄屏四按钮是否挤切、结果图是否能显示、Dialog 遮罩是否挡住操作
- 处置：必修（可执行档 → [device-check-list/demo-ui-polish.md](device-check-list/demo-ui-polish.md)）
- 缺口要点：①进出页；②成功/失败浮窗；③Lab 三 Tab 与底栏；④窄屏底栏可见性

## P2

### P2-demo-copy-scatter
- 证据：`PerspectiveCropLabPager.kt` 外观/手势/放大镜 chip 文案（如「放大镜·开」）未进 `DemoCopy`；`HomePager` 入口串亦散落
- 影响：与角色 Rule「用户可见文案集中常量」不一致；改文案易漏
- 处置：建议（收拢到 `DemoCopy`）

### P2-result-actions-alias
- 证据：`PerspectiveCropDemoPager.kt` / Lab：`onAgain` 与 `onDone` 均仅 `resultSrc = null`
- 影响：Plan 两按钮语义在 Demo 内等价，不影响再裁
- 处置：可选（「完成」可 `closePage`，非必须）

## 规范候选观察

无。

## 真机

- 可执行档：[device-check-list/demo-ui-polish.md](device-check-list/demo-ui-polish.md)
- 代码审不了：浮窗出图、窄屏底栏、Tab 切换观感、失败浮窗文案可读性

## 对照上轮

（首轮审查，无上轮）
