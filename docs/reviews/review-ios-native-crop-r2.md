# Review · ios-native-crop（r2）

- 结论：**通过**
- 依据：对照 `review-ios-native-crop.md`；码农已关旋源/文件名/CG alpha 阻断；用户回报真机验证 OK（2026-11-09，含原生放大镜）

## P0

无。

## P1

无未关项。

## P2

无未关必修项（历史 P2 见下表）。

## 真机

- 可执行档：[device-check-list/ios-native-crop.md](device-check-list/ios-native-crop.md)
- 用户确认：步骤 1–7 **OK**；步骤 8 可选跳过

## 开放发现摘要（相对 r1）

| 卡 / 项 | 结论 |
|---|---|
| P1-device-verify | **已关** — 真机表步骤 1–7 用户确认 OK |
| P2-rotate-uigfx | **已关** — 纯 `CGBitmapContext` 旋源；步骤 #7 OK |
| P2-filename-collision | **已关** — 毫秒 + 原子序号 token |
| P2-premul-alpha | **已关（回退）** — `kCGImageAlphaLast` 在 iOS Create 失败；恢复 `PremultipliedLast`；出图/放大镜真机 OK |
| 放大镜策略 | **已切原生** — `MagnifierBlitStore` + `KRPerspectiveCropMagnifier`；步骤 #6 OK |

## 写权限自检

未改业务源码、未改 severity；仅关审查项与真机表勾选。
