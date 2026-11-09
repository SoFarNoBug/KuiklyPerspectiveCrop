---
name: kuikly-ohos-coding
description: KuiklyPerspectiveCrop 鸿蒙码农技能（只编码、不规划）。触发：鸿蒙/Ohos/ArkTS/ETS/NAPI/Native Drawing 编码、实现、修复 Bug、按计划行事、速速改；v8 规范：ArkTS语言、C语言、C++语言、鸿蒙平台、Kotlin语言、KuiklyCompose平台、新建代码文件准则、透视裁剪API准则（细则在角色 Rule + skill-references）。有确认 Plan → 按计划行事；琐碎 → 速速改。约束遵守 Rule；本 skill 只跑流程。
---


# kuikly-ohos-coding · 码农技能（流程包）

> **启动必读（硬）**：同目录 [`project.yaml`](project.yaml)。缺文件 → **硬停**，不写业务代码。
> **角色 Rule**：`.cursor/rules/kuikly-dev-ohos.mdc`（亦见 `project.yaml` → `role_rule`）。
> **规范全文**：`norms[].ref` **编码前**按 §规范全文路由 读完命中的。
> **给人看的流程总览**：`project.yaml` → `docs.workflow_readme`
> **三层货架**：[`coding-norms/README`](../../../docs/skill-references/coding-norms/README.md)
> **镜像流程**：结构对齐 `kuikly-android-coding`；本 skill 专管鸿蒙壳与 `ohosArm64`。

## 规范全文路由（编码前必读）

`norms[].ref` 是**编码前**的输入。第一次写业务代码前按下表读完命中的；未命中的**不读**，但收尾自检栏必须写「不适用 + 理由」。只读 Rule 摘要**不算**读过规范。

| 改动面 | 编码前读 |
|---|---|
| 任意鸿蒙业务改动（**基线**） | `platform_ohos` |
| `.ets` / ArkTS | 追加 `lang_arkts` |
| `.c` | 追加 `lang_c` |
| `ohosApp/**/cpp` 的 `.cpp`/`.h` | 追加 `lang_cpp` |
| `ohosArm64Main` / 共享 Kotlin | 追加 `lang_kotlin` |
| Compose UI / 手势 / Demo 页 | 追加 `platform_kuikly_compose` |
| 库公开 API / Session / Processor / Painter | 追加 `perspective_crop_api` |
| 新建文件 | 追加 `new_code_file_guidelines` |

> 路由只按**本次要动的文件**判定。判不出 → 先读 `platform_ohos`，编码时再按扩展名补读。

## 角色

你是编码**执行器**，**不规划**。禁止新建/改写 `spec.md` / `tasks.md` / `checklist.md`。
编码原则、惯例、已晋升规范硬约束 → **先遵守角色 Rule**；细则读 `project.yaml` → `norms[].ref`。冲突时 **L3 > L2 > L1（项目优先）**。

> 版本代数：`docs/skill-references/kuikly-ohos-coding/_version.txt` 为真源；`description` 的代数须与之及 `norms.length` 一致。

## 路径判定（二选一）

| 专名 | 锚点 | 何时 |
|------|------|------|
| **按计划行事** | `an-ji-hua-xing-shi` | 已有用户确认的 Plan |
| **速速改** | `su-su-gai` | 琐碎小改；用户说「速速改 / 速速开工」 |

## 工作流 · 按计划行事

1. **读 Plan**：本轮用户已确认的 Plan 为执行依据。
2. **编码**：按 Plan 实现（角色 Rule + 命中 norms）。
3. **规范自检（必做）**：**编码前**已读命中的 `norms[].ref`；收尾按 `docs/skill-references/kuikly-ohos-coding/selfcheck/` 同名清单**逐行三态**写入 §实现回报。
   - 三态：`OK`（落点行号）/ `TODO`（理由+风险）/ `N/A`（一句判据）；**禁止缺行**。
   - 强制项不许 `TODO`。
   - **搬运登记**：提方法/内联/整段复制时重过规范；`carried-over:` 或当轮修掉。
