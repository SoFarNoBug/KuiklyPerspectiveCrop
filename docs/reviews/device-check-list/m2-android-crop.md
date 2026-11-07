# 真机可执行档 · m2-android-crop

- 关联报告：`docs/reviews/review-m2-android-crop.md` / `review-m2-android-crop-r2.md`
- 包名 / 入口：`com.jlj.kuiklyperspectivecropui` / `home` →「进入透视裁剪演示」→ `perspective_crop_demo`
- 前置条件：已 `installDebug`；`KRApplication` 已调 `PerspectiveCropAndroid.init`；样例图 `assets://crop_sample.webp` 可见
- **结果（2026-11-07）**：通过（#4 N/A）

## 步骤

| # | 操作 | 类型 | 期望 | 结果 |
|---|---|---|---|---|
| 1 | 打开 Demo，确认底图 + 蒙版框 + 角点可见 | assert | 预览正常，非整屏空白 | pass |
| 2 | 不旋转，拖四角缩小选区后点「确认裁切」 | tap | 状态区出现 `ok`；下方「结果预览」显示裁切后矩形图（非空白） | pass（初版裸 path 空白 → 已改 `file://` 后复验 pass） |
| 3 | 点 `reset`，再点 `rotate` 1～2 次，再「确认裁切」 | tap | `ok`；结果图朝向与当前预览一致（相对未旋转有 90°/180° 差） | pass |
| 4 | 将四角拖成明显非法/过小（若难触发：连续极端拖拽至限制边界后仍点确认） | tap | 若 `canCrop()==false`：状态区为 `ILLEGAL_POINTS: …`，无脏结果图；若仍合法则本步记 N/A 并在备注说明 | **N/A** — 手势约束夹紧，拖不到非法四点；`canCrop`/文案路径仍由代码覆盖 |
| 5 | 合法出图后，确认结果 path 已显示（步骤 2 预览） | assert | 预览图可辨认原图内容裁切区域 | pass |
| 6 | （可选）`adb logcat` 过滤 CropException / FATAL | track_logcat | 正常裁切无崩溃；失败仅为业务文案 | 未跑（可选；主路径无崩溃） |

类型合法值：`tap` `assert` `scroll` `input` `wait` `screenshot` `track_logcat` `navigate`

## 覆盖对照

| 审查缺口 | 本表步骤 | 闭环 |
|---|---|---|
| `assets://` 解码能否出图 | #1 #2 | 是 |
| 结果路径能否被 Image 显示 | #2 #5 | 是（须 `file://`） |
| Q17 旋转后出图朝向 | #3 | 是 |
| 非法四点文案 / 不出脏图 | #4 | N/A（拖不到；API 仍拒非法） |
| 无崩溃 | #6 | 主路径观察通过 |

## 备注

- 拖不到非法四点属手势夹紧预期行为，不阻塞 M2 真机闭环。
