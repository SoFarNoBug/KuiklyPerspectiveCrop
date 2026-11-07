doc_version: 20261027_1
layer: L1

# Java 语言规范（L1）

> **范围**：改既有 `.java` 时的语言惯用（语义），**不含**排版。本文件属 **可移植 L1**，多项目共享；是否允许新建 Java、包名 keep、事故个案等写在**各项目 L3**。  
> **来源摘录**：[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)。未列条目默认跟 Google Style + IDE；冲突以本文件为准。冲突序 **L3 > L2 > L1**。  
> ★`NullPointerException` 是 Java 生产环境最常见崩溃之一：改 Java 时把「防空指针」当默认验收项，不可省略。

## 硬约束

### 空指针防护（P0 · 必守）

写 / 改 Java **必须**按下列做，Review 可直接按此拦：

1. **先契约、再解引用**：对「可能为 null」的参数、返回值、字段、集合元素、Map `get`、Intent/Bundle extra、回调入参——**先判空或早退**，再 `.` 调用；禁止「默认它一定有」。
2. **公开 API 写清空值**：Javadoc 或（模块已用时）`@Nullable` / `@NonNull` 标明；Kotlin 会读这些注解生成平台类型，契约错了会把 NPE 推到对端。
3. **返回「没有」优先空集合**：列表/集合「无数据」返回空集合，**禁止返回 null 列表**；单对象「没有」才用 `@Nullable` 并强制调用方处理。
4. **链式调用拆开**：`a.getB().getC()` 任一段可能 null 时，拆成局部变量逐步判空，或在确认非空的路径上写注释说明不变量；禁止超长裸链。
5. **equals / 常量写左边**：`"x".equals(s)` 或 `Objects.equals(a, b)`；勿 `s.equals("x")` 在 `s` 可能 null 时。
6. **禁止空 catch 吞 NPE**：捕获后至少日志 + 明确降级/上抛并保留 cause；「吞掉当没事」会变成线上难查崩溃。
7. **改被 Kotlin 调用的方法**：收窄或放宽空值语义必须同步调用方；禁止静默把「保证非 null」改成可 null（或反过来）却不改对端。

### 不可变与集合

- **`final` 强制（不再「尽量」）**：局部变量与字段，凡「声明后不再重新赋值」一律加 `final`，包括 `for`-each 循环变量、`catch` 形参外的临时变量。唯一例外只能是**本质上需要重新赋值**的变量（累加器、循环计数、状态机字段等），此类不需额外说明。
  - 范围：**本单新增 / 修改的代码**。未触及的既有代码不回填（避免把 diff 膨胀成重构）。
  - 方法参数**不强制**（加 `final` 会改签名观感、影响调用方阅读），但参数重赋值请改局部变量。
  - 「忘了加」不是理由：清单里该条为**强制项**，未做即未整改，不接受 `TODO` 兜底。
- 对外只读：返回不可变视图或拷贝；**勿直接返回内部 `ArrayList` 引用**。
- 编程对接口：`List`/`Map`/`Set`，实现类留在 `new` 处。

### 可见性与 API 稳定

- 能 `private` / 包可见就不抬 `public`。
- 覆盖方法必须 `@Override`。
- 被 Kotlin 调用的 Java API：改签名/命名前评估调用方（与空值节第 7 条一并验收）。

### 命名

- 包名全小写无下划线；类 UpperCamel；方法与非常量字段 lowerCamel；常量 `SCREAMING_SNAKE`。
- 单类型一文件，文件名与公开顶层类型同名。忌空泛 `Util`/`Manager` 堆砌。

### equals / hashCode / toString

- 覆写 `equals` 必须同时覆写 `hashCode`。
- `toString` 仅调试可读；勿当业务协议解析。

### 异常与资源

- 不吞异常；可关闭资源用 try-with-resources。

### 类成员顺序

常量 → 字段 → 构造 → 方法；重载相邻；相关逻辑放一起。

## 判据速查

| 场景 | 结论 |
|---|---|
| 解引用前 | 已判空 / 早退 / 不变量注释，否则 **不过** |
| 「无数据」的 List | 空列表，**禁止 null** |
| `a.getB().getC()` | 拆开判空，禁裸长链 |
| 字符串比较 | `"x".equals(s)` 或 `Objects.equals` |
| 改 Kotlin 已依赖的非 null 返回 | 必须同步改调用方或保持契约 |
| 只赋一次的局部 / 字段 | **必须 `final`**（强制；既有未触及代码不回填） |
| 是否允许新建 `.java` | 看**各项目 L3**（许多 Android 仓禁止新建、改开 `.kt`） |

## 非本文件

- Android / DEBUG / View → L2 [`platform_android.md`](platform_android.md)
- 新建文件语言、keep 包名、项目特有政策 → **各项目 L3**
- 排版 → IDE / Google Style 格式化节
