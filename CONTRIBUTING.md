# Contributing · KuiklyPerspectiveCrop

本文件给**本仓维护者**：怎么起 Demo、文档与决策入口在哪。  
库接入用法见根目录 [README.md](README.md)。

## 模块速览

```text
KuiklyPerspectiveCrop/
├── KuiklyPerspectiveCropCore/   # 发布库
├── perspectiveCropDemo/         # Demo 共享页（不叫 shared）
├── androidApp / iosApp / h5App / ohosApp
├── CONTEXT.md                   # 术语表
├── README.md                    # 接入者文档
└── docs/                        # milestones / adr / specs / reviews …
```

## 术语与决策

| 文档 | 用途 |
|---|---|
| [CONTEXT.md](CONTEXT.md) | 领域术语 SoT |
| [docs/decisions.md](docs/decisions.md) | 已定决策台账（含 Q 编号） |
| [docs/adr/](docs/adr/) | 难逆转架构决策 |
| [docs/milestones/](docs/milestones/README.md) | M0–M5 清单与 v0.1 定义 |
| [docs/context/file-map.md](docs/context/file-map.md) | 路径地图 |

## Demo 起服（最短）

建议 **JDK 17**（可用 `.tools/jdk-17.0.2`）。

### Android

```bash
./gradlew :androidApp:installDebug
```

### H5（浏览器冒烟）

本仓用**精简 8083 静态服**提供业务包 `nativevue2.js`（**无** Kuikly 官方 whistle）。宿主 webpack 仍在 8080。

前置：JDK 17；`node` 在 PATH。若 `packLocalJsBundleDebug` 因 GitHub 拉 Yarn 超时：先跑 `node static_server/fetch-yarn-ivy.js`（腾讯镜像写入 `%USERPROFILE%\.gradle\caches\manual-yarn\ivy`；根 `build.gradle.kts` 已改用该地址）。

必留：`h5App/webpack.config.d/output.js`（IIFE，防 `h5App.js` UMD 覆盖 `window.com`）；`index.html` **不要**默认同步挂会超时的 libpag CDN。

```bash
# 终端 1：业务 JS 包 + 8083 静态服
./gradlew :perspectiveCropDemo:packLocalJsBundleDebug
node static_server/serve/index.js

# 终端 2：样例图 + 宿主 webpack（持续跑）
./gradlew :h5App:copyAssetsToWebpackDevServer
./gradlew :h5App:jsBrowserDevelopmentRun
```

打开：`http://localhost:8080/?page_name=home` →「透视裁剪」入口。  
改 Demo 后重跑 `packLocalJsBundleDebug` 并重启 8083；改 `webpack.config.d` 后必须重启 webpack；改 `assets` 后重跑 `copyAssetsToWebpackDevServer`。

| pageName | 角色 |
|---|---|
| `home` | 入口 |
| `perspective_crop_demo` | 最小实现 |
| `perspective_crop_lab` | 全能 Lab |

完整拓扑与白屏排障以 runbook 为准：**[docs/specs/kuikly-dev-h5/runbook.md](docs/specs/kuikly-dev-h5/runbook.md)**。

### 其它端

| 端 | 入口 |
|---|---|
| Android 工程约定 | [docs/specs/kuikly-dev-android/](docs/specs/kuikly-dev-android/README.md) |
| 鸿蒙起服 / 排障 | [docs/specs/kuikly-dev-ohos/runbook.md](docs/specs/kuikly-dev-ohos/runbook.md) |
| iOS 起服 / 排障 | [docs/specs/kuikly-dev-ios/runbook.md](docs/specs/kuikly-dev-ios/runbook.md) |

## 审查与真机表

- [docs/reviews/](docs/reviews/README.md)
- 真机勾选：`docs/reviews/device-check-list/`

## 发布到 Maven Central（别人可直接依赖）

模块：`:KuiklyPerspectiveCropCore` → `io.github.sofarnobug:KuiklyPerspectiveCropCore:0.0.1-2.30.0`。  
鸿蒙（`settings.ohos.gradle.kts`）本阶段不发。

```bash
CENTRAL_USERNAME=… CENTRAL_PASSWORD=… GPG_KEY_PASSWORD=… ./publish-maven.sh
```

消费方只需 `mavenCentral()` + README 里的 `implementation(...)`。

## 发布到 CocoaPods（iOS 原生放大镜）

源：`KuiklyPerspectiveCropIOS/` + 根 `KuiklyPerspectiveCrop.podspec`（`0.0.1`）。  
Demo 经 symlink 引用同文件；`perspectiveCropDemo` framework **export** Core 以便 Swift 见桥。

```bash
# 需已 commit + 打 tag 0.0.1 并 push 到 GitHub
pod trunk push KuiklyPerspectiveCrop.podspec
```

## 编码规范（AI / 人工）

技能与自检素材在 [docs/skill-references/](docs/skill-references/README.md)；Cursor skills 在 `.cursor/skills/`。
