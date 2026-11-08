# 真机可执行档 · compose-dp-units

- 关联报告：`docs/reviews/review-compose-dp-units.md`
- 包名 / 入口：`androidApp` Debug；pageName `perspective_crop_lab`（可对照产品页 `perspective_crop_demo`）
- 前置条件：JDK17 已 `installDebug`；样例图可加载；测试机建议高密度（如 1440p）；已知旧库 20dp 角/边 catch、描边观感作对照（有旧 Demo 更佳）

## 步骤

| # | 操作 | 类型 | 期望 |
|---|---|---|---|
| 1 | 打开全能配置 Lab，外观 Tab 默认态 | assert | 裁剪框描边、角点圆相对改前（裸 px）明显更大；接近旧库 dp 量级，非「细若发丝」 |
| 2 | 拖四角 | tap | 命中圈好点；无需像素级对准 |
| 3 | 开「矩形模式」；在边外约一指宽内按下再拖；开关「矩形模式触点吸附」 | tap | 吸附·开：边靠向触点；关：空隙更易保留（默认 catch=20dp 下应可感，弱于临时 100px 试参） |
| 4 | 外观 Tab 开「内缩显示开关」 | assert | 四周留白约 **24.dp** 视觉宽度（高密度上大于旧「24 裸 px」）；蒙版仍满屏 |
| 5 | （可选）低密度机或系统显示大小切换后重复 #1 | assert | 物理尺寸大体稳定，不随分辨率「缩成针尖」 |

类型合法值：`tap` `assert` `scroll` `input` `wait` `screenshot` `track_logcat` `navigate`

## 覆盖对照

| 审查缺口 | 本表步骤 |
|---|---|
| 描边/角点 dp 观感 | #1 |
| catch 命中 | #2 #3 |
| 矩形吸附可感 | #3 |
| contentInset 按 dp | #4 |
| 跨密度稳定（可选） | #5 |
