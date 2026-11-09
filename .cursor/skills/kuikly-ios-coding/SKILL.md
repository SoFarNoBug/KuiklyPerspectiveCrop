---
name: kuikly-ios-coding
description: KuiklyPerspectiveCrop iOS 码农技能（只编码、不规划）。触发：iOS/Swift/Objective-C/ObjC/UIKit/iosApp/iosMain 编码、实现、修复 Bug、按计划行事、速速改；v7 规范：Swift语言、Objective-C语言、iOS平台、Kotlin语言、KuiklyCompose平台、新建代码文件准则、透视裁剪API准则（细则在角色 Rule + skill-references）。有确认 Plan → 按计划行事；琐碎 → 速速改。约束遵守 Rule；本 skill 只跑流程。
---


# kuikly-ios-coding · 码农技能（流程包）

> **启动必读（硬）**：同目录 [`project.yaml`](project.yaml)。缺文件 → **硬停**，不写业务代码。
> **角色 Rule**：`.cursor/rules/kuikly-dev-ios.mdc`（亦见 `project.yaml` → `role_rule`）。
> **规范全文**：`norms[].ref` **编码前**按 §规范全文路由 读完命中的。
> **给人看的流程总览**：`project.yaml` → `docs.workflow_readme`
> **三层货架**：[`coding-norms/README`](../../../docs/skill-references/coding-norms/README.md)
> **镜像流程**：结构对齐 `kuikly-android-coding` / `kuikly-ohos-coding`；本 skill 专管 iOS 壳与 `iosMain`。

## 规范全文路由（编码前必读）

| 改动面 | 编码前读 |
|---|---|
| 任意 iOS 业务改动（**基线**） | `platform_ios` |
| `.swift` | 追加 `lang_swift` |
| `.m` / `.mm` / ObjC 头 | 追加 `lang_objc` |
| `iosMain` / 共享 Kotlin | 追加 `lang_kotlin` |
| Compose UI / Demo | 追加 `platform_kuikly_compose` |
| 库公开 API / Session / Processor / Painter | 追加 `perspective_crop_api` |
| 新建文件 | 追加 `new_code_file_guidelines` |

> 判不出改动面 → 先读 `platform_ios`，再按扩展名补读。只读 Rule 摘要**不算**读过规范。

## 角色

你是编码**执行器**，**不规划**。禁止新建/改写 `spec.md` / `tasks.md` / `checklist.md`。
冲突时 **L3 > L2 > L1（项目优先）**。

> 版本代数：`docs/skill-references/kuikly-ios-coding/_version.txt`；须与 `description` 的 vN 及 `norms.length` 一致。

## 路径判定（二选一）

| 专名 | 锚点 | 何时 |
|------|------|------|
| **按计划行事** | `an-ji-hua-xing-shi` | 已有用户确认的 Plan |
| **速速改** | `su-su-gai` | 琐碎小改；用户说「速速改 / 速速开工」 |

## 工作流 · 按计划行事

1. **读 Plan** → 2. **编码**（Rule + 命中 norms）→ 3. **规范自检**（`selfcheck/` 同名清单逐行三态，禁缺行；强制项不许 `TODO`；搬运登记）→ 4. **changelog** append + 治理行 → 5. **收尾三问**（`sediment`）→ 6. **询问 Review** → 7. 元变更才跑门禁。

## 工作流 · 速速改

编码 + 最小自检 + 条件性 changelog；默认不问 Review；元变更才跑门禁。

## 参考文件

| 文件 | 用途 |
|---|---|
| `project.yaml` | 特化配置 |
| `docs/skill-references/coding-norms/` | L1/L2（含 Swift / ObjC / iOS） |
| `docs/skill-references/kuikly-ios-coding/project/` | L3 |
| `docs/skill-references/kuikly-ios-coding/selfcheck/` | 三态清单 |
| `docs/skill-references/kuikly-ios-coding/misuse.md` | 反例 |
| `docs/skill-references/kuikly-ios-coding/_backlog.md` | 沉淀池 |
| `docs/skill-references/kuikly-ios-coding/_version.txt` | 代数真源 |

## Swift语言规范（核心）

见角色 Rule + `docs/skill-references/coding-norms/base/lang_swift.md`（[Swift API Design Guidelines](https://www.swift.org/documentation/api-design-guidelines/)）。

## Objective-C语言规范（核心）

见角色 Rule + `docs/skill-references/coding-norms/base/lang_objc.md`（Apple Coding Guidelines for Cocoa）。

## iOS平台规范（核心）

见角色 Rule + `docs/skill-references/coding-norms/base/platform_ios.md`。

## Kotlin语言规范（核心）

见角色 Rule + `docs/skill-references/coding-norms/base/lang_kotlin.md`（`iosMain` 适用）。

## KuiklyCompose平台规范（核心）

见角色 Rule + `docs/skill-references/coding-norms/base/platform_kuikly_compose.md`。

## 新建代码文件准则规范（核心）

见角色 Rule + `docs/skill-references/kuikly-ios-coding/project/new_code_file_guidelines.md`。

## 透视裁剪API准则规范（核心）

见角色 Rule + `docs/skill-references/kuikly-android-coding/project/perspective_crop_api.md`（共用）。

## §实现回报模板

- 路径：按计划行事 | 速速改
- Task-id / 变更文件 / 行为变化 / 自测建议
- 执行依据 / 文档同步
- **规范自检**：命中 norms + selfcheck 逐行三态
- **搬运登记** / **真机验证表** / **三问** / **待你批**

### 轻量治理行

> `- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：…｜待批 N（已达标 M）`

## 术语表

- **按计划行事** / **速速改**：双执行路径专名。
- **三态自检**：`OK` / `TODO` / `N/A`，禁缺行。
- _Avoid_ **自升级** / **标准路径** / **快路径**。

## 挂载

收尾读同目录 `project.yaml`。`sediment: off` 则跳过三问与入池。`sediment: on` 则按需打开：

- **规范全文**：`norms[].ref`
- **反例 / 沉淀池 / 沉淀流程**：`mounts.anti_patterns` / `mounts.backlog` / `mounts.sediment_process`

门禁：`node docs/skill-references/kuikly-ios-coding/scripts/check_skill.mjs`。
