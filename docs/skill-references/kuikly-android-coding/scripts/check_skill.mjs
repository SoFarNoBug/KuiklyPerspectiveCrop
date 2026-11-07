#!/usr/bin/env node
// kuikly-android-coding · 技能健康自检（零依赖）
// 用法: node docs/skill-references/kuikly-android-coding/scripts/check_skill.mjs
// 退出码: 0 无 FAIL；1 有 FAIL
// project.yaml：仅解析受限子集（顶层标量、docs 一层、norms 的 id/title/ref）

import { readFileSync, readdirSync, existsSync } from 'node:fs';
import { spawnSync } from 'node:child_process';
import { dirname, join, basename } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = join(__dirname, '..', '..', '..', '..');
const SKILL_DIR = join(REPO_ROOT, '.cursor', 'skills', 'kuikly-android-coding');
const SKILL_MD = join(SKILL_DIR, 'SKILL.md');
const PROJECT_YAML = join(SKILL_DIR, 'project.yaml');
/** 由 project.yaml 的 content_root 解析；load 前暂空 */
let CONTENT_ROOT = null;
let MISUSE_MD = null;
let BACKLOG_MD = null;

const WARN_LINES = 350, FAIL_LINES = 500;
const WARN_CHARS = 20000, FAIL_CHARS = 30000;
const versionRe = () => /(?<![A-Za-z0-9_/])v(\d+)\b/g;
const results = [];

const ok = (m) => results.push(['OK', m]);
const warn = (m) => results.push(['WARN', m]);
const fail = (m) => results.push(['FAIL', m]);
const read = (p) => readFileSync(p, 'utf8');

const REQUIRED_DOCS = [
  'workflow_readme',
  'skill_changelog',
  'feature_changelog',
  'tasks_optional',
  'file_map',
  'context_glob',
  'reviews_dir',
];

function readFrontmatter(text) {
  const m = text.match(/^---\s*\n([\s\S]*?)\n---\s*\n/);
  if (!m) return { meta: {}, endLines: 0 };
  const meta = {};
  for (const line of m[1].split('\n')) {
    const km = line.match(/^([A-Za-z_][\w-]*):\s*(.*)$/);
    if (km) meta[km[1]] = km[2].trim();
  }
  return { meta, endLines: m[0].split('\n').length - 1 };
}

