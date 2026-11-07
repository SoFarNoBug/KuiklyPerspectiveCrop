#!/usr/bin/env node
// docs/skill-references · CodeBuddy 规范自检（零依赖，不是 skill）
// 用法: node scripts/check_codebuddy_spec.mjs [rulesDir] [skillsDir]
//   默认 rulesDir = .cursor/rules，skillsDir = .cursor/skills
// 判据来源（官方文档）:
//   Rules  https://www.codebuddy.cn/docs/ide/User-guide/Rules
//   Skills https://www.codebuddy.cn/docs/ide/Features/Skills
// 退出码: 0 无 FAIL；1 有 FAIL
//
// 为什么需要它：规则 description 一旦写成 YAML 块标量（>- / |），IDE 规则索引只回显 ">-"，
// 智能体请求型规则等于没有描述、永远不会被加载；skill description 过长会在技能列表里被截断，
// 尾部触发词静默丢失；description 含 "<" ">" 会被官方校验器判错。这些都不会报错，只能靠扫描拦。

import { readFileSync, existsSync, readdirSync, statSync } from 'node:fs';
import { join, basename, resolve, dirname } from 'node:path';

const RULE_KEYS = new Set(['description', 'alwaysApply', 'enabled', 'updatedAt', 'provider', 'globs', 'type']);
const SKILL_KEYS = new Set(['name', 'description', 'allowed-tools', 'disable', 'license', 'metadata']);
const DESC_WARN = 300; // 实测技能列表在 ~330 字符处截断
const DESC_FAIL = 400;
const BODY_WARN = 4000; // 官方上限 < 5k 词（CJK 按字计）
const BODY_FAIL = 5000;

let failCount = 0;
let warnCount = 0;
const ok = (m) => console.log(`  [ ok ] ${m}`);
const warn = (m) => { warnCount += 1; console.log(`  [WARN] ${m}`); };
const fail = (m) => { failCount += 1; console.log(`  [FAIL] ${m}`); };

const rulesDir = resolve(process.cwd(), process.argv[2] || '.cursor/rules');
const skillsDir = resolve(process.cwd(), process.argv[3] || '.cursor/skills');

