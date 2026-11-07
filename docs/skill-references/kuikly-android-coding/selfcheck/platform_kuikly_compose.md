# 自检清单 · platform_kuikly_compose（L2）

> 全文 SoT：`docs/skill-references/coding-norms/base/platform_kuikly_compose.md`。  
> 三态：`OK`（落点行号）/ `TODO`（不做 + 理由）/ `N/A`（一句判据）。

| # | 条款 | 什么算 `OK`（判据） |
|---|---|---|
| K1 | Compose 包名正确 | 新增 import 无 `androidx.compose.ui/foundation/material3/animation`；仅 runtime 用 androidx |
| K2 | 未使用不可用 API | 无 verticalScroll/horizontalScroll/原生 nestedScroll 签名/OutlinedTextField 等上表项 |
| K3 | pointerInput key 完整 | 闭包读取的可变状态均已进入 `pointerInput(...)` key；或本轮无 pointerInput |
| K4 | Kuikly 线程约束 | 异步完成后回 Kuikly/主约定线程再改 UI 状态；无后台直接写 Compose state / Module |
| K5 | willInit 不调 Module | 新页面/改 willInit 未在其中 acquireModule；或本轮未碰页面入口 |

## 说明

命中条件：本轮改动触及 `*.kt` 且路径含 Compose UI / `commonMain` 页面 / 手势叠层。纯 `CropSession` 几何、纯 Android `CropProcessor` 解码可对 K1–K3、K5 写 `N/A`，仍须核对 K4（若有异步）。
