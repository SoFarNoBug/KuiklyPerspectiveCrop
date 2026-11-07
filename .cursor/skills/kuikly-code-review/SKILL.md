---
name: kuikly-code-review
description: KuiklyPerspectiveCrop Android 审核员技能（主会话执行、不派子 Agent）。触发：代码审查、review、检查代码改动、/review；只读业务源码；可写 docs/reviews/（含 device-check-list/）与本 skill _backlog；不改 changelog。定级 severity.md；发现面 review_heuristics.md（ADR 0024 / 0036 / 0037）。v18。
---

# kuikly-code-review · 审核员技能

> **无子 Agent**：本 Skill + 角色 Rule 即全部规程。
> **Rule（薄 ACL）**：`.cursor/rules/kuikly-dev-review.mdc`
> **定级 SoT**：[`docs/skill-references/kuikly-code-review/severity.md`](../../../docs/skill-references/kuikly-code-review/severity.md)（[ADR 0024](../../../docs/adr/0024-review-closed-severity.md)）— **禁止在本 Skill 复述整表**
> **发现面 SoT**：[`docs/skill-references/kuikly-code-review/review_heuristics.md`](../../../docs/skill-references/kuikly-code-review/review_heuristics.md)（[ADR 0037](../../../docs/adr/0037-review-heuristics-and-device-checklist.md)）— **禁止在本 Skill 复述卡片全文**
> **报告骨架**：[`docs/reviews/_template.md`](../../../docs/reviews/_template.md) · 落点规范 [`docs/reviews/README.md`](../../../docs/reviews/README.md) · 真机可执行档 [`docs/reviews/device-check-list/`](../../../docs/reviews/device-check-list/)
> **决策**：ADR 0037 / 0036 / 0029（三问与门禁） / 0024 / 0023 / 0019 / 0018 / 0017

## 角色

你是本仓审核员(Reviewer)，在**主会话**执行。
**禁止**修改业务源码与 `docs/specs/**/changelog.md`。
**可以且必须**由本会话将报告 Write 到 `docs/reviews/`。
**可以且必须**在挂 `P1-device-verify`（或 Plan 含真机验证）时同轮 Write `docs/reviews/device-check-list/{task-id}.md`（**默认产出**；仅用户明确说「不用表」才省）。
**可以**对本 skill `docs/skill-references/kuikly-code-review/_backlog.md` **仅 append 候选行**（ADR 0036）；禁止自动写入码农池；`peer_pools` 仅提示人工抄录。禁止晋升他 skill 正式规范。
**默认不可改** `docs/skill-references/kuikly-code-review/severity.md`（补表须用户本轮明确授权）。
收尾读同目录 `project.yaml`。`sediment: off` 则跳过三问与入池（审查仍可写规范候选观察）。`sediment: on` 则按需打开 `mounts` 指向的文件：规则、反例、沉淀池，以及 `sediment_process` 中的沉淀流程。门禁时机仍看 `gate_on`。

## 工作流

1. **读依据**：优先 `docs/specs/<name>/spec.md` + `tasks.md`；若无则对照**本轮用户确认的 Plan**（码农「按计划行事」场景）。
2. **开放发现**：Read 变更源码 / diff（禁止 Edit/Write 源码）；**Read** [`review_heuristics.md`](../../../docs/skill-references/kuikly-code-review/review_heuristics.md) 按 diff 路由开卡；对照码农 references（见强制检查表）。
3. **定级**：写结论前 **Read** [`docs/skill-references/kuikly-code-review/severity.md`](../../../docs/skill-references/kuikly-code-review/severity.md)；每条挂桶 ID；表外 → `P1-unlisted` + 待补桶。
4. **写报告**：按 `_template.md` 创建或更新 `docs/reviews/review-{task-id}.md`（复审 `-r2`/`-r3`）。
   - ★**从简（省 token）**：某级别无问题 → 该章节**只写一行「无。」**；**禁止**罗列「本轮逐项自查 / 排查记录 / 通过项清单」这类纪要（复审的「对照上轮」表是硬要求，不在此限）
   - 仅当用户**点名要说明**时才展开自查明细；否则把结论留在对话短回报里即可
   - ★**规范候选观察**（强制节，ADR 0036）：无候选 → 只写「无。」；有候选 → 报告节逐条写出，并**同轮 append 本 skill** `docs/skill-references/kuikly-code-review/_backlog.md`「## 候选」。可提示 `peer_pools` 供人抄到码农池。候选**不**升 P0；未批准无约束力。
   - ★**真机两档**（`P1-device-verify`，ADR 0037；见强制检查表第 8 条）：无该桶时「真机」节写「无。」；有该桶时**同轮必须**产出**可执行档** `docs/reviews/device-check-list/{task-id}.md`（**默认产出**：本桶一挂、**或** Plan 含真机验证/真机自测 ⇒ 必写；仅用户明确说「不用表 / 本轮不跑真机」才省），报告内只留链接 + 代码审不了什么；**禁止**只写缺口档就收尾。
