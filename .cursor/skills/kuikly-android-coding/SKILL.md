---
name: kuikly-android-coding
description: KuiklyPerspectiveCrop Android 码农技能（只编码、不规划）。触发：编码、实现、修复 Bug、修改代码、按计划行事、速速改、速速开工；v5 规范：Kotlin语言、Android平台、KuiklyCompose平台、新建代码文件准则、透视裁剪API准则（细则在角色 Rule + skill-references / coding-norms）。有确认 Plan → 按计划行事；琐碎 → 速速改。约束遵守 Rule；本 skill 只跑流程。
---


# kuikly-android-coding · 码农技能（流程包）

> **启动必读（硬）**：同目录 [`project.yaml`](project.yaml)。缺文件 → **硬停**，不写业务代码。
> **角色 Rule**：`.cursor/rules/kuikly-dev-android.mdc`（亦见 `project.yaml` → `role_rule`）。
> **规范全文**：`norms[].ref` **编码前**按 §规范全文路由 读完命中的。
> **给人看的流程总览**：`project.yaml` → `docs.workflow_readme`
> **三层货架**：[`coding-norms/README`](../../../docs/skill-references/coding-norms/README.md)

## 规范全文路由（编码前必读）

`norms[].ref` 是**编码前**的输入，不是收尾作业。第一次写业务代码前按下表读完命中的规范；未命中的**不读**，但收尾自检栏必须写「不适用 + 理由」。只读 Rule 摘要**不算**读过规范。

| 改动面 | 编码前读 |
|---|---|
| 任何业务改动（**基线**） | `lang_kotlin` + `platform_android` |
| 触及 Compose UI / 手势 / 页面入口 | 追加 `platform_kuikly_compose` |
| 改库公开 API / Stage / Session / Processor / Painter | 追加 `perspective_crop_api` |
| 新建文件 | 追加 `new_code_file_guidelines` |

> 路由只按**本次要动的文件**判定。判不出改动面（如「先看看再说」）→ 按基线两行读，编码时再按实际文件补读。
> 本仓不挂 `lang_java`（无 Java 源码）；共享货架 `coding-norms/base/lang_java.md` 仍可供他仓复用。

## 角色

你是编码**执行器**，**不规划**。禁止新建/改写 `spec.md` / `tasks.md` / `checklist.md`。
编码原则、惯例、已晋升规范硬约束 → **先遵守角色 Rule**（路径见配置），细则读 `project.yaml` → `norms[].ref` 与 `content_root`。冲突时 **L3 > L2 > L1（项目优先）**。

> 版本代数：`docs/skill-references/kuikly-android-coding/_version.txt` 为真源；`description` 的代数须与之及 `norms.length` 一致（`project.yaml` → `version_counts: norms`）。每份规范另有份级 `doc_version`（与代数双轨）。其它位置不写代数号。

## 路径判定（二选一）

| 专名 | 锚点 | 何时 |
|------|------|------|
| **按计划行事** | `an-ji-hua-xing-shi` | 已有用户确认的 Plan（IDE Plan / 附件 / 对话定稿） |
| **速速改** | `su-su-gai` | 琐碎小改；用户说「速速改 / 速速开工」亦走此路 |

禁止用「标准路径」「快路径」称呼上述两条。规划（grill / Plan 模式）不属于本 skill。

## 工作流 · 按计划行事

路径键均来自 `project.yaml` 的 `docs.*`（下称「配置」）。

