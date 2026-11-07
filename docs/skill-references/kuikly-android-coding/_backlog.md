# 待沉淀池（references/_backlog.md）

> **分层**：本文件为候选/已晋升池**真源**。晋升后红线摘要进 Rule、全文进 `docs/skill-references/kuikly-android-coding/{规范}.md`；流程见 Skill（[ADR 0021](../../adr/0021-rule-redline-references-sot.md)、[ADR 0029](../../adr/0029-android-discovery-open-promotion-closed.md)）。
> 本池从空开始积累本仓候选；晋升或作废走人工评审。

**定位**：候选规则的**暂存区**。凡「可能成为规范 / 判据，但未满足准入三条件，或属规范级但未获授权」的观察先落这里；达标并按 `project.yaml` 的 `mounts.sediment_process` 授权后，才晋升到正式文件。

**写入方**：仅码农 skill 收尾三问。审查 skill 不得自动写入本池（ADR 0036）；可人工把审查池条目抄入「## 候选」。

**条目格式（候选）**：`- [ ] {规则一句话} ｜{日期} ｜证据：{路径 / 条目} ｜自评：已达标 / 未达标（缺什么）`

**晋升 DoD（缺一不可；门禁会查分段）**：
1. 正式文件已落地：`docs/skill-references/kuikly-android-coding/{规范}.md`；**Rule** 增加对应 ≤3 行红线 + 全文链接；Skill：`## X规范（核心）` 一行索引 + `description` 含规范名
2. **从「## 候选」整行删除**（禁止在候选段留下 `[x] 已晋升…`）
3. **追加到「## 已晋升」**：`- [x] 已晋升（{日期}）→ {落点}`（**禁止**写版本号 `v{N}`）
4. 跑 `node docs/skill-references/kuikly-android-coding/scripts/check_skill.mjs` 至 FAIL 0

**★红线**：**未批准的候选不得被后续任务引用或执行**（未批准 = 无约束力）。

**★去重**：晋升 / 出池前先 grep 正式 `docs/skill-references/kuikly-android-coding/*.md` 与 `docs/context/*`，确认无同源条文（门禁不查语义重复）。

**催办**：池子非空时，每次编码任务收尾的归档通知**必须列一行**「待你批：N 条（其中 M 条自评已达标）」；**超 90 天未批**额外提示「归档或删除」。N =「## 候选」下未勾选条数。

**准入三条件**（详见 `mounts.sediment_process`）：① 跨需求可复用 ② 有真实事故佐证 ③ 已验证。

## 候选

（暂无）

## 已晋升

（暂无）
