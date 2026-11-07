doc_version: 20261106_1

# 编码常见错误反例（references/misuse.md）

> **分层**：本文件为**反例真源**。Rule 只链错误编号；流程见 Skill。勿与 Rule 双写完整条文（[ADR 0021](../../adr/0021-rule-redline-references-sot.md)）。
> 新增反例按「错误 N：一句话」续排，写清 **错误写法 / 为什么错 / 正确写法** 三段。

以下均为真实踩坑形态，判据与本仓通用，务必规避。

---

### 错误 1：新建文件用 Java（本项目新代码优先 Kotlin）

**错误写法**：新增数据类 / 工具类时跟着所在目录的语言走，写成 `.java`。

**为什么错**：本仓 Java 存量占绝大多数，一旦「跟着旁边走」，新文件永远继续用 Java，空安全 / 数据类 / 扩展等表达力永远用不上，Java 生态被反向固化。该约束**只针对新建**，不存在「改老代码被迫大改」的代价。

**正确写法**：新建默认 `.kt`（见 L3 [`project/new_code_file_guidelines.md`](project/new_code_file_guidelines.md)）。会被 Java 调用时：静态入口 `object` + `@JvmStatic`、公开字段 `@JvmField`、带默认参数的方法加 `@JvmOverloads`、Parcelable 手写 `CREATOR`；需要派生判定写成 `fun isEmpty()`（别写成只读属性，Java 会变 `getIsEmpty()`）。确需新建 Java（与该模块既有 Java 强耦合、注解处理器、反射契约等）必须在 changelog 写明理由。

---

### 错误 2：在 `onCreate` 同步调用的 `initView` 里用 `View.post{}` 读兄弟 View 尺寸

**错误写法**：`initView()` 内 `flTop.post(() -> { int w = flEdit.getWidth(); if (w <= 0) return; /* 用 w 定位 */ })`。

**为什么错**：`initView` 在 `onCreate` 内**同步**执行时 DecorView 尚未 attach，`View.post` 的 action 进 `HandlerActionQueue`，会在 `dispatchAttachedToWindow` 时**立刻投递**——早于首帧 measure ⇒ 读到的尺寸恒为 0；`return` 掉之后再无第二次机会。

**正确写法**：用 `viewTreeObserver.addOnGlobalLayoutListener`，**未测量时保留监听**（`if (width <= 0) return` 但不移除），测得后 `if (vto.isAlive) vto.removeOnGlobalLayoutListener(this)` 再消费；若「尺寸就位前不得先显示」，把可见性一并门控在回调内。

---

### 错误 3：用「透明覆盖层 + `layout_constraintGuide_percent`」近似另一个坐标系的 View 边界

**错误写法**：为实现「点击某区域外空白触发 X」，造一块透明盖板，用 `Guideline` 百分比定位其顶边，百分比由运行期**一次性**布局监听算出。

**为什么错**：①**坐标系不同源**——`targetView.getBottom()` 是内容坐标系，`setGuidelinePercent()` 是父容器坐标系，二者只在「目标恰好占满可视高度」时近似相等，目标越矮盖板越往上、反而盖住目标下半部分；②一次性监听 ⇒ 尺寸 / 键盘 / 比例变化后不再更新；③`== 0` 早退会退回 XML 写死值；④盖板在最上层且 `clickable` ⇒ 吞掉从该区域起手的滚动手势。

**正确写法**：让响应域由**布局结构**天然界定——把点击判定挂在**约束链已唯一确定边界**的容器上，目标内触摸由子 View 先消费，其余空白自然冒泡，两条路径互斥、与目标尺寸比例彻底解耦。
★若响应域落在 `ScrollView` 上：标准 `ScrollView.onTouchEvent` 完整覆写、UP 分支从不 `performClick` ⇒ `setOnClickListener` **永不回调**；须用 `OnTouchListener` + `GestureDetector.onSingleTapUp` 判 tap，且 listener **恒返回 `false`**（滚动与 fling 零影响）。

---

### 错误 4：注册过 insets 监听后，仍指望 `fitsSystemWindows` 自动生效

**错误写法**：基类 / 外层容器已注册 `ViewCompat.setOnApplyWindowInsetsListener`，页面继续靠 `android:fitsSystemWindows="true"` 避让系统栏，而监听器里**不补 padding**。

**为什么错**：`View.dispatchApplyWindowInsets` 是**二选一**——存在 `OnApplyWindowInsetsListener` 时就只调 listener，不再走 `View.onApplyWindowInsets`，而 `fitsSystemWindows` 的默认消费正在后者 ⇒ 「自称全出血 + 指望根布局避让」的页面**没有任何一方**留白，顶部内容被状态栏压住。