1. **读 Plan**：本轮用户已确认的 Plan 为执行依据；若有配置 `docs.tasks_optional`（`<name>` 换成本次 feature）可作核对，**不得**擅自发明需求。
2. **编码**：按 Plan / tasks 实现（遵守角色 Rule + `norms` 已晋升规范）。新源文件 → 可更新配置 `docs.file_map`；稳定规则变 → 可更新配置 `docs.context_glob`。
3. **规范自检（必做；缺行视为未执行）**：**编码前**已按 §规范全文路由 读完命中的 `norms[].ref`；收尾**按清单逐行填三态**，写入 §实现回报的**规范自检**栏。
   - **清单不自己出题**：条款全集取自 `docs/skill-references/kuikly-android-coding/selfcheck/`（与本轮命中的 `norms[].id` 同名清单）。清单有 N 行，回报就有 N 行，**禁止缺行、禁止留空**。
   - **三态只有三种**：`OK` = 做了且写**落点行号**；`TODO` = 该做但没做，写**理由 + 风险承担**；`N/A` = 与本轮无关，写**一句判据**。「已遵守」「无需处理」这类空话不算任何一种。
   - **强制项不许写 `TODO`**：清单里标注「强制」的条款只许 `OK` 或 `N/A`；写 `TODO` 视为未整改，审核侧直接开 P1 —— 强制项的定义见 `norms[].ref` 正文，清单只是索引。
   - **只读 Rule 摘要不算读过规范**：Rule 是薄 ACL（只摘 P0 级硬句），条文不全，必须打开**命中的** `norms[].ref` 全文。
   - 未命中的规范**不要求读**，但回报里对应清单行仍写 `N/A` + 判据（例：本单无新建文件）。
   3.1 **搬运登记（提方法 / 内联 / 整段复制时必做）**：先按 `selfcheck/` 把被搬那段重过一遍规范（**不得以「原本就这样」豁免**），扫三类既有缺陷 —— **空值**（解引用未判空）、**单位量纲**（同一字段两种语义）、**顺序**（先读后写导致写进 null / 用到旧值）；二选一：当轮修掉，或在 changelog「已知遗留」写一行 `carried-over: <缺陷一句话>｜不修理由`。**发生过搬运却没有这行 ⇒ 审核侧可直接开 P1。**
4. **业务 changelog**：append 配置 `docs.feature_changelog`；条目末追加**轻量治理行**（见 §实现回报；门禁栏填「跳过」，除非本轮改了本 skill）。★本单含「真机验证 / 真机自测」时，条目里**登记可执行档路径**（配置 `docs.device_check_list_dir` + `{task-id}.md`）——**表由审核员产出**（码农不写 `docs/reviews/`）；无真机验证则写「本单无真机验证」。
5. **收尾三问**：读 `project.yaml`。`sediment: off` 则跳过三问与入池。`sediment: on` 则打开 `mounts.sediment_process` 并按其中规则执行；小结级写 `mounts.anti_patterns`，规范级只入 `mounts.backlog`。
6. **询问 Review**：问用户「是否开始 Review？」（`review_skill` 有值时）。
7. **修改循环**：Review 不通过则改完再问。
8. **技能治理（门禁）**：`gate_on: meta_only` —— **纯业务不强制**；仅元变更必跑。
9. **归档通知**：changelog 路径；池非空则催批「待你批：N（已达标 M）」。

## 工作流 · 速速改

1. **编码**：直接改（遵守角色 Rule · 最小改动）；收尾仍须按 §实现回报的**规范自检**栏，对本轮触碰的规范文件做一次最小对照（琐碎改也不例外）。
2. **changelog**：有明确 feature 目录则 append 配置 `docs.feature_changelog` 一行 + **轻量治理行**；否则只在 §实现回报写清变更与三问，**不新建** spec/tasks。
3. **收尾三问**：同按计划行事（看 `sediment` 与 `mounts`）。
4. **默认不问 Review**（用户主动 `/review` 除外）。
5. **技能治理（门禁）**：同按计划行事（纯业务不强制门禁）。
6. **归档通知**：池非空则催批。

> **职责边界**：`review_skill` 有值时，收尾可问是否把 Task-id 交给该审查 skill；为空则不问。本 skill 不代写审查报告。审查候选进**审查自己的** `_backlog`（ADR 0036）；可人工抄入本池，审查不得自动写入。晋升仍本 skill + 用户批。沉淀流程见 `mounts.sediment_process`。

## 参考文件

| 文件 | 用途 |
|---|---|
| `project.yaml` | **特化配置**（`sediment` / `gate_on` / `version_counts` / `mounts` / `norms[]` / `review_skill` / `docs.*`） |
| `mounts.sediment_process` | 沉淀流程（仅 `sediment: on` 时打开） |
| `docs/skill-references/coding-norms/` | L1/L2 可移植货架（见 README） |
| `docs/skill-references/kuikly-android-coding/project/` | L3 本仓规范全文 |
| `docs/skill-references/kuikly-android-coding/selfcheck/` | **规范自检清单**（三态打勾；每单必填） |
| `docs/skill-references/kuikly-android-coding/misuse.md` | 编码反例 |
| `docs/skill-references/kuikly-android-coding/_backlog.md` | 本 skill 待沉淀池 |
| `docs/skill-references/kuikly-android-coding/_version.txt` | 代数真源 |

## Kotlin语言规范（核心）

见角色 Rule「硬约束 · L1 Kotlin语言」+ `docs/skill-references/coding-norms/base/lang_kotlin.md`。

## Android平台规范（核心）

见角色 Rule「硬约束 · L2 Android平台」+ `docs/skill-references/coding-norms/base/platform_android.md`。

