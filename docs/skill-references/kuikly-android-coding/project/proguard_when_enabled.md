doc_version: 20261104_1
layer: L3

# 混淆安全（网络模型 keep）· **休眠条款**

> **状态：本仓当前未启用混淆（`minifyEnabled = false`），本条不进入 `norms[]`、不做每日自检。**
> 仅当某个 release 构建把 `minifyEnabled` 改为 `true`（或引入 R8 裁剪）时，**该批次改动**才按下文执行。
> **分层**：全文真源在本文件；编码会话红线摘要见 `.cursor/rules/kuikly-dev-android.mdc`；流程见 Skill。勿与 Rule 双写条文（[ADR 0021](../../../adr/0021-rule-redline-references-sot.md)）。

## 一、本仓现状（2026-11-04 实测，启用混淆前须复核）

- `app/build.gradle`：`release { minifyEnabled false }` —— **不混淆、不裁剪**；`basiclib`（两处）、`pay` 同为 `false`；其余模块未写该属性（默认 false）。
- 各模块 `proguard-rules.pro`（26 个）为默认模板空文件，且**未以 `consumerProguardFiles` 装配**（全仓 `grep consumerProguardFiles` 零命中）⇒ 对 app 的 release 包无效。
- 结论：当前 keep 规则**零作用**；写进模块 `.pro` 的规则即使将来开启混淆也**不会生效**。

## 二、启用混淆后必须做的事（红线）

1. **keep 单源 = `app/proguard-rules.pro`**。新增 / 移动的网络模型（Retrofit 泛型参数类型、Gson `fromJson` / `toJson` 的目标类型）必须在同一批改动里补 `-keep`：
   - 类本身：`-keep class com.quyin.<module>.bean.** { *; }`（或实际包名 `model` / `entity`）
   - 成员与泛型：`-keepclassmembers class … { <fields>; }`，必要时 `-keepattributes Signature`
2. **禁止**把规则写进模块级 `proguard-rules.pro`（未装配，写了不生效，属静默失效）。
3. **禁止**用「全仓包名通配」一刀切（会误 keep 大量无关类），按本次实际新增的包补齐。

## 三、为何是红线

违反后果是**静默**的：release 包字段被改名 → 反序列化全空 / `toJson` 写出混淆 key → 界面卡加载或回退、**不报错**。

## 四、自验（本仓无 Gradle 门禁任务）

```bash
./gradlew :app:assembleRelease
# 在 mapping 中确认模型类/字段未被改名（应看到原类名 → 原类名，字段逐个在列）
grep -n "com.quyin.<your.module>.bean.<YourBean>" app/build/outputs/mapping/release/mapping.txt
```

判据：模型类**没有**被映射成新名字，且字段名逐字保留。出证方式写进 changelog（贴命令与关键行）。

## 五、如何重新启用本条款

1. 确认 `minifyEnabled = true` 已进入某个构建类型，且 `proguardFiles` 指向 `app/proguard-rules.pro`。
2. 本文件恢复为 `project/proguard_model_keep.md`，并在 `project.yaml` 的 `norms[]` 追加 `proguard_model_keep`（`layer: L3`）。
3. SKILL.md 补「## 混淆安全规范（核心）」索引 + `description` 同步代数（`norms.length` 变化即升 `vN`）。
4. Rule 恢复 ≤3 行红线；审查侧 `P0-proguard-verify` 桶由「休眠」改为常规判据。
5. 跑 `node docs/skill-references/kuikly-android-coding/scripts/check_skill.mjs` 至 FAIL 0。
