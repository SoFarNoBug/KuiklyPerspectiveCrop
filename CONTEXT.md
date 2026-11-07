# KuiklyPerspectiveCrop

斜拍照片上框选文档或物体，裁剪并做透视校正的 Kuikly Compose 库。

## Language

**PerspectiveCropStage**:
对外可组合入口：底图 `Image(src)` 之上绘制选区叠层并处理手势。
_Avoid_: 非本仓命名的历史 View 控件名

**PerspectiveCropController**:
选区与出图的命令入口（设图、旋转、`crop` / `cropToFile`）。
_Avoid_: Player, ViewModel（除非宿主自己包一层）

**imageSrc**:
待裁原图的字符串地址（相机 `filePath`、相册 `uri`、assets 路径等），供预览与解码共用。
_Avoid_: Bitmap（作为公开设图类型）, ImageBitmap（作为出图契约）

**PlatformImage**:
出图结果的跨端句柄；common 不暴露平台位图类型，可 `saveToFile` 得到可再显示的 src。
_Avoid_: 在 common API 中直接返回 Bitmap / UIImage

**CropSession**:
选区状态机（四点、命中、凸性 / 矩形模式约束），与 UI 框架无关的纯逻辑。
_Avoid_: 把几何判定写进 Composable 闭包

**CropProcessor**:
按四点做透视裁切 / 旋转的可替换策略；Android 先实装，其它端可 stub。
_Avoid_: 把平台策略类名泄漏到 common 公开面

**CropException**:
出图失败的公开异常；用 `code` + `message`（可带 `cause`）细分原因，禁止静默回原图或可空假成功。
_Avoid_: 用 null / Result 改写已定双 API 非空返回面

**PerspectiveCropAndroid**:
Android 宿主初始化入口（`init(application)`），供解码与缓存目录使用；不绑定业务 Application 类名。
_Avoid_: 在库内直接依赖 Demo 的 `KRApplication`

**CropOverlayPainters（六层 SPI）**:
可分别注入的叠层绘制扩展点：**蒙版 / 框线 / 角点 / 边中点 / 辅助线 / 放大镜**；库提供默认实现，业务皮肤只进 Demo。可有门面聚合，但分层必须可单独替换。
_Avoid_: 非本仓 Drawer 前缀；把换皮收成「只能整包换一个 Painter」当唯一扩展方式

**rectMode**:
矩形模式：角点拖动保持轴对齐矩形；默认关闭，自由凸四边形为主路径。

**edgeSnapToTouch / edgeClampToBounds**:
边拖吸附手指、边拖越界钳制；**默认开启**，保留开关。自由模式下 clamp 开=两端位移取交集齐步；关=越界整帧丢弃。
_Avoid_: 写成「仅 rectMode 才有钳制」或拖到后期才暴露字段；clamp 关时两端各自 coerceIn 导致斜边

**touchEdgeProjectionError**:
边命中沿边投影外延（视图逻辑像素，默认 100）；≤0 为严格线段。
_Avoid_: 配成 Android dp；与 contentInset 混为一谈

**showMagnifierCropLineMirror**:
放大镜圆内再画选区框线镜像；**默认开**；样式复用框线色宽。
_Avoid_: 旧名 need2DrawInsideAreaLineMirror；只改 Compose 兜底而 Android 原生圆镜不画

**透视校正**:
将斜拍四边形选区变换为正面矩形图的出图过程（非 OCR、非边缘检测）。
_Avoid_: 扫描识别, deskew（仅旋转校正）

**MagnifierSampler**:
放大镜降采样采样器（expect/actual）；Android 建最长边受限的采样缓存并裁 patch，其它端 stub。
_Avoid_: 每帧全分辨率解码；依赖残缺 ImageBitmap.readPixels

**MagnifierPatch**:
Stage 交给放大镜 Painter 的像素块 + 触点几何（非平台 Bitmap 公开类型）。
_Avoid_: 把 Android Bitmap 挂到 common 公开 API

**unhitPassthrough**:
DOWN 未命中选区手柄时透传父容器手势；**默认 false**。
_Avoid_: 默认改成 true 或当作「仅 Demo」行为

**writePoints / applyAutoScanPoints**:
写入四点选区；亦作为外部 autoScan **注入钩子**（库内无边缘检测算法）。
_Avoid_: 在库内实现 OCR / 边缘检测 / 内置 autoScan

已定决策全文见 [docs/decisions.md](docs/decisions.md)。