## KuiklyCompose平台规范（核心）

见角色 Rule「硬约束 · L2 Kuikly Compose」+ `docs/skill-references/coding-norms/base/platform_kuikly_compose.md`。

## 新建代码文件准则规范（核心）

见角色 Rule「硬约束 · L3 新建代码文件准则」+ `docs/skill-references/kuikly-android-coding/project/new_code_file_guidelines.md`。

## 透视裁剪API准则规范（核心）

见角色 Rule「硬约束 · L3 透视裁剪 API」+ `docs/skill-references/kuikly-android-coding/project/perspective_crop_api.md`。

## §实现回报模板

- 路径：按计划行事 | 速速改
- Task-id / 变更文件 / 行为变化 / 自测建议
- 执行依据：（Plan 路径或「口令小改」）
- 文档同步：changelog 路径（配置 `docs.feature_changelog`）或「速速改未建 specs」
- **规范自检**：本轮读过的 `norms[].ref` 清单 + 按 `selfcheck/` 逐行三态表（`OK` 落点行号 / `TODO` 理由 + 风险 / `N/A` 一句判据），**行数不许少于清单**；**缺此栏或缺行视为未执行规范**（ADR 0038 三层货架）
- **搬运登记**：本轮无搬运动作写「无」；有则列 `carried-over: <缺陷>｜<修/不修理由>`
- **真机验证表**：`docs/reviews/device-check-list/{task-id}.md`（本单含「真机验证 / 真机自测」时由**审核员**产出，码农只登记路径）；本单无真机验证则写「本单无真机验证」
- **三问**：新规范 / 新反例 / 冲突写法 → 是|否；全否须短理由；有「是」写「小结级：…」或「已入池：…」
- **待你批**：N（已达标 M）；池空写 0

### 轻量治理行（写入 feature changelog 时必带；纯业务）

> `- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：…｜待批 N（已达标 M）`
> 三问顺序：新规范 / 新反例 / 冲突写法。无 feature changelog 时，三问只写在上方 §实现回报。

### 元变更附加（仅改了本 skill 时）

> **治理收尾行**：`- 治理收尾：门禁 FAIL0/WARN0｜三问 否/否/否｜理由：…｜待批 N（已达标 M）`
> 三问顺序：新规范 / 新反例 / 冲突写法；**先跑门禁再填数字**。

## 术语表（本 skill 内部词汇）

- **项目配置**：同目录 `project.yaml`；路径 / `norms[]` / `gate_on`；**不含 ACL**。
- **退役**：删除规范（`norms[]` + 本文件索引壳 + description + `_version.txt`）；**不**保留「已废弃」壳。
- **按计划行事** / **速速改**：双执行路径专名；勿称标准/快路径。
- **技能治理**：收尾读 `project.yaml` 的 `sediment` 与 `mounts`。ADR 0038 / 0036 / 0029 / 0026 / 0020 / 0019。
- **规范**：`norms[]` 含 `id` / `title` / **`layer`（L1|L2|L3）** / `ref`；全文路径与文首 `layer` 须一致（门禁验）。Rule 仅短硬句。
- **世代（代数）**：`_version.txt` + `description` + skill changelog；代数 == `norms.length`。
- **三态自检**：`OK` / `TODO` / `N/A` 三选一、**禁止缺行**的规范自检表；条款全集来自 `selfcheck/`，缺失即视为未执行。
- **搬运登记（carried-over）**：提方法 / 内联 / 整段复制时，对被搬段重审规范后留下的「既有缺陷 + 修否」记录；**不是**本次新引入的问题。
- **doc_version**：单份规则份级版本；正文任意改动即升。
- _Avoid_ **自升级** / **范式** / **标准路径** / **快路径** / 在 Skill 写死本仓 docs 路径。

## 挂载

收尾读同目录 `project.yaml`。`sediment: off` 则跳过三问与入池（审查仍可写规范候选观察）。`sediment: on` 则按需打开：

- **规范全文**：`norms[].ref`（本 skill **不**再挂 `mounts.rules`，避免与 norms 双写）
- **反例 / 沉淀池 / 沉淀流程**：`mounts.anti_patterns` / `mounts.backlog` / `mounts.sediment_process`

门禁时机仍看 `gate_on`。可晋升单元是 `norms[]`；退役是直接删除规范壳。门禁：`node docs/skill-references/kuikly-android-coding/scripts/check_skill.mjs`（技能路径参数用 `.cursor/skills/kuikly-android-coding`）。
