# 自检清单 · new_code_file_guidelines（L3 · 鸿蒙）

> 全文 SoT：`docs/skill-references/kuikly-ohos-coding/project/new_code_file_guidelines.md`。

| # | 条款 | 什么算 `OK`（判据） |
|---|---|---|
| N1 | 语言落点正确 | 库→Kotlin；壳 UI/Module→`.ets`；NAPI→`.cpp`；无理由 `.c` 已留痕 |
| N2 | 文件头 | 新建文件含日期/作者/职责（Kotlin 三字段或其它语言等价头） |
| N3 | 头文件保护 | 新建 `ohosApp` `.h` 用 `#ifndef`，非 `#pragma once` |
