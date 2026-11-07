# H5 冒烟跑通经验（已验证 2026-11-07）

## 结论

本仓 H5 可冒烟：**双进程 + 双端口**；业务包走精简 Node 静态服（**不用** Kuikly 官方 whistle / npm serve）。  
**M4** 起 JS 实装真像素放大镜与 CPU 透视裁剪（`blob:` 落盘）；见 [M4-checklist](../../milestones/M4-checklist.md) / Q33–Q37。

## 拓扑

```text
浏览器
  └─ http://localhost:8080/          ← h5App webpack-dev-server（宿主：h5App.js + index.html + assets）
         │  <script> 拉取
         └─ http://127.0.0.1:8083/nativevue2.js
                                       ← static_server/serve（业务 KMP 包，CORS *）
```

| 端口 | 产物 | 谁生成 / 谁托管 |
|---|---|---|
| **8080** | `h5App.js`、`index.html`、样例图 | `:h5App:jsBrowserDevelopmentRun` + `copyAssetsToWebpackDevServer` |
| **8083** | `nativevue2.js`（多 MB） | `:perspectiveCropDemo:packLocalJsBundleDebug` + `node static_server/serve/index.js` |

`index.html` 脚本顺序必须是：**先** `nativevue2.js`，**再** `h5App.js`。中间不要插会挂起的同步 CDN。

## 标准步骤

前置：JDK 17（可用 `.tools/jdk-17.0.2`）；`node` 在 PATH（可用 Cursor 自带 helpers）。

```bash
# 若 pack 因 GitHub 拉 Yarn 超时（一次即可）
node static_server/fetch-yarn-ivy.js

# 终端 1：业务包 + 8083
./gradlew :perspectiveCropDemo:packLocalJsBundleDebug
node static_server/serve/index.js

# 终端 2：宿主
./gradlew :h5App:copyAssetsToWebpackDevServer
./gradlew :h5App:jsBrowserDevelopmentRun
```

打开：`http://localhost:8080/?page_name=home` → 应见「透视裁剪」与两个入口按钮。

| 改了什么 | 重跑什么 |
|---|---|
| Demo / Core common 或 js 逻辑 | `packLocalJsBundleDebug`，并**重启** 8083（或确认 serve 读到新文件） |
| `h5App` 宿主 / `webpack.config.d` | 重启 `jsBrowserDevelopmentRun`（config.d **不会**热更新） |
| `src/.../assets` 样例图 | `copyAssetsToWebpackDevServer` |
| 仅 `index.html` | 一般 `jsProcessResources` 后刷新即可；不保险时重启 webpack |

## 工程约定（务必保留）

1. **`h5App/webpack.config.d/output.js`（方案 X）**  
   把 `h5App.js` 打成 **IIFE**，禁止 UMD 整 key 覆盖 `window.com`。  
   多模块下 `nativevue2` 挂的是 `com.tencent.kuikly.core.nvi.*`，`h5App` 挂的是 `…render.web.*`；UMD 后加载会整块抹掉 `nvi` → `registerCallNative` 失败 → 白屏。  
   官方说明见 Kuikly `h5App/README.md`「多模块工程下 UMD 全局命名空间被覆盖」。备选方案 Y（深合并）本仓未启用；**勿与 X 同时开**。

2. **`static_server/serve`**  
   零 npm 依赖的纯 `http` 服；按候选目录找够大的 `nativevue2.js`；对 JS 回 `Access-Control-Allow-Origin: *`。不要为冒烟再引回 whistle。

3. **`index.html` 不要默认同步挂 libpag CDN**  
   CDN 超时会卡住后续 `h5App.js`（白屏）。需要 PAG 时再加，并保证可访问或改本地/async。

4. **Yarn / Node 下载**  
   根 `build.gradle.kts` 指向 `%USERPROFILE%\.gradle\caches\manual-yarn\ivy`；GitHub 不通时先跑 `fetch-yarn-ivy.js`（腾讯镜像）。

5. **Windows + 未装 CocoaPods**  
   `gradle.properties` 可设 `kotlin.native.ignoreDisabledTargets=true`，避免无关 native 目标拖死配置。

6. **协程调度**  
   common 里重活用 `Dispatchers.Default`，**不要**写 `Dispatchers.IO`（JS target 无 IO）。  
   `warmCache` / `process` 为 **`suspend`**（Q34）；须在协程内调用。

7. **`assets://` 解码（Q36）**  
   库内 JS 解码把 `assets://crop_sample.webp` 映射为 `/assets/crop_sample.webp`（须先 `copyAssetsToWebpackDevServer`）。跨域图无 CORS 会 `DECODE_FAILED`。

8. **`cropToFile` / `saveToFile`（Q37）**  
   H5 返回 **`blob:`** ObjectURL（非持久文件）；刷新或 revoke 后失效。

## 白屏排障（本仓踩过）

| 现象 | 先查 | 处理 |
|---|---|---|
| `#root` 空、无 UI | Network：`libpag` / 外链是否 Pending | 去掉或本地化阻塞脚本；保证 `h5App.js` 能执行 |
| Console：`registerCallNative` / `nvi` undefined | `h5App.js` 是否仍为 UMD（头尾含 `webpackUniversalModuleDefinition`） | 确认存在 `webpack.config.d/output.js` 并**重启** webpack |
| 8080 200 但仍空 | 8083 的 `nativevue2.js` 是否 200、体积是否数 MB | 重跑 pack；看 serve 启动日志解析到的目录 |
| pack 卡在 Yarn | GitHub 超时 | `node static_server/fetch-yarn-ivy.js` 后再 pack |
| 改了 config.d 无效 | 旧 webpack 进程仍在 | 杀 8080 占用进程后重跑 `jsBrowserDevelopmentRun` |

快速自检（PowerShell 思路）：

- `http://127.0.0.1:8080/`、`/h5App.js`、`http://127.0.0.1:8083/nativevue2.js` 均 200  
- `h5App.js` **不含** `webpackUniversalModuleDefinition`  
- headless / 真浏览器 DOM：`#root` 下出现业务节点（标题「透视裁剪」等）

## 能力边界（避免误验）

- **可验（M4）**：home / Demo / Lab；拖角真像素放大镜；确认裁切 → `blob:` 再显示；`CropException` 失败面。  
- **仍非目标**：iOS 真采样/出图（stub）；WebGL 加速；OCR / autoScan。  
- Android 真机验收仍以 M2/M3 device-check-list 为准。