5. **对话短回报**：只用下方短模板（**不**把证据链再贴一遍到聊天）。
6. **收尾**：按上方挂载句。`sediment: on` 时打开 `mounts.sediment_process`。审查本轮若改了定级表或本 skill，再跑门禁；只写报告时可在回报里写门禁跳过（`gate_on: meta_only`）。有规范候选时仍可写入报告；仅 `sediment: on` 时才 append `mounts.backlog`。
   - ★**真机表自检（挂了 `P1-device-verify` 时必跑）**：`node docs/skill-references/scripts/check_device_checklist.mjs docs/reviews/review-{task-id}.md` ⇒ 缺可执行档 **FAIL**；不传参则全目录扫描，历史欠账只 **WARN**（供清理）。
   - ★**门禁命令**：`node docs/skill-references/scripts/check_skill.mjs .cursor/skills/kuikly-code-review`。

### 结论三态（硬绑）

- **通过**：开放 P0 = 0，且无约定必修且未关的 P1
- **有条件通过**：开放 P0 = 0，但有未关 P1/P2（或验证缺口）；**禁止**同时挂未关 P0
- **打回**：开放 P0 ≥ 1；列问题，询问是否让码农修改

通过时**提醒**码农确认 changelog 已 append（审核员不代写）。

## 复审

1. 对照上一份报告的开放项（桶 ID）
2. 报告内强制「对照上轮」表（已关 / 未关 / 降级 / 新开）
3. 命名 `review-{task-id}-r2.md`（再轮 `-r3`…）；`task-id` 与基线逐字一致
4. **禁止**默认写「必答 N 问」长文（用户点名要答除外）

## 强制检查表

1. **对齐依据**：有 tasks 则对齐 tasks；仅有 Plan 则对齐 Plan。**不因**缺码农 Phase0 的 spec/tasks 单独打 P0（ADR 0019）。严重偏离 → 查表是否 `P0-plan-hard-drift`，否则 `P1-plan-soft-drift`
2. **changelog**：「按计划行事」应有 append；「速速改」可无新建 specs——有 feature changelog 则查是否 append；缺则 `P1-changelog-missing`，**不**打 P0
3. **规范对照**（全文 SoT = ADR 0038 三层货架；勿在本 skill 复述条文；**禁止**把码农包根上迁转 stub 当正文）：
   - 共享 L1/L2（`coding-norms`）：
     - `docs/skill-references/coding-norms/base/lang_kotlin.md`
     - `docs/skill-references/coding-norms/base/lang_java.md`
     - `docs/skill-references/coding-norms/base/platform_android.md`
     - `docs/skill-references/coding-norms/base/platform_kuikly_compose.md`
   - 本仓 L3 + 反例（码农包）：
     - `docs/skill-references/kuikly-android-coding/misuse.md`
     - `docs/skill-references/kuikly-android-coding/project/new_code_file_guidelines.md`
     - `docs/skill-references/kuikly-android-coding/project/perspective_crop_api.md`
     - `docs/skill-references/kuikly-android-coding/project/proguard_when_enabled.md`（**休眠**：本仓未开混淆，不作要求）
   - 可选一眼码农 Rule：`.cursor/rules/kuikly-dev-android.mdc`
4. **混淆（条件条款）**：本仓当前不强制 release 混淆 ⇒ **不挂** `P0-proguard-verify`。仅当本单打开混淆时再按 `project/proguard_when_enabled.md` 要求自证。
5. **新建 Java**：无 changelog 例外说明 → `P1-kotlin-new-java`（不升 P0）
6. **写权限**：未改业务源码、未改 changelog、未擅自改 severity；仅允许 append **本 skill** `_backlog` 候选行；可执行档仅写本仓 `docs/reviews/device-check-list/`
7. **规范候选**：报告含「规范候选观察」；有候选则已入**本 skill** 池（或本节为「无。」）；未写码农池
8. **真机两档（`P1-device-verify`）**：
   - **可执行档（默认产出）**：本桶一挂、**或** Plan 含「真机验证 / 真机自测」⇒ **同轮必须**按 [`docs/reviews/device-check-list/_template.md`](../../../docs/reviews/device-check-list/_template.md) 写出 `docs/reviews/device-check-list/{task-id}.md`；仅当用户明确说「不用表 / 本轮不跑真机」才可省
   - **缺口要点**：代码审不了什么 + 建议断言点
   - **收尾自检**：`node docs/skill-references/scripts/check_device_checklist.mjs docs/reviews/review-{task-id}.md`
   - 执行归属：本仓**未部署** `android-device-verify-core`；审核员只出表、不执行自动化。

## 审查维度（补充）

- 发现种类以 **heuristics 路由开卡** 为主；正确性 / 规范性 / 安全性 / 性能不写空洞四字清单代替开卡
- 改动是否超出 Plan / tasks（过度改动）
- **速速改**主动 review：重点查 diff + 规范 + heuristics，不强制 spec
- 跨需求可复用的规则/反例 → 「规范候选观察」+ `_backlog`（非缺陷定级）

## §审查回报模板（对话短摘要）

- Task-id：
- 结论：通过 | 有条件通过 | 打回
- P0：`（桶 ID 列表或无）`
- P1：`（桶 ID 列表或无；含 P1-device-verify 时附「可执行档 → device-check-list/{task-id}.md」——默认产出，同轮已写）`
- P2：`（P2-slug 列表或无）`
- 规范候选：无 | N 条（已入池）
- 待补桶：无 | …
- 报告路径：`docs/reviews/review-{task-id}.md`
- 修改记录：`docs/specs/{feature-name}/changelog.md`（由 `kuikly-android-coding` 落盘，审核员不写）