**正确写法**：注册方必须**复刻**这一消费（`if (root.getFitsSystemWindows()) 按 inset 补 padding`），且**只补页面自称铺满的那几边**；未处理的边用「置 0 后继续下发」的方式交回子容器，避免祖先消费后子 View 拿 0。

---

### 错误 5：Fragment「浮层」加到 `android.R.id.content` 后，仍指望基类做系统栏 / 键盘留白

**错误写法**：页面继承基类（基类已统一做 insets 分流），又在 Fragment 里 `activity.findViewById(android.R.id.content)` 当容器 add 满屏浮层，浮层自身不做任何 insets 处理。

**为什么错**：基类的留白写在 `setContentView(...)` 的那个根布局上；`FragmentTransaction.add(android.R.id.content, …)` 加进来的浮层是**那个根布局的兄弟**，不在其 padding 覆盖面内（`addContentView` / `WindowManager` 挂载同理）⇒ 底部面板被键盘压住、顶部工具条被状态栏压住。

**正确写法**：浮层自己消费 insets——在浮层根布局注册 `ViewCompat.setOnApplyWindowInsetsListener`，把 `systemBars()` / `ime()` **覆盖式**写进「进入时的初始 padding」（禁 `+=`），并按系统版本门控（≤15 系统仍在替页面避让，无条件补会「顶两次」）。★反向自检：凡「基类已经统一处理」的假设，先确认目标 View 是否真在那棵布局树里。

---

### 错误 6：跨模块传「长度 / 补边 / 偏移」类常量时，单位与消费方不一致（点数当毫米）

**错误写法**：SDK / 库侧暴露**打印点数**（如 203dpi 下 `5mm → 40 点`），消费方把它加到**毫米**值上：`length += padDots`。

**为什么错**：量纲不同（1 点 = 0.125mm @203dpi）⇒ 真值 5mm 被当成 40 记，里程 / 上报 size 被**静默**污染（编译、运行、出图全部正常，只有统计与「首次打印」判据被带偏）。本仓是打印类 App，此坑高频。

**正确写法**：跨模块暴露长度类常量时**方法名 + KDoc 必须写明单位**，消费方按同一单位相加；跨单位换算**只在一处**发生（对外给毫米就别再给点数）。★反向自检：任何 `+=` 之前先问「被加的两个值单位一样吗」，并对照基准实现确认口径。

---

### 错误 7：用户可见文案留「布局硬编码 / 第二来源」，或用 `tools:ignore="HardcodedText"` 掩盖

**错误写法**：①布局里直接写 `android:text="中文文案"`；②同一文案在语言表已有 Key、模块里又写一份；③用根布局 `tools:ignore="HardcodedText"` 让 lint 闭嘴；④换个 Key 后不删已失去引用的旧串；⑤需求来了就自造一个 `Key_*` + 自造译文。

**为什么错**：①双份来源在改文案时**必然只改一份**，两处漂移；②`HardcodedText` 是**治理信号**不是噪音，掩盖后同一布局后续新增的硬编码一并免检；③换 Key 后残留旧串是孤儿，读者会误判「仍在用」；④自造 Key 越权（文案归产品 / 设计），且易与已有 Key 形成同文案双来源。

**正确写法**：用户可见文案**单源**——一律引用 `strings.xml` 的 `Key_*`（本仓 `app` / 各模块 `values/`），布局不留副本、不写硬编码。确无对应 Key：**先按中文值 + 词根全量检索**（`grep "中文"` 与 `grep Key_Abc`），确认「零引用的孤儿 Key」也不能漏；确需新增时默认包与已有 `values-*` 同步落值，并在 changelog 登记缺口。`tools:ignore="HardcodedText"` 只在该布局**确无**硬编码文本时才可移除，移除后不得再新增。

---

### 错误 8：新建 / 移动网络模型未补 keep（**仅当 release 开启混淆时适用**）

**错误写法**：新增 Retrofit 泛型模型或 Gson 目标类型后，既未落在既有受保护包、也未改 `app/proguard-rules.pro`。

**为什么错**：一旦 `minifyEnabled=true`，模型字段被改名 ⇒ 反序列化静默变空、`toJson` 写出混淆 key，界面卡加载或回退但**不报错**。本仓当前 `minifyEnabled=false`（见 [`project/proguard_when_enabled.md`](project/proguard_when_enabled.md)），故本条**休眠**；开启混淆的那一批改动必须同时补规则。

