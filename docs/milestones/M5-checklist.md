# M5 checklist · 放大镜原生像素贴图

> 决策：**Q38**；ADR [0006](../adr/0006-magnifier-native-blit.md)。

## 完成项

- [x] Q38 / ADR 0006 落文档
- [x] Android `MagnifierBlitStore` + `KRPerspectiveCropMagnifierView`（BitmapShader）
- [x] Stage：`PlatformMagnifierOverlay` 主路径；网格仅兜底；停指升密仅兜底
- [x] androidApp 注册 `KRPerspectiveCropMagnifier`
- [x] H5 暂退网格兜底（关双 Store P0；已撤 h5App 注册；Canvas 贴图另单）
- [x] 编译通过（Android + JS/h5App）
- [x] 真机拖动全程清晰（表：[m5-magnifier-native](../reviews/device-check-list/m5-magnifier-native.md)；结案 [r4](../reviews/review-m5-magnifier-native-r4.md)）