/** frontmatter 原文 → { meta, body }；同时返回逐键是否为块标量 */
function frontmatter(text) {
  if (text.charCodeAt(0) === 0xfeff) return { bom: true };
  const m = text.match(/^---\r?\n([\s\S]*?)\r?\n---[ \t]*\r?\n?/);
  if (!m) return { missing: true };
  const meta = {};
  let cur = null;
  for (const line of m[1].split('\n')) {
    const km = line.match(/^([A-Za-z_][\w-]*):\s*(.*)$/);
    if (km) {
      const raw = km[2].trim();
      cur = { key: km[1], block: /^[|>][-+]?$/.test(raw), value: raw, lines: [] };
      meta[km[1]] = cur;
      continue;
    }
    if (cur && /^\s+\S/.test(line)) cur.lines.push(line.trim());
  }
  for (const v of Object.values(meta)) {
    if (v.block || (!v.value && v.lines.length)) v.value = v.lines.join(' ');
    v.value = v.value.replace(/^["']|["']$/g, '').trim();
  }
  return { meta, body: text.slice(m[0].length) };
}

/** CJK 按字计 + 拉丁按词计，逼近官方词数口径 */
function wordCount(s) {
  const cjk = (s.match(/[\u3400-\u9fff\u3040-\u30ff]/g) || []).length;
  const latin = (s.replace(/[\u3400-\u9fff\u3040-\u30ff]/g, ' ').match(/[A-Za-z0-9_'.\\/-]+/g) || []).length;
  return cjk + latin;
}

function subdirs(dir) {
  if (!existsSync(dir)) return [];
  return readdirSync(dir).filter((e) => statSync(join(dir, e)).isDirectory()).sort();
}

/** Cursor：扁平 `rules/*.mdc`；CodeBuddy 兼容：`rules/<name>/RULE.mdc` */
function listRuleFiles(dir) {
  if (!existsSync(dir)) return [];
  const out = [];
  for (const e of readdirSync(dir)) {
    const p = join(dir, e);
    if (statSync(p).isFile() && e.endsWith('.mdc')) {
      out.push({ label: e, path: p });
      continue;
    }
    if (statSync(p).isDirectory()) {
      const nested = join(p, 'RULE.mdc');
      if (existsSync(nested)) out.push({ label: `${e}/RULE.mdc`, path: nested });
      else {
        console.log(`\n- ${e}`);
        fail(`缺固定名 RULE.mdc（.cursor/rules/${e}/RULE.mdc）`);
      }
    }
  }
  return out.sort((a, b) => a.label.localeCompare(b.label));
}

function checkRuleFile(label, rulePath) {
  console.log(`\n- ${label}`);
  const fm = frontmatter(readFileSync(rulePath, 'utf8'));
  if (fm.bom) { fail('文件以 BOM 开头，frontmatter 不会被识别'); return; }
  if (fm.missing) { fail('缺少 YAML frontmatter（首行 --- 且闭合）'); return; }

  const d = fm.meta.description;
  if (!d || !d.value) fail('frontmatter 缺 description（智能体请求型规则仅靠它决定是否加载）');
  else if (d.block) fail('description 用了 YAML 块标量（>- / |），IDE 规则索引只回显 ">-" —— 改为单行普通标量');
  else ok(`description 为单行标量（${d.value.length} 字符）`);

  if (!fm.meta.alwaysApply) warn('缺 alwaysApply（无法区分 always / 智能体请求 / 手动）');
  else if (!/^(true|false)$/i.test(fm.meta.alwaysApply.value)) warn(`alwaysApply 非布尔值: ${fm.meta.alwaysApply.value}`);
  // Cursor 扁平规则可不写 enabled/updatedAt
  if (!fm.meta.enabled) warn('缺 enabled（可选）');
  if (!fm.meta.updatedAt) warn('缺 updatedAt（可选）');

  for (const key of Object.keys(fm.meta)) {
    if (!RULE_KEYS.has(key)) warn(`非规范 frontmatter 键: ${key}`);
  }
  if (!fm.body.trim()) fail('frontmatter 后正文为空');
  const lines = fm.body.split('\n').length;
  if (lines > 500) warn(`正文 ${lines} 行，超过官方「单条规则 ≤500 行」建议`);
}

// ---------------- Rules ----------------
console.log(`\n== Rules: ${rulesDir}`);
const ruleFiles = listRuleFiles(rulesDir);
for (const r of ruleFiles) checkRuleFile(r.label, r.path);

// ---------------- Skills ----------------
console.log(`\n== Skills: ${skillsDir}`);
const alwaysCount = (() => {
  let n = 0;
  for (const r of ruleFiles) {
    const fm = frontmatter(readFileSync(r.path, 'utf8'));
    if (!fm.meta || !fm.meta.alwaysApply) continue;
    if (/^true$/i.test(fm.meta.alwaysApply.value)) n += 1;
  }
  return n;
})();
if (alwaysCount > 5) warn(`alwaysApply: true 的规则共 ${alwaysCount} 条，官方建议只保留 3–5 条`);

for (const name of subdirs(skillsDir)) {
  const dir = join(skillsDir, name);
  const skillPath = join(dir, 'SKILL.md');
  console.log(`\n- ${name}`);
  if (!existsSync(skillPath)) {
    fail('缺 SKILL.md');
    continue;
  }
  const fm = frontmatter(readFileSync(skillPath, 'utf8'));
  if (fm.bom) { fail('文件以 BOM 开头，frontmatter 不会被识别'); continue; }
  if (fm.missing) { fail('缺少 YAML frontmatter（首行 --- 且闭合）'); continue; }

  const nm = fm.meta.name;
  if (!nm || !nm.value) fail('frontmatter 缺 name');
  else if (nm.value !== name) fail(`name(${nm.value}) 与目录名(${name}) 不一致`);
  else if (!/^[a-z0-9]+(-[a-z0-9]+)*$/.test(nm.value)) warn(`name 非 kebab-case: ${nm.value}`);
  else ok(`name 与目录一致（${nm.value}）`);

  const d = fm.meta.description;
  if (!d || !d.value) fail('frontmatter 缺 description');
  else {
    const v = d.value;
    if (/[<>]/.test(v)) fail('description 含 "<" 或 ">"，官方校验器会判错');
    if (v.length > DESC_FAIL) fail(`description ${v.length} 字符，超过 ${DESC_FAIL}（技能列表必被截断）`);
    else if (v.length > DESC_WARN) warn(`description ${v.length} 字符，超过 ${DESC_WARN}（实测 ~330 处截断）`);
    else ok(`description ${v.length} 字符`);
  }

  for (const key of Object.keys(fm.meta)) {
    if (!SKILL_KEYS.has(key)) warn(`非规范 frontmatter 键: ${key}（官方仅 name / description / allowed-tools / disable）`);
  }

  if (!fm.body.trim()) fail('frontmatter 后正文为空');
  const wc = wordCount(fm.body.replace(/```[\s\S]*?```/g, ' '));
  if (wc > BODY_FAIL) fail(`正文约 ${wc} 词，超过 <5k 词上限（下沉到 references/）`);
  else if (wc > BODY_WARN) warn(`正文约 ${wc} 词，接近 5k 词上限`);

  const extra = readdirSync(dir).filter((e) => !['SKILL.md', 'scripts', 'references', 'assets'].includes(e));
  if (extra.length) warn(`非规范顶层条目（官方仅 SKILL.md + scripts/references/assets）: ${extra.join(', ')}`);

  // 相对链接（解析基准 = skill 目录）
  const broken = new Set();
  for (const m of fm.body.matchAll(/\[[^\]]*\]\(([^)\s]+)\)/g)) {
    const target = m[1];
    if (/^[a-z]+:/i.test(target) || target.startsWith('#')) continue;
    const rel = target.split('#')[0];
    if (!rel) continue;
    if (!existsSync(rel.startsWith('/') ? rel : join(dir, rel))) broken.add(target);
  }
  if (broken.size) warn(`SKILL.md 相对链接指向不存在文件（基准=${name}/）: ${[...broken].join(', ')}`);
}

console.log(`\n== 汇总: ${failCount} FAIL, ${warnCount} WARN`);
if (failCount > 0) {
  console.error('存在不合规项，见上方 [FAIL]。');
  process.exit(1);
}
console.log('符合 CodeBuddy 规范。');