**正确写法**：开启混淆后，新增 / 移动模型同批在 `app/proguard-rules.pro` 补 `-keep`（keep **单源**，模块级 `.pro` 未装配、写了不生效），并用 `assembleRelease` + `mapping.txt` 自证未被改名。

---

### 错误 9：匿名内部类实现同名方法时未限定外部类（编译期静默、运行期栈溢出）

**错误写法**：匿名内部类实现的方法与**外部类同名方法**重名时，类体内不加限定直接调用：

```java
class FooViewModel {
    boolean hasImages() { ... }

    Host host = new Host() {
        @Override public boolean hasImages() {
            return hasImages();          // ← 调到自己，自递归
        }
    };
}
```

**为什么错**：Java 方法解析优先命中**最近作用域** ⇒ 调到自己 ⇒ `StackOverflowError`；且**编译期完全静默**（无警告、无 lint）。跨语言 / 跨类搬运状态机时高频出现。

**正确写法**：唯一调用点显式限定 `return FooViewModel.this.hasImages();` 并加注释。★反向自检：匿名内部类覆写方法体里，凡与外部类同名的方法调用，先问「这是外部类的还是我自己的」。

---

### 错误 10：「一次性消费标志」由**调用点**置位，而消费方可能**根本不会触发**（标志滞留、污染下一次真实事件）

**错误写法**：程序化路径在调用点写「一次性」标志，指望某个回调把它消费掉：

```kotlin
suppressTrack = true
setSwitchChecked(true)     // 控件内「同值早退」⇒ 不播动画、也不派发回调
// 消费方
val isUserClick = !suppressTrack
suppressTrack = false      // 永远等不到
```

**为什么错**：**置位与消费不同源**——只要那次拨动因「同值早退 / `animate=false`」而不派发回调，标志就滞留到下一次**真实**用户操作，把真实操作误判为程序化（漏报事件，并连带污染「用户已手动操作过」类守卫）。

**正确写法**：把标志收口进**唯一的程序化入口**，并按「是否真的会触发消费」精确判定，不触发时显式置回 `false`。★反向自检：凡「一次性标志 / 待消费状态」（`isUserClick`、`pendingXxx`、`consumeXxx()` 令牌），先问三句——①谁置位？②保证消费的那次回调**在什么条件下不触发**？③不触发时谁清？

---

### 错误 11：切页回调里「先隐藏」共享视图，等异步重判再恢复（确定性闪烁）

**错误写法**：把提示条 / 角标上提为宿主级共享视图后，在分页回调里**先** `GONE`，指望随后重判再显示：

```kotlin
fun onPageSelected(position: Int) {
    hideNoticeBar()                                      // ← 错：先隐藏
    binding.vp.post { adapter.notifyItemChanged(index) } // 下一帧才重绑
}
```

**为什么错**：共享视图的「切页重判」**必然晚于**切页回调里的同步代码（晚到下一帧的 item 重绑，还叠加动画）⇒ 每次切页都经历「GONE → 至少一帧空白 → VISIBLE」= **确定性闪烁**。

**正确写法**：共享视图显隐**只由「当前所属对象」的判据直接覆盖**，不做「先隐藏、等重判」；「收起」只保留**真值变更**场景（用户关闭当前项 / 门控关 / 数据为空）。★反向自检：凡「切换**共享**视图所属对象」的路径，落笔任何 `setVisibility(GONE)` 前先问「重判发生在本行之后吗」。

---

### 错误 12：同一排 Tab 的「选中」用绝对像素（dp）字号、未选中用 `sp`

**错误写法**：Tab item 的 TextView 写 `textSize="14sp"`，TabLayout 上写 `app:sbtl_selected_text_size="14dp"` 交给 `SelectedBoldTabLayout` 处理，认为「两边都写 14，肯定一样大」。

**为什么错**：`AbsoluteSizeSpan(int)` 收的是 **px**，而 `getDimensionPixelSize("14dp")` 用 `density` 换算、**不随系统字体缩放**；未选中项的 `14sp` 用 `scaledDensity`、**跟系统字体放大**。系统字体调到 1.15~1.2 倍后「没选中的比选中的还大」，与设计意图反向。

**正确写法**：同一排 Tab 的选中 / 未选中**单位必须同源**：`sbtl_*` 走 dp 绝对像素时，自定义 item 的 `textSize` 也写 **dp**（配 `tools:ignore="SpUsage"`）；或改 `changeTabTextSize` 按 sp 换算后再下发（影响面大，需评估）。★反向自检：凡给文字设绝对字号（`AbsoluteSizeSpan`、`setTextSize(px)`、`dp2px` 后 `setTextSize`），都要回头看同屏同类文字是否走 sp。

