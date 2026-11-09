# 自检清单 · lang_swift（L1）

> 全文 SoT：`docs/skill-references/coding-norms/base/lang_swift.md`。

| # | 条款 | 什么算 `OK`（判据） |
|---|---|---|
| S1 | 大小写 | 类型 UpperCamel；函数/属性/枚举 case lowerCamel |
| S2 | 使用点清晰 | 新 API 参数标签使调用可读；无含糊基名 |
| S3 | 不可变优先 | 能 `let` 不用 `var` |
| S4 | 可选纪律 | 少用裸 `!`；有则旁注契约 |
| S5 | 文档摘要 | 新建公开 API 有 `///` 一句摘要（或说明为何内部不暴露） |
