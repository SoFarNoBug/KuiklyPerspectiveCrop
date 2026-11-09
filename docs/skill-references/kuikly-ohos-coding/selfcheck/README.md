# 规范自检清单（kuikly-ohos-coding / selfcheck/）

> 编码收尾**按命中的 `norms[].id` 打开同名清单**，逐行填三态：`OK` / `TODO` / `N/A`。
> 全文判据以 `norms[].ref` 为准。

## 怎么用

1. **按改动面选清单**（见 Skill §规范全文路由）：
   - 任意鸿蒙业务改动 → 基线 `platform_ohos`
   - `.ets` → + `lang_arkts`
   - `.c` → + `lang_c`
   - `.cpp` / 应用侧 `.h` → + `lang_cpp`
   - `ohosArm64` / 共享 Kotlin → + `lang_kotlin`
   - Compose UI → + `platform_kuikly_compose`
   - 库公开 API → + `perspective_crop_api`
   - 新建文件 → + `new_code_file_guidelines`
2. 清单有 N 行，回报就有 N 行，**禁止缺行**。
