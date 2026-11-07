doc_version: 20261027_1
layer: L1

# Kotlin 语言规范（L1）

> **范围**：语言惯用（语义），**不含**排版。本文件属 **可移植 L1**，多项目共享；项目政策（新建文件语言、`@Jvm*`、Parcelable 插件等）写在**各项目 L3**。  
> **来源**：[Coding conventions](https://kotlinlang.org/docs/coding-conventions.html)、[Null safety](https://kotlinlang.org/docs/null-safety.html)、[Idioms](https://kotlinlang.org/docs/idioms.html)。未列条目默认跟官方；冲突以本文件为准。冲突序 **L3 > L2 > L1**。

## 硬约束

### 不可变

- 能不改就用 **`val`**，不要默认 `var`。
- 对外只读集合声明为 **`List` / `Set` / `Map`**，工厂优先 `listOf` / `setOf` / `mapOf`；可变集合留在实现内，边界再收窄为只读。

### 空安全

- 可空显式 `T?`；优先 `?.`、`?:`、判空后的智能转换。
- **`!!` 非默认**：仅「已校验 / 契约非空」且旁注一句；禁止链式 `a!!.b!!`。
- `Boolean?` 条件写 `if (v == true)` / `== false`，勿当非空 Boolean。
- **平台类型**（Java 等返回的 `T!`）：作为公开函数返回值或属性初值时，必须写出明确 Kotlin 类型（`String` / `String?`），禁止推断泄漏到公开 API。

### 命名

- 包名全小写、无下划线；类型 UpperCamel；函数/属性/局部变量 lowerCamel。
- **以下两条是上一条的例外（特例优先，不判违规）**：
  - 深不可变的 `const val` 与同类顶层/`object` `val` → `SCREAMING_SNAKE`；可变或带行为的 → camelCase。
  - **私有成员变量（字段）一律加 `_` 前缀**，`_` 之后仍是 lowerCamel（`_items` / `_panelBinding`）；公开只读属性写不带下划线的名字并在 `get()` 里返回该字段（`private val _items` ↔ `val items: List<X> get() = _items`）。
    - 范围：**只有私有成员变量加**。局部变量、函数参数、公开属性、`companion object` 常量**一律不加**（`_` 不是"内部"的通用记号）。
    - 生效范围：**本单新增 / 修改的私有成员变量**；既有未触及的字段不回填（避免把 diff 膨胀成全仓改名）。
- 缩写：两字母全大写（`IOStream`）；更长只首字母大写（`HttpInputStream`）。避免空泛 `Util`/`Manager`；单类型文件名与类型名一致。

### 控制流与函数

- 单表达式优先 `fun f() = …`；返回 `Unit` 时省略 `: Unit`。
- `if` / `when` / `try` 优先作**表达式**；**二分用 `if`，≥3 支用 `when`**。
- Kotlin 侧默认参数优于一堆重载；若会被 **Java** 调用，另遵各项目 L3 的互操作约定（如 `@JvmOverloads`）。

### Lambda、扩展与属性

- 短非嵌套可用 `it`；**嵌套 lambda 必须显式参数名**。少用多个标号 `return`。
- 同型基本类型或 `Boolean` 实参含义不清时，用**命名参数**。
- 扩展尽量收窄可见性，避免无约束顶层扩展污染 API。
- 无参、廉价、不抛、状态不变则结果不变 → 倾向只读**属性**；否则用**函数**。

### 类成员顺序

属性与 init → 次构造 → 方法 → `companion`；相关逻辑放一起；**重载相邻**（勿按字母机械排序）。

## 判据速查

| 场景 | 结论 |
|---|---|
| 只赋一次 / 对外只读集合 | `val` / `List` + `listOf` |
| 可空访问 / `Boolean?` / Java→公开 API | `?.`/`?:`；`== true`；显式 Kotlin 类型 |
| 二分 / 多分支 / 嵌套 lambda | `if` / `when` / 显式参数名 |
| 无参纯计算 | 优先 `val` 属性 |
| 新建文件语言、`@Jvm*`、Parcelable 政策 | **各项目 L3** |

## 非本文件

- Android 平台通用 → L2 [`platform_android.md`](platform_android.md)
- DEBUG 包声明 / insets 等 → 各项目红线或 misuse
- 新建文件 / Java 互操作细则 → **各项目 L3**
- 排版 → IDE；scope 函数 → [Scope functions](https://kotlinlang.org/docs/scope-functions.html)（禁炫技多层嵌套）
