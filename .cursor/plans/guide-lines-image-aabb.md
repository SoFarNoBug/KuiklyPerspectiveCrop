# 辅助线对齐 freequadcrop（整图 Fit 九宫格）

## 依据

用户确认：**A**（与 `DefaultQuadCropGuideLineDrawer` 一致：在图片 Fit 显示矩形上画横平竖直九宫格）+ **先计划后编码**。

旧库参考：[`DefaultQuadCropGuideLineDrawer.kt`](d:\code_space\lib_feature_freequadcrop\lib_feature_freequadcropview\src\main\java\com\quin\freequadcropview\drawer\guideline\DefaultQuadCropGuideLineDrawer.kt) — 用 `realLeft/Top/Width/Height`，不用 `cropPoints`。

本仓现状：[`DefaultGuideOverlayPainter`](d:\code_space\KuiklyPerspectiveCrop\KuiklyPerspectiveCropCore\src\commonMain\kotlin\com\jlj\kuiklybase\perspectivecrop\overlay\DefaultCropOverlayPainters.kt) 沿选区四边 1/3 透视插值，斜框时线会「歪」。

## 目标行为

- `showGuideLines=true` 时：在 **Image Fit 内容矩形**（`ImageFitMapping.contentLeft/Top/W/H`）上画 2 竖 + 2 横（1/3、2/3）
- 线始终轴对齐，不随选区变形
- 绘制顺序不变：mask → guide → border → …

## 改动

1. **SPI** [`CropOverlayPainters.kt`](d:\code_space\KuiklyPerspectiveCrop\KuiklyPerspectiveCropCore\src\commonMain\kotlin\com\jlj\kuiklybase\perspectivecrop\overlay\CropOverlayPainters.kt)  
   `GuideOverlayPainter.drawGuides` 增加图片内容矩形参数（建议 `contentLeft/Top/Width/Height: Float`，与旧库四参对齐）。  
   仅默认实现与 Stage 调用点；Demo `LabSkinPainters` 仍挂默认 guide，无自定义实现要改。

2. **默认画家** `DefaultGuideOverlayPainter`  
   删透视 lerp；按 freequadcrop 公式：
   - `widthStep = contentW/3`，`heightStep = contentH/3`
   - 竖线：`contentLeft + step` / `+ 2*step`，y 从 `contentTop` 到 `contentTop+contentH`
   - 横线：对称  
   仍读 `guideLineColor` / `guideLineWidth` / `showGuideLines`。

3. **Stage** [`PerspectiveCropStage.kt`](d:\code_space\KuiklyPerspectiveCrop\KuiklyPerspectiveCropCore\src\commonMain\kotlin\com\jlj\kuiklybase\perspectivecrop\PerspectiveCropStage.kt)  
   `mapping != null` 时把 `contentLeft/Top/W/H` 传入 `drawGuides`。

4. **文档**  
   - append `docs/specs/m3-magnifier/changelog.md`（或新建小 feature changelog；优先挂 m3，因辅助线属 M3）  
   - 若 README / decisions 写过「三分线插值」→ 改成「整图 Fit 九宫格（对齐 freequadcrop）」  
   - 可选：真机表一句断言更新（码农不写 reviews；执行后若需 Review 再挂）

## 非目标

- 不改 mask / border / 其它层 SPI  
- 不做选区 AABB 方案（用户已否 B）  
- 不改 Config 字段名

## 验收

- Lab 开「辅助线」：见横平竖直九宫格，覆盖整张 Fit 图（含蒙层外缘可见部分）
- 拖角改变选区：辅助线**不跟着斜变**（相对整图位置不变）
- 关辅助线：无线
- `:androidApp:compileDebugKotlin` 或 `:perspectiveCropDemo:compileDebugKotlinAndroid` 通过

## 执行时

走 `kuikly-android-coding` **按计划行事**：先读 SKILL + norms（`lang_kotlin` / `platform_android` / `platform_kuikly_compose` / `perspective_crop_api`），再改码；收尾 changelog + §实现回报。
