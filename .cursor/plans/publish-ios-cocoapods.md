# Plan · iOS CocoaPods 发布（KuiklyPerspectiveCrop）

> 用户选 **2**：先计划、确认后再编码。  
> 对齐姊妹仓 **KuiklyEcho**：`KuiklyEchoIOS` + `pod trunk push`；KMP 层已在 Maven Central，本单只补 **iOS 原生壳**。

## 目标 / 非目标

| 目标 | 非目标 |
|---|---|
| 外人可 `pod 'KuiklyPerspectiveCrop'` 拿到放大镜原生 View | 不重发 Maven（Core 已 `0.0.1-2.30.0`） |
| Demo `iosApp` 仍能起服、放大镜可用 | 本单不发鸿蒙 HAR / ohpm |
| README / CONTRIBUTING 写清 iOS 接入 | 不改裁剪算法与 Compose API |

## 背景（为何不能只 push 现成 Swift）

- `KRPerspectiveCropMagnifier.swift` 在 `iosApp/`，且 `import perspectiveCropDemo`。
- `PerspectiveCropIosMagnifierBridge` 在 **Demo** `iosMain`，不在 Core。
- Echo 的 `KREchoModule` 只依赖 `OpenKuiklyIOSRender`、不 import KMP；本库放大镜必须读 Core 里的 `MagnifierBlitStore`，发布形态要多一步「桥进 Core + 壳可独立成 pod」。

## 方案（推荐）

对齐 Echo 目录，并解决「Swift 不能写死 import Demo」：

```text
KuiklyPerspectiveCrop/
├── KuiklyPerspectiveCropCore/          # 已 Maven；本单：桥迁入 iosMain
├── KuiklyPerspectiveCropIOS/           # 新建：Swift 壳 + podspec
│   ├── KRPerspectiveCropMagnifier.swift
│   └── KuiklyPerspectiveCrop.podspec
├── KuiklyPerspectiveCrop.podspec       # 可选：根目录转发（对齐 Echo 根 podspec）
└── iosApp/                             # Demo：Podfile 改指本地 IOS 目录；删壳内重复 Swift
```

### 步骤

1. **桥迁 Core**  
   - 将 `PerspectiveCropIosMagnifierBridge` 从 `perspectiveCropDemo/.../iosMain` 挪到  
     `KuiklyPerspectiveCropCore/src/iosMain/.../perspectivecrop/`  
   - 包名改为 `com.jlj.kuiklybase.perspectivecrop`（与 `MagnifierBlitStore` 同包）。  
   - Demo 侧删除旧文件（或薄 `typealias` 一轮后删净，优先直接删 + 改引用）。

2. **新建 `KuiklyPerspectiveCropIOS/`**  
   - 迁入（或复制后删原文件）`KRPerspectiveCropMagnifier.swift`。  
   - `import OpenKuiklyIOSRender` + **`import KuiklyPerspectiveCropCore`**（见下「模块名」）。  
   - 仍用 `@objc(KRPerspectiveCropMagnifier)`，类名与 `viewName()` 契约不变。

3. **CocoaPods 模块名 / Demo 联调**  
   - Demo 继续 `pod 'perspectiveCropDemo', :path => ...`（内嵌 Core 符号）。  
   - 本地联调两种选一（计划默认 **A**）：  
     - **A（推荐）**：Podfile 增加  
       `pod 'KuiklyPerspectiveCrop', :path => '../KuiklyPerspectiveCropIOS'`；  
       Swift 改为经 **ObjC 可见 API** 调桥（见步骤 4），避免 Swift `import` 绑死 Demo 框架名。  
     - **B**：Swift `import perspectiveCropDemo` 仅留 Demo；trunk 包另用 Core 模块名——双份难维护，不采用。

