# 编码前确认闸（Cursor）

> 另有一道 **编码 Skill 硬闸**（拦未读 skill 就改 `.kt`）：见 [coding-skill-gate.md](./coding-skill-gate.md)。两道独立。

## 两道防线（本闸）

1. **Rule**（soft）：`.cursor/rules/coding-confirm-gate.mdc` — AI 落盘前须问 1/2。
2. **Hook**（hard）：`.cursor/hooks.json` → `node .cursor/hooks/coding-gate-ask.mjs`。

## 常用命令

```bash
node .cursor/hooks/gate-unlock.mjs on       # 解锁 20 分钟
node .cursor/hooks/gate-unlock.mjs off      # 恢复拦截
node .cursor/hooks/gate-unlock.mjs disable  # 本机彻底关闭（写 .gate-off，已 gitignore）
node .cursor/hooks/gate-unlock.mjs enable   # 重新开启
```

环境变量 `CODING_GATE=off` 亦可整闸关闭（仅对继承了该环境的进程有效）。

## 放行词

`1` / 直接改 / 直接开始 / 跳过确认 / 不用问 / 开始执行 / 按计划执行 / 照做 / 就这么改 / 可以改 / 接受计划 / 批准计划 / 同意计划 / 立即开始 / 开始吧 / 开始编码

## 免闸

`.md` `.markdown` `.txt` `.log` `.json` `.csv` `.mdc`；路径含 `/tmp/` `/memory/` `/docs/` `/.git/`。
