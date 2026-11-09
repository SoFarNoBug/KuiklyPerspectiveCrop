# 自检清单 · lang_c（L1）

> 全文 SoT：`docs/skill-references/coding-norms/base/lang_c.md`。

| # | 条款 | 什么算 `OK`（判据） |
|---|---|---|
| C1 | 后缀与纯度 | `.c` 无 C++ 语法；头为 `.h` |
| C2 | 头文件保护 | `#ifndef` 保护；无 `#pragma once` |
| C3 | 命名 | 函数 UpperCamel；变量 lowerCamel；全局 `g_`；宏 UPPER_SNAKE |
| C4 | 定宽整数 | 新接口用 `stdint` 类型，无随意 typedef 基本类型 |
| C5 | 指针与堆 | malloc 判空；free 后置 NULL；输入长度/索引已校验 |
| C6 | 返回值检查 | 新调用的可能失败 API 有检查或显式忽略理由 |
