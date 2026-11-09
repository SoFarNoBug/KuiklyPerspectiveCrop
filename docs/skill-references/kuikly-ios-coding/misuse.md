doc_version: 20261109_1

# iOS 编码常见错误反例

> 本文件为**反例真源**。新增按「错误 N」续排。

---

### 错误 1：后台线程直接改 UIKit

**错误写法**：`DispatchQueue.global().async { self.view.backgroundColor = … }` 或在 Kotlin/Native 回调线程直接调 UIView。

**为什么错**：UIKit 要求 UI 在主线程；轻则警告，重则崩溃/错乱。

**正确写法**：计算在后台，UI 更新派发主队列 / `@MainActor`。

---

### 错误 2：主线程同步大图解码或磁盘 IO

**错误写法**：`viewDidLoad` / 手势回调里同步读大文件或解码全尺寸图。

**为什么错**：阻塞主 runloop → hang（见 WWDC「eliminate hangs」）。

**正确写法**：后台解码/读盘，完成后再回主线程赋图像。

---

### 错误 3：新建 ObjC 类无前缀

**错误写法**：壳内新建 `ViewController` / `ImageHelper` 无前缀。

**为什么错**：ObjC 类名全局唯一；易与系统/依赖冲突（Apple Conventions）。

**正确写法**：沿用仓内 `HR*` / `Kuikly*` / 业务前缀。
