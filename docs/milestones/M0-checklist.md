# M0 — 空架与文档

> 验证完本清单全部项后，才进入 [M1](./M1-checklist.md)。总览见 [README](./README.md)。

## 非目标（本里程碑）

- 不实现几何触控、出图、放大镜

## 文档

- [x] 根目录 `CONTEXT.md` 术语表
- [x] `docs/decisions.md`（Q1–Q15 锁定 SoT；2026-11-07 补齐）
- [x] `docs/adr/0001-preview-src-compose-crop-dual-api.md`
- [x] `docs/adr/0002-kmp-demo-scaffold.md`
- [x] `docs/milestones/README.md` + 本清单（含 v0.1=M0–M3）
- [x] 根 `README.md` / `AGENTS.md` 指向里程碑与 decisions

## 工程

- [x] 多端壳就绪：库 `:KuiklyPerspectiveCropCore` + Demo `:perspectiveCropDemo` + 各端 App
- [x] Kuikly 版本 `2.30.0`（坐标 `2.30.0-2.1.21`）
- [x] 库公开占位：`PerspectiveCropStage` / `Controller` / `Config` / `PlatformImage`
- [x] Demo：`home` → `perspective_crop_demo` 可路由到空 Stage
- [x] iOS Pod / h5 `businessPathName` 等路径与 Demo 模块名一致
- [x] 库模块仅透视裁剪相关源码/资源（抽查 `KuiklyPerspectiveCropCore/`：`perspectivecrop` 包）
- [x] `:androidApp:assembleDebug` 或下列 compile 任务通过（本机 JDK 17 + `local.properties` → `sdk.dir`）

## 验收命令

```bash
# JDK 17+，工程根 local.properties: sdk.dir=...
./gradlew :KuiklyPerspectiveCropCore:compileDebugKotlinAndroid :perspectiveCropDemo:compileDebugKotlinAndroid :androidApp:compileDebugKotlin
```

## 验收标准

1. 上述 Gradle 任务 **BUILD SUCCESSFUL**
2. 安装 Demo 后能打开首页并进入「透视裁剪演示」，看到 Stage 占位文案
3. 库模块目录名/包名与 README 一致

## 备注

- 本机 Android Studio JBR 25 与 Gradle 8.5 / 当前 Kotlin 不兼容；用 JDK 17 编译（可用 `.tools/jdk-17.0.2`，已 gitignore）
- AI norms / `.cursor` 配置**不属于**本里程碑验收范围
