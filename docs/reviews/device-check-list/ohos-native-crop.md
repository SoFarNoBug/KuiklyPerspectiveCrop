# 真机可执行档 · ohos-native-crop

- 关联报告：`docs/reviews/review-ohos-native-crop.md` / `review-ohos-native-crop-r2.md`
- 包名 / 入口：`com.jlj.kuiklyperspectivecropui` / EntryAbility → Demo / Lab
- 前置条件：已 `./ohos_build.sh` + `./ohosApp/runOhosApp.sh` 装机；`PerspectiveCropOhos.init` 已由壳注入
- **结果：用户确认 OK（2026-11-09）** — 步骤 1–7 通过；步骤 8 可选未强制

## 步骤

| # | 操作 | 类型 | 期望 | 结果 |
|---|---|---|---|---|
| 1 | 冷启动 App，进入裁切 Demo，默认引擎（Auto）完成一次四点裁切并预览 | tap | 出图成功；颜色正常（无紫蓝）；非原图静默回退 | OK |
| 2 | 打开结果图路径/预览，确认可再显示 | assert | `file://` 缓存图可见 | OK |
| 3 | Lab 切 **Native**，同四点再裁一次 | tap | 出图成功；几何与步骤 1 观感接近 | OK |
| 4 | Lab 切 **Common**，同四点再裁一次 | tap | 出图成功；可与 Native 肉眼对比（允许轻微滤波差） | OK |
| 5 | 记录 Native / Common 主观耗时（可看 Lab 耗时若有） | assert | Native 不慢于 Common 一个数量级；无 ANR | OK |
| 6 | Demo 拖动角点/边，观察放大镜圆镜 | tap | 圆镜跟随；贴图像素清晰（非长期灰底）；框线镜像大致对齐触点 | OK |
| 7 | 旋转源图 90° 后再 Auto 裁切 | tap | 出图方向正确，无崩溃 | OK |
| 8 | （可选）截图 Native vs Common 结果并排 | screenshot | 归档对照 | 跳过 |

类型合法值：`tap` `assert` `scroll` `input` `wait` `screenshot` `track_logcat` `navigate`

## 覆盖对照

| 审查缺口 | 本表步骤 |
|---|---|
| Auto/Native 出图与颜色 | #1 #3 #7 |
| Common 对比与耗时 | #4 #5 |
| 落盘再显示 | #2 |
| 放大镜拖动贴图 | #6 |
| 紫蓝回归 | #1 #3 |