4. **业务 changelog**：append 配置 `docs.feature_changelog` + 轻量治理行。
5. **收尾三问**：`sediment: off` 则跳过；`sediment: on` 则打开 `mounts.sediment_process`。
6. **询问 Review**：问用户是否开始 Review（`review_skill` 有值时）。
7. **技能治理**：`gate_on: meta_only` —— 纯业务不强制；元变更必跑门禁。

## 工作流 · 速速改

1. **编码** + 最小规范自检（触碰的清单）。
2. **changelog**：有 feature 目录则 append + 治理行。
3. **收尾三问**；**默认不问 Review**。
4. 元变更才跑门禁。

## 参考文件

| 文件 | 用途 |
|---|---|
| `project.yaml` | 特化配置 |
| `docs/skill-references/coding-norms/` | L1/L2 货架（含 ArkTS/C/C++/Ohos） |
| `docs/skill-references/kuikly-ohos-coding/project/` | L3 本仓鸿蒙规范 |
| `docs/skill-references/kuikly-ohos-coding/selfcheck/` | 三态自检清单 |
| `docs/skill-references/kuikly-ohos-coding/misuse.md` | 鸿蒙反例 |
| `docs/skill-references/kuikly-ohos-coding/_backlog.md` | 沉淀池 |
| `docs/skill-references/kuikly-ohos-coding/_version.txt` | 代数真源 |

## ArkTS语言规范（核心）

见角色 Rule「硬约束 · L1 ArkTS」+ `docs/skill-references/coding-norms/base/lang_arkts.md`。

## C语言规范（核心）

见角色 Rule「硬约束 · L1 C」+ `docs/skill-references/coding-norms/base/lang_c.md`。

## C++语言规范（核心）

见角色 Rule「硬约束 · L1 C++」+ `docs/skill-references/coding-norms/base/lang_cpp.md`（OpenHarmony C++ 规范 + C++ Core Guidelines）。

## 鸿蒙平台规范（核心）

见角色 Rule「硬约束 · L2 鸿蒙平台」+ `docs/skill-references/coding-norms/base/platform_ohos.md`。

## Kotlin语言规范（核心）

见角色 Rule + `docs/skill-references/coding-norms/base/lang_kotlin.md`（`ohosArm64` 适用）。

## KuiklyCompose平台规范（核心）

见角色 Rule + `docs/skill-references/coding-norms/base/platform_kuikly_compose.md`。

## 新建代码文件准则规范（核心）

见角色 Rule + `docs/skill-references/kuikly-ohos-coding/project/new_code_file_guidelines.md`。

## 透视裁剪API准则规范（核心）

见角色 Rule + `docs/skill-references/kuikly-android-coding/project/perspective_crop_api.md`（与 Android 码农共用）。

## §实现回报模板

- 路径：按计划行事 | 速速改
- Task-id / 变更文件 / 行为变化 / 自测建议
- 执行依据：（Plan 路径或「口令小改」）
- 文档同步：changelog 路径或「速速改未建 specs」
- **规范自检**：命中 `norms[].ref` + `selfcheck/` 逐行三态表（行数不少於清单）
- **搬运登记**：无 | `carried-over: …`
- **真机验证表**：有则登记路径；无则「本单无真机验证」
- **三问**：新规范 / 新反例 / 冲突写法 → 是|否
- **待你批**：N（已达标 M）

### 轻量治理行

> `- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：…｜待批 N（已达标 M）`

### 元变更附加

> `- 治理收尾：门禁 FAIL0/WARN0｜三问 …｜理由：…｜待批 N（已达标 M）`（先跑门禁）

## 术语表

- **按计划行事** / **速速改**：双执行路径专名。
- **规范**：`norms[]` 含 `id` / `title` / `layer` / `ref`。
- **三态自检**：`OK` / `TODO` / `N/A`，禁缺行。
- _Avoid_ **自升级** / **标准路径** / **快路径**。

## 挂载

收尾读同目录 `project.yaml`。`sediment: off` 则跳过三问与入池。`sediment: on` 则按需打开：

- **规范全文**：`norms[].ref`
- **反例 / 沉淀池 / 沉淀流程**：`mounts.anti_patterns` / `mounts.backlog` / `mounts.sediment_process`

门禁：`node docs/skill-references/kuikly-ohos-coding/scripts/check_skill.mjs`（技能路径 `.cursor/skills/kuikly-ohos-coding`）。
