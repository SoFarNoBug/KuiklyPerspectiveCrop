#!/usr/bin/env node
// docs/skill-references · 通用门禁（零依赖，不是 skill）
// 用法: node scripts/check_skill.mjs <业务 skill 目录>
// 退出码: 0 无 FAIL；1 有 FAIL

import { readFileSync, existsSync } from 'node:fs';
import { join, basename, isAbsolute } from 'node:path';

const skillDir = process.argv[2];
if (!skillDir) {
  console.error('用法: node scripts/check_skill.mjs <业务 skill 目录>');
  process.exit(1);
}

const abs = isAbsolute(skillDir) ? skillDir : join(process.cwd(), skillDir);
const skillMdPath = join(abs, 'SKILL.md');
const yamlPath = join(abs, 'project.yaml');

let failCount = 0;
let warnCount = 0;
const ok = (m) => console.log(`  [ ok ] ${m}`);
const warn = (m) => { warnCount += 1; console.log(`  [WARN] ${m}`); };
const fail = (m) => { failCount += 1; console.log(`  [FAIL] ${m}`); };

function read(p) { return readFileSync(p, 'utf8'); }

function frontmatter(text) {
  const m = text.match(/^---\s*\n([\s\S]*?\n)---\s*\n/);
  if (!m) return null;
  const meta = {};
  for (const line of m[1].split('\n')) {
    const km = line.match(/^([A-Za-z_][\w-]*):\s*(.*)$/);
    if (km) meta[km[1]] = km[2].trim();
  }
  return meta;
}

/** 顶层标量、docs、norms、peer_pools、extra_gates、mounts.rules 与 mounts 标量 */
function parseYaml(text) {
  const config = { docs: {}, norms: [], peer_pools: [], extra_gates: [], mounts: { rules: [] } };
  let mode = null;
  let norm = null;
  const flush = () => {
    if (!norm) return;
    if (!norm.id || !norm.title || !norm.ref) throw new Error(`norms 缺 id/title/ref: ${JSON.stringify(norm)}`);
    config.norms.push(norm);
    norm = null;
  };
  const lines = text.split('\n');
  for (let i = 0; i < lines.length; i++) {
    const raw = lines[i];
    if (!raw.trim() || raw.trim().startsWith('#')) continue;
    if (/^\S/.test(raw)) {
      flush();
      mode = null;
      const m = raw.match(/^([A-Za-z_][\w-]*)\s*:\s*(.*)$/);
      if (!m) throw new Error(`L${i + 1}: 无法解析 ${raw.trim()}`);
      const key = m[1];
      const val = m[2].trim();
      if (['docs', 'norms', 'peer_pools', 'extra_gates', 'mounts'].includes(key)) {
        if (val !== '') throw new Error(`L${i + 1}: ${key} 应为块头`);
        mode = key;
      } else {
        config[key] = val;
      }
      continue;
    }
    if (mode === 'docs') {
      const m = raw.match(/^  ([A-Za-z_][\w-]*)\s*:\s*(.+)$/);
      if (!m) throw new Error(`L${i + 1}: docs 标量非法`);
      config.docs[m[1]] = m[2].trim();
      continue;
    }
    if (mode === 'norms') {
      const head = raw.match(/^  - id:\s*(.+)$/);
      if (head) { flush(); norm = { id: head[1].trim() }; continue; }
      const field = raw.match(/^    (id|title|ref|layer):\s*(.+)$/);
      if (field) {
        if (!norm) throw new Error(`L${i + 1}: norms 字段无列表项`);
        norm[field[1]] = field[2].trim();
        continue;
      }
      throw new Error(`L${i + 1}: norms 行非法`);
    }
    if (mode === 'peer_pools' || mode === 'extra_gates') {
      const item = raw.match(/^  - (.+)$/);
      if (!item) throw new Error(`L${i + 1}: 列表项非法`);
      config[mode].push(item[1].trim());
      continue;
    }
    if (mode === 'mounts' || mode === 'mounts_rules') {
      const ruleItem = raw.match(/^    - (.+)$/);
      if (mode === 'mounts_rules' && ruleItem) {
        config.mounts.rules.push(ruleItem[1].trim());
        continue;
      }
      const rulesHead = raw.match(/^  rules\s*:\s*$/);
      if (rulesHead) { mode = 'mounts_rules'; continue; }
      const scalar = raw.match(/^  ([A-Za-z_][\w-]*)\s*:\s*(.+)$/);
      if (scalar) {
        mode = 'mounts';
        config.mounts[scalar[1]] = scalar[2].trim();
        continue;
      }
      throw new Error(`L${i + 1}: mounts 行非法`);
    }
    throw new Error(`L${i + 1}: 意外缩进`);
  }
  flush();
  return config;
}

function versionOf(text) {
  const m = text.match(/(?<![A-Za-z0-9_/])v(\d+)\b/);
  return m ? Number(m[1]) : null;
}

function existsRel(rel) {
  return existsSync(join(abs, rel));
}

function countSeverity(rules) {
  let n = 0;
  for (const rel of rules) {
    const p = join(abs, rel);
    if (!existsSync(p)) continue;
    n += read(p).split('\n').filter((l) => /^\|\s*`P[01]-[a-z0-9-]+`/.test(l)).length;
  }
  return n;
}

console.log(`== skill-references 门禁 ${abs}`);

if (!existsSync(skillMdPath)) fail('缺少 SKILL.md');
if (!existsSync(yamlPath)) fail('缺少 project.yaml');
if (failCount) {
  console.log(`\nFAIL ${failCount} / WARN ${warnCount}`);
  process.exit(1);
}

