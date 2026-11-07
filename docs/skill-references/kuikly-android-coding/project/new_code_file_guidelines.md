doc_version: 20261107_1
layer: L3

# 新建代码文件准则（L3）

> **分层**：本文件为规范**全文真源**。编码会话红线摘要见 `.cursor/rules/kuikly-dev-android.mdc`；流程见 Skill。勿与 Rule 双写完整条文（[ADR 0021](../../../adr/0021-rule-redline-references-sot.md)）。
> **本意**：新代码文件**优先写 Kotlin**（KMP `commonMain` 优先）；禁止「为了和旁边文件一致」而新建 Java。纯 Kotlin 惯用法见 L1 [`coding-norms/base/lang_kotlin.md`](../../coding-norms/base/lang_kotlin.md)。

**级别**：核心规范（强制）。违反 = 该次编码不合格，须整改后再进 Review。

## 规则

1. **新建文件优先 Kotlin**：数据类、工具类、常量持有者、新建模块、expect/actual，全部适用本仓 module（`:KuiklyPerspectiveCropCore` / `:perspectiveCropDemo` / 各端 App）。
2. **需要新开文件承载新逻辑时，默认开 `.kt`**。
   - **例外（须留痕）**：必须与真 Java 契约强耦合（注解处理器 / 反射契约等）时可新建 `.java`，**必须在该次 changelog 写明「为何不能用 Kotlin」**。
   - 例外不是默认值：判不出理由就写 Kotlin。
3. **文件头注释**：新建类必须有
   ```kotlin
   /**
    * @CreateDate: yyyy/MM/dd
    * @Author: 作者名
    * @Description: 功能描述
    */
   ```
4. **迁移不是本规范的目标**：不要求顺手把既有文件改语言；只约束**新建**。

## 判据速查

| 场景 | 结论 |
|---|---|
| 新增数据类 / 工具类 / expect·actual | Kotlin |
| 新逻辑要求新文件 | **默认新建 `.kt`**；确有强耦合例外才 `.java` + changelog 留痕 |
| 只被 Kotlin / KMP 消费的新类型 | Kotlin |

## 自检（提交前）

- `git status --porcelain` 中新增的 `.java`（`??` / `A`）必须在 changelog 有例外理由；无理由即须转 Kotlin。
- 新建 `.kt` 已带 `@CreateDate` / `@Author` / `@Description`。

## 判例

- 本仓暂无判例。后续按「违规 → 整改 → 记入本条」积累。
- 反例形态见 [`misuse.md`](../misuse.md) 错误 1（新建 Java 未留痕）。
