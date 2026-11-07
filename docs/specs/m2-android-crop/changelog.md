# M2 Android 透视出图 · changelog

## 2026-11-07（真机闭环）

- 关 `P1-device-verify`：真机表 #1–3/#5 pass；#4 拖不到非法四点记 N/A（手势夹紧）
- 修结果预览空白：`PlatformImage.saveToFile` 返回 `file://…`（`KRImageAdapter` 不认裸绝对路径）
- Demo 状态行附带 path，便于确认 URI
- M2 checklist / milestones README 标完成；r2 结论改通过

## 2026-11-07（复审整改）

- 关 `P1-doc-drift`：更正 `CropPoint` KDoc 为「当前显示图像素空间」+ Q17 出图对齐说明
- 关 `P2-dispatcher-io`：`crop`/`cropToFile` 改 `Dispatchers.IO`
- 关 `P2-demo-config-reset`：Demo 用 `controller.config.copy(rectMode=…)`，避免冲掉其它旋钮
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：审查整改｜待批 0（已达标 0）

## 2026-11-07

- 落盘 decisions Q16–Q22、ADR 0003、CONTEXT（CropException / PerspectiveCropAndroid）、L3 perspective_crop_api §8–10
- 实装 `CropException`、`CropProcessor` Android（先旋源图再 setPolyToPoly）/ ios·js stub
- `PerspectiveCropController.crop` / `cropToFile()` / `cropToFile(path)`；Config `outputFormat`/`jpegQuality`
- `PerspectiveCropAndroid.init`；`KRApplication.onCreate` 接线；Demo「确认裁切」+ 失败文案 + 结果预览
- androidApp 增加对 `:KuiklyPerspectiveCrop` 依赖以便宿主 init
- 真机验证表：`docs/reviews/device-check-list/m2-android-crop.md`（由审核员产出；本单含真机验收）
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：纯业务实现，契约已定，无新规范/反例/冲突写法｜待批 0（已达标 0）
