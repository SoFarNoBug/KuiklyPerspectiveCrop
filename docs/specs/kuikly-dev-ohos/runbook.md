# 鸿蒙壳冒烟（进行中 2026-11-09）

## 结论

- **已打通（macOS）**：`./ohos_build.sh` → `libshared.so` → 签名 HAP → 真机 arm64 安装启动。
- **放大镜（策略 A）**：KN `MagnifierBlitStore` + NAPI `getMagnifierBlit` + ArkTS `KRPerspectiveCropMagnifier` Canvas 圆镜；采样经 OH Image NDK。
- **裁切**：`Auto`/`Native` → Drawing `SetPolyToPoly`；`Common` → CPU 单应；落盘均为软件 PNG → `file://`；须 `PerspectiveCropOhos.init(resfileDir, cacheDir)`。

## 构建（macOS）

```bash
# 仓库根；DevEco 默认路径 /Applications/DevEco-Studio.app
./ohos_build.sh
```

产物：

- `ohosApp/entry/libs/arm64-v8a/libshared.so`
- `ohosApp/entry/src/main/cpp/libshared_api.h`
- `ohosApp/entry/src/main/resources/resfile/crop_sample.webp`

HAP：

```bash
# 仓库根
./ohosApp/runOhosApp.sh
```

## 签名（必做一次）

1. DevEco Studio 打开 `ohosApp`
2. File → Project Structure → Signing Configs → 勾选 **Automatically generate signature**
3. 保存后 `build-profile.json5` 的 `signingConfigs` 会写入 `~/.ohos/config/default_…`
4. 再跑 `./ohosApp/runOhosApp.sh`

## 工程注意

| 项 | 说明 |
|---|---|
| Gradle 入口 | `settings.ohos.gradle.kts` + 各模块 `build.ohos.gradle.kts` |
| Kotlin | `2.0.21-KBA-010`；coroutines 用 `1.8.0-KBA-002`（ohos_arm64） |
| Kuikly Maven | `getKuiklyOhosVersion()` = `2.30.0-2.0.21-ohos` |
| render HAR | `@kuikly-open/render@2.30.0` |
| `ohosApp/local.properties` | gitignore；`kuikly.compilePluginEnabled=false` |
| `PerspectiveCropOhos.init` | NAPI `setPerspectiveCropOhosDirs(resourceDir, cacheDir)` |
| 放大镜 View | `KuiklyViewDelegate` 注册 `KRPerspectiveCropMagnifier` |
| 放大镜 NAPI | `getMagnifierBlit`；`@CName` blit snapshot 在 Demo shared |
| 裁切 | `Auto`/`Native` → Drawing `SetPolyToPoly`；`Common` → CPU 单应；落盘 **软件 PNG**（不走 ImagePacker Native，避免 SIGSEGV） |
| 链接库 | `image_source_ndk` / `pixelmap_ndk` / `native_drawing`（解码/放大镜/Native 出图） |

## 能力边界

- **可验**：home / Demo / Lab UI、框选手势、原生圆镜、`cropToFile` 再显示、Lab 出图引擎切换（Native/Auto vs Common）。
- **不可当完成**：ImagePacker Native 编码；ArkUI Matrix4 出图。
