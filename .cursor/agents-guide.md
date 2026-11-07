# Cursor Agent 协作指南（KuiklyPerspectiveCrop）

本仓 AI 配置在 **`.cursor/`**。

## 目录

| 路径 | 用途 |
|---|---|
| `.cursor/rules/*.mdc` | 规则（Cursor 扁平命名；`alwaysApply` / description 触发） |
| `.cursor/skills/*/SKILL.md` | 技能流程包 |
| `.cursor/hooks.json` + `.cursor/hooks/` | 编码前确认闸 + **编码 Skill 硬闸**（机械兜底） |
| `docs/skill-references/` | 规范全文 / 定级表 / 门禁脚本（仓内公共素材） |

## 角色路由

| 意图 | Rule | Skill |
|---|---|---|
| 编码 / 修 Bug / 按计划 / 速速改 | `kuikly-dev-android.mdc` | `kuikly-android-coding`（路径：`.cursor/skills/kuikly-android-coding/`） |
| 审查 / review | `kuikly-dev-review.mdc` | `kuikly-code-review` |
| 烤一下 / grill一下 / 审讯一下 | `grill-with-docs.mdc` | `grill-with-docs` → `grilling` + `domain-modeling` |
| Kuikly Compose DSL | `kuiklyComposeDSL.mdc` | — |
| 任何落盘改码前 | `coding-confirm-gate.mdc`（always） | + coding-gate hook |
| 确认 Plan 后执行 | `plan-exec-coding-skill.mdc`（always） | **必须先 Read** 上表码农 Skill；+ skill-gate hook |

## 两道硬闸（勿混）

| 闸 | Hook | 拦什么 | 如何放行 |
|---|---|---|---|
| 编码前确认闸 | `coding-gate-ask.mjs` | 未选 1/2 就写非文档文件 | 对话选 1/2，或 `gate-unlock.mjs on` |
| **编码 Skill 硬闸** | `coding-skill-gate.mjs` | 未 Read 码农 SKILL 就写 `.kt`/`.java`/`.kts` | **Read** `.cursor/skills/kuikly-android-coding/SKILL.md`；或说「速速改」；或 `skill-gate-unlock.mjs on` |

说明：确认闸解锁 **≠** Skill 硬闸武装。Implement Plan 时两道都要过。  
Skill 硬闸文档：`docs/context/coding-skill-gate.md`；确认闸：`docs/context/coding-gate.md`。

## Plan 与 grill-with-docs

- 设计空间大、用户说「烤一下 / grill一下 / 审讯一下」→ 先 grill，再落 CONTEXT/ADR / 计划。
- 用户已确认计划且说执行 → **先 Read** `kuikly-android-coding` Skill → 再改业务源码（hook 会验）。

## 里程碑

业务实现按 `docs/milestones/M0–M3`（**v0.1 = 全部做完**）；验完一步再下一步。  
已定决策 SoT：`docs/decisions.md`；硬约束见根目录 `AGENTS.md`、`CONTEXT.md`。