---

### 错误 13：把「修哪个文件」按类名推断，未读声明就写进计划

**错误写法**：计划里写「`XxxFragment` 的 `mTypefaceVM.getCategoryId()` 传给 `modifyTypeface`」，理由是「它 observe 了 `getTypefaceIdLD()`，应该就是那个 VM」。

**为什么错**：同名 / 近名类在不同模块各有一份，按「名字相近 / 谁 observe 了谁」推断必然指错 ⇒ 开跑即「改错文件 / 编译不过 / 改完不生效」三类返工之一。

**正确写法**：凡写进计划 / 任务卡的「落点」，必须指到 `path:line` 且能用 `read_file` 看到声明行（字段类型、方法签名、所属类）；同族类多时先全仓搜类型定义、看清 `import` 来自哪个模块再定落点。★反向自检：计划里出现「XXFragment 的 mXXXVM.xxx()」这类跨对象链式调用时，逐段确认每一段的**声明类型**，任一段没看到声明就不许写进计划。

---

### 错误 14：序号 / 位置 / 归属类埋点断言只覆盖「首次选择」

**错误写法**：验 `tag_position` 只走一遍「进页面 → 选某分类第 1 个资源 → 抓事件 → 数值等于预期」就判通过。

**为什么错**：位置由「当前选中分类」推导。**首次选择时全局选中态与点击位置天然一致** ⇒ 无论实现取的是「点击位置」还是「全局残留值」，第一次都对；缺陷只在**切换分类后再选**时暴露。

**正确写法**：这类断言在计划里固定两条场景——①首次选择；②**先选 A 分类、再切 B 分类**再选；断言同时抓 `tag_id`（区分「取错源」与「算错位」）与 `tag_position`（数值），只抓后者易把「取错分类」误读成 off-by-one。

---

### 错误 15：面板类控件的「回显缓存」与控件值读写不同源

**错误写法**：面板收起再展开时回退到旧值，因为缓存**写入点只有「外部回显（选中态变化等）」**，没有「用户操作回调」。

**为什么错**：用户拖动滑杆等操作只改了控件值、没进缓存 ⇒ 「收起 → 展开」必然回显旧值（表现为「设置不生效」或「一展开就跳回去」）。

**正确写法**：回显缓存的**读写必须同源**——用户操作回调与外部回显都写同一份状态，展示时只从该状态渲染。★反向自检：凡「面板 / 浮层收起再打开」会重置的控件，先问「用户操作那次有没有写进这份状态」。

---

### 错误 16：`pointerInput` 闭包读可变状态却未进 key

**错误写法**：`Modifier.pointerInput(Unit) { … 使用 session / scale / 角点坐标 … }`，可变状态只在闭包外 `var`/`mutableStateOf` 更新。

**为什么错**：Kuikly/Compose 的 `pointerInput(key)` 闭包捕获创建时的值；key 不变则手势回调永远读旧几何，表现为拖不动、拖错角、Lazy 项点错行。

**正确写法**：闭包读取的可变状态全部进入 `pointerInput(a, b, …)` 的 key；或只读 `State`/`rememberUpdatedState` 的当前值且符合项目既有安全模式。见 L2 [`platform_kuikly_compose.md`](../coding-norms/base/platform_kuikly_compose.md)。

---

### 错误 17：common 公开 API 用 Bitmap 设图 / 出图

**错误写法**：`fun setImage(bitmap: Bitmap)` 或 `suspend fun crop(): ImageBitmap` 挂在 common 公开面。

**为什么错**：打破 KMP 边界，iOS/H5 无法消费；与本仓 `imageSrc` / `PlatformImage` 契约冲突。

**正确写法**：设图 `imageSrc: String`；出图 `PlatformImage` + `crop()` / `cropToFile()`。见 L3 [`perspective_crop_api.md`](project/perspective_crop_api.md)。

---

### 错误 18：用 `androidx.compose.ui` 包写 Kuikly Compose

**错误写法**：`import androidx.compose.ui.Modifier.Modifier` / `androidx.compose.foundation.layout.Box`。

**为什么错**：Kuikly Compose 只认 `com.tencent.kuikly.compose.*`（runtime 除外），错误包名导致编译失败或链到错误实现。

**正确写法**：仅 `androidx.compose.runtime.*` 用官方包；其余走 Kuikly 包名。
