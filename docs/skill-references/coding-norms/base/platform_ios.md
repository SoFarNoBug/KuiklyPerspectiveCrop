doc_version: 20261109_1
layer: L2

# iOS 平台规范（L2）

> **范围**：UIKit / App 生命周期、主线程与并发、资源与沙箱路径等 **可移植** 平台硬约束。  
> **来源**：[UIKit and AppKit apps](https://developer.apple.com/documentation/technologyoverviews/uikit-appkit)（UI 必须在主线程）、[Understand and eliminate hangs](https://developer.apple.com/videos/play/wwdc2021/10258/)（主线程勿阻塞）、UIApplication / 场景生命周期官方文档。未列条目默认跟 Apple；冲突以本文件为准。冲突序 **L3 > L2 > L1**。

## 硬约束

### 1. 主线程与 UI

- **所有 UIKit UI 相关工作必须在主线程**（创建/修改/销毁 `UIView` / `UIViewController`、改图层与约束等）。
- 后台计算完成后用主队列更新 UI（`DispatchQueue.main` / `@MainActor`）；禁止后台直接碰 UIKit。
- **耗时操作不得堵主线程**：同步网络、大图解码、磁盘大 IO、重计算 → 后台；否则易 hang / 掉帧。

### 2. 生命周期

- 尊重 App / Scene 状态转换；进入后台后勿假设可无限跑 UI 或网络（按 Background Modes 能力声明）。
- 视图控制器消失后取消或忽略会更新 UI 的异步回调（避免对已释放/`deinit` 对象投递）。
- 长生命周期对象（单例、缓存）**勿强持有** `UIViewController`；用弱引用或显式解绑。

### 3. 内存与资源

- 大图 / 缓存设上限；收到内存警告时释放可再生缓存。
- 文件写在 App 沙箱允许目录（Documents / Caches / tmp）；Caches 可被系统清，勿当唯一真源。
- 打开的文件句柄 / 通知观察者 / KVO 须成对注销。

### 4. 与 KMP / Kuikly 壳

- Kotlin/Native `iosMain` 与 Swift/ObjC 壳边界：公开契约用稳定字符串路径 / 句柄，**禁止**静默吞失败。
- Bridging / Module 命名与现有 `HR*` / `Kuikly*` 壳一致；新建自定义 View 注册须两边对齐。

## 判据速查

| 场景 | 结论 |
|---|---|
| 后台线程改 UIView / 弹 UI | **FAIL** |
| 主线程同步大 IO / 解码 | **FAIL** → 下沉后台 |
| VC 已消失仍强更 UI | **FAIL** |
| 单例强持 VC | **FAIL** |
| Swift / ObjC 命名风格 | L1 `lang_swift` / `lang_objc` |

## 非本文件

- Swift → L1 [`lang_swift.md`](lang_swift.md)
- Objective-C → L1 [`lang_objc.md`](lang_objc.md)
- 透视裁剪公开 API → 各项目 L3 `perspective_crop_api`
