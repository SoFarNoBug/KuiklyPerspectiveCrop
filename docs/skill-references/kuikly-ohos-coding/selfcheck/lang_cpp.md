# 自检清单 · lang_cpp（L1）

> 全文 SoT：`docs/skill-references/coding-norms/base/lang_cpp.md`。

| # | 条款 | 什么算 `OK`（判据） |
|---|---|---|
| P1 | 后缀 | 实现 `.cpp`、头 `.h` |
| P2 | 头文件保护 | `ohosApp` 侧 `#ifndef`；未对应用 cpp 用 `#pragma once` |
| P3 | 命名 | 类型/函数 UpperCamel；变量 lowerCamel；全局 `g_`；类成员尾 `_` |
| P4 | 格式 | 4 空格、行宽≤120、控制流有 `{}` |
| P5 | RAII / 释放 | 新资源有明确所有权；裸 delete 后置 nullptr |
| P6 | 转换与位运算 | 无 C 风格强制转换滥造；位运算用无符号类型 |
| P7 | 异常边界 | NAPI/`extern "C"` 边界无异常穿透 |
