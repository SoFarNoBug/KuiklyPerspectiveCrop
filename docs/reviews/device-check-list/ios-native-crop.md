# 真机可执行档 · ios-native-crop

- 关联报告：`docs/reviews/review-ios-native-crop.md` / `review-ios-native-crop-r2.md`
- 包名 / 入口：`iosApp` → Demo / Lab
- 前置条件：已 `pod install` + Xcode 装机；`PerspectiveCropIos.init` 已由 `iOSApp.init` → `PerspectiveCropIosBridge.configure` 注入 Caches；Demo framework `perspectiveCropDemo` 含 `assets` 样例图
- **结果：用户确认 OK（2026-11-09）** — Duo 模拟器 + 真机主路径通过（含原生放大镜）
- **复验（2026-11-10）**：放大镜灰底/倒像/旋源 90·270 已修；用户确认 Duo **#6 #7 正常**（圆镜像素、触点对齐、0/90/180/270 裁切与放大镜）

## 步骤

| # | 操作 | 类型 | 期望 | 结果 |
|---|---|---|---|---|
| 1 | 冷启动 App，进入裁切 Demo，默认引擎（Auto）完成一次四点裁切并预览 | tap | 出图成功；非原图静默回退；无崩溃；无明显上下颠倒 | OK |
| 2 | 打开结果图路径/预览，确认可再显示 | assert | `file://` 缓存图可见 | OK |
| 3 | Lab 切 **Native**，同四点再裁一次 | tap | 出图成功；几何与步骤 1 观感接近 | OK |
| 4 | Lab 切 **Common**，同四点再裁一次 | tap | 出图成功；可与 Native 肉眼对比（允许轻微滤波差） | OK |
| 5 | 记录 Native / Common 主观耗时 | assert | Native 不慢于 Common 一个数量级；无主线程卡死 | OK |
| 6 | Demo 拖动角点/边，观察放大镜圆镜（Duo 展开 + 普通机各一次） | tap | 圆镜跟随；贴图像素清晰（非黑屏/网格马赛克）；框线镜像大致对齐触点；**圆镜直径约为 Stage 宽度 ~14%（`2×magnifierRadiusRatio=0.07`），非占半屏** | OK（Duo 2026-11-10） |
| 7 | 旋转源图 0/90/180/270 后再 Auto 裁切 + 拖角看镜 | tap | 出图与镜向正确，无崩溃（旋源 UIGraphics 顺时针） | OK（Duo 2026-11-10） |
| 8 | （可选）未 init 构建对照：去掉壳 configure 后裁切/落盘 | assert | 明确失败文案（NOT_INITIALIZED / WRITE_FAILED），非空白假成功 | 跳过 |

类型合法值：`tap` `assert` `scroll` `input` `wait` `screenshot` `track_logcat` `navigate`

## 覆盖对照

| 审查缺口 | 本表步骤 |
|---|---|
| Auto/Native 出图与 CI 坐标 | #1 #3 |
| Common 对比与耗时 | #4 #5 |
| 落盘再显示 | #2 |
| 放大镜原生贴图 | #6 |
| 旋源 CTM | #7 |
| init 契约 | #8 |
