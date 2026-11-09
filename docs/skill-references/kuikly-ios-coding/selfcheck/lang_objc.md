# 自检清单 · lang_objc（L1）

> 全文 SoT：`docs/skill-references/coding-norms/base/lang_objc.md`。

| # | 条款 | 什么算 `OK`（判据） |
|---|---|---|
| O1 | 类前缀 | 新建类有仓内前缀，非裸通用名 |
| O2 | 方法命名 | 小写开头 camelCase；选择子表意完整 |
| O3 | KVC 访问器 | 属性/setter 符合惯例 |
| O4 | ARC | 无手写 retain/release（CF 例外已注释） |
| O5 | 可空性标注 | 暴露给 Swift 的头有 nullability |
| O6 | delegate 弱引用 | delegate 用 weak（或说明非 UI 所有权模型） |
