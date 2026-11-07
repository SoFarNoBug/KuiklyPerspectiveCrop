#!/usr/bin/env node
// docs/skill-references · 真机可执行档收尾自检（零依赖，不是 skill）
// 用法: node docs/skill-references/scripts/check_device_checklist.mjs [报告路径…]  （须在仓根运行）
//   传路径: 逐份校验「挂了 P1-device-verify ⇒ 必须有 device-check-list/{task-id}.md」，缺 FAIL
//   不传参: 扫描 docs/reviews/ 全目录，「挂桶但缺表」的历史欠账仅 WARN
// 退出码: 0 无 FAIL；1 有 FAIL
// 依据: ADR 0037（2026-10-20 修订 —— 可执行档由「用户明确说要跑」改为「默认产出」）

import { readFileSync, existsSync, readdirSync } from 'node:fs';
import { join, basename } from 'node:path';

const REPO = process.cwd();
const REVIEWS_DIR = join(REPO, 'docs/reviews');
const DCL_DIR = join(REVIEWS_DIR, 'device-check-list');
const BUCKET = 'P1-device-verify';

let failCount = 0;
let warnCount = 0;
const ok = (m) => console.log(`  [ ok ] ${m}`);
const warn = (m) => { warnCount += 1; console.log(`  [WARN] ${m}`); };
const fail = (m) => { failCount += 1; console.log(`  [FAIL] ${m}`); };

// 报告文件名推 task-id：review-{task-id}.md / review-{task-id}-rN.md → {task-id}（复审沿用基线表）
function taskIdOf(fileName) {
  return fileName.replace(/^review-/, '').replace(/\.md$/, '').replace(/-r\d+$/, '');
}

// 是否仍挂该桶：命中即算；标「已关/已闭环」的行不计（复审已闭环）
function hasOpenBucket(text) {
  return text.split('\n').some((line) => line.includes(BUCKET) && !/已关|已关闭|已闭环/.test(line));
}

function checkReport(path, strict) {
  if (!existsSync(path)) {
    fail(`报告不存在：${path}`);
    return;
  }
  const text = readFileSync(path, 'utf8');
  if (!hasOpenBucket(text)) {
    ok(`${basename(path)}：未挂 ${BUCKET}，无需可执行档`);
    return;
  }
  const id = taskIdOf(basename(path));
  if (existsSync(join(DCL_DIR, `${id}.md`))) {
    ok(`${basename(path)}：可执行档已产出 → device-check-list/${id}.md`);
  } else if (strict) {
    fail(`${basename(path)}：挂了 ${BUCKET} 但缺可执行档 → docs/reviews/device-check-list/${id}.md`);
  } else {
    warn(`${basename(path)}：挂了 ${BUCKET} 但缺可执行档（历史欠账，缺 device-check-list/${id}.md）`);
  }
}

const args = process.argv.slice(2);
if (args.length) {
  console.log(`== 真机可执行档自检（指定 ${args.length} 份报告 · 严格）`);
  for (const a of args) {
    checkReport(a.startsWith('/') ? a : join(REPO, a), true);
  }
} else {
  console.log('== 真机可执行档自检（全目录扫描 · 历史欠账仅告警）');
  if (!existsSync(REVIEWS_DIR)) {
    fail(`找不到目录：${REVIEWS_DIR}`);
  } else {
    for (const f of readdirSync(REVIEWS_DIR).filter((x) => /^review-.*\.md$/.test(x))) {
      checkReport(join(REVIEWS_DIR, f), false);
    }
  }
}

console.log(`\nFAIL ${failCount} / WARN ${warnCount}`);
process.exit(failCount ? 1 : 0);
