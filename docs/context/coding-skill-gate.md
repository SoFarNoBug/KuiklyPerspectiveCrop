# 编码 Skill 硬闸

> 配套：`.cursor/hooks/coding-skill-gate.mjs`、`plan-exec-coding-skill.mdc`  
> 与 [编码前确认闸](./coding-gate.md) **独立**，两道都要过。

## 拦什么

未武装时，对业务源码扩展名 **`.kt` / `.java` / `.kts`** 的写入类工具（Write / StrReplace / Delete 等）→ **deny**。

不拦：`docs/`、`.md`/`.mdc`、`.cursor/hooks|rules|skills/` 等（见脚本内 `EXEMPT_SEGMENTS`）。

## 如何武装（20 分钟 TTL）

1. **推荐**：Agent `Read` `.cursor/skills/kuikly-android-coding/SKILL.md`（postToolUse 观测后武装）
2. 用户说「速速改 / 速速开工 / 跳过 skill」
3. `node .cursor/hooks/skill-gate-unlock.mjs on`

## 关闭（本机）

- `CODING_SKILL_GATE=off`
- 或 `node .cursor/hooks/skill-gate-unlock.mjs disable`（写 `.cursor/hooks/.skill-gate-off`，应 gitignore）

## 与确认闸的关系

| | 确认闸 | Skill 硬闸 |
|---|---|---|
| 典型失败 | 没问 1/2 就改码 | Implement Plan 没读 skill 就改 `.kt` |
| 选「1. 直接开始编码」 | 解锁确认闸 | **不**自动武装 Skill 闸 |
| Plan 执行 | 可能已解锁 | **仍须** Read SKILL |

## 熔断

同一目标连续 deny 达上限后 fail-open（与确认闸类似），避免卡死；日志在系统临时目录 `cursor-skill-gate-<tag>-log`。
