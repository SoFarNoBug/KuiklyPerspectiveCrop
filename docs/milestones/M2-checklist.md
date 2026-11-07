# M2 — Android 透视出图

> 依赖 [M1](./M1-checklist.md) 验收通过。总览见 [README](./README.md)。决策见 [decisions.md](../decisions.md) Q1/Q5′/Q6 与 **Q16–Q22**。

## 非目标（本里程碑）

- 不做 iOS / JS / 鸿蒙真实透视实现（stub，明确失败）
- 不做放大镜采样缓存、辅助线（→ M3；仍属 v0.1）
- 不做 OCR / 边缘检测 / autoScan 算法

## 文档

- [x] README「出图」小节与双 API 签名一致（可与 M3 对接章合并，本里程碑至少有 API 说明）
- [x] 非法四点 / stub 失败行为写进 KDoc 或 README 一句
- [x] 本清单一项项勾完（真机验收后勾）

## 工程 · Processor

- [x] `CropProcessor` Android：`setPolyToPoly`（或等价）透视裁切
- [x] 像素旋转与 M1 UI 旋转度数对齐（`rotateClockwise` 出图正确）— 真机通过
- [x] `canCrop()` / 四点合法性
- [x] 其它端 actual：**明确失败**（`CropException` + code，禁止静默假成功）

## 工程 · 出图 API

- [x] `suspend fun crop(): PlatformImage`
- [x] `suspend fun cropToFile(...): String`（返回可再设为 `imageSrc` 的路径；Android 为 `file://`）
- [x] `PlatformImage`：common 不暴露 Bitmap；可 `saveToFile`
- [x] 解码 / 变换在后台；回调用方协程上下文再改 UI 状态（见 L2 Kuikly Compose）

## 工程 · Controller

- [x] 出图入口挂到 Controller；与 Session 当前四点一致
- [x] 出图前走 `canCrop()`；非法点拒绝并保持可预期

## Demo

- [x] 「确认裁切」→ 展示结果图（`PlatformImage` 落盘后再 `Image(src)`，或等价）— 真机通过
- [x] 非法四点时按钮可点 + 文案提示 code/message（Q22）— 代码路径具备；真机拖拽触发 **N/A**（手势夹紧拖不到非法）

## 验收命令

```bash
./gradlew :KuiklyPerspectiveCropCore:compileDebugKotlinAndroid :perspectiveCropDemo:compileDebugKotlinAndroid :androidApp:compileDebugKotlin
```

## 验收标准

1. 合法四点 Android 出图成功；非法四点 `canCrop()==false` 且不出脏图
2. `cropToFile` 结果可再作为 `imageSrc` 显示
3. 非 Android 调用失败原因可区分（stub）
4. 上述 Gradle 任务通过

## 备注

- 出图像素坐标系必须与 M1 Fit 映射、旋转状态一致；漂移优先查映射而非只怪 Matrix
- M2 已定：**Q16–Q22**（`CropException`+code、先旋源图再透视、`cropToFile`/`path`、Config 导出格式、`PerspectiveCropAndroid.init`、边长定尺寸、Demo 文案展示失败）；见 [decisions.md](../decisions.md) / [ADR 0003](../adr/0003-crop-exception-init-rotate-then-poly.md)
- **真机 2026-11-07**：见 [device-check-list/m2-android-crop.md](../reviews/device-check-list/m2-android-crop.md)；#4 N/A 不阻塞闭环
