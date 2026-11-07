# 真机可执行档 · guide-lines-image-aabb

- 关联报告：`docs/reviews/review-guide-lines-image-aabb.md`
- 包名 / 入口：`androidApp` Debug；pageName `perspective_crop_lab`
- 前置条件：样例图可加载；进入全能配置 Lab
- 结果汇总：2026-11-07 用户真机 **pass**（主路径）

## 步骤

| # | 操作 | 类型 | 期望 | 结果 |
|---|---|---|---|---|
| 1 | 进入 Lab，确认当前在「外观」Tab | assert | 见「辅助线·开/关」chip | pass（2026-11-07 用户） |
| 2 | tap 打开辅助线 | tap | 图上出现横平竖直九宫格（2 竖 + 2 横），落在整张 Fit 图上 | pass（2026-11-07 用户） |
| 3 | 拖某一角改变选区 | tap | 选区变形；辅助线位置/方向**不变**（不跟边斜变） | pass（2026-11-07 用户） |
| 4 | tap 关闭辅助线 | tap | 九宫格消失 | pass（2026-11-07 用户） |
| 5 | （可选）再开辅助线后 tap 旋转 | assert | 线仍为轴对齐九宫格，大致贴合当前图显示区 | — |
| 6 | （可选）H5 重复 #2–#4 | assert | 同 Android 观感 | — |

类型合法值：`tap` `assert` `scroll` `input` `wait` `screenshot` `track_logcat` `navigate`

## 覆盖对照

| 审查缺口 | 本表步骤 |
|---|---|
| 横平竖直九宫格可见 | #2 |
| 拖框不斜变 | #3 |
| 开关关闭 | #4 |
| 旋转后贴合（可选） | #5 |
| H5 | #6 |
