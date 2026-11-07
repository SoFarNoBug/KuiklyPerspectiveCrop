# H5 壳 / 冒烟 · changelog

## 2026-11-07（M4：JS 放大镜 + 透视裁剪）

- `warmCache` / `process` → suspend；JS `MagnifierSampler` / `CropProcessor` / `PlatformImage` 实装
- CPU 单应出图；`saveToFile` → `blob:`；`assets://` → `/assets/…`
- 决策 Q33–Q37、ADR 0005、M4-checklist

## 2026-11-07（H5 冒烟跑通 + 白屏修复）

- 精简 `static_server/serve`（8083，无 whistle）；`fetch-yarn-ivy` 应对 GitHub Yarn 超时
- 补 `h5App/webpack.config.d/output.js`（IIFE，防 UMD 覆盖 `window.com` / `nvi`）
- `index.html` 去掉默认同步 libpag CDN（超时阻塞 `h5App.js` → 白屏）
- 验证：首页「透视裁剪」+ 双入口可渲染；经验沉淀见 [runbook.md](./runbook.md)
