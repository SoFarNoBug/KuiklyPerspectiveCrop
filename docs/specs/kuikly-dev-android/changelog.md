# kuikly-android-coding · skill changelog（本仓）

## 2026-11-07（编码 Skill 硬闸）

- 新增 `.cursor/hooks/coding-skill-gate.mjs` + `skill-gate-unlock.mjs`，挂入 `hooks.json`
- 未 Read `kuikly-android-coding/SKILL.md` 时拦 `.kt`/`.java`/`.kts` 写入；与确认闸独立
- 收紧 `plan-exec-coding-skill.mdc`；文档：`docs/context/coding-skill-gate.md`、`agents-guide`
- 治理收尾：门禁 跳过｜三问 否/是/否｜理由：机械兜底防再跳过 skill｜待批 0

## v5 · 2026-11-07

- 退役本仓 `lang_java` 挂载与 selfcheck；基线路由改为 `lang_kotlin` + `platform_android`
- 删除休眠 `proguard_when_enabled`；审查侧去掉 `P0-proguard-verify` / Java 强制对照
- 精简 `new_code_file_guidelines`（去宿主仓 Java 互操作，保留 Kotlin 优先 + 文件头）
- misuse：收紧错误 1；删除原错误 8（ProGuard）并重编号
- 顺带修通用门禁 `docs/skill-references/scripts/check_skill.mjs`：Windows 下用 `path.isAbsolute` 识别 skill 目录（原 `startsWith('/')` 导致双拼路径）
- 治理收尾：门禁 FAIL0/WARN0｜三问 否/是/否｜理由：卸挂与删休眠；反例修订｜待批 0（已达标 0）

## 历史 · 2026-11-06（norms 曾挂六条后回落）

- 落地 `.cursor/` 码农技能，`project_id` 为 `kuikly-perspective-crop`
- 曾新增 L2 `platform_kuikly_compose`、L3 `perspective_crop_api` + selfcheck；Rule 补 P0；misuse 末三条 Kuikly 反例
- 治理收尾：门禁 跳过｜三问 是/是/否｜理由：新规范+新反例入池级已落地正式 norms｜待批 0（已达标 0）

## 其它记录

- 2026-11-07：技能 / 规则 / skill-references / specs 统一为 `kuikly-*` 身份命名。
  - 治理收尾：门禁 跳过｜三问 是/是/否｜理由：仅身份重命名与文案清洗｜待批 0（已达标 0）
- 2026-11-07：对齐已定决策——新增 `docs/decisions.md`（Q1–Q15 SoT）；六层 Painter SPI / Q9 边拖默认 / v0.1=M0–M3 回写 milestones、CONTEXT、L3、Rule。
  - 治理收尾：门禁 跳过｜三问 是/是/否｜理由：文档与已定决策对齐，未改 skill 壳｜待批 0（已达标 0）
