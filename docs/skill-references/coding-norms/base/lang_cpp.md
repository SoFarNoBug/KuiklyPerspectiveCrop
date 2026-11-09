doc_version: 20261109_1
layer: L1

# C++ 语言规范（L1）

> **范围**：C++（`.cpp` / `.h`）风格与安全惯用；**不含**鸿蒙 NDK / NAPI 平台政策（→ L2 `platform_ohos`）。本文件属 **可移植 L1**。  
> **来源（双轨，冲突时 OpenHarmony 风格优先于泛化 Core Guidelines）**：  
> 1. [OpenHarmony C++ 语言编程规范](https://gitee.com/openharmony/docs/blob/master/zh-cn/contribute/OpenHarmony-cpp-coding-style-guide.md)（命名、格式、头文件、资源）  
> 2. [C++ Core Guidelines](https://isocpp.github.io/CppCoreGuidelines/CppCoreGuidelines)（RAII、所有权、禁止滥用例外）  
> 未列条目默认跟上述来源；冲突序 **L3 > L2 > L1**。

## 硬约束

### 文件与头文件

- 实现 **`.cpp`**，头 **`.h`**；文件名与主类型一致时用 **下划线小写**（`napi_init.cpp` 等壳入口可保留既有名单文件名）。
- 头文件保护用 **`#ifndef` / `#define` / `#endif`**；**禁止 `#pragma once`**（OpenHarmony 规则）。
- **例外（本仓）**：Kotlin/Native **cinterop 最小头**（`ohosInterop/include/*_min.h`）可继续 `#pragma once`，因其非 OH 应用 Native 模块源码树；**`ohosApp/**/cpp` 禁止该例外**。

### 命名（OpenHarmony）

| 类别 | 风格 |
|---|---|
| 类 / 结构体 / 枚举类型 / 命名空间 | `UpperCamelCase` |
| 函数（含成员） | `UpperCamelCase` |
| 变量 / 参数 | `lowerCamelCase` |
| 全局变量 | `g_` + lowerCamel |
| 类成员变量 | lowerCamel + **尾下划线**（`fileName_`） |
| struct/union 成员 | lowerCamel（**无**尾下划线） |
| 宏 / 枚举值 / 全局 `const` 基本量 | `UPPER_SNAKE_CASE` |

### 格式

- 缩进 **4 空格**；行宽 ≤ **120**；控制流必须 `{}`；开括号同行（K&R）。
- 优先 `<cstdint>` 定宽类型；少对基本类型乱 `typedef`。

### 资源与现代 C++（OH + Core Guidelines）

- **优先 RAII**（智能指针、容器、`std::vector`）；避免裸 `new`/`delete` 散落；`delete` 后立即置 `nullptr`。
- 所有权清晰：接口用引用表示非空借用；可空才用指针并判空。
- 禁止 C 风格强制转换；用 `static_cast` / `reinterpret_cast`（后者仅位型/互操作且旁注）。
- 位运算只用**无符号**类型。
- 异常：NAPI 边界与 `extern "C"` 互操作层**不要让异常穿透**；内层可按项目策略使用，但须在边界吞并转错误码。
- 修改第三方 / 生成代码时**保持对方风格**（OH 例外条款）。

## 判据速查

| 场景 | 结论 |
|---|---|
| `ohosApp` 头文件 `#pragma once` | **FAIL** |
| 类成员无尾 `_` / 全局无 `g_` | **FAIL** |
| 裸 `new` 无配对释放 / 无 RAII | **FAIL** |
| 有符号类型做位掩码 | **FAIL** |
| NAPI 参数未校验 / UI 线程 | **L2 platform_ohos** |

## 非本文件

- 纯 C → L1 [`lang_c.md`](lang_c.md)
- Node-API、Ability、Drawing/PixelMap 生命周期 → L2 [`platform_ohos.md`](platform_ohos.md)
