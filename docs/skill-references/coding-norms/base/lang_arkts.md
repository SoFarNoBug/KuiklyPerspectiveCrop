doc_version: 20261109_1
layer: L1

# ArkTS / ETS 语言规范（L1）

> **范围**：ArkTS（`.ets`）语言风格与编程实践；**不含** Ability / UI 线程等平台政策（→ L2 `platform_ohos`）。本文件属 **可移植 L1**。  
> **来源**：[华为 ArkTS 编程规范](https://developer.huawei.com/consumer/cn/doc/harmonyos-guides/arkts-coding-style-guide)、DevEco `@hw-stylistic` 风格规则。未列条目默认跟官方；冲突以本文件为准。冲突序 **L3 > L2 > L1**。

## 硬约束

### 命名

- 标识符用正确英文单词，禁止拼音/无意义单字母（循环计数等短名除外）。
- **类 / 枚举 / 命名空间**：`UpperCamelCase`。
- **变量 / 方法 / 参数**：`lowerCamelCase`。
- **常量**（`const` 字面量级）：`UPPER_SNAKE_CASE`。
- 布尔命名用肯定形式，前缀 `is` / `has` / `can`；避免 `isNotXxx`。

### 格式（对齐 @hw-stylistic）

- **缩进 2 空格**；禁止 Tab。
- **行宽 ≤ 120**（URL / 长路径注释可例外）。
- `if` / `for` / `while` / `do` **必须加大括号**，即使单行。
- 字符串优先**单引号**；运算符两侧空格；逗号后空格。
- 一行只声明一个变量；开括号不换行（K&R）。

### 类型与实践

- **非跨语言场景禁止 `ESObject`**（会引入多余跨语言开销）。
- 数组类型写 **`T[]`**，不要写 `Array<T>`（官方建议）。
- 类成员建议显式 `private` / `protected` / `public`（默认可被视作 public）。
- `NaN` 用 `Number.isNaN`；浮点字面量写完整（`0.5` 而非 `.5`）。
- 优先用数组高阶方法表达变换；复杂副作用仍可用清晰循环。

### 本仓壳约定（与官方不冲突）

- 自定义 View / Module 类名保持 `KR*` 前缀（与 Kuikly 壳一致）。
- 与 KN / NAPI 交互的字符串路径、句柄字段名与 Kotlin `@CName` / NAPI 导出名对齐，禁止静默改名。

## 判据速查

| 场景 | 结论 |
|---|---|
| 类名小写 / 常量 camelCase | **FAIL** |
| 单行 `if` 无大括号 | **FAIL** |
| 非跨语言用 `ESObject` | **FAIL** |
| 数组写成 `Array<T>` | **FAIL**（改为 `T[]`） |
| Ability 生命周期 / UI 线程 | **L2 platform_ohos** |

## 非本文件

- Stage 模型、UI 线程、NAPI、PixelMap 生命周期 → L2 [`platform_ohos.md`](platform_ohos.md)
- 新建文件语言与文件头 → 各项目 L3
