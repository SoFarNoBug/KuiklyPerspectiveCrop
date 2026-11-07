# Review · guide-lines-image-aabb

- 结论：**通过**
- 依据：Plan「辅助线对齐 freequadcrop（整图 Fit 九宫格）」A 方案

## P0

无。

## P1

### P1-device-verify · 九宫格相对整图、拖框不斜变须目视
- 证据：
  - `DefaultGuideOverlayPainter` 按 `contentLeft/Top/W/H` 画 1/3、2/3 横竖线
  - `PerspectiveCropStage` 仅在 `mapping != null` 且 `viewPoints.size==4` 时调用
- 影响：代码可证公式与旧库一致，无法证旋转后 Fit 矩形与线重合、拖角后线不跟斜
- 处置：**已关**（2026-11-07 用户真机验证 ok → [device-check-list/guide-lines-image-aabb.md](device-check-list/guide-lines-image-aabb.md)）
- 缺口要点：①Lab 开辅助线见横平竖直九宫格；②拖角线不随选区变形；③关开关无线；④可选 rotate 后线仍贴合图面

## P2

### P2-unused-viewPoints
- 证据：`DefaultGuideOverlayPainter.drawGuides` 的 `viewPoints` 未使用（与旧库忽略 `cropPoints` 一致）
- 影响：接口保留供自定义画家；默认实现有未用参噪音
- 处置：可选（`@Suppress` 或 KDoc 标明「默认忽略」；不挡通过）

## 规范候选观察

无。

## 真机

- 可执行档：[device-check-list/guide-lines-image-aabb.md](device-check-list/guide-lines-image-aabb.md)
- 结果：用户 2026-11-07 真机验证 **ok**（主路径 #1–#4）

## 对照上轮

| 桶 | 状态 |
|---|---|
| P1-device-verify | 已关（真机 ok） |
| P2-unused-viewPoints | 未关（可选） |
