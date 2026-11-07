#!/usr/bin/env node
// skill-shell · 壳脚本同步巡检（维护者工具，零依赖，**不是**业务门禁）
//
// 用法: node scripts/check-shell-sync.mjs <A 壳目录> <B 壳目录>
//   例:  node ~/Studio/my_skills/skill-shell/scripts/check-shell-sync.mjs \
//            <项目>/docs/skill-references  ~/Studio/my_skills/skill-shell
//
// 检什么: 两侧 `scripts/*.mjs` 的集合与内容。
//   ★「署名差异」按白名单归一化后再比 —— 只放行壳名的两种写法：
//       `docs/skill-references`（项目内署名） ↔ `skill-shell`（上游署名），
//       含门禁输出前缀（`== skill-shell 门禁` ↔ `== skill-references 门禁`）。
//   归一化后仍不一致 ⇒ FAIL（说明两侧真的有一边落后/多改）。
//
// 退出码: 0 一致（只余署名差异）；1 有真实差异或用法错误
//
// 维护约定：改壳时两边同改，改完跑本脚本；本脚本自身也属壳，改它同样要两边同改。

import { readFileSync, existsSync, readdirSync } from 'node:fs';
import { join, basename } from 'node:path';

const [dirA, dirB] = process.argv.slice(2);
if (!dirA || !dirB) {
  console.error('用法: node check-shell-sync.mjs <A 壳目录> <B 壳目录>');
  console.error('例:   node ~/Studio/my_skills/skill-shell/scripts/check-shell-sync.mjs <项目>/docs/skill-references ~/Studio/my_skills/skill-shell');
  process.exit(1);
}

/** 归一化白名单：壳名两种写法 → 同一占位符 */
const normalize = (text) =>
  text
    .replace(/\r\n/g, '\n')
    .replace(/docs\/skill-references|skill-shell|skill-references/g, 'SHELL')
    .replace(/[ \t]+$/gm, '');

let failCount = 0;
const ok = (m) => console.log(`  [ ok ] ${m}`);
const fail = (m) => { failCount += 1; console.log(`  [FAIL] ${m}`); };

const scriptsOf = (dir) => {
  const p = join(dir, 'scripts');
  if (!existsSync(p)) return null;
  return readdirSync(p).filter((f) => f.endsWith('.mjs')).sort();
};

console.log(`== 壳脚本同步巡检`);
console.log(`   A = ${dirA}   (${basename(dirA)})`);
console.log(`   B = ${dirB}   (${basename(dirB)})`);

const a = scriptsOf(dirA);
const b = scriptsOf(dirB);
if (a === null) fail(`A 侧无 scripts/ 目录：${dirA}`);
if (b === null) fail(`B 侧无 scripts/ 目录：${dirB}`);

if (a && b) {
  const onlyA = a.filter((f) => !b.includes(f));
  const onlyB = b.filter((f) => !a.includes(f));
  if (onlyA.length) fail(`仅 A 侧有：${onlyA.join(', ')}（应两边同改）`);
  if (onlyB.length) fail(`仅 B 侧有：${onlyB.join(', ')}（应两边同改）`);

  for (const f of a.filter((x) => b.includes(x))) {
    const pa = join(dirA, 'scripts', f);
    const pb = join(dirB, 'scripts', f);
    const la = normalize(readFileSync(pa, 'utf8')).split('\n');
    const lb = normalize(readFileSync(pb, 'utf8')).split('\n');
    if (la.length === lb.length && la.every((l, i) => l === lb[i])) {
      ok(`${f}：归一化署名后逐行一致（${la.length} 行）`);
      continue;
    }
    const max = Math.max(la.length, lb.length);
    const diffs = [];
    for (let i = 0; i < max && diffs.length < 5; i++) {
      if ((la[i] ?? '') !== (lb[i] ?? '')) diffs.push(i + 1);
    }
    const more = [];
    for (let i = 0; i < max; i++) if ((la[i] ?? '') !== (lb[i] ?? '')) more.push(i + 1);
    fail(`${f}：真实差异 ${more.length} 行（行号 ${diffs.join(', ')}${more.length > 5 ? ' …' : ''}）—— 除署名外应完全一致`);
  }
}

console.log(`\nFAIL ${failCount}`);
console.log(failCount === 0
  ? '结论：两侧壳脚本一致 ✓（差异仅限署名白名单）'
  : '结论：不一致 ✗ —— 把缺的一边补齐后重跑');
process.exit(failCount ? 1 : 0);
