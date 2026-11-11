# 真机可执行档 · config-corner-border-operation-ui

- 关联报告：`docs/reviews/review-config-corner-border-operation-ui.md`
- 包名 / 入口：Demo `perspective_crop_lab`（外观 Tab）
- 前置条件：已装含本改动的 Debug 包；默认 Config（勿预开换皮）

## 步骤

| # | 操作 | 类型 | 期望 |
|---|---|---|---|
| 1 | 打开 Lab，看默认角点与框线 | assert | 框线红；角点**白芯 + 红描边**（Accent Ring）；非旧版纯红实心 |
| 2 | 拖任一角点 | tap | 可拖；放大镜仍出现（`showOperationUI` 默认开） |
| 3 | 外观 Tab 点「角点描边·关」 | tap | Toggle 变关；角点变为**白实心**（无红圈）；框线仍红 |
| 4 | 再点「角点描边·开」 | tap | 恢复白芯红圈 |
| 5 | （可选）临时代码 / 调试把 `showOperationUI=false` 后重进页 | assert | **无**蒙版/框/角/镜；底图仍在；仍可拖角/边改选区 |
| 6 | 恢复 `showOperationUI=true`，开「绘制边线中点」 | tap | 中点为半透明白圆、**无**红描边 |

类型合法值：`tap` `assert` `scroll` `input` `wait` `screenshot` `track_logcat` `navigate`

## 覆盖对照

| 审查缺口 | 本表步骤 |
|---|---|
| Accent Ring 默认观感（白芯红圈 vs 旧红实心） | #1 #3 #4 |
| 描边 Stroke 居中导致视觉半径略胀 | #1（目视可接受即可） |
| `showOperationUI=false` 整包藏叠层仍可拖（Lab 无 Toggle） | #5 |
| 边中点无描边、只复用填充 α | #6 |
