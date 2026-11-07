# M5 · 放大镜原生像素贴图

- 决策：Q38；ADR [0006](../../adr/0006-magnifier-native-blit.md)
- 主路径：Android `BitmapShader` + H5 Canvas 像素贴图
- 兜底：N×N `drawRect` 网格（自定义画家 / 未实装平台）
- changelog：[changelog.md](./changelog.md)
