# 规范自检清单（kuikly-ios-coding / selfcheck/）

> 编码收尾按命中的 `norms[].id` 打开同名清单，逐行三态：`OK` / `TODO` / `N/A`。

## 怎么用

1. 任意 iOS 业务改动 → 基线 `platform_ios`
2. `.swift` → + `lang_swift`
3. `.m` / `.mm` / ObjC `.h` → + `lang_objc`
4. `iosMain` Kotlin → + `lang_kotlin`
5. Compose → + `platform_kuikly_compose`
6. 库公开 API → + `perspective_crop_api`
7. 新建文件 → + `new_code_file_guidelines`
