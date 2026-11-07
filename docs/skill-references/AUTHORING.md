# 业务内容怎么写（AUTHORING）

> **给谁看**：把壳拷到**新项目**后，负责为每个业务 skill 编写 `docs/skill-references/<skill>/` 资料的人。
> **唯一真源**：`scripts/check_skill.mjs`（门禁查什么，本文件就写什么）。**两者不一致时以脚本为准**，并来改本文件。
> **自检命令**（写完就跑，FAIL 必须清零）：
> ```bash
> node docs/skill-references/scripts/check_skill.mjs .cursor/skills/<skill>
> ```

## 一、先分清两类要求（别把本仓习惯当契约）

| 类别 | 含义 | 例子 |
|---|---|---|
| **壳强制** | 不满足就 **FAIL**（脚本查得到） | `content_root` 存在、`_version.txt` 与代数一致、`backlog` 含两个小节 |
| **示例约定** | 本仓的写法习惯，**壳不检查**；别的项目可以另立 | `p_` / `g_` / `s_` / `t_` 文件名前缀、反例编号连续、版本"三处同步" |

下面第二~八节是**壳强制**；第九节是**示例约定**（可抄，但不强制）。

## 二、`project.yaml` 逐键（壳强制）

| 键 | 必填 | 合法值 | 门禁行为 |
|---|---|---|---|
| `gate_on` | ✅ | `always` \| `meta_only` | 其它值/缺失 ⇒ **FAIL**（`always` = 每次任务都跑门禁；`meta_only` = 只在改 skill 自己时跑） |
| `sediment` | ✅ | `on` \| `off` | 其它值/缺失 ⇒ **FAIL**；`on` ⇒ 必须给 `mounts.sediment_process` 与 `mounts.backlog` |
| `version_counts` | ✅ | `norms` \| `paradigms` \| `severity_buckets` | 决定"代数怎么数"，见 §三 |
| `content_root` | ✅ | 相对 **业务 skill 目录** 的路径（常见 `../../../docs/skill-references/<skill>`） | 路径不存在 ⇒ **FAIL** |
| `mounts.version_file` | ✅ | 文件路径 | 不存在 ⇒ **FAIL**；★目录必须**等于** `content_root` |
| `mounts.rules` | 按需 | 文件路径列表 | 任一不存在 ⇒ **FAIL** |
| `mounts.anti_patterns` | 可选 | 文件路径 | 声明了但不存在 ⇒ **FAIL**（反例簿通常放这里） |
| `mounts.backlog` | `sediment: on` 时必填 | 文件路径 | 不存在 ⇒ **FAIL**；**内容**必须含 `## 候选` 与 `## 已晋升` |
| `mounts.sediment_process` | `sediment: on` 时必填 | 文件路径 | 不存在 ⇒ **FAIL** |
| `norms` | `version_counts: norms` 时必填 | 列表项 `id` / `title` / `ref` **三者齐全**；可选 `layer`（`L1`\|`L2`\|`L3`） | 缺 id/title/ref ⇒ **解析失败 FAIL**；`ref` 不存在 ⇒ **FAIL**；`layer` 若出现须被解析器接受（码农包**强制**有 layer，见特化门禁 / ADR 0038） |
| `extra_gates` | 可选 | 脚本路径列表 | 声明了但不存在 ⇒ **FAIL** |
| `docs` | 可选 | `键: 值` 标量组 | 只解析，不校验存在性（放项目文档指针） |
| `peer_pools` | 可选 | 路径列表 | 只解析（兄弟 skill 的池，供**人工**抄录） |

### 附：哪些是常驻、哪些是**按需**（加载语义）

壳只管"文件在哪儿"，**不负责自动加载**。`project.yaml` 里声明的文件分两类：

| 声明 | 文件 | 谁加载 | 何时进上下文 |
|---|---|---|---|
| `role_rule` | IDE 规则目录（如 `.cursor/rules/<域>/RULE.mdc`） | **宿主的 Rules 机制**，看该文件自己的 `alwaysApply` | `true` ⇒ 每轮常驻；`false` ⇒ 按它的 `description` 触发词**条件加载** |
| `mounts.*`（`rules` / `anti_patterns` / `backlog` / `sediment_process`） | 内容根里的普通 md | **无人自动加载** | 只有 Agent 按 `SKILL.md` 的挂载句**主动读**才进 |
| `norms[].ref` | 与 `mounts.rules` 同一批文件 | 同上（门禁另会数条目） | 同上 |
| `mounts.version_file` | `_version.txt` | **门禁脚本机械读**（不进上下文） | — |
| `docs.*` | 文档指针（含 `context_glob`） | 无人自动展开 | 需要时按指针去读 |
| `project.yaml` 自身 | — | 无人自动注入 | 靠 `SKILL.md` 写"启动先读 / 收尾读" |