/** 受限子集解析；失败返回 { error }；成功返回 { config } */
function parseProjectYaml(text) {
  const lines = text.split('\n');
  const config = { docs: {}, norms: [] };
  let mode = null; // null | 'docs' | 'norms'
  let currentNorm = null;

  const flushNorm = () => {
    if (!currentNorm) return;
    if (!currentNorm.id || !currentNorm.title || !currentNorm.ref) {
      throw new Error(`norms 项缺 id/title/ref：${JSON.stringify(currentNorm)}`);
    }
    config.norms.push(currentNorm);
    currentNorm = null;
  };

  for (let i = 0; i < lines.length; i++) {
    const raw = lines[i];
    const lineNo = i + 1;
    if (!raw.trim() || raw.trim().startsWith('#')) continue;

    if (/^\S/.test(raw) && raw.includes(':')) {
      flushNorm();
      mode = null;
      const m = raw.match(/^([A-Za-z_][\w-]*)\s*:\s*(.*)$/);
      if (!m) throw new Error(`L${lineNo}: 无法解析顶层键：${raw.trim()}`);
      const key = m[1];
      const val = m[2].trim();
      if (key === 'docs') {
        if (val !== '') throw new Error(`L${lineNo}: docs 应为映射头，勿写内联值`);
        mode = 'docs';
      } else if (key === 'norms') {
        if (val !== '') throw new Error(`L${lineNo}: norms 应为列表头，勿写内联值`);
        mode = 'norms';
      } else if (key === 'mounts') {
        if (val !== '') throw new Error(`L${lineNo}: mounts 应为映射头，勿写内联值`);
        mode = 'mounts';
        config.mounts = { rules: [] };
      } else if (key === 'project_id' || key === 'role_rule' || key === 'review_skill' || key === 'version_counts' || key === 'gate_on' || key === 'sediment' || key === 'content_root') {
        if (!val && key !== 'review_skill') throw new Error(`L${lineNo}: ${key} 值不能为空`);
        config[key] = val;
      } else {
        throw new Error(`L${lineNo}: 不支持的顶层键「${key}」（受限子集）`);
      }
      continue;
    }

    if (mode === 'docs') {
      const m = raw.match(/^  ([A-Za-z_][\w-]*)\s*:\s*(.+)$/);
      if (!m) throw new Error(`L${lineNo}: docs 下仅允许两空格缩进的标量：${raw}`);
      config.docs[m[1]] = m[2].trim();
      continue;
    }

    if (mode === 'norms') {
      const listHead = raw.match(/^  - id:\s*(.+)$/);
      if (listHead) {
        flushNorm();
        currentNorm = { id: listHead[1].trim() };
        continue;
      }
      const field = raw.match(/^    (id|title|ref|layer):\s*(.+)$/);
      if (field) {
        if (!currentNorm) throw new Error(`L${lineNo}: norms 字段出现在列表项之外`);
        currentNorm[field[1]] = field[2].trim();
        continue;
      }
      throw new Error(`L${lineNo}: norms 仅允许「  - id:」与「    title/ref/id/layer:」：${raw}`);
    }

    if (mode === 'mounts' || mode === 'mounts_rules') {
      const ruleItem = raw.match(/^    - (.+)$/);
      if (mode === 'mounts_rules' && ruleItem) {
        config.mounts.rules.push(ruleItem[1].trim());
        continue;
      }
      if (/^  rules\s*:\s*$/.test(raw)) {
        mode = 'mounts_rules';
        continue;
      }
      const scalar = raw.match(/^  ([A-Za-z_][\w-]*)\s*:\s*(.+)$/);
      if (scalar) {
        mode = 'mounts';
        config.mounts[scalar[1]] = scalar[2].trim();
        continue;
      }
      throw new Error(`L${lineNo}: mounts 行非法：${raw}`);
    }

    throw new Error(`L${lineNo}: 意外缩进或语法（超出受限子集）：${raw}`);
  }
  flushNorm();

  if (!config.project_id) throw new Error('缺 project_id');
  if (!config.role_rule) throw new Error('缺 role_rule');
  if (!config.review_skill) throw new Error('缺 review_skill');
  for (const k of REQUIRED_DOCS) {
    if (!config.docs[k]) throw new Error(`docs 缺键：${k}`);
  }
  if (!config.norms.length) throw new Error('norms 不能为空');
  return config;
}

function loadProjectConfig() {
  if (!existsSync(PROJECT_YAML)) {
    fail('project.yaml 不存在（与 SKILL.md 同目录）');
    return null;
  }
  try {
    const config = parseProjectYaml(read(PROJECT_YAML));
    if (!config.content_root) throw new Error('缺 content_root');
    CONTENT_ROOT = join(SKILL_DIR, config.content_root);
    if (!existsSync(CONTENT_ROOT)) throw new Error(`content_root 不存在：${config.content_root}`);
    MISUSE_MD = join(CONTENT_ROOT, 'misuse.md');
    BACKLOG_MD = join(CONTENT_ROOT, '_backlog.md');
    ok(`project.yaml 受限子集解析成功（norms ${config.norms.length}；content_root ok）`);
    return config;
  } catch (e) {
    fail(`project.yaml 解析失败：${e.message}`);
    return null;
  }
}

function listRefMd() {
  if (!CONTENT_ROOT || !existsSync(CONTENT_ROOT)) return [];
  return readdirSync(CONTENT_ROOT).filter((f) => f.endsWith('.md')).sort();
}

