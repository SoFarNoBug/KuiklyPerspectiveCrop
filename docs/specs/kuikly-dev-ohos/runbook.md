# 鸿蒙壳冒烟（进行中 2026-11-07）

## 结论

- **已打通**：`-c settings.ohos.gradle.kts` → `libshared.so`（`sharedLib("shared")`）→ `entry/libs/arm64-v8a`；hvigor `assembleHap` 产出 unsigned HAP；Core 含 `ohosArm64` stub。
- **当前阻塞**：本机模拟器为 **x86_64**，Kuikly/本仓只产 **arm64-v8a** so → `hdc install` 报 ABI 不匹配（code 9568347）。需 **arm64 真机** 或 ARM 系统镜像模拟器。

## 构建

```bat
:: 仓库根
set DEVECO_SDK_HOME=D:\DevEco_Studio_26.0\sdk
ohos_build.bat
```

产物：

- `ohosApp/entry/libs/arm64-v8a/libshared.so`
- `ohosApp/entry/src/main/cpp/libshared_api.h`

HAP：

```bat
cd ohosApp
ohpm install --all
:: DevEco node + hvigor
hvigorw --mode module -p module=entry@default -p product=default -p buildMode=debug assembleHap
```

建议在 DevEco 勾选 **自动签名**，否则只有 `entry-default-unsigned.hap`（部分环境仍可装，但真机通常要 signed）。

## 安装（arm64 设备）

```powershell
cd ohosApp
.\runOhosApp.ps1
```

包名：`com.jlj.kuiklyperspectivecropui`，Ability：`EntryAbility`。

## 工程注意

| 项 | 说明 |
|---|---|
| Gradle 入口 | `settings.ohos.gradle.kts` + 各模块 `build.ohos.gradle.kts` |
| Kotlin | `2.0.21-KBA-010`；coroutines 用 `1.8.0-KBA-002`（ohos_arm64） |
| Kuikly Maven | `getKuiklyOhosVersion()` = `2.30.0-2.0.21-ohos` |
| render HAR | `@kuikly-open/render@2.30.0`（与短版本对齐） |
| 裁切/放大镜 | ohos stub → `UNSUPPORTED_PLATFORM` / 空圆 |

## 能力边界

- **可验（有 arm64 设备后）**：home / Demo / Lab UI、框选手势。
- **不可当完成**：真透视出图、真像素放大镜（需后续 ohos actual）。
