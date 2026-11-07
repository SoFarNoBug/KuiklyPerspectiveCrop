# skill-references · 领域术语

> 仓内公共素材包，不是可触发 skill。决策见 `docs/adr/0001-shell-not-a-skill.md`。

## 术语表

| 术语 | 含义 |
|---|---|
| **skill 壳** | 可拷贝的契约 + 门禁引擎 + 默认沉淀流程模板。本仓落点 `docs/skill-references/`。无 `SKILL.md`，不能当第四个 skill 启用。 |
| **挂载（mounts）** | 业务 skill 的 `project.yaml` 用路径声明普通规则、反例、沉淀池、沉淀流程、版本文件。物理目录不必改名。 |
| **sediment** | 是否在收尾做三问与入池。`on` / `off`，与 `gate_on` 无关。 |
| **gate_on** | 仅决定何时必跑门禁：`always` 或 `meta_only`。 |
| **按需加载** | Agent 只 Read 当前步骤需要的挂载文件；`sediment: off` 不打开沉淀流程。 |
| **发现面** | 审查时「看什么、按 diff 开哪张卡」的规程，落点 `kuikly-code-review/review_heuristics.md`；与定级面（`severity.md`）分离。见 [ADR 0037](../adr/0037-review-heuristics-and-device-checklist.md)。 |
| **缺口档** | `P1-device-verify` 默认写法：报告内只写代码审不了什么 + 建议断言点；可挂有条件通过。 |
| **可执行档** | **默认产出**的完整路径表：报告挂 `P1-device-verify` **或** Plan 含真机验证/真机自测 ⇒ 同轮必须写（仅用户明确说「不用表」才省）；落点 `docs/reviews/device-check-list/{task-id}.md`。 |
| **device-check-list** | 可执行档目录：`docs/reviews/device-check-list/`；文件名与 review 的 task-id 一致。 |
