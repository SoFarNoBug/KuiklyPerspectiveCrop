doc_version: 20261109_1
layer: L3

# 新建代码文件准则 · 鸿蒙（L3）

> **分层**：本文件为规范**全文真源**。编码会话红线摘要见 `.cursor/rules/kuikly-dev-ohos.mdc`；流程见 Skill。勿与 Rule 双写完整条文。  
> **本意**：按落点选语言——KMP/`ohosArm64` 用 Kotlin；壳 UI / Module 用 ArkTS（`.ets`）；NAPI 桥用 C++（`.cpp`/`.h`）；确需纯 C 再用 `.c`。

**级别**：核心规范（强制）。违反 = 该次编码不合格，须整改后再进 Review。

## 规则

1. **按层选语言（禁止图省事串层）**
   - 库逻辑 / expect·actual → **Kotlin**（`ohosArm64Main` 等）
   - `ohosApp` ArkUI 页面、自定义组件、Module → **ArkTS `.ets`**
   - Node-API 注册与 C++ 互操作 → **`.cpp` + `.h`**
   - 仅当必须与纯 C ABI / 既有 `.c` 工具链对齐时新建 `.c`，并在 changelog 写明理由
2. **文件头**：新建业务源文件须带创建信息
   - Kotlin：`@CreateDate` / `@Author` / `@Description`
   - ArkTS / C / C++：文件头注释含日期、作者、一句话职责（可与既有壳文件风格对齐）
3. **头文件保护**：`ohosApp/**/cpp` 的 `.h` 用 `#ifndef` 保护（见 L1 `lang_cpp`）；KN cinterop 最小头例外见该文。
4. **迁移不是目标**：不要求顺手改写既有文件语言；只约束**新建**。

## 判据速查

| 场景 | 结论 |
|---|---|
| 新库逻辑放 `.ets` | **FAIL** → Kotlin actual / common |
| 新自定义 View 放 `.cpp` | **FAIL** → `.ets` |
| 新 NAPI 导出放 `.ets` | **FAIL** → `.cpp` + 注册表 |
| 无理由新建 `.c` | **FAIL** → `.cpp` 或 changelog 留痕 |

## 自检（提交前）

- 新增扩展名与落点表一致；新建文件有文件头。
- 新增 `.h`（ohosApp）非 `#pragma once`。
