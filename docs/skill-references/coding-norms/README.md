# 编码规范三层货架（可移植）

> 本目录是 **L1 语言级 / L2 平台级** 规范全文的**多项目共享货架**（可整包拷贝）；**L3 项目级** 由各仓自建（如某仓码农 skill 的 `content_root/project/`）。  
> 决策：[ADR 0038](../../adr/0038-coding-norms-three-layer.md)。

## 术语

| 词 | 含义 |
|---|---|
| **L1 语言级** | `coding-norms/base/lang_<lang>.md`；语言惯用法；**跨项目共享**，不含单仓政策 |
| **L2 平台级** | `coding-norms/base/platform_<platform>.md`；平台通用约束；**跨项目共享** |
| **L3 项目级** | 贴合单仓业务/技术栈；在各 skill 的 `project/` 下，**不进本共享目录** |
| **规范入口** | 被某 skill `project.yaml` → `norms[]` 引用的 md（`id` / `title` / `layer` / `ref`）；计入该 skill 代数 `vN` |
| **layer** | `L1` / `L2` / `L3`；写在 `norms[].layer` 与全文文首；门禁交叉验路径/文件名前缀 |
| **doc_version** | 单份规则文首的份级版本：`YYYYMMDD_N`；与 skill `_version.txt` 的 `vN` **双轨** |
| **skill 代数** | `_version.txt` / Skill `description` 的 `vN`，等于已启用 `norms.length` |

## 目录约定

- L1/L2 **统一**放在 [`base/`](base/)，用文件名前缀区分：`lang_*` = L1，`platform_*` = L2（**不分** `lang/`、`platform/` 子目录）。
- L3 仍在各 skill 的 `project/`。

## 冲突优先级

**L3 > L2 > L1（项目优先）**：同主题冲突时以项目级为准。

## doc_version 规则

- 文首（标题上或 YAML 头）写：`doc_version: YYYYMMDD_N`（例 `20261021_1`）
- **正文任意改动都升号**（含错别字）：同日多次 → `_N+1`；跨日首次 → 新日期 `_1`
- 不替代 skill 代数；门禁两者都查

## 共享入口（L1 / L2 · 随本目录分发）

| layer | 文件 | 典型 norms id |
|---|---|---|
| L1 | [`base/lang_kotlin.md`](base/lang_kotlin.md) | `lang_kotlin` |
| L1 | [`base/lang_java.md`](base/lang_java.md) | `lang_java` |
| L1 | [`base/lang_arkts.md`](base/lang_arkts.md) | `lang_arkts` |
| L1 | [`base/lang_c.md`](base/lang_c.md) | `lang_c` |
| L1 | [`base/lang_cpp.md`](base/lang_cpp.md) | `lang_cpp` |
| L2 | [`base/platform_android.md`](base/platform_android.md) | `platform_android` |
| L2 | [`base/platform_ohos.md`](base/platform_ohos.md) | `platform_ohos` |
| L2 | [`base/platform_kuikly_compose.md`](base/platform_kuikly_compose.md) | `platform_kuikly_compose` |

未用到的语言/平台**不建空壳**。各仓在自己的 `project.yaml` → `norms[]` 写明 `layer:` 与 `ref`。

## 消费方示例（kuikly-android-coding · L3 不共享）

| layer | 文件 | norms id |
|---|---|---|
| L3 | `../kuikly-android-coding/project/new_code_file_guidelines.md` | `new_code_file_guidelines` |
| L3 | `../kuikly-android-coding/project/perspective_crop_api.md` | `perspective_crop_api` |

> 本仓 `kuikly-android-coding` **未挂载** `lang_java`（纯 Kotlin/KMP）。`base/lang_java.md` 仍留在共享货架，供需要 Java 的仓挂载。

## 消费方示例（kuikly-ohos-coding · L3 不共享）

| layer | 文件 | norms id |
|---|---|---|
| L3 | `../kuikly-ohos-coding/project/new_code_file_guidelines.md` | `new_code_file_guidelines` |
| L3 | `../kuikly-android-coding/project/perspective_crop_api.md` | `perspective_crop_api`（与 Android 码农共用全文） |

> 本仓 `kuikly-ohos-coding` 挂载 `lang_arkts` / `lang_c` / `lang_cpp` / `platform_ohos`，并按改动面附加 `lang_kotlin` / `platform_kuikly_compose`。

## 移植到其他仓

1. 拷贝 `docs/skill-references/coding-norms/`（至少 `base/` 下要用的 L1/L2）。
2. 目标仓 `project.yaml` → **`norms[]`** 挂载启用的 L1/L2；`version_counts: norms` 时不必再挂 `mounts.rules`。
3. L3 在目标仓自建 `content_root/project/`，**勿照搬**他仓包名/业务政策。
4. 跑目标仓门禁至 FAIL 0。
