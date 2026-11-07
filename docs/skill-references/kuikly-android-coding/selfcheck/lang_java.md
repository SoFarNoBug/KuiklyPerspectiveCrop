# 自检清单 · lang_java（L1）

> 全文 SoT：`docs/skill-references/coding-norms/base/lang_java.md`。本文件只摘「条款 + 什么算做到了」，判据细节以全文为准。
> 三态图例见同目录 `README.md`：`OK`（落点行号）/ `TODO`（不做 + 理由）/ `N/A`（一句判据）。

| # | 条款 | 什么算 `OK`（判据） |
|---|---|---|
| J1 | 空指针防护（P0） | 每个"可能为 null"的解引用前有判空/早退/不变量注释：方法参数、返回值、字段、集合元素、`Map.get`、Bundle extra、回调入参 |
| J2 | 公开 API 空值契约 | 新公开方法的可空参数/返回值在 Javadoc 或 `@Nullable`/`@NonNull` 写明；null 语义变更已同步 Kotlin 调用方 |
| J3 | "没有"返回空集合 | 列表/集合无数据返回空集合；**禁止返回 null 列表** |
| J4 | 禁裸长链 | `a.getB().getC()` 已拆局部变量逐步判空，或注释说明为何必然非空 |
| J5 | 字符串比较 | `"x".equals(s)` 或 `Objects.equals(a, b)`，无 `s.equals("x")` |
| J6 | 禁空 catch | 无空 catch；捕获后至少日志或明确降级，且保留 cause |
| J7 | 不可变：`final` **强制** | 本单新增/修改的局部变量与字段，凡只赋一次均已加 `final`（含 for-each 变量）。**强制项不接受 `TODO`**：只许 `OK`（落点行号）或 `N/A`（"本单无任何变量声明"这类判据）。既有未触及代码不回填 |
| J8 | 集合接口化 | 对外返回只读视图/拷贝、不返回内部 `ArrayList` 引用；变量类型写 `List`/`Map`/`Set` |
| J9 | 可见性与覆盖 | 能不 `public` 就不 `public`；所有重写方法带 `@Override` |
| J10 | 命名 | 包全小写无下划线；类 `UpperCamel`；方法/非常量字段 `lowerCamel`；常量 `SCREAMING_SNAKE` |
| J11 | equals/hashCode/toString | 覆写 `equals` 必同时覆写 `hashCode`；`toString` 未用于业务协议解析 |
| J12 | 异常与资源 | 不吞异常；可关闭资源用 try-with-resources |
| J13 | 类成员顺序 | 常量 → 字段 → 构造 → 方法；**重载相邻**；相关逻辑放一起 |

## 搬运/抽方法时的附加要求

提方法、内联、整段复制时，被搬动的那段要**重新过一遍 J1–J13**（不得以"原本就这样"豁免），并把发现的既有缺陷按 SKILL §搬运登记（`carried-over:`）写进 changelog 或当轮修掉。
