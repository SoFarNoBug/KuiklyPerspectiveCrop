doc_version: 20261022_1
layer: L2

# Android 平台规范（L2）

> **范围**：Android 平台通用硬约束（线程、生命周期、Context）。本文件属 **可移植 L2**，多项目共享；项目贴肉政策与低频事故反例写在**各项目仓内红线 / 反例簿**，不进本文件。  
> **来源**：[Processes and threads](https://developer.android.com/guide/components/processes-and-threads)、[Activity lifecycle](https://developer.android.com/guide/components/activities/activity-lifecycle)、[Context](https://developer.android.com/reference/android/content/Context)。未列条目默认跟官方；冲突以本文件为准。冲突序 **L3 > L2 > L1**。

## 硬约束

### 1. 线程

来源：[Processes and threads](https://developer.android.com/guide/components/processes-and-threads)

- **耗时操作不得堵主线程**：磁盘 I/O、网络、解码、重计算等须切到**后台线程**或**协程**（如 `Dispatchers.IO` / `Default`）执行。
- **仅主线程改 UI** / 碰 View；后台结果须切回主线程再更新。
- 主线程上的短同步工作可以保留；一旦可能阻塞帧或 ANR，一律下沉。
- **不规定**各仓用 Executor / Rx / 哪版协程——具体栈留各项目 L3。

### 2. 生命周期

来源：[Activity lifecycle](https://developer.android.com/guide/components/activities/activity-lifecycle)

- 组件销毁后**不再更新 UI**、不再投递会泄漏的回调（含匿名监听、未取消的异步完成回调）。
- 长任务与生命周期绑定：用项目选定的**生命周期感知取消机制**（取消，或在合适回调停止）。
- **不绑定**具体「必须用 viewModelScope / lifecycleScope」等单栈 API；选哪种取消机制由各项目 L3 / 技术栈决定。

### 3. Context

来源：[Context](https://developer.android.com/reference/android/content/Context)

- 需要 UI / 主题 / 窗口时用 Activity（或等价 UI Context）。
- 长生命周期对象（单例、缓存、全局 Manager 等）**勿持有 Activity**；能 `applicationContext` 的别抓 Activity。
- Toast / Dialog 等勿滥用 Application Context，以免主题 / 窗口错配（按官方口径点明：需要主题或窗口的 UI 附着用 UI Context）。

## 判据速查

| 场景 | 结论 |
|---|---|
| 主线程做磁盘 / 网络 / 解码 / 重计算 | **FAIL** → 切后台线程或协程 |
| 后台线程直接改 View / 触 UI | **FAIL** → 切回主线程再更新 |
| 组件已 destroy 仍改 UI / 投递泄漏回调 | **FAIL** → 绑定生命周期取消或停止 |
| 单例 / 长生命周期对象持有 Activity | **FAIL** → 用 `applicationContext` 或弱引用 / 不持有 |
| Toast / Dialog 用错 Context 导致主题或窗口异常 | **FAIL** → 用 UI Context（或按官方适用场景） |
| 本仓具体用 Executor / Rx / 协程版本 | **各项目 L3**（本文件不规定） |
| 必须用某生命周期 Scope API | **各项目 L3**（本文件只要求「有取消机制」） |

## 非本文件

- DEBUG 声明一致性（禁止用 DEBUG 开关包住整个声明）→ **各项目仓内红线**
- 系统栏 / insets、UI 附着 / binding 等低频事故 → **各项目反例簿 misuse**
- 网络模型 keep、proguard 落点、新建文件语言政策 → **各项目 L3**
- Kotlin / Java 语言惯用 → L1 [`lang_kotlin.md`](lang_kotlin.md) / [`lang_java.md`](lang_java.md)
