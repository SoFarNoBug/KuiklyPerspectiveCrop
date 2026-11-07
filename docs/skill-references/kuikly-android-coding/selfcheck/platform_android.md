# 自检清单 · platform_android（L2）

> 全文 SoT：`docs/skill-references/coding-norms/base/platform_android.md`。本文件只摘「条款 + 什么算做到了」，判据细节以全文为准。
> 三态图例见同目录 `README.md`：`OK`（落点行号）/ `TODO`（不做 + 理由）/ `N/A`（一句判据）。

| # | 条款 | 什么算 `OK`（判据） |
|---|---|---|
| A1 | 耗时操作不堵主线程 | 磁盘 IO / 网络 / 解码 / 重计算已切后台线程或协程；主线程只剩短同步工作。新代码若在主线程做上述任一动作即判 `TODO` |
| A2 | 仅主线程碰 UI | 后台结果切回主线程后才改 View；无后台直接 `setText`/`invalidate`/弹 Toast |
| A3 | 组件销毁后不更新 UI | 新增的异步完成回调有生命周期守卫（`isFinishing`/`lifecycleScope`/取消机制），或明确说明不涉及 |
| A4 | 长任务可取消 | 新增长任务绑定了项目既有的取消机制，未裸跑 |
| A5 | 长生命周期对象不持 Activity | 单例/缓存/Manager 未持有 Activity；能用 `applicationContext` 的没用 UI Context |
| A6 | UI Context 正确 | Toast/Dialog 等需要主题或窗口的用 UI Context，未误用 Application Context |

## 说明

本清单对**每一单**都要填（任何业务改动都会命中 A1/A2 至少其一）。纯算法/纯数据改动可对 A3–A6 写 `N/A` + 判据。
