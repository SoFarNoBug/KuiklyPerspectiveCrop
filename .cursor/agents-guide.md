# Cursor Agent 协作指南（KuiklyPerspectiveCrop）

本仓 AI 配置在 **`.cursor/`**。

## 目录

| 路径 | 用途 |
|---|---|
| `.cursor/rules/*.mdc` | 规则（Cursor 扁平命名；`alwaysApply` / description 触发） |
| `.cursor/skills/*/SKILL.md` | 技能流程包 |
| `.cursor/hooks.json` + `.cursor/hooks/` | 编码前确认闸（机械兜底） |
| `docs/skill-references/` | 规范全文 / 定级表 / 门禁脚本（仓内公共素材） |

## 角色路由

| 意图 | Rule | Skill |
|---|---|---|
| 编码 / 修 Bug / 按计划 / 速速改 | `kuikly-dev-android.mdc` | `kuikly-android-coding` |
| 审查 / review | `kuikly-dev-review.mdc` | `kuikly-code-review` |
| 烤一下 / grill一下 / 审讯一下 | `grill-with-docs.mdc` | `grill-with-docs` → `grilling` + `domain-modeling` |
| Kuikly Compose DSL | `kuiklyComposeDSL.mdc` | — |
| 任何落盘改码前 | `coding-confirm-gate.mdc`（always） | + hooks |

## 编码前确认闸

1. Soft：Rule 要求输出 1/2 确认块。
2. Hard：`node .cursor/hooks/coding-gate-ask.mjs` 拦写文件工具。
3. 解锁：`node .cursor/hooks/gate-unlock.mjs on`（或对话放行词）。
4. 说明：`docs/context/coding-gate.md`。

## Plan 与 grill-with-docs

- 设计空间大、用户说「烤一下 / grill一下 / 审讯一下」→ 先 grill，再落 CONTEXT/ADR / 计划。
- 用户已确认计划且说执行 → 走码农「按计划行事」，先确保闸已解锁。

## 里程碑

业务实现按 `docs/milestones/M0–M3`（**v0.1 = 全部做完**）；验完一步再下一步。  
已定决策 SoT：`docs/decisions.md`；硬约束见根目录 `AGENTS.md`、`CONTEXT.md`。