4. **Swift ↔ Kotlin 解耦（相对 Echo 的增量）**  
   - 在 Core 保证 `PerspectiveCropIosMagnifierBridge` 对 ObjC/Swift 导出稳定（KN 默认导出 public object）。  
   - 壳内 Swift **优先**写：  
     `PerspectiveCropIosMagnifierBridge.shared()`（或生成名以编译为准），  
     由 **App 已链接的 KN framework**（Demo 的 `perspectiveCropDemo` 或消费方 `shared`）提供实现。  
   - 若 `import KuiklyPerspectiveCropCore` 在「仅有 Demo framework」下编不过：壳只保留 `@objc` View + 对桥的调用，**不**写死 import 业务 framework；用与 Echo 相同思路——运行时类已在进程内（Kuikly 按类名创建 View；桥在 KN 里）。实现时以 **本机 `pod install` + 模拟器编译** 为准二选一落地，changelog 记最终写法。

5. **podspec**（对齐 Echo）  
   - `name`: `KuiklyPerspectiveCrop`  
   - `version`: `0.0.1`（与 Maven 主版本对齐；CocoaPods 不用 `0.0.1-2.30.0` 后缀，避免 hyphen 坑；README 注明与 Core Maven 版本配套）  
   - `source`: `git@github.com:SoFarNoBug/KuiklyPerspectiveCrop.git` + tag  
   - `source_files`: `KuiklyPerspectiveCropIOS/**/*.swift`（或目录内相对路径）  
   - `platform`: iOS 15.0（与现 Podfile 一致）  
   - `dependency`: `OpenKuiklyIOSRender`  
   - `license`: MIT（根 `LICENSE`）  
   - 根目录可放一份转发 podspec（Echo 有根 `KuiklyEcho.podspec`），`source_files` 指到子目录。

6. **Demo 收尾**  
   - 删除 `iosApp/.../KRPerspectiveCropMagnifier.swift`（改由 pod 提供）。  
   - `pod install`；Xcode 编过；放大镜冒烟（runbook 级：圆镜有贴图即可）。

7. **发布**  
   - 提交并打 tag（如 `0.0.1` 或 `ios-0.0.1`，与 podspec.version 一致）。  
   - `pod trunk push KuiklyPerspectiveCrop.podspec`（或 IOS 目录内 spec）。  
   - 前置：`pod trunk me` 已登录；未登录则 `pod trunk register`。  
   - 验证：`pod search KuiklyPerspectiveCrop` / trunk 页可见。

8. **文档**  
   - README：iOS 节补  
     `pod 'KuiklyPerspectiveCrop'` + 仍需 Maven Core + `PerspectiveCropIos.init`。  
   - CONTRIBUTING：发布节补 CocoaPods 命令（与现 Central 脚本并列）。  
   - `docs/specs/kuikly-dev-ios/changelog.md` 或 `ios-native-crop/changelog` 一行。

## 验收

- [x] Core 含桥；Demo 无重复桥源文件  
- [x] `KuiklyPerspectiveCropIOS` + podspec 存在；iosApp **symlink** 同 Swift（path-pod 因无法 import Demo 放弃）  
- [x] Demo 模拟器 `BUILD SUCCEEDED` + launch home（2026-11-10）  
- [ ] `pod trunk push`：待 commit + tag `0.0.1` + 网络；`pod trunk me` 曾 Net::OpenTimeout  
- [x] README / CONTRIBUTING 已补 CocoaPods 接入

## 风险 / 决策点（确认计划时请拍板）

| # | 项 | 默认 |
|---|---|---|
| D1 | Pod 名 | `KuiklyPerspectiveCrop`（与库名一致；Echo 同名策略） |
| D2 | Pod version | `0.0.1`（配套 Maven `0.0.1-2.30.0`） |
| D3 | Swift 与 KN 链接 | 优先「进程内 KN 提供桥、壳不 import Demo」；编不过再退 `import` 本仓 framework 名并文档约定 |
| D4 | 根 podspec vs 仅子目录 | 根 + `KuiklyPerspectiveCropIOS/` 双份路径对齐 Echo |
| D5 | git tag / trunk push | 编码通过后由你说「执行 push」再打 tag+trunk（或计划确认时授权一次做完） |

## 非本单

- 鸿蒙 ohpm  
- 升 Maven 版本号  
- 改 Android `KRPerspectiveCropMagnifierView` 位置

---

确认本计划后回复 **执行** / **按计划执行**（或改 D1–D5）。确认前不改业务代码。
