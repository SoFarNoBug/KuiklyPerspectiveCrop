# 真机可执行档 · m5-magnifier-native

- 关联报告：`docs/reviews/review-m5-magnifier-native.md`
- 包名 / 入口：`com.jlj.kuiklyperspectivecropui` · Demo / Lab 透视裁剪页
- 前置条件：装 Debug APK；选一张清晰大图；预热完成后再评「拖动清晰」（步骤 6 另测冷启动）

## 步骤

| # | 操作 | 类型 | 期望 |
|---|---|---|---|
| 1 | 打开 perspective_crop_demo 或 lab，加载图片 | navigate | 预览与选区正常 |
| 2 | 按住角点连续拖动 ≥2s，注视放大镜圆内 | assert | **拖动全程为连续像素，无 N×N 马赛克色块** |
| 3 | 拖动中停指约 200ms 再微移 | assert | 清晰度与拖动中一致，无「突然变清晰」 |
| 4 | 拖至近左上角 | assert | 圆镜翻到右上（TopAutoFlip） |
| 5 | Lab 关闭 showMagnifier 后再拖角 | tap | 无放大镜 |
| 6 | 冷启动进页后立刻拖角（预热未完成） | assert | 可短暂灰/边框；预热后出真像素且仍无马赛克 |
| 7 | （可选）截图拖动中圆镜 | screenshot | 存证对照旧库清晰度 |

类型合法值：`tap` `assert` `scroll` `input` `wait` `screenshot` `track_logcat` `navigate`

## 覆盖对照

| 审查缺口 | 本表步骤 |
|---|---|
| 拖动全程非网格马赛克 | #2 #3 |
| Compose 几何 vs 原生 View 对齐 / TopAutoFlip | #4 |
| 开关与预热边界 | #5 #6 |
| 视觉存证 | #7 |
