doc_version: 20261109_1
layer: L2

# 鸿蒙（HarmonyOS / OpenHarmony）平台规范（L2）

> **范围**：鸿蒙应用与 NDK 通用硬约束（线程、Ability 生命周期、Node-API、Native 图形/图像资源）。本文件属 **可移植 L2**。  
> **来源**：[NDK 开发概述](https://gitcode.com/openharmony/docs/blob/OpenHarmony-5.0.2-Release/zh-cn/application-dev/napi/ndk-development-overview.md)、[Node-API 开发指导](https://gitcode.com/openharmony/docs/blob/OpenHarmony-5.0.2-Release/zh-cn/application-dev/napi/napi-guidelines.md)、ArkUI / Stage 模型官方指南、OH Image / Drawing NDK 头文件契约。未列条目默认跟官方；冲突以本文件为准。冲突序 **L3 > L2 > L1**。

## 硬约束

### 1. 线程与 UI

- **耗时操作不得堵 UI 线程**：解码、大图变换、磁盘编解码、重计算须在 Worker / TaskPool / 后台或 KN 侧异步路径完成。
- **仅 UI 线程改 ArkUI 状态与自定义组件绘制**；后台结果切回 UI 后再 `invalidate` / 改 `@State` / Canvas 绘制。
- 短同步工作可留在 UI 线程；可能掉帧或 ANR 的一律下沉。

### 2. Ability / 组件生命周期

- Ability / 页面销毁后**不再更新 UI**、不再投递会泄漏的 NAPI / 定时器回调。
- 长任务须可取消或在 `onDestroy` / 对应 teardown 停止；单例勿持有 Ability 实例。
- 需要 UI 上下文的 API 用正确的 UI Context；长生命周期对象只用 application / 显式注入的目录句柄（如本仓 `resfileDir` / `cacheDir`）。

### 3. Node-API（NAPI）

- 每个 `napi_*` 调用检查状态；参数个数与类型先校验，失败早退（返回 `nullptr` 或抛 JS 异常，**禁止**半初始化对象外泄）。
- `napi_env` / `napi_value` **不得跨线程缓存后直接用**；跨线程须按官方异步工作（AsyncWork）或等价安全机制。
- 字符串：先查长度再分配缓冲；与 KN `@CName` 互操作时注意指针生命周期（缓冲须在被调方读完前有效）。
- 导出符号与 `Init` 注册表保持同步；删除/改名 NAPI 函数必须同步 ArkTS 调用方。

### 4. Native 图像 / Drawing 资源

- `OH_PixelmapNative` / `OH_ImageSourceNative` / Drawing `Bitmap`/`Canvas`/`Matrix`/`PixelMap`：**谁创建谁释放**；`try/finally` 或等价路径保证 Release / Destroy / Dissolve。
- Drawing 包装 `OH_Drawing_PixelMapGetFromOhPixelMapNative` 后必须 `PixelMapDissolve`；源 `OH_PixelmapNative` 另按所有权 `Release`。
- **像素布局写清再解包**：`GetArgbPixels` → 字节序 **A,R,G,B**；Drawing `BGRA_8888` 位图内存 → **B,G,R,A**。禁止混用两套假设（会导致紫蓝等通道错乱）。
- **禁止**调用已知不可用/桩实现的编码路径（本生态：`OH_ImagePackerNative_*` 真机 SIGSEGV）→ 用已验证的软件落盘或其他官方可用 API。
- 出图失败抛明确错误（对齐业务 `CropException` 口径），**禁止静默回原图**。

### 5. 路径与资源

- `assets://` / rawfile / resfile 不得当成普通沙箱绝对路径直接 `fopen`；须经宿主注入的 `resourceDir` 解析或官方资源 API。
- 写缓存须用注入的 `cacheDir`（或官方 cache 接口）；目录未 init 时**显式失败**，禁止默默写到错误位置。

## 判据速查

| 场景 | 结论 |
|---|---|
| UI 线程大图 warp / 同步 PNG 编码 | **FAIL** → 下沉后台或已验证的短路径并评估耗时 |
| Ability 销毁后仍改自定义 View | **FAIL** |
| NAPI 不查 argc/类型 / 跨线程乱用 env | **FAIL** |
| PixelMap / Drawing 对象缺 Release | **FAIL** |
| BGRA 按 ARGB 字节解包 | **FAIL** |
| 走 ImagePacker Native 编码 | **FAIL**（本生态禁） |
| `assets://` 未 init 当文件路径 | **FAIL** |
| ArkTS 命名 / C++ 命名风格 | L1 `lang_arkts` / `lang_cpp` |

## 非本文件

- ArkTS 风格 → L1 [`lang_arkts.md`](lang_arkts.md)
- C / C++ 语言 → L1 [`lang_c.md`](lang_c.md) / [`lang_cpp.md`](lang_cpp.md)
- 透视裁剪公开 API → 各项目 L3 `perspective_crop_api`
