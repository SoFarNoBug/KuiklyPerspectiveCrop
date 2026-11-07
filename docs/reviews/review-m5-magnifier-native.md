# Review · m5-magnifier-native

- 结论：**打回**
- 依据：Plan「放大镜原生像素贴图（网格仅兜底）」/ Q38 / ADR 0006

## P0

### P0-silent-fail · H5 MagnifierBlitStore 双实例，像素贴图主路径静默失效
- 证据：
  - `h5App/build.gradle.kts`：`implementation(project(":KuiklyPerspectiveCropCore"))`
  - `h5App/.../KRPerspectiveCropMagnifier.kt`：读 `com.jlj.kuiklybase.perspectivecrop.MagnifierBlitStore`
  - `perspectiveCropDemo` 业务包亦依赖 Core，Stage/`PlatformMagnifier.js` 在业务包内 `MagnifierBlitStore.publish`
- 影响：宿主壳与页面 JS 各有一份 `object MagnifierBlitStore`；publish 写 Demo 实例，View 读 h5App 实例 → 圆镜长期无 patch 像素（灰底/空），`platformSupportsNativeMagnifier()==true` 却走「假主路径」。Android 单 ClassLoader 不受影响。
- 处置：必修（打通同实例：Module/桥/挂 `window` 单例，或 H5 暂 `platformSupportsNativeMagnifier=false` 回网格并改 ADR/口径）

## P1

### P1-device-verify · Android 拖动全程清晰 / 几何对齐须真机
- 证据：`KRPerspectiveCropMagnifierView.onDraw`（BitmapShader）；`PlatformMagnifier.android` 用 Compose `containerW/H` + `magnifierCenter` 下发 cx/cy/radius
- 影响：代码无法证明密度单位与原生 View 画布一致，也无法证明拖动无马赛克、摆放与旧库一致
- 处置：必修（可执行档 → [device-check-list/m5-magnifier-native.md](device-check-list/m5-magnifier-native.md)）
- 缺口要点：①拖动中是否连续像素；②cx/cy/radius 与圆镜视觉位置；③TopAutoFlip；④预热前/后

## P2

### P2-compose-impure-publish
- 证据：`PlatformMagnifier.android.kt` / `.js.kt` 在 `@Composable` 体内直接 `MagnifierBlitStore.publish`
- 影响：违反 Compose 纯组合约定；极端重组路径可能多余 publish（一般同帧 UI 线程尚可）
- 处置：建议（`SideEffect` / 显式非组合边界）

### P2-h5-redraw-alloc
- 证据：`KRPerspectiveCropMagnifier.redraw` 每帧 `createElement("canvas")` + 全量填 `ImageData`
- 影响：拖动掉帧风险
- 处置：可选（复用 offscreen canvas / ImageData）

### P2-dead-generation-field
- 证据：`KRPerspectiveCropMagnifierView.lastGeneration` 仅写、无实质使用（空 if）
- 影响：可读性噪音；invalidate 已由 setProp 触发
- 处置：可选（删除字段或用于脏检查）

## 规范候选观察

无。

## 真机

- 可执行档：[device-check-list/m5-magnifier-native.md](device-check-list/m5-magnifier-native.md)
- 代码审不了：拖动清晰度、密度/摆放像素对齐、H5 双实例是否在实际打包形态下合并（若构建已单包则可降级；当前依赖图显示双依赖）

## 对照上轮

（基线审查，无上轮）
