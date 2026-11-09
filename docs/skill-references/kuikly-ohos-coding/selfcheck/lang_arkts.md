# 自检清单 · lang_arkts（L1）

> 全文 SoT：`docs/skill-references/coding-norms/base/lang_arkts.md`。

| # | 条款 | 什么算 `OK`（判据） |
|---|---|---|
| T1 | 命名风格 | 类 UpperCamel；变量/方法 lowerCamel；常量 UPPER_SNAKE；无拼音标识符 |
| T2 | 缩进与行宽 | 2 空格、无 Tab、行宽≤120（例外已注释） |
| T3 | 控制流大括号 | if/for/while 均有 `{}` |
| T4 | 禁滥用 ESObject | 非跨语言路径未标注 ESObject |
| T5 | 数组类型 `T[]` | 未新增 `Array<T>` 写法 |
| T6 | 访问修饰符 | 新增类成员有明确 private/protected/public（或说明沿用文件既有风格） |
