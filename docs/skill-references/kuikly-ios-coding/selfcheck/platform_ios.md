# 自检清单 · platform_ios（L2）

> 全文 SoT：`docs/skill-references/coding-norms/base/platform_ios.md`。

| # | 条款 | 什么算 `OK`（判据） |
|---|---|---|
| I1 | UI 仅主线程 | UIKit 创建/修改在主线程或 `@MainActor` |
| I2 | 耗时不堵主线程 | 解码/IO/重计算已下沉后台 |
| I3 | 生命周期 | 异步完成有取消/忽略；不向已消失 VC 强更 UI |
| I4 | 无单例强持 VC | 长寿命对象未强持 ViewController |
| I5 | 沙箱路径 | 文件落在允许目录；Caches 不当唯一真源 |
| I6 | 观察者成对 | 通知/KVO 有移除 |