function checkMeta(text, meta) {
  if (!meta.name && !meta.description) {
    fail('SKILL.md frontmatter 缺失或格式非法');
    return;
  }
  if (!meta.name) fail('frontmatter 缺 name');
  else if (meta.name !== basename(SKILL_DIR))
    fail(`frontmatter name（${meta.name}）≠ 目录名（${basename(SKILL_DIR)}）`);
  else ok(`frontmatter name 与目录名一致：${meta.name}`);
  const desc = meta.description || '';
  if (desc.length < 30) fail('description 过短或缺失');
  else ok(`description 长度 ${desc.length}`);
}

function isMigrationStub(absPath) {
  if (!existsSync(absPath)) return false;
  const head = read(absPath).slice(0, 400);
  return /已迁转|migration stub|\(stub\)/i.test(head);
}

function checkRefs(text, config) {
  // 仅统计 content_root 顶层 md：SKILL 路径引用 + 顶层 norms 文件名
  const referencedTop = new Set(
    [
      ...text.matchAll(/skill-references\/kuikly-android-coding\/([A-Za-z0-9_.\-]+\.md)/g),
      ...text.matchAll(/references\/([A-Za-z0-9_.\-]+\.md)/g),
    ].map((m) => m[1]),
  );
  if (config) {
    for (const n of config.norms) {
      const refPath = join(SKILL_DIR, n.ref);
      if (!existsSync(refPath)) fail(`norms.ref 不存在：${n.ref}`);
      // 顶层文件才纳入 content_root 孤儿/缺失对照；子目录与 coding-norms 由 checkConfigPaths 覆盖
      const relFromRoot = refPath.startsWith(CONTENT_ROOT + '/')
        ? refPath.slice(CONTENT_ROOT.length + 1)
        : null;
      if (relFromRoot && !relFromRoot.includes('/')) referencedTop.add(relFromRoot);
    }
  }
  // 迁转 stub 视为已登记（正文禁双源，仅跳转）
  const onDisk = new Set(listRefMd());
  for (const f of onDisk) {
    if (isMigrationStub(join(CONTENT_ROOT, f))) referencedTop.add(f);
  }
  for (const ref of [...referencedTop].filter((r) => !onDisk.has(r)).sort())
    fail(`引用了不存在的文件：content_root/${ref}`);
  for (const f of [...onDisk].filter((r) => !referencedTop.has(r)).sort()) {
    if (f.startsWith('_')) warn(`辅助文件未被 SKILL.md / norms 提及：content_root/${f}`);
    else fail(`孤儿 content_root：${f}`);
  }
  const n = [...referencedTop].filter((r) => onDisk.has(r)).length;
  if (n) ok(`content_root 顶层引用一致（${n} 个；子目录/外置 norms 另验）`);
  return [...onDisk];
}

const DOC_VERSION_RE = /^doc_version:\s*(\d{8}_\d+)\s*$/m;
const LAYER_RE = /^layer:\s*(L[123])\s*$/m;
// L1/L2 统一在 coding-norms/base/，用文件名前缀 lang_* / platform_* 区分
const LAYER_PATH = {
  L1: /coding-norms\/base\/lang_[^/]+\.md$/,
  L2: /coding-norms\/base\/platform_[^/]+\.md$/,
  L3: /kuikly-android-coding\/project\//,
};

/** 码农三层货架：norms[].layer 必填，且与路径、文首 layer 一致（ADR 0038） */
function checkNormLayers(config) {
  if (!config?.norms?.length) return;
  let okCount = 0;
  for (const n of config.norms) {
    const layer = n.layer;
    if (!layer) {
      fail(`norms[${n.id}] 缺 layer（须为 L1|L2|L3）`);
      continue;
    }
    if (!LAYER_PATH[layer]) {
      fail(`norms[${n.id}] layer 非法：${layer}（须为 L1|L2|L3）`);
      continue;
    }
    if (!LAYER_PATH[layer].test(n.ref.replace(/\\/g, '/'))) {
      fail(`norms[${n.id}] layer=${layer} 与路径不符：${n.ref}`);
      continue;
    }
    const abs = join(SKILL_DIR, n.ref);
    if (!existsSync(abs)) continue;
    const bodyLayer = read(abs).match(LAYER_RE)?.[1];
    if (!bodyLayer) fail(`norms[${n.id}] 全文缺文首 layer: L1|L2|L3`);
    else if (bodyLayer !== layer) fail(`norms[${n.id}] yaml layer=${layer} ≠ 文首 layer=${bodyLayer}`);
    else okCount += 1;
  }
  if (okCount) ok(`norms.layer 与路径/文首一致（${okCount}/${config.norms.length}）`);
}

