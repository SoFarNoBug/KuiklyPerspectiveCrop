# skill-references 契约（项目内壳 + 业务内容）

本目录是**仓内公共素材包**（无 `SKILL.md`），供 Cursor 等 IDE 的业务 skill 共用。

- **壳**（本目录根）：`scripts/check_skill.mjs`、`templates/sediment_process.md`、本 SCHEMA、[`AUTHORING.md`](AUTHORING.md)（业务内容怎么写）
- **业务内容**：`kuikly-android-coding/`、`kuikly-code-review/`（原各 skill 的 `references/`）；跨 skill 可移植的 **L1/L2 编码规范货架**见 [`coding-norms/`](coding-norms/README.md)（[ADR 0038](../adr/0038-coding-norms-three-layer.md)）
- 壳可拷到其他仓的 `docs/skill-references/`；业务内容按项目自建子目录。

## 移植到其他项目

1. 拷贝壳到目标仓的 `docs/skill-references/`。
2. 为每个业务 skill 建 `docs/skill-references/<skill>/`，放入规则 / 反例 / 池 / `_version.txt`（**壳强制项与写法见 [`AUTHORING.md`](AUTHORING.md)**）。
3. 业务 skill 目录只留 `SKILL.md` + `project.yaml`（+ 可选人读 README）；`content_root` 与 `mounts` 指到上述子目录。
4. 门禁：无特化检项时直接跑共享核；有特化检项时脚本放在 `docs/skill-references/<skill>/scripts/`，由业务 `check_skill.mjs` 调共享核，并在 `extra_gates` 列附加门禁：

```bash
# 审查（无特化）
node docs/skill-references/scripts/check_skill.mjs .cursor/skills/kuikly-code-review

# 码农（有特化）
node docs/skill-references/kuikly-android-coding/scripts/check_skill.mjs
```

> 本仓宿主为 **Cursor**（`.cursor/`）。

## project.yaml

```yaml
sediment: on
gate_on: always
version_counts: paradigms
content_root: ../../../docs/skill-references/<skill>

mounts:
  rules:
    - ../../../docs/skill-references/<skill>/p_example.md
  anti_patterns: ../../../docs/skill-references/<skill>/misuse.md
  backlog: ../../../docs/skill-references/<skill>/_backlog.md
  sediment_process: ../../../docs/skill-references/templates/sediment_process.md
  version_file: ../../../docs/skill-references/<skill>/_version.txt
```

路径相对**业务 skill 目录**。`content_root` 须与 `version_file` 同目录。`sediment: on` 时 `sediment_process` 与 `backlog` 必须存在。

## 按需加载

| 步骤 | 打开 |
|---|---|
| 做业务 | `mounts.rules` 里当前需要的那几份 |
| 写反例 | `mounts.anti_patterns` |
| `sediment: on` 的收尾 | `mounts.sediment_process`，够格再写 `mounts.backlog` |
| `sediment: off` | 不打开沉淀流程，不入池 |
