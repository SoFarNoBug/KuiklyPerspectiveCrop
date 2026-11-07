# 规范自检清单（selfcheck/）

> 编码收尾**按命中的 `norms[].id` 打开同名清单**，逐行填三态：`OK`（落点行号）/ `TODO`（不做 + 理由）/ `N/A`（一句判据）。
> 全文判据以 `norms[].ref` 为准；本目录只摘「条款 + 什么算做到了」。

## 怎么用

1. **按改动面选清单**：基线 `lang_kotlin` + `platform_android`；触及 Compose UI / 手势 / 页面 → 加 `platform_kuikly_compose`；改库公开 API / Stage / Session / Processor / Painter → 加 `perspective_crop_api`；新建文件再加 L3（清单文件名 = norm id；`new_code_file_guidelines` 无独立清单时对照全文自检段）。
2. 清单有 N 行，回报就有 N 行，**禁止缺行、禁止留空**。
3. 本仓**不挂** `lang_java`（无 Java 源码）；不要再为业务单填 Java 清单。

## 为何强制「禁缺行」

漏写「为什么不做」会让检查表变成自我证明。强制三态后，未做项必须暴露为 `TODO` 或判据清楚的 `N/A`。
