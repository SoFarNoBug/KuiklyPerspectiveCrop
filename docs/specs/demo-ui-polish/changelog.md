# demo-ui-polish changelog

## 2026-11-07

- Demo 产品向布局：共用 `DemoChrome`（顶栏 / 底栏 / Toggle / 成功·失败浮窗）；`perspective_crop_demo` 与 `perspective_crop_lab` 改顶栏+Stage+底栏；Lab 分组 Tab（外观/手势/放大镜）；Home 文案「产品演示 / 全能配置」
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：纯 Demo UI 布局，无新规范/反例/冲突｜待批 0（已达标 0）
- 真机验证：本单无真机验证

## 2026-11-07（H5 文字行盒）

- H5：`lineHeight=fontSize`；按钮竖向 padding 只放 Box（勿打在 Text，否则字贴块顶）；次要底栏按钮改深灰底与 Surface 拉开
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：速速改 H5 观感｜待批 0（已达标 0）

## 2026-11-07（浮窗按钮平分）

- 成功浮窗「再裁一张 / 完成」`Modifier.weight(1f)` 等分；失败「知道了」`fillMaxWidth`
- 治理收尾：门禁 跳过｜三问 否/否/否｜理由：速速改｜待批 0（已达标 0）
