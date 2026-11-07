# 审查严重级别（severity.md）

> **SoT**：本文件。Skill / Rule / README **不复述**整表。  
> **决策**：[ADR 0024](../../adr/0024-review-closed-severity.md) · [ADR 0037](../../adr/0037-review-heuristics-and-device-checklist.md)（发现面 / 真机两档）  
> **报告条目格式**：见 [`docs/reviews/_template.md`](../../reviews/_template.md)

## 总则

1. **发现开放**：任何问题都可写入报告。
2. **定级封闭**：级别必须命中下表；**禁止自创 P0**。
3. **表外**：暂标 `P1-unlisted`，文末「待补桶」写建议 ID + 一句理由；**禁止**审查会话擅自改本文件（须用户确认后再改）。
4. **定级时机**：开放发现之后、写结论 / 落盘之前 **Read 本文件**。

## 结论挂钩

| 结论 | 条件 |
|------|------|
| **打回** | 开放（未关）P0 ≥ 1 |
| **有条件通过** | 开放 P0 = 0，且存在未关 P1/P2，或约定真机/联调缺口未关 |
| **通过** | 开放 P0 = 0，且无约定必修且未关的 P1 |

禁止：「有条件通过」同时列出未关 P0。

---

## P0 白名单（仅此五类）

| ID | 触发 | 判定句 | 勿升格为 P0 |
|----|------|--------|-------------|
| `P0-crash` | 本次改动可归因的必崩路径 | NPE / 越界 / 错误资源 id 等，主路径或明确可达 | 仅「可能」、无可达论证 |
| `P0-silent-fail` | 核心功能静默失效 | 回调未接线、子类吞掉父类关键路径、功能整条不生效 | 文案/注释错误、次要入口体验差 |
| `P0-api-contract` | 同业务两路不同源且行为错 | 漏扣费 / 漏门闸 / 尺寸与数据源不一致等已论证 | 仅文档口径不一致（→ P1-doc-drift） |
| `P0-plan-hard-drift` | 宣称可达/产物与 Plan·tasks **严重**不符 | 任务声称已覆盖但代码不可达，或产物 flag 与任务目标明显矛盾 | 入口遗漏、软覆盖差（→ P1-plan-soft-drift） |
| `P0-build-break` | 干净树明显编不过 | 缺入库资源、明显编译错误由本次引入 | 需真机才知的布局对错 |

**明确不进 P0**：新建 Java、业务 changelog 缺 append、DEBUG 整段包声明、埋点口径争议、纯真机定标项。

---

## P1 穷尽桶

| ID | 触发 | 判定句 |
|----|------|--------|
| `P1-doc-drift` | 注释 / 双真源 / ADR 与实现不符 | 不影响主路径正确性，但口径漂移 |
| `P1-device-verify` | 代码审查无法闭环，须真机/联调 | 作为放行条件列出，不升 P0（[ADR 0037](../../adr/0037-review-heuristics-and-device-checklist.md)）。**缺口档**（默认）：报告「真机」节须写清「代码审不了什么 + 建议断言点」；无缺口说明 ⇒ 该桶未闭环、不得挂桶。**可执行档（默认产出）**：本桶一挂、**或** Plan 含「真机验证 / 真机自测」⇒ **同轮必须**写出 `docs/reviews/device-check-list/{task-id}.md`（完整路径表：前置 + 步/类型/内容/预期；类型列只许 `docs/reviews/device-check-list/_template.md` 列出的合法值；须有覆盖对照）；仅当用户明确说「不用表 / 本轮不跑真机」才可省；报告正文只留链接，**不得**无文件却宣称已可执行交接。收尾跑 `node docs/skill-references/scripts/check_device_checklist.mjs <报告路径>`（缺表 ⇒ FAIL）。无完整文件时仍可「有条件通过」挂缺口档 |
| `P1-i18n` | 文案仅中文、Key 未对齐等 | 用户可见文案/多语言问题 |
| `P1-plan-soft-drift` | 入口/覆盖遗漏，未到硬漂移 | 部分入口未覆盖、宣称略超实现 |
| `P1-tracking` | 埋点 pageCode/曝光/归因待确认 | 非功能崩溃类 |
| `P1-concurrency` | 状态竞态 / LiveData↔SP / TOCTOU | 有风险但未证必崩或必错账 |
| `P1-main-thread-io` | 主线程 IO / 重 IPC | 未证必 ANR |
| `P1-layout-visual` | 定标/视觉/间距，需对照 UE 或真机 | 非静默功能失效 |
| `P1-kotlin-new-java` | 无 changelog 例外说明的新建 `.java` | 对齐 `project/new_code_file_guidelines.md`（本仓口径：优先 Kotlin，例外须留痕）；不升 P0 |
| `P1-changelog-missing` | 按计划行事应 append 而未 append | 不因缺整目录打 P0（ADR 0019） |
| `P1-debug-guard` | 临时日志缺 DEBUG 守卫等 | 未构成发布阻断时 |
| `P1-unlisted` | 未命中上表任一 P1，且非 P0 | 必须同时写「待补桶」 |

---

## P2（不穷尽）

- 格式：`P2-<kebab-slug>`（自拟短名）或统一 `P2-note`
- 不要求命中固定表；**不挡**「有条件通过」
- 典型：命名/格式、轻度注释、非主路径微性能、缺单测、临时日志等

---

## 报告条目（摘要）

```markdown
### {桶ID} 一句话
- 证据：`path:line`（默认 1～3 条）
- 影响：…
- 处置：必修 | 建议 | 可选（复审可写 已关/未关）
```

完整骨架与复审对照表：`docs/reviews/_template.md`。
