# skill-references（项目内壳）

> 本仓业务目录为 `kuikly-*`。壳变更请追加在文件顶部。历史条目中的路径已按本仓命名归一。

## 2026-10-21 新增 Rules/Skills frontmatter 自检脚本

- 新增 `scripts/check_codebuddy_spec.mjs`（零依赖 Node ESM，按需运行，**不是**业务门禁、不进任何 `mounts` / `extra_gates`）：扫 `.cursor/rules/*/RULE.mdc` 与 `.cursor/skills/*/SKILL.md` 的 frontmatter 形态。
  - 规则侧：`RULE.mdc` 必须在位；键白名单 `description/alwaysApply/enabled/updatedAt/provider/globs/type`；`description` **不得为块标量**（`>-` / `|` 会让 IDE 规则索引只回显 `>-`，智能体请求型规则等于没有描述）；`alwaysApply` 必须显式；`alwaysApply: true` 超过 5 条给 WARN。
  - 技能侧：`name` 与目录名一致且 kebab-case；键白名单 `name/description/allowed-tools/disable/license/metadata`；`description` 非空、不含 `<` `>`、>400 字符 FAIL（>300 WARN，实测技能列表 ~330 字符处截断）；正文 <5k 词（>4000 WARN）；`SKILL.md` 内相对链接（基准 = skill 目录）必须存在；顶层非规范条目 WARN。
- 用法：`node docs/skill-references/scripts/check_codebuddy_spec.mjs [rulesDir] [skillsDir]`；缺省 `.cursor/rules .cursor/skills`；退出码 0 无 FAIL。
- 可用 `check-shell-sync.mjs` 与另一份壳副本做脚本一致性巡检（可选）。

## 2026-10-16 新增壳同步巡检脚本（随壳两份）

- 新增 `scripts/check-shell-sync.mjs`：比较两侧壳的 `scripts/*.mjs`，**归一化署名白名单**（`skill-shell` ↔ `docs/skill-references`）后逐行比；缺/多文件或真实差异 ⇒ FAIL。
- 目的：把「改壳要多份同改」从口头约定变成可跑命令（见 `AUTHORING.md`）。
- **不是业务门禁**：不进任何 `mounts` / `extra_gates`，`gate_on` 与它无关。

## 2026-10-16 业务内容编写指南

- 新增 [`AUTHORING.md`](AUTHORING.md)：以 `scripts/check_skill.mjs` 为**唯一真源**，写清 `project.yaml` 逐键（必填/合法值/门禁行为）、代数 `vN` 三种口径（`norms` / `paradigms` / `severity_buckets` 各怎么数）、内容文件清单、`SKILL.md` 侧两条硬要求、YAML 缩进形状、最小骨架、常见 FAIL 对照；并**明确区分「壳强制」与「示例约定」**（避免别的项目照抄本仓习惯当契约）。
- `SCHEMA.md`：壳清单与 §移植 第 2 步加指针。
- `AUTHORING.md` §二 增「附：哪些是常驻、哪些是按需（加载语义）」：`role_rule` 由宿主按 `alwaysApply` 条件加载；`mounts.*` / `norms[].ref` / `docs.*` / `project.yaml` 均**无人自动加载**，靠 Agent 主动读；`version_file` 只被门禁机械读、不进上下文。

## 2026-10-16 特化 scripts 迁入业务子目录

- 码农等特化门禁迁到 `docs/skill-references/<skill>/scripts/`；无特化时直接跑共享核。
- SCHEMA：业务 skill 目录只留薄入口；特化脚本不进 IDE skills 树。
- 门禁入口：① 共享核 `node docs/skill-references/scripts/check_skill.mjs .cursor/skills/<skill>`；② 特化脚本 `node docs/skill-references/<skill>/scripts/check_skill.mjs`（见 [`SCHEMA.md`](SCHEMA.md)）。

## 2026-10-16 业务 references 迁入子目录

- 业务内容迁到 `kuikly-android-coding/`、`kuikly-code-review/` 等（原各 skill 的 `references/`）。
- 业务 skill 增加 `content_root`；`mounts` / `norms` / `peer_pools` 改指新路径；特化门禁读 `content_root`。

## 2026-10-16 迁到 docs/skill-references

- 从 `.cursor/skills/_shell/` 迁到 `docs/skill-references/`，供多 IDE 共用。
- 业务 skill 的 `sediment_process` / 门禁 spawn 改为相对路径 `../../../docs/skill-references/...`。

## 2026-10-16 取代 skill-self-governance

- 壳不是 skill：无触发词。业务内容改由 `project.yaml` 的 `mounts` 挂载。
- `sediment` 与 `gate_on` 分开：前者管要不要入池，后者管何时跑门禁。
- 通用门禁按挂载路径检查，不再写死 `references/`。
