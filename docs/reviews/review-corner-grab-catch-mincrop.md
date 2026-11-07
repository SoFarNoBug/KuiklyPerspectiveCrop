# Review · corner-grab-catch-mincrop

- 结论：**通过**
- 依据：Plan「角点 grab / catch·minCrop 默认对齐」；changelog `docs/specs/corner-grab-catch-mincrop/changelog.md`

## P0

无。

## P1

### P1-device-verify · 帧增量/抓取/命中/minCrop 须目视与触控
- 证据：同首轮（Lab 场景文案已改）
- 影响：—
- 处置：**已关**（2026-11-08 用户真机验证 ok → [device-check-list/corner-grab-catch-mincrop.md](device-check-list/corner-grab-catch-mincrop.md)）
- 缺口要点：同首轮

## P2

### P2-fitscale-default-one
- 证据：`_fitScale` 初值/`clearImage` 置 `0`；未就绪 `canCrop()=false`；`minCropImage` 返回 `MAX_VALUE`
- 影响：—
- 处置：**已关**（速速改）

## 规范候选观察

无。

## 真机

- 可执行档：[device-check-list/corner-grab-catch-mincrop.md](device-check-list/corner-grab-catch-mincrop.md)
- 结果：用户 2026-11-08 真机验证 **ok**（主路径）；同日 H5 冒烟已跑；Lab/README 场景文案已补

## 对照上轮

| 桶 | 状态 |
|---|---|
| P1-device-verify | 已关（真机 ok） |
| P2-fitscale-default-one | 已关（fitScale 未就绪偏严） |
