# 真机可执行档 · demo-ui-polish

- 关联报告：`docs/reviews/review-demo-ui-polish.md`
- 包名 / 入口：`androidApp` Debug 或 H5 `?page_name=home`；pageName `home`
- 前置条件：JDK 17；Android `PerspectiveCropAndroid.init` 已在 Application；样例图 `assets://crop_sample.webp` 可加载

## 步骤

| # | 操作 | 类型 | 期望 |
|---|---|---|---|
| 1 | 启动进入 `home` | assert | 见「产品演示」「全能配置」两入口 |
| 2 | tap「产品演示」 | navigate | 进入 `perspective_crop_demo`：顶栏「透视裁剪」+ 返回、Stage、底栏重置/旋转/确认裁切 |
| 3 | tap 返回 | navigate | 回到 `home` |
| 4 | 再进产品演示，拖角后 tap「确认裁切」 | tap | 弹出「裁切成功」浮窗并见结果图；点「再裁一张」或「完成」或 ✕ 关闭浮窗 |
| 5 | （可选）制造失败路径或观察异常文案 | assert | 失败时弹出「裁切失败」+ code/message，「知道了」可关 |
| 6 | home →「全能配置」 | navigate | 顶栏「全能配置」；见外观/手势/放大镜 Tab；底栏含重置/旋转/注入点/裁切 |
| 7 | 依次 tap 三 Tab，各点一两个开关 | tap | 仅当前组 chip；开关文案开/关切换；Stage 行为可感知变化（如关放大镜后拖角无镜） |
| 8 | tap「注入点」 | assert | 四点跳到内缩位置 |
| 9 | tap「换皮·开」（外观 Tab）后看框 | assert | 框线/角点变为蓝/绿配方 |
| 10 | tap「裁切」 | assert | 成功浮窗出图（与产品演示同款） |
| 11 | 窄屏或折叠外屏看 Lab 底栏 | assert | 四按钮可见可点，无严重裁切到不可点 |
| 12 | （可选 H5）`packLocalJsBundleDebug` 后走 #1–#4 | navigate | 同 Android 主路径；blob: 结果图可显示 |

类型合法值：`tap` `assert` `scroll` `input` `wait` `screenshot` `track_logcat` `navigate`

## 覆盖对照

| 审查缺口 | 本表步骤 |
|---|---|
| 进出页与 Home 新文案 | #1–#3 #6 |
| 成功/失败浮窗 | #4 #5 #10 |
| Lab 分组 Tab + 换皮/注入点 | #7–#9 |
| 窄屏底栏 | #11 |
| H5 结果图 | #12 |