两条容易踩的：

1. **`mounts` 里的文件不会自动进上下文**。要让某条约束"每次都必须生效"，只有两条路：写进一条 `alwaysApply: true` 的短 Rule，或在流程里显式要求先读它 —— 代价都是**非相关会话也吃 context**。
2. **`role_rule` 若是 `alwaysApply: false`，非编码会话可能根本不加载**（靠触发词命中）。用「Rule ↔ Skill 双向必读」兜底：Rule 指 Skill、Skill 指 Rule。

## 三、代数 `vN` 怎么数（最易错的一节）

`vN` = **"增长单元"的个数**，由 `version_counts` 决定数什么；**三处必须相等**，否则 FAIL：

```
references/_version.txt 的 vN  ==  按 version_counts 数出来的个数  ==  SKILL.md description 里的 vN
```

| `version_counts` | 数什么 | 资料怎么写才数得对 |
|---|---|---|
| `norms` | `norms[]` 的**条数** | 每加一条规范：① 正文写进 `ref` 指向的文件；② 在 `project.yaml` 的 `norms` 里**登记一行**（`- id:` + `title:` + `ref:`）。漏登记 ⇒ 数不到 ⇒ FAIL |
| `paradigms` | `mounts.rules` 中**文件名形如 `p_*.md`** 的条数 | 一范式一文件；文件名必须以 `p_` 开头、`.md` 结尾；且必须挂在 `mounts.rules` 下 |
| `severity_buckets` | `mounts.rules` 各文件里**表格行**的条数（匹配行首 `\|` + 反引号包裹的 `` `P0-…` ``/`` `P1-…` ``） | 定级必须写成 **Markdown 表格**，桶 ID 用反引号包起来、前缀 `P0-` 或 `P1-`（例：`` | `P1-kotlin-new-java` | … | ``）；写在段落里数不到 ⇒ FAIL |

> 换版本的姿势：改这三处**同一个数字** + 在项目 `CHANGELOG` 追加一条。**别在别处写版本号**（第二份副本必漂移）。

## 四、内容文件清单（各自写什么）

| 文件（角色） | 最小要求 | 谁校验 |
|---|---|---|
| `_version.txt` | 单行 `vN`（可带 `#` 注释） | 壳：值必须 == 代数 |
| `_backlog.md` | 必须含 `## 候选` 与 `## 已晋升` 两个小节 | 壳：缺 ⇒ FAIL |
| 反例簿（`mounts.anti_patterns`） | 每条反例 = 错法 + 正确写法；**内容自定** | 壳只查"文件在不在" |
| 规则 / 范式 / 定级文件（`mounts.rules`、`norms[].ref`） | 业务硬约束正文；**内容自定** | 壳只查"文件在不在"与代数计数 |
| 沉淀流程（`mounts.sediment_process`） | 收尾三问 + 何时入池的流程（可复用 `templates/sediment_process.md`） | 壳只查"文件在不在" |
| 特化脚本（`extra_gates`） | 建议零依赖（`node` 内置模块即可）；**不进 IDE skills 树** | 壳只查"文件在不在" |

> **不挂载 = 不校验**：没被 `mounts.rules` / `norms[].ref` / `extra_gates` 引用到的文件，门禁**不会**看它一眼。

## 五、`SKILL.md` 侧的两条硬要求

1. **必须字面包含** `sediment: off` 与 `mounts` 两个字符串（挂载引导句）。哪怕你是 `sediment: on`，引导句里通常也要带 `sediment: off` 这句话术 —— 缺 ⇒ **FAIL**。
2. frontmatter `name` **必须等于业务 skill 目录名**。

## 六、YAML 缩进（解析器很窄，缩进错就 FAIL）

壳用的是**手写解析器**，只认下面这套形状：

