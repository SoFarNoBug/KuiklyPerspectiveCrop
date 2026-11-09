doc_version: 20261109_1
layer: L1

# Swift 语言规范（L1）

> **范围**：Swift 语言命名与 API 惯用（语义）；**不含** UIKit 线程/生命周期（→ L2 `platform_ios`）。本文件属 **可移植 L1**。  
> **来源**：[Swift API Design Guidelines](https://www.swift.org/documentation/api-design-guidelines/)（SE-0023；swift.org 真源）。未列条目默认跟官方；冲突以本文件为准。冲突序 **L3 > L2 > L1**。

## 硬约束

### 基本原则

- **使用点清晰优先于简短**（Clarity at the point of use > brevity）。
- 公开声明尽量写 **文档注释**（`///`，Swift Markdown）；摘要用一句片段、句号结尾。

### 命名与大小写

- **类型 / 协议**：`UpperCamelCase`；其余（函数、属性、变量、枚举 case）`lowerCamelCase`。
- 首部缩写：非类型名开头的缩写按小写驼峰（如 `htmlElement`）；类型名中缩写惯例跟 Apple SDK。
- **不要**给 Swift-only API 加历史 ObjC 式三字母前缀（Apple 现行 Swift-only 口径）；与 ObjC 互操作的桥接类型可保留仓内既有前缀（本仓 Kuikly 壳 `HR*` / `KR*` 等）。

### 方法与参数标签

- 优先 **方法/属性**，少用无必要的自由函数。
- 参数标签使调用点可读：`move(from:to:)`，避免把语义全塞进基名却省略标签。
- 常用单值参数可给 **默认参数**；副作用命名跟官方 `-ed` / `-ing` 等约定（mutating vs 返回新值）。
- 布尔参数名用肯定形式，避免 `isNot…`。

### 惯用

- 优先 `let`；可变再用 `var`。
- 可空用 `Optional`；优先 `if let` / `guard let` / `??`；少用强制解包 `!`（契约非空须旁注）。
- 错误用 `throws` / `Result`；勿默默吞错。
- 计算属性若非 O(1)，文档注明复杂度。

## 判据速查

| 场景 | 结论 |
|---|---|
| 类型名 lowerCamel / 方法 UpperCamel | **FAIL** |
| 调用点含糊、缺参数标签 | **FAIL** → 按 API Design Guidelines 改名 |
| 裸 `!` 无契约说明 | **FAIL** |
| UIKit 主线程 / 生命周期 | **L2 platform_ios** |

## 非本文件

- Objective-C / Cocoa 命名 → L1 [`lang_objc.md`](lang_objc.md)
- 主线程 UI、App 生命周期 → L2 [`platform_ios.md`](platform_ios.md)