const skillText = read(skillMdPath);
let meta = null;
let config = null;
try {
  meta = frontmatter(skillText);
  if (!meta) fail('frontmatter 无法解析');
  else ok('frontmatter 可解析');
} catch (e) {
  fail(String(e.message || e));
}
try {
  config = parseYaml(read(yamlPath));
  ok('project.yaml 可解析');
} catch (e) {
  fail(`project.yaml: ${e.message || e}`);
}

if (meta?.name && meta.name !== basename(abs)) fail(`name（${meta.name}）≠ 目录名（${basename(abs)}）`);
else if (meta?.name) ok(`name == 目录名：${meta.name}`);

if (!skillText.includes('sediment: off') || !skillText.includes('mounts')) {
  fail('SKILL.md 缺少挂载引导（须含 sediment: off 与 mounts）');
} else ok('SKILL.md 挂载引导在位');

const gate = config?.gate_on;
if (gate !== 'always' && gate !== 'meta_only') fail(`gate_on 必须是 always 或 meta_only（当前 ${gate || '缺'}）`);
else ok(`gate_on = ${gate}`);

const sediment = config?.sediment;
if (sediment !== 'on' && sediment !== 'off') fail(`sediment 必须是 on 或 off（当前 ${sediment || '缺'}）`);
else ok(`sediment = ${sediment}`);

const kind = config?.version_counts;
if (!['norms', 'paradigms', 'severity_buckets'].includes(kind)) {
  fail(`version_counts 非法（${kind || '缺'}）`);
} else ok(`version_counts = ${kind}`);

const mounts = config?.mounts || { rules: [] };
const rules = mounts.rules || [];

const contentRoot = config?.content_root;
if (!contentRoot) fail('缺少 content_root');
else if (!existsRel(contentRoot)) fail(`content_root 不存在：${contentRoot}`);
else {
  ok(`content_root = ${contentRoot}`);
  const vf = mounts.version_file;
  if (vf) {
    const vfDir = vf.replace(/\/[^/]+$/, '');
    if (vfDir !== contentRoot) fail(`version_file 目录（${vfDir}）≠ content_root（${contentRoot}）`);
    else ok('version_file 与 content_root 同目录');
  }
}

if (kind === 'norms' && config) {
  for (const n of config.norms) {
    if (!existsRel(n.ref)) fail(`norms.ref 不存在：${n.ref}`);
  }
  if (config.norms.length && config.norms.every((n) => existsRel(n.ref))) ok(`norms 引用存在（${config.norms.length}）`);
}

for (const rel of rules) {
  if (!existsRel(rel)) fail(`mounts.rules 不存在：${rel}`);
}
if (rules.length && rules.every((rel) => existsRel(rel))) ok(`mounts.rules 存在（${rules.length}）`);

if (mounts.anti_patterns) {
  if (!existsRel(mounts.anti_patterns)) fail(`mounts.anti_patterns 不存在：${mounts.anti_patterns}`);
  else ok(`anti_patterns = ${mounts.anti_patterns}`);
}

if (sediment === 'on') {
  if (!mounts.sediment_process) fail('sediment: on 但缺少 mounts.sediment_process');
  else if (!existsRel(mounts.sediment_process)) fail(`sediment_process 不存在：${mounts.sediment_process}`);
  else ok(`sediment_process = ${mounts.sediment_process}`);
  if (!mounts.backlog) fail('sediment: on 但缺少 mounts.backlog');
} else if (mounts.sediment_process && !existsRel(mounts.sediment_process)) {
  warn(`sediment: off 且 sediment_process 不存在：${mounts.sediment_process}`);
}

let expected = null;
if (kind === 'norms' && config) expected = config.norms.length;
else if (kind === 'paradigms') expected = rules.filter((rel) => /\/p_[^/]+\.md$/.test(`/${rel}`)).length;
else if (kind === 'severity_buckets') expected = countSeverity(rules);

const versionRel = mounts.version_file;
if (!versionRel) fail('缺少 mounts.version_file');
else if (!existsRel(versionRel)) fail(`version_file 不存在：${versionRel}`);
else if (expected == null) fail('无法按 version_counts 计数');
else {
  const vFile = versionOf(read(join(abs, versionRel)));
  const vDesc = versionOf(meta?.description || '');
  if (vFile !== expected) fail(`${versionRel} v${vFile} ≠ 计数 ${expected}（${kind}）`);
  else ok(`version_file v${vFile} == 计数`);
  if (vDesc !== expected) fail(`description v${vDesc} ≠ 计数 ${expected}`);
  else ok(`description v${vDesc} == 计数`);
}

if (mounts.backlog) {
  if (!existsRel(mounts.backlog)) fail(`backlog 不存在：${mounts.backlog}`);
  else {
    const b = read(join(abs, mounts.backlog));
    if (!b.includes('## 候选') || !b.includes('## 已晋升')) fail('backlog 缺「## 候选」或「## 已晋升」');
    else ok('backlog 分段存在');
  }
} else if (sediment !== 'on') warn('未挂载 backlog');

for (const rel of config?.extra_gates || []) {
  if (!existsRel(rel)) fail(`extra_gates 不存在：${rel}`);
  else ok(`extra_gates 存在：${rel}`);
}

console.log(`\nFAIL ${failCount} / WARN ${warnCount}`);
process.exit(failCount ? 1 : 0);
