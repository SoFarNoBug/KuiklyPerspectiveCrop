# interaction-parity · changelog

## 2026-11-07 · P2 blit viewPoints 拷贝

- [MODIFY] `MagnifierBlitStore.viewPointsXy`：`copyOf()` 返回，避免别名写穿
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：琐碎封装｜待批 0（已达标 0）

## 2026-11-07 · 自由边钳制 / 镜内框线 / 投影容差

- [MODIFY] `CropGeometry`：`clampEdgeTranslate` / `bothEndsInside` / `edgeHitDistance`
- [MODIFY] `CropSession.moveEdge`：自由模式接 Q9 钳制开/关语义（齐步饱和 / 整帧丢弃）
- [MODIFY] `CropSession.hitTest`：`touchEdgeProjectionError` 视图 px → 图像外延
- [MODIFY] `PerspectiveCropConfig`：`touchEdgeProjectionError`（默认 100）、`showMagnifierCropLineMirror`（默认 true）
- [MODIFY] Android `MagnifierBlitStore` + `KRPerspectiveCropMagnifierView`：镜内框线镜像
- [MODIFY] `DefaultMagnifierOverlayPainter`：兜底路径同步画镜线
- [MODIFY] `PlatformMagnifierOverlay`：传入 `viewPoints`
- [MODIFY] Lab：Gesture「投影·默认/关」、Magnifier「镜线·开/关」
- [MODIFY] `CONTEXT.md` / `docs/decisions.md` Q9 澄清
- 非本单：`contentInset*`、角点 grab、catch/minCrop 默认
- 真机验证表路径（审核产出）：`docs/reviews/device-check-list/interaction-parity.md`
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：对齐旧库手感与加旋钮，无新规范/反例/冲突写法｜待批 0（已达标 0）
