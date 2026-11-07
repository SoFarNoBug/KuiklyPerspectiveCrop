# 里程碑总览

按清单推进：**验证完一步再下一步**。  
术语见 [CONTEXT.md](../../CONTEXT.md)；**已定决策 SoT** 见 [docs/decisions.md](../decisions.md)；难逆转子集见 [docs/adr/](../adr/)。

## v0.1 定义

**v0.1 = M0 + M1 + M2 + M3 全部验收通过**（含 Q14 真像素放大镜）。  
中间里程碑可 Demo 冒烟，但不算 v0.1 交付完成。分期不得把已定能力挪到「v0.1 之后」。

| 里程碑 | 目标 | 清单 | 状态 |
|---|---|---|---|
| **M0** | 空架 + 文档 | [M0-checklist.md](./M0-checklist.md) | **已完成**（编译通过） |
| **M1** | 几何触控 + 默认叠层 | [M1-checklist.md](./M1-checklist.md) | **已完成**（真机验收通过） |
| **M2** | Android 透视出图 | [M2-checklist.md](./M2-checklist.md) | **已完成**（真机验收通过；非法四点拖拽 N/A） |
| **M3** | 放大镜 + 剩余旋钮 + 对接说明 | [M3-checklist.md](./M3-checklist.md) | 未开始 |

## 依赖

```text
M0 → M1 → M2 → M3  ⟸  v0.1 完成线
```

不允许跳步合入「下一步能力」到未验收的里程碑（文档 / norms 除外）。

## 完成定义（v0.1 能力面）

**通用能力**到位：Controller / Config / 六层默认叠层 / 手势约束 / Android 出图 / 真像素放大镜。  
**非目标**（任意里程碑都不做）——与 [decisions.md](../decisions.md) 一致：

- OCR / 边缘检测 / 内置 `autoScan` 算法（仅留钩子）
- 业务皮肤进库默认（Demo 换皮示例可做）
- common 公开 API 暴露 `Bitmap` / `ImageBitmap`
- 非 Android 端真实透视出图（M2 起 stub，明确失败即可）

## 分期对照（易混项）

| 能力 | 里程碑 | 备注 |
|---|---|---|
| 蒙版路径挖洞、框线、角点 | **M1** | |
| 六层 Painter SPI 接口 + 默认可注入 | **M1** 接线 | 放大镜/辅助线实现可空到 M3 |
| 边中点：绘制开关 + 命中 | **M1** 接线 | 默认关；样式精调可 M3 |
| `edgeSnapToTouch` / `edgeClampToBounds` | **M1** | **默认 true**（Q9）；保留开关，非「仅 rect / 仅 M3」 |
| 辅助线、**真像素**放大镜 + 降采样缓存 | **M3** | **v0.1 必达**（Q14） |
| `unhitPassthrough`、捕捉距离 `≤0` 关闭等旋钮 | **M3** | M1 可用合理默认 |
| `crop` / `cropToFile` / `canCrop` | **M2** | |
| UI 旋转 90° 状态 | **M1** | |
| 像素旋转跟出图 | **M2** | |
| Camera/Album → `imageSrc` | **M3** | README + Demo 注释；Demo 不强制依赖（Q12） |

## v0.1 能力速查

**Controller 面**：设 `imageSrc`、读写四点、全图重置、`canCrop`、`crop`/`cropToFile`、顺时针旋转、注入 Config / Processor / **六层 Painters**、可选 touch 回调。  
**Config**：`rectMode`、insideDrag、各类 catchDistance、min 尺寸/角度、**`edgeSnapToTouch`/`edgeClampToBounds`（默认开）**、指南/中点/放大镜开关、描边与点样式。  
**手势**：角 / 边(中点或边带) / 内部整体拖；凸性或 rect 约束；图域边界。  
**Demo-only**：业务主题换皮示例（不进库默认）。

细节以各 `M*-checklist.md` 勾选为准；与本表或 [decisions.md](../decisions.md) 冲突时改清单，不改已定项。