function checkDocVersions(config) {
  const targets = [];
  if (config) {
    for (const n of config.norms) {
      targets.push({ label: `norms[${n.id}]`, path: join(SKILL_DIR, n.ref) });
    }
  }
  if (MISUSE_MD && existsSync(MISUSE_MD)) {
    targets.push({ label: 'misuse.md', path: MISUSE_MD });
  }
  let okCount = 0;
  for (const t of targets) {
    if (!existsSync(t.path)) {
      fail(`doc_version 目标不存在：${t.label}`);
      continue;
    }
    const body = read(t.path);
    const m = body.match(DOC_VERSION_RE);
    if (!m) fail(`${t.label} 缺 doc_version: YYYYMMDD_N`);
    else if (!/^\d{8}_\d+$/.test(m[1])) fail(`${t.label} doc_version 格式非法：${m[1]}`);
    else okCount += 1;
  }
  if (okCount) ok(`doc_version 合规（${okCount}/${targets.length}）`);
}

function checkConfigPaths(config) {
  if (!config) return;
  const rolePath = join(REPO_ROOT, config.role_rule);
  if (!existsSync(rolePath)) fail(`role_rule 文件不存在：${config.role_rule}`);
  else ok(`role_rule 存在：${config.role_rule}`);

  const ledgerRel = config.docs.skill_changelog;
  const ledgerPath = join(REPO_ROOT, ledgerRel);
  if (!existsSync(ledgerPath)) fail(`docs.skill_changelog 不存在：${ledgerRel}`);
  else ok(`skill_changelog 存在：${ledgerRel}`);

  for (const n of config.norms) {
    const refPath = join(SKILL_DIR, n.ref);
    if (!existsSync(refPath)) fail(`norms[].ref 不存在：${n.ref}（${n.title}）`);
  }
}

function ledgerVersion(ledgerPath) {
  if (!existsSync(ledgerPath)) return { best: null, line: `未找到 ${ledgerPath}` };
  let best = null, bestLine = '';
  for (const line of read(ledgerPath).split('\n')) {
    if (!line.startsWith('## ')) continue;
    for (const m of line.matchAll(versionRe())) {
      const n = +m[1];
      if (best === null || n > best) {
        best = n;
        bestLine = line.trim();
      }
    }
  }
  return { best, line: bestLine };
}

