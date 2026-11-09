doc_version: 20261109_1
layer: L1

# Objective-C 语言规范（L1）

> **范围**：Objective-C / Cocoa 命名与语言惯用；**不含** UIKit 线程与 App 生命周期（→ L2 `platform_ios`）。本文件属 **可移植 L1**。  
> **来源**：[Coding Guidelines for Cocoa](https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/CodingGuidelines/CodingGuidelines.html)、[Programming with Objective-C · Conventions](https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/ProgrammingWithObjectiveC/Conventions/Conventions.html)。未列条目默认跟 Apple；冲突以本文件为准。冲突序 **L3 > L2 > L1**。

## 硬约束

### 命名（Cocoa）

- **类名**全局唯一；使用 **前缀**（Apple 两字母前缀保留；本仓壳沿用既有 `HR*` / `KR*` / `Kuikly*`，新建勿抢 `NS`/`UI`）。
- **方法**：小写开头 + camelCase；选择子应表达完整意图（`stringByAppendingString:`），勿无意义缩写。
- **属性 / 访问器**须符合 KVC 惯例（`foo` / `setFoo:`；布尔可用 `isFoo`）。
- 常量：相关枚举 / 字符串常量跟框架风格；宏名 `UPPER_SNAKE` 且慎用。
- 实例变量：若显式声明，惯用前缀 `_`（与合成 ivar 一致）。

### 头文件与导入

- 公开接口放 `.h`；实现放 `.m`（ObjC++ 用 `.mm`）。
- 头文件尽量 `#import` 框架模块或前向声明，避免头文件互相拖入重依赖。
- Bridging Header 只放确需 Swift 可见的 ObjC 头；勿整仓无差别导入。

### 内存与空值（ARC）

- 默认 **ARC**；禁止在 ARC 工程里手写 `retain`/`release`（CF 桥接等例外须注释）。
- `weak` 用于打破循环（delegate 通常 `weak`）；块捕获 `self` 注意循环引用（`weakSelf`/`strongSelf` 模式）。
- 消息发往 `nil` 安全，但**不要**用「靠 nil 吞错」代替显式失败处理。

### 互操作

- 与 Swift 混编：可空性用 `_Nullable` / `_Nonnull`（或 `NS_ASSUME_NONNULL` 区段）标注，避免把隐式可空泄漏到 Swift。
- 工厂方法 / init 命名跟 Cocoa（`initWith…`、`…With…`）。

## 判据速查

| 场景 | 结论 |
|---|---|
| 无前缀的通用类名（如 `ViewController`）进库/壳 | **FAIL** |
| 选择子含糊 / 违反 KVC 访问器名 | **FAIL** |
| ARC 工程手写 retain/release | **FAIL** |
| delegate `strong` 导致环 | **FAIL** |
| UI 线程策略 | **L2 platform_ios** |

## 非本文件

- Swift API → L1 [`lang_swift.md`](lang_swift.md)
- 主线程 / 生命周期 → L2 [`platform_ios.md`](platform_ios.md)
