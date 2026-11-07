---
name: grilling
description: Grill the user relentlessly about a plan, decision, or idea. 仅在用户说「烤一下」「grill一下」「审讯一下」时启用（逐字或同句内完整包含）；不因方案/设计/评审/压力测试等语义词触发。Use only on those three phrases.
---

Interview the user relentlessly until you reach a shared understanding. Map this as a **design tree**: every decision branches into the decisions that hang off it.

Work the tree in **rounds**. The **frontier** is every decision whose prerequisites are already settled: the questions you can ask _now_ without guessing at answers you haven't heard yet.

## 提问纪律（用户 2026-09-12 明确的硬规则，优先于本文其它措辞）

1. **一次只问一个问题**。只有**完全同源**（同一决策主题）的问题才允许合并成一组。**禁止**一次性抛出整轮 frontier。
2. **每题必须给选项 a/b/c/d**，至少 2 个可选项；末项固定为 `d) 其他（你补充）`（可为 a/b/c）。不要出"是/否"式无选项问题。
3. **必须标注推荐项 + 推荐理由**，理由要带代码/文档/实测证据（文件:行号）；证据不足就直说"证据不足"，不许凭直觉硬推。
4. 用户回答后**重算 frontier**，再问**下一题**；用户没回答前**不动任何文件**、不擅自开工。
5. 事实由我自己查（读文件 / 派子代理），**不要把"查事实"包装成问题抛给用户**；题干里要给出背景与证据。

Format each question like so:

```
❓ **Q1 - <question title>**

<题干：背景 + 证据（文件:行号），必要时给现状与约束>

- **a) <选项>**（推荐）
- **b) <选项>**
- **c) <选项>**
- **d) 其他（你补充）**

➡️ **推荐 a**。理由：<证据支撑的理由>
```

Each answer reshapes the tree: settled decisions push the frontier outward and unblock questions that depended on them. A question whose answer depends on a still-open question belongs to a _later_ round, not this one — never ask it early.

Finding _facts_ is your job, never the user's. When a frontier question needs a fact from the environment (filesystem, tools, etc.), dispatch a sub-agent to find it; don't ask the user for anything you could look up yourself. Don't block on it: a running exploration is an unsettled prerequisite, so only the questions downstream of it wait for the sub-agent to report.

The _decisions_ are the user's: put each to them and wait.

The session is done when the frontier is empty: every branch of the design tree visited, nothing left silently assumed. Do not act on it until the user confirms you have reached a shared understanding.
