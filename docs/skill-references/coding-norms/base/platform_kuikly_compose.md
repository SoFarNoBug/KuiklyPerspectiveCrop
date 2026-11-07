doc_version: 20261106_1
layer: L2

# Kuikly Compose 平台规范（L2）

> **范围**：Kuikly Compose DSL 跨端硬约束（包名、不可用 API、手势闭包、线程）。本文件属 **可移植 L2**；库契约（`imageSrc` / Painter SPI 等）写本仓 L3，不进本文件。  
> 冲突序 **L3 > L2 > L1**。细则表与示例仍可按需打开 `.cursor/rules/kuiklyComposeDSL.mdc` 作速查。

## 硬约束

### 1. 包名

- **仅** `androidx.compose.runtime.*` 使用官方包。
- `Modifier` / `ui` / `foundation` / `material3` / `animation` 等一律 `com.tencent.kuikly.compose.*`。
- **禁止**用 Jetpack Compose / `androidx.compose.ui.*` 语法顶替 Kuikly Compose。

### 2. 不可用 API（编译失败或静默失效）

- 不用：`OutlinedTextField`、`BasicTextField2`/`TextFieldState`、`PullToRefreshBox`、`ClickableText`、`Icon`/`IconImage`。
- 不用：`Modifier.verticalScroll` / `horizontalScroll` / `scrollable`；原生签名的 `nestedScroll(connection, dispatcher)`；`imePadding`；Lazy 内 `animateItem` / `animateItemPlacement`（Grid 系除外）。
- Lazy 列表须删 `reverseLayout`、`flingBehavior`；NestedScroll 用 Kuikly 双回调签名（见速查手册第八章）。

### 3. 输入与手势

- `TextField` 必须显式传 `onValueChange`；长度限制用 Kuikly `maxLength` / `onLimitChange`，**禁止**在 `onValueChange` 里截断字符串。
- **`pointerInput(key)` 闭包陷阱（P0）**：闭包内读取的可变状态**必须**作为 `key`（或多个 key），否则读到创建时旧值；Lazy 项内尤其致命。

### 4. 线程（Kuikly）

- View / Attr / Event / observable / Module / `setTimeout` 等 **只能在 Kuikly 线程**访问。
- 异步（IO、解码、裁剪）完成后，**必须回到 Kuikly 线程**再改 UI / 响应式状态。
- 动态化场景勿假定 kotlinx 协程可用；本仓 native/KMP 可用 kotlinx，但仍须遵守回线程规则。
- 开发期可开 `Pager.VERIFY_THREAD`（勿在 `verifyFailed` 回调里再碰 UI）。

### 5. 页面与 Module

- Compose 页：`ComposeContainer` + `@Page` + `willInit { setContent { … } }`。
- **`willInit` 过程中不能使用 Module**（尚未注册）；等 Pager 生命周期启动后再调。

## 判据速查

| 场景 | 结论 |
|---|---|
| `import androidx.compose.ui.*` / foundation / material3 | **FAIL** → 改 Kuikly 包名 |
| 使用上表不可用组件 / Modifier | **FAIL** → 按替代方案改写 |
| `pointerInput` 闭包读可变状态但未入 key | **FAIL** → 补 key 或改读 `State`/`rememberUpdatedState` 等价安全写法 |
| 后台线程直接改 Compose 状态 / 调 Module | **FAIL** → 切回 Kuikly 线程 |
| `willInit` 内调 Module | **FAIL** → 延后到生命周期可用后 |
| 纯算法 / 无 Compose 触达 | **N/A**（本规范） |

## 非本文件

- 透视裁剪公开 API 契约 → 本仓 L3 `perspective_crop_api`
- 完整差异表 / 代码样例 → `.cursor/rules/kuiklyComposeDSL.mdc`（速查，非 norms 代数）
- 传统 Kuikly DSL（attr/event）→ 不在本规范范围
