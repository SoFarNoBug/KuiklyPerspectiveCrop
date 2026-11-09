doc_version: 20261109_1

# 鸿蒙编码常见错误反例

> 本文件为**反例真源**。Rule 只链错误编号；流程见 Skill。新增按「错误 N」续排。

---

### 错误 1：Drawing 位图像素按 GetArgbPixels 字节序解包

**错误写法**：`COLOR_FORMAT_BGRA_8888` 位图 `memcpy` 后按 `A,R,G,B` 组装 ARGB Int。

**为什么错**：Drawing BGRA 内存是 `B,G,R,A`；与 `OH_PixelmapNative_GetArgbPixels` 的 `A,R,G,B` 不同。错解 → 红蓝对调（紫蓝色）。

**正确写法**：BGRA → `b,g,r,a = bytes[0..3]` 再 `(a<<24)|(r<<16)|(g<<8)|b`；注释写明来源格式。

---

### 错误 2：调用 OH_ImagePackerNative 落盘

**错误写法**：裁切结果走 ImagePacker Native 编码 PNG/JPEG。

**为什么错**：SDK/真机该符号可为不可执行桩 → SIGSEGV。

**正确写法**：软件 PNG（或其它已验证路径）写 `cacheDir`；见 runbook / ADR 0009。

---

### 错误 3：PixelMap / Drawing 对象只创建不释放

**错误写法**：`CreatePixelmap` / `BitmapCreate` / `PixelMapGetFromOhPixelMapNative` 后在失败分支或正常返回漏 `Release` / `Destroy` / `Dissolve`。

**为什么错**：Native 资源泄漏；多次裁切后 OOM 或不稳定。

**正确写法**：所有权单一；`try/finally`（或 KN 等价）保证成对释放。

---

### 错误 4：assets:// 未 init 当本地路径 fopen

**错误写法**：直接把 `assets://crop_sample.webp` 当文件路径，或未 `PerspectiveCropOhos.init` 就裁切。

**为什么错**：resfile 真实路径依赖宿主注入；未 init → 解码失败或写到错误目录。

**正确写法**：壳 NAPI 注入 `resfileDir`/`cacheDir` 后再解码；解析走 `resolveOhosImageUri`。