function checkVersionSync(text, desc, config) {
  const dm = versionRe().exec(desc);
  const vDesc = dm ? +dm[1] : null;
  const core = [...text.matchAll(/^##\s*(.+?)规范（核心）\s*$/gm)].map((m) => m[1]);
  const dep = [...text.matchAll(/^##\s*(.+?)规范（已废弃[^）]*）\s*$/gm)].map((m) => m[1]);

  if (dep.length) {
    fail(`禁止「已废弃」规范节（退役即删）：发现 ${dep.length} 个：${dep.join(', ')}`);
  } else {
    ok('无「已废弃」规范节');
  }

  if (vDesc === null) fail('description 未声明版本代数 vN');

  if (!config) {
    fail('无 project.yaml，跳过 norms 版本对齐');
    return;
  }

  const titles = config.norms.map((n) => n.title);
  const nNorms = titles.length;
  ok(`norms 清单条数 = ${nNorms}`);

  if (!core.length) fail('未发现「## X规范（核心）」章节');
  else ok(`SKILL 核心索引壳数 = ${core.length}`);

  if (vDesc !== null && vDesc === nNorms) ok(`description 版本 v${vDesc} == norms.length`);
  else if (vDesc !== null) fail(`版本失同步：description=v${vDesc}，norms.length=${nNorms}`);

  if (core.length === nNorms) ok(`核心索引壳数 == norms.length`);
  else fail(`索引壳与 norms 数量不一致：壳=${core.length}，norms=${nNorms}`);

  const coreSet = new Set(core);
  const titleSet = new Set(titles);
  for (const t of titles) {
    if (!coreSet.has(t)) fail(`norms title「${t}」缺少 SKILL「## ${t}规范（核心）」壳`);
  }
  for (const c of core) {
    if (!titleSet.has(c)) fail(`SKILL 壳「${c}」不在 project.yaml norms[].title 中`);
  }

  const ledgerPath = join(REPO_ROOT, config.docs.skill_changelog);
  const { best: vLedger, line: ledgerLine } = ledgerVersion(ledgerPath);
  if (vDesc !== null && vLedger !== null) {
    if (vDesc === vLedger) ok(`description 版本 v${vDesc} == changelog 最高版本`);
    else fail(`版本失同步：description=v${vDesc}，changelog=v${vLedger}（${ledgerLine}）`);
  } else if (vDesc !== null) fail(`无法从 skill changelog 读到版本号：${ledgerLine}`);

  for (const name of titles) {
    if (desc.includes(name)) ok(`规范「${name}」已在 description 声明`);
    else fail(`规范「${name}」未出现在 description`);
  }
}

function checkVersionCopies(text, refNames, fmEnd) {
  const offenders = [];
  text.split('\n').forEach((line, i) => {
    if (fmEnd && i + 1 <= fmEnd) return;
    if (versionRe().test(line)) offenders.push(`SKILL.md:${i + 1}`);
  });
  for (const name of refNames) {
    read(join(CONTENT_ROOT, name)).split('\n').forEach((line, i) => {
      versionRe().lastIndex = 0;
      if (versionRe().test(line)) offenders.push(`content_root/${name}:${i + 1}`);
    });
  }
  if (offenders.length)
    fail(`版本副本残留 ${offenders.length} 处：${offenders.slice(0, 6).join(', ')}`);
  else ok('无版本副本残留（单一真源）');
}

function checkMisuse() {
  if (!MISUSE_MD || !existsSync(MISUSE_MD)) {
    warn('content_root/misuse.md 不存在');
    return;
  }
  const nums = [...read(MISUSE_MD).matchAll(/^#{2,3}\s*错误\s*(\d+)/gm)].map((m) => +m[1]);
  if (!nums.length) warn('misuse.md 未发现「错误 N」条目');
  else if (nums.join(',') !== [...Array(nums.length)].map((_, i) => i + 1).join(','))
    fail(`misuse.md 错误编号不连续或有重复：${nums}`);
  else ok(`misuse 反例编号连续：1..${nums.length}`);
}

function checkSize(text) {
  const nLines = text.split('\n').length;
  const nChars = text.length;
  if (nLines >= FAIL_LINES || nChars >= FAIL_CHARS)
    fail(`SKILL.md 过大（${nLines} 行 / ${nChars} 字符）`);
  else if (nLines >= WARN_LINES || nChars >= WARN_CHARS)
    warn(`SKILL.md 逼近阈值（${nLines} 行 / ${nChars} 字符）`);
  else ok(`SKILL.md 体量正常（${nLines} 行 / ${nChars} 字符）`);
}

function checkMechanism(text) {
  if (!text.includes('sediment: off') || !text.includes('mounts')) {
    fail('SKILL.md 缺少挂载引导（须含 sediment: off 与 mounts）');
  } else ok('挂载引导在位（沉淀步骤在 mounts.sediment_process）');
}

function checkBacklog() {
  if (!BACKLOG_MD || !existsSync(BACKLOG_MD)) {
    warn('content_root/_backlog.md 不存在');
    return;
  }
  const bl = read(BACKLOG_MD);
  const mCand = bl.match(/^##\s*候选\s*$/m);
  const mDone = bl.match(/^##\s*已晋升\s*$/m);
  if (!mCand || !mDone) {
    fail('content_root/_backlog.md 缺「## 候选」或「## 已晋升」');
    return;
  }
  if (!bl.includes('未批准')) warn('_backlog.md 未写明「未批准」红线');
  const iCand = bl.search(/^##\s*候选\s*$/m);
  const iDone = bl.search(/^##\s*已晋升\s*$/m);
  let candBody, doneBody;
  if (iCand < iDone) {
    candBody = bl.slice(bl.indexOf('\n', iCand) + 1, iDone);
    doneBody = bl.slice(bl.indexOf('\n', iDone) + 1);
  } else {
    doneBody = bl.slice(bl.indexOf('\n', iDone) + 1, iCand);
    candBody = bl.slice(bl.indexOf('\n', iCand) + 1);
  }
  const stray = candBody.split('\n').filter((l) => /^\s*-\s*\[[xX]\]/.test(l) && l.includes('已晋升'));
  if (stray.length) fail(`_backlog「候选」残留已晋升行：${stray.map((s) => s.trim().slice(0, 48))}`);
  const candEntries = candBody.split('\n').filter((l) => /^\s*-\s*\[[ xX]\]/.test(l));
  const doneEntries = doneBody.split('\n').filter((l) => /^\s*-\s*\[[xX]\]/.test(l));
  const noEv = candEntries.filter((l) => !l.includes('证据'));
  if (noEv.length) warn(`_backlog 候选有 ${noEv.length} 条缺「证据：」`);
  const pending = candEntries.filter((l) => /^\s*-\s*\[\s\]/.test(l)).length;
  ok(`_backlog 格式合规（候选待批 ${pending} / 已晋升 ${doneEntries.length}）`);
}

function checkFirstLine(refNames) {
  for (const name of refNames) {
    const first = read(join(CONTENT_ROOT, name)).split('\n').find((l) => l.trim()) || '';
    if (/^doc_version:\s*\d{8}_\d+\s*$/.test(first) || /^#\s/.test(first)) continue;
    warn(`content_root/${name} 首行非标题/doc_version`);
  }
}

function report() {
  const icon = { FAIL: 'x', WARN: '!', OK: 'v' };
  for (const level of ['FAIL', 'WARN', 'OK']) {
    for (const [lv, msg] of results) if (lv === level) console.log(`  [${icon[lv]}] ${msg}`);
  }
  const nFail = results.filter(([lv]) => lv === 'FAIL').length;
  const nWarn = results.filter(([lv]) => lv === 'WARN').length;
  console.log(`\n${'-'.repeat(64)}\nFAIL ${nFail} / WARN ${nWarn}`);
  console.log('FAIL 必修；WARN 记入 docs/specs/kuikly-dev-android/changelog.md');
  return nFail ? 1 : 0;
}

function main() {
  if (!existsSync(SKILL_MD)) {
    fail(`SKILL.md 不存在：${SKILL_MD}`);
    return report();
  }
  const text = read(SKILL_MD);
  const { meta, endLines } = readFrontmatter(text);
  checkMeta(text, meta);
  const config = loadProjectConfig();
  checkConfigPaths(config);
  const refNames = checkRefs(text, config);
  checkVersionSync(text, meta.description || '', config);
  checkVersionCopies(text, refNames, endLines);
  checkDocVersions(config);
  checkNormLayers(config);
  checkMisuse();
  checkSize(text);
  checkMechanism(text);
  checkBacklog();
  checkFirstLine(refNames);
  const code = report();
  if (code) return code;
  const shared = join(SKILL_DIR, '..', '..', '..', 'docs', 'skill-references', 'scripts', 'check_skill.mjs');
  const child = spawnSync(process.execPath, [shared, SKILL_DIR], { stdio: 'inherit' });
  return child.status ?? 1;
}

process.exit(main());
