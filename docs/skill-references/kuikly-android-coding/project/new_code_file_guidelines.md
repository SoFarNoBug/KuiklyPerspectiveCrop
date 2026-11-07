doc_version: 20261104_1
layer: L3

# 新建代码文件准则（L3 · 原 kotlin_first）

> **分层**：本文件为规范**全文真源**。编码会话红线摘要见 `.cursor/rules/kuikly-dev-android.mdc`；流程见 Skill。勿与 Rule 双写完整条文（[ADR 0021](../../../adr/0021-rule-redline-references-sot.md)）。
> **本意**：新代码文件**优先写 Kotlin**；本仓 Java 存量为主，禁止「为了和旁边文件一致」而新建 Java。纯 Kotlin 语言惯用法见 L1 [`coding-norms/base/lang_kotlin.md`](../../coding-norms/base/lang_kotlin.md)。三层货架见 [ADR 0038](../../../adr/0038-coding-norms-three-layer.md)。

**级别**：核心规范（强制）。违反 = 该次编码不合格，须整改后再进 Review。

## 规则

1. **新建文件优先 Kotlin**：数据类、Parcelable / 实体、工具类、常量持有者、新建模块，全部适用本仓**全部** module（`app` / `basiclib` / `basicres` / 业务模块）。
2. **既有 Java 文件可改，且鼓励就地改 Java**：改 Java 老代码时就地改；需要「新开一个文件」承载新逻辑时，**默认开 `.kt`**。
   - **例外（须留痕）**：所在模块**整体为 Java 工程且该文件必须与既有 Java 强耦合**（如继承同包 Java 抽象类、实现该模块 Java 回调用法的内部类、需要真 Java 类的注解处理器 / 反射契约、必须与 `databinding` 旧式绑定形态一致），可新建 `.java`，但**必须在该次 changelog 写明「为何不能用 Kotlin」**。
   - 例外不是默认值：判不出理由就写 Kotlin。
3. **新建 Kotlin 必须对 Java 调用点零改造成本**（本仓主体现为 Java，几乎必然被 Java 调用）：
   - 静态入口（工具类 / 单例）用 `object` + `@JvmStatic`，Java 侧调用形态与静态工具类完全一致；
   - 公开数据字段用主构造 `@JvmField var`（或 `@JvmField val`），Java 侧继续「字段直读」，**避免同一数据出现 getter / 字段两种读法并存**；需要「无效判定」这类派生逻辑写成 `fun isEmpty()`（不要写成只读属性，Java 会变成 `getIsEmpty()` 造成读法分裂）；
   - 编译期常量用 `const val`（放在 `object` / `companion object` 内）；
   - Kotlin **默认参数**（`fun f(a: Int, b: Int = X)`）被 Java 调用时必须加 `@JvmOverloads`：否则 JVM 只生成全参方法 ⇒ Java 侧编译失败，或既有二进制调用点 `NoSuchMethodError`；
   - 一个类型**只**被 Kotlin 消费时不受本条限制。
4. **Parcelable**：本仓未启用 `kotlin-parcelize`，Kotlin 版手写 `CREATOR`（`companion object CREATOR : Parcelable.Creator<T>` 这种命名伴随对象即生成静态 `CREATOR` 字段），**不引入新插件 / 新依赖**。
5. **文件头注释**：新建类必须有
   ```kotlin
   /**
    * @CreateDate: yyyy/MM/dd
    * @Author: 作者名
    * @Description: 功能描述
    */
   ```
   Java 同款（`@CreateDate / @Author / @Description`）。
6. **迁移不是本规范的目标**：不要求顺手把既有 Java 文件改成 Kotlin（违反「最小改动 / 不重构未损坏逻辑」）；只约束**新建**。

## 判据速查

| 场景 | 结论 |
|---|---|
| 新增 Parcelable 实体 / 数据类 | Kotlin（被 Java 读字段则用 `@JvmField`） |
| 新增工具类 / 静态方法集合 | Kotlin `object` + `@JvmStatic` |
| 改既有 Java 文件里的一行逻辑 | 就地改 Java（不新开文件） |
| 新逻辑要求新文件、但旧代码是 Java | **默认新建 `.kt`**；确有强耦合例外才 `.java` + changelog 留痕 |
| 该模块整体 Java 且需继承同包 Java 基类 | 可 `.java`（changelog 写理由） |
| 只被 Kotlin 消费的新类型 | Kotlin（无需 `@JvmStatic` / `@JvmField`） |
| 新增带默认参数的 Kotlin 方法且被 Java 调用 | 必须 `@JvmOverloads`（`object` + `@JvmStatic` 的方法同样适用） |
| Kotlin 调 Java 的 `hasXxx()` | **普通方法**须带括号（`vm.hasXxx()`）；只有 `getXxx()` / `isXxx()` 才是属性访问器 |

## 自检（提交前）

- `git status --porcelain` 中新增的 `.java`（`??` / `A` 状态）必须在 changelog 有例外理由；无理由即须转 Kotlin。
- 新 Kotlin 类型若被 Java 调用：编译对应模块（例 `./gradlew :basiclib:compileDebugKotlin`，业务模块同名任务），确认 **Java 调用点零改动**（若被迫写 `getXxx()`，说明该字段应改 `@JvmField`）。

## 判例

- 本仓暂无判例。后续按「违规 → 整改 → 记入本条」积累。
- 反例形态见 [`misuse.md`](../misuse.md) 错误 8（新建 Java 未留痕）。
