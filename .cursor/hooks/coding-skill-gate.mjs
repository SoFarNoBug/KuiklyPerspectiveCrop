#!/usr/bin/env node
/**
 * 编码 Skill 硬闸 · Cursor hooks（beforeSubmitPrompt / preToolUse / postToolUse）
 *
 * 目的：拦住「Implement Plan / 按计划改码」时未 Read kuikly-android-coding/SKILL.md
 * 就写入业务源码（.kt / .java / .kts）。
 *
 * 武装（任一条即可写业务源）：
 * 1. 本会话 postToolUse 观测到 Read …/kuikly-android-coding/SKILL.md
 * 2. 用户提示命中「速速改 / 跳过 skill」→ 速速改武装（TTL）
 * 3. node .cursor/hooks/skill-gate-unlock.mjs on
 * 4. CODING_SKILL_GATE=off 或 .cursor/hooks/.skill-gate-off
 *
 * stdout 只输出一个 JSON；异常 fail-open。
 */
import { readFileSync, writeFileSync, appendFileSync, existsSync, statSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { createHash } from 'node:crypto';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const PROJECT_ROOT = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const OFF_FILE = join(PROJECT_ROOT, '.cursor', 'hooks', '.skill-gate-off');

if ((process.env.CODING_SKILL_GATE || '').toLowerCase() === 'off' || existsSync(OFF_FILE)) {
  process.stdout.write(JSON.stringify({ permission: 'allow' }));
  process.exit(0);
}

const projectDir =
  process.env.CURSOR_PROJECT_DIR ||
  process.env.CODEBUDDY_PROJECT_DIR ||
  process.env.CLAUDE_PROJECT_DIR ||
  PROJECT_ROOT;
const TAG = createHash('md5').update(projectDir).digest('hex').slice(0, 8);
const TMP = tmpdir();
const P = (name) => join(TMP, `cursor-skill-gate-${TAG}-${name}`);

const LOG = P('log');
const ARMED_MANUAL = P('armed-manual');
const armedSession = (sid) => P(`armed-${sid || 'default'}`);
const fastSession = (sid) => P(`fast-${sid || 'default'}`);
const denyStore = (sid) => P(`deny-${sid || 'default'}.json`);

const TTL_MS = 20 * 60 * 1000;
const MAX_DENY = 8;

const WRITE_TOOLS = new Set([
  'Write', 'Edit', 'StrReplace', 'Delete', 'DeleteFile', 'EditNotebook',
  'write_to_file', 'replace_in_file', 'multi_edit', 'multiEdit', 'apply_patch',
  'create_file', 'write_file', 'edit_file', 'str_replace', 'str_replace_editor',
  'delete_file', 'deleteFile', 'remove_file', 'move_file', 'rename_file',
  'copy_file', 'save_file', 'notebook_edit'
]);

const READ_TOOLS = new Set(['Read', 'read_file', 'readFile', 'ReadFile']);

const NON_FILE_TOOLS = new Set([
  'todo_write', 'todoWrite', 'TodoWrite',
  'plan_create', 'plan_update', 'create_plan', 'update_plan', 'CreatePlan',
  'SwitchMode', 'AskQuestion', 'Task', 'Shell', 'AwaitShell'
]);

const WRITE_KEYWORD = /(write|edit|create|delete|remove|rename|move|patch|apply|save|str_replace)/i;

const FAST_PROMPT_RE =
  /(速速改|速速开工|跳过\s*skill|跳过编码\s*skill|跳过\s*编码skill|CODING_SKILL_GATE\s*=\s*off)/i;

const SKILL_PATH_RE = /kuikly-android-coding[/\\]SKILL\.md$/i;

/** 业务源码扩展名（需 skill 武装后才可写） */
const CODE_EXT = new Set(['.kt', '.java', '.kts']);

/** 始终放行（文档 / 闸门自身 / skills 元数据） */
const EXEMPT_SEGMENTS = [
  '/tmp/',
  '/memory/',
  '/docs/',
  '/.git/',
  '/.cursor/hooks/',
  '/.cursor/rules/',
  '/.cursor/skills/',
  '/.cursor/agents-guide'
];

function isWriteTool(name) {
  if (!name) return false;
  if (NON_FILE_TOOLS.has(name)) return false;
  if (WRITE_TOOLS.has(name)) return true;
  return WRITE_KEYWORD.test(name);
}

function isReadTool(name) {
  return !!name && READ_TOOLS.has(name);
}

function extOf(p) {
  const norm = p.replace(/\\/g, '/');
  const i = norm.lastIndexOf('.');
  if (i < 0) return '';
  const slash = norm.lastIndexOf('/');
  if (i < slash) return '';
  return norm.slice(i).toLowerCase();
}

function isExemptPath(p) {
  if (!p) return false;
  const norm = p.replace(/\\/g, '/');
  if (EXEMPT_SEGMENTS.some((seg) => norm.includes(seg))) return true;
  return false;
}

/** 需要 skill 武装的目标 */
function needsSkillArm(p) {
  if (!p) return false;
  if (isExemptPath(p)) return false;
  return CODE_EXT.has(extOf(p));
}

function isFresh(file) {
  try {
    return Date.now() - statSync(file).mtimeMs < TTL_MS;
  } catch {
    return false;
  }
}

function touch(file) {
  try {
    writeFileSync(file, String(Date.now()));
    return true;
  } catch {
    return false;
  }
}

function isArmed(sid) {
  return isFresh(armedSession(sid)) || isFresh(ARMED_MANUAL) || isFresh(fastSession(sid));
}

function log(msg) {
  try {
    appendFileSync(LOG, `${new Date().toISOString()} ${msg}\n`);
  } catch {
    /* ignore */
  }
}

function emit(payload) {
  process.stdout.write(JSON.stringify(payload));
  process.exit(0);
}

function allow(extra) {
  emit({ permission: 'allow', ...(extra || {}) });
}

function deny(target, times, tool) {
  emit({
    permission: 'deny',
    user_message:
      '编码 Skill 硬闸：请先 Read `.cursor/skills/kuikly-android-coding/SKILL.md`（及 project.yaml）再改业务源码；琐碎改可说「速速改」。',
    agent_message:
      `编码 Skill 硬闸：本次操作（${tool} → ${target || '(无路径)'}）已被阻止（第 ${times}/${MAX_DENY} 次）。` +
      `禁止继续改 .kt/.java/.kts。` +
      `正确顺序：1) Read \`.cursor/skills/kuikly-android-coding/SKILL.md\` 与同目录 project.yaml；` +
      `2) 按 skill「按计划行事」编码并输出 §实现回报。` +
      `琐碎改：用户说「速速改」后重试。` +
      `手动武装：node .cursor/hooks/skill-gate-unlock.mjs on（20 分钟）。` +
      `彻底关闭：CODING_SKILL_GATE=off 或 skill-gate-unlock.mjs disable。`
  });
}

function bumpDeny(file, target) {
  try {
    let map = {};
    if (existsSync(file)) map = JSON.parse(readFileSync(file, 'utf8')).map || {};
    const n = (map[target] || 0) + 1;
    map[target] = n;
    writeFileSync(file, JSON.stringify({ ts: Date.now(), map }));
    return n;
  } catch {
    return MAX_DENY;
  }
}

function extractPath(ti) {
  return (
    ti.filePath ||
    ti.file_path ||
    ti.path ||
    ti.target_file ||
    ti.targetFile ||
    ti.target_notebook ||
    ''
  );
}

try {
  const input = JSON.parse(readFileSync(0, 'utf8'));
  const event = input.hook_event_name || input.event || 'preToolUse';
  const sid = input.session_id;

  if (event === 'UserPromptSubmit' || event === 'beforeSubmitPrompt') {
    const prompt = input.prompt || input.text || '';
    if (FAST_PROMPT_RE.test(prompt)) {
      const ok = touch(fastSession(sid));
      log(`FAST-ARM sid=${sid} ok=${ok}`);
      allow({
        additional_context:
          '编码 Skill 硬闸：已按「速速改」武装本会话（20 分钟）。仍建议遵守角色 Rule；正式 Plan 执行请改走 Read SKILL。'
      });
    }
    allow();
  }

  const tool = input.tool_name || input.toolName || '';
  const ti = input.tool_input || input.toolInput || input.arguments || {};
  const target = extractPath(ti);

  // 观测 Read SKILL → 武装
  if (
    (event === 'PostToolUse' || event === 'postToolUse') &&
    isReadTool(tool) &&
    target &&
    SKILL_PATH_RE.test(target.replace(/\\/g, '/'))
  ) {
    const ok = touch(armedSession(sid));
    log(`SKILL-ARM sid=${sid} ok=${ok} path=${target}`);
    allow({
      additional_context:
        '编码 Skill 硬闸：已观测到 Read kuikly-android-coding/SKILL.md，本会话可写业务源码（20 分钟）。请继续读 project.yaml 与命中 norms，收尾交 §实现回报。'
    });
  }

  if (event === 'PostToolUse' || event === 'postToolUse') {
    allow();
  }

  if (!isWriteTool(tool)) {
    allow();
  }

  if (!target) {
    if (WRITE_TOOLS.has(tool)) {
      log(`WARN-NO-PATH tool=${tool}`);
      // 无路径的已知写工具：保守拦截若未武装
      if (!isArmed(sid)) {
        const times = bumpDeny(denyStore(sid), '(no-path)');
        if (times >= MAX_DENY) {
          log(`FUSE-OPEN no-path deny×${times}`);
          allow({
            additional_context: `⚠️ 编码 Skill 硬闸：无路径写工具被拦 ${times} 次后熔断放行。`
          });
        }
        deny('(no-path)', times, tool);
      }
    }
    allow();
  }

  if (!needsSkillArm(target)) {
    log(`SKIP-NON-CODE ${target}`);
    allow();
  }

  if (isArmed(sid)) {
    log(`ARMED-OK ${target} sid=${sid}`);
    allow();
  }

  const times = bumpDeny(denyStore(sid), target);
  if (times >= MAX_DENY) {
    log(`FUSE-OPEN ${target} deny×${times}`);
    allow({
      additional_context: `⚠️ 编码 Skill 硬闸：${target} 被拦 ${times} 次后已自动放行。请补跑 kuikly-android-coding。`
    });
  }

  log(`DENY ${target} (${times}/${MAX_DENY}) tool=${tool}`);
  deny(target, times, tool);
} catch (e) {
  log(`ERR ${e && e.message ? e.message : e}`);
  allow();
}
