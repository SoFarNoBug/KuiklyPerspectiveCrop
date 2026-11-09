doc_version: 20261109_1
layer: L1

# C 语言规范（L1）

> **范围**：纯 C（`.c` / 与 C 兼容的 `.h`）惯用；**不含**鸿蒙 NDK 平台政策（→ L2 `platform_ohos`）。本文件属 **可移植 L1**。  
> **来源**：ISO C11 惯用实践；命名/头文件与 [OpenHarmony C++ 编程规范](https://gitee.com/openharmony/docs/blob/master/zh-cn/contribute/OpenHarmony-cpp-coding-style-guide.md) 中可复用于 C 的条款对齐（文件后缀、头文件保护、整数类型）。未列条目默认跟 ISO；冲突以本文件为准。冲突序 **L3 > L2 > L1**。

## 硬约束

### 文件与头文件

- 实现用 **`.c`**，头文件用 **`.h`**；勿把 C++ 语法写进 `.c`。
- 头文件必须用 **`#ifndef` / `#define` / `#endif` 保护**；**禁止 `#pragma once`**（与 OpenHarmony 规范一致）。
- 头文件只放声明；避免在头文件定义有外部链接的对象/函数（`static inline` 等有意例外须注释）。

### 命名

- 类型（`struct` / `enum` / `typedef`）：`UpperCamelCase` 或项目既有 `snake` 前缀风格二选一，**单文件内统一**；本仓新代码倾向 `UpperCamel` + `typedef struct Foo Foo`。
- 函数：`UpperCamelCase`（动宾）；与系统/POSIX 对接的回调可保留其既有风格。
- 变量 / 参数：`lowerCamelCase`；**全局变量**加 `g_` 前缀。
- 宏、枚举值：`UPPER_SNAKE_CASE`。
- 固定宽度整数优先 `<stdint.h>`：`int32_t` / `uint32_t` / `size_t` 等，禁止随意 `typedef int MyInt`。

### 安全与资源

- 所有外部输入（长度、索引、指针）使用前校验；失败早退。
- `malloc`/`calloc` 结果判空；释放后指针置 `NULL`。
- 禁止对有符号类型做位运算依赖；位运算用无符号类型。
- 字符串操作核对其目标缓冲大小；优先有界 API（`snprintf` 等）。
- 错误码 / 返回值必须检查；禁止静默忽略。

### 格式

- 缩进 **4 空格**；行宽 ≤ **120**。
- 控制流必须加大括号；开括号不换行。

## 判据速查

| 场景 | 结论 |
|---|---|
| `.c` 内出现 `class` / `new` / 引用 | **FAIL** → 改 `.cpp` 或去掉 C++ |
| 头文件只用 `#pragma once` | **FAIL** → `#ifndef` 保护 |
| 全局变量无 `g_` | **FAIL** |
| `malloc` 不判空 / free 后悬空用 | **FAIL** |
| NAPI env / 业务 Ability | **L2 platform_ohos** |

## 非本文件

- C++ 命名（成员后缀 `_`）与现代 C++ → L1 [`lang_cpp.md`](lang_cpp.md)
- Node-API、线程、PixelMap → L2 [`platform_ohos.md`](platform_ohos.md)