```yaml
gate_on: always                 # 顶层键：顶格
sediment: on
version_counts: paradigms
content_root: ../../../docs/skill-references/<skill>

docs:                           # 2 空格子键
  changelog: docs/specs/<feature>/changelog.md

norms:                          # 每项：'  - id:' 起头，字段 4 空格
  - id: n1
    title: 规范标题
    ref: ../../../docs/skill-references/<skill>/xxx.md

peer_pools:                     # 列表项：'  - 值'
  - ../../../docs/skill-references/<other-skill>/_backlog.md

extra_gates:                    # 同上
  - ../../../docs/skill-references/<skill>/scripts/check_xxx.mjs

mounts:
  rules:                        # 子项：'    - 值'（4 空格）
    - ../../../docs/skill-references/<skill>/p_example.md
  anti_patterns: ../../../docs/skill-references/<skill>/misuse.md
  backlog: ../../../docs/skill-references/<skill>/_backlog.md
  sediment_process: ../../../docs/skill-references/templates/sediment_process.md
  version_file: ../../../docs/skill-references/<skill>/_version.txt
```

## 七、抄这个最小骨架

```text
<项目仓>/
├── docs/skill-references/            # 壳：全项目共用
│   ├── SCHEMA.md  AUTHORING.md       # 契约 + 本指南
│   ├── scripts/check_skill.mjs       # 通用门禁（零依赖）
│   ├── templates/sediment_process.md # 沉淀流程模板
│   └── <skill-a>/                    # 业务内容（每项目自建）
│       ├── _version.txt              # v1
│       ├── _backlog.md               # ## 候选 / ## 已晋升
│       ├── misuse.md                 # 反例簿
│       ├── p_example.md              # 范式/规则（按你的 version_counts 决定命名）
│       └── scripts/check_xxx.mjs     # 可选：特化门禁（列入 extra_gates）
└── .cursor/skills/<skill-a>/      # 业务 skill 只留两件（+ 可选人读 README）
    ├── SKILL.md                      # frontmatter name == skill-a；正文含 sediment: off / mounts
    └── project.yaml                  # 见 §二
```

`project.yaml` 里 `content_root` 写 `../../../docs/skill-references/<skill-a>`（从 `.cursor/skills/<skill-a>/` 往上三级）。
从 IDE skills 树拷到另一个仓时，**`docs/skill-references/` 必须一起搬**——只拷 skill 目录会缺资料。

## 八、常见 FAIL 对照表

| FAIL 文案 | 真实原因 |
|---|---|
| `version_file 目录（…）≠ content_root（…）` | `_version.txt` 没放在内容根里（或 `content_root` 写错层级） |
| `_version.txt v3 ≠ 计数 2（norms）` | 规范正文写了但 `norms[]` 漏登记（或反之） |
| `description v2 ≠ 计数 3` | 忘了改 `SKILL.md` 的 `description`（三处没同步） |
| `backlog 缺「## 候选」或「## 已晋升」` | 候选池文件缺小节（哪怕内容是「（暂无）」也要留小节） |
| `SKILL.md 缺少挂载引导（须含 sediment: off 与 mounts）` | 引导句没写、或把这两个词只写进了 `project.yaml` |
| `project.yaml: L12: …` | YAML 缩进不合 §六（解析器窄，不是标准 YAML 解析） |
| `norms 缺 id/title/ref` | `norms` 某项三字段没写齐 |

## 九、示例约定（本仓习惯，壳**不**检查；别的项目可另立）

- 文件名前缀分类：`p_` 范式 / `g_` 治理规程 / `s_` 契约 / `t_` 模板；`_` 前缀 = 辅助文件；`misuse.md`、`_backlog.md` 为固定名。
- 反例簿编号**连续**、新增接末尾（不插队）；候选池「候选 → 已晋升」用整行移动，不留在候选段。
- 版本**三处同步**（`_version.txt` / `description` / 项目 `CHANGELOG` 置顶条），别处不写版本号。
- 命名与落点：人读说明书放业务 skill 目录根 `README.md`；实现流水留痕放 `docs/specs/<feature>/changelog.md`。
- 门禁脚本改动 = 规范级，须项目负责人批准（本仓 ADR 0007）。

## 十、改壳之后：跑一次同步巡检（可选）

若另有一份壳副本用于多仓同步，两边应只差署名类差异。改任何一边之后可跑：

```bash
diff <另一份壳>/scripts/check_skill.mjs <本壳>/scripts/check_skill.mjs
node <另一份壳>/scripts/check-shell-sync.mjs <本壳> <另一份壳>
```

不一致时判断哪边更新，再把另一边补上（署名各保持各自身份）。
