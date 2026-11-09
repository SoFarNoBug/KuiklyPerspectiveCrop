# 自检清单 · platform_ohos（L2）

> 全文 SoT：`docs/skill-references/coding-norms/base/platform_ohos.md`。

| # | 条款 | 什么算 `OK`（判据） |
|---|---|---|
| O1 | 耗时不堵 UI 线程 | 解码 / 大图变换 / 编解码 / 重计算不在 UI 线程同步长跑 |
| O2 | 仅 UI 线程改 ArkUI | 后台结果切回 UI 后再改状态 / 自定义 View / Canvas |
| O3 | 销毁后不更新 UI | 异步完成有生命周期守卫或明确不涉及 Ability/页面销毁 |
| O4 | NAPI 参数与状态检查 | 新/改 NAPI 查 argc、类型、`napi_*` 状态；失败早退 |
| O5 | NAPI env 不跨线程乱用 | 未把 env/value 缓存到异线程直接回调 |
| O6 | Native 图像/Drawing 成对释放 | PixelMap / Bitmap / Canvas / Matrix / Dissolve 路径完整 |
| O7 | 像素字节序正确 | BGRA vs GetArgbPixels 假设与注释一致；无通道对调 |
| O8 | 禁 ImagePacker Native | 未引入 `OH_ImagePackerNative_*` 落盘 |
| O9 | 路径经 init 解析 | assets/resfile/cache 经注入目录；未 init 显式失败 |
