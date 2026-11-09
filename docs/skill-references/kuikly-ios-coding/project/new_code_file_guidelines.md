doc_version: 20261109_1
layer: L3

# 新建代码文件准则 · iOS（L3）

> **本意**：按落点选语言——KMP/`iosMain` 用 Kotlin；壳 UI 优先 **Swift**；须对接既有 ObjC Kuikly 壳或 C ABI 时用 **Objective-C**（`.m`/`.h`）；ObjC++ 仅在确需时用 `.mm` 并 changelog 留痕。

**级别**：强制。

## 规则

1. **语言落点**
   - 库逻辑 / expect·actual → **Kotlin**（`iosMain` / `iosArm64` 等）
   - `iosApp` 新页面 / 新 SwiftUI·UIKit 扩展 → **Swift**（`.swift`）
   - 扩展既有 `HR*` / `Kuikly*` ObjC 类、或必须进 Bridging Header 的 API → **Objective-C**
   - 无理由新建 `.mm` → **FAIL**（须 changelog 写明为何需要 ObjC++）
2. **文件头**：新建业务源文件含日期、作者、一句话职责（Kotlin 三字段或 Swift/ObjC 等价注释）。
3. **前缀**：新建 ObjC 类保持仓内前缀惯例，禁止裸 `ViewController` 式全局名。
4. **迁移不是目标**：只约束新建。

## 判据速查

| 场景 | 结论 |
|---|---|
| 新库逻辑放 `.swift` 却本可 actual | **FAIL** → Kotlin |
| 新壳页面强行 `.m` 无互操作理由 | **FAIL** → Swift |
| 无理由 `.mm` | **FAIL** |
