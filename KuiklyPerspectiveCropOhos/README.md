# @jlj/kuikly-perspective-crop-ohos

Kuikly 斜拍透视裁剪的 **鸿蒙** 原生放大镜 View（`KRPerspectiveCropMagnifier`）。  
KMP 算法与 Compose 在 Maven Central；本包只发 ArkTS 壳。

- 包名：`@jlj/kuikly-perspective-crop-ohos`
- 版本：`1.0.0`
- View 名：`KRPerspectiveCropMagnifier`（Kuikly 按名创建，须在 Delegate 注册）
- 依赖：`@kuikly-open/render@2.30.0`
- 配套 Core：`io.github.sofarnobug:KuiklyPerspectiveCropCore:1.0.0-2.30.0`

## 集成

### 1. 安装

```bash
ohpm install @jlj/kuikly-perspective-crop-ohos
```

### 2. 注入 blit NAPI

宿主 so 须导出与 Demo 同形的 `getMagnifierBlit`（像素来自 KN `MagnifierBlitStore`）。在 so 已加载后：

```typescript
import { PerspectiveCropOhosNative } from '@jlj/kuikly-perspective-crop-ohos';
import Napi from 'libentry.so'; // 或你的 native 模块名

PerspectiveCropOhosNative.configure(() => Napi.getMagnifierBlit());
```

另需 `PerspectiveCropOhos.init(resfileDir, cacheDir)`（Kotlin / NAPI），见主仓 README。

### 3. 注册 View

```typescript
import { KRPerspectiveCropMagnifierView } from '@jlj/kuikly-perspective-crop-ohos';

getCustomRenderViewCreatorRegisterMap(): Map<string, KRRenderViewExportCreator> {
  const map = new Map<string, KRRenderViewExportCreator>();
  map.set(KRPerspectiveCropMagnifierView.VIEW_NAME, () => new KRPerspectiveCropMagnifierView());
  return map;
}
```

## 许可

MIT © 2026 jlj
