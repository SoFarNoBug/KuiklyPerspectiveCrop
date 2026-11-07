#!/usr/bin/env node
/**
 * 编码前确认闸 · Cursor hooks（beforeSubmitPrompt / preToolUse / postToolUse）
 * 自 CodeBuddy 版移植，路径与协议改为 .cursor。
 *
 * stdout 只输出一个 JSON；异常 fail-open。
 * 关闭：CODING_GATE=off 或 .cursor/hooks/.gate-off
 * 解锁：放行词 / node .cursor/hooks/gate-unlock.mjs on
 */
import { readFileSync, writeFileSync, appendFileSync, existsSync, statSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { createHash } from 'node:crypto';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const PROJECT_ROOT = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const OFF_FILE = join(PROJECT_ROOT, '.cursor', 'hooks', '.gate-off');

if ((process.env.CODING_GATE || '').toLowerCase() === 'off' || existsSync(OFF_FILE)) {
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
const P = (name) => join(TMP, `cursor-gate-${TAG}-${name}`);

const LOG = P('log');
const UNLOCK_MANUAL = P('unlock');
const unlockSession = (sid) => P(`unlock-${sid || 'default'}`);
const approvedStore = (sid) => P(`approved-${sid || 'default'}.json`);
const denyStore = (sid) => P(`deny-${sid || 'default'}.json`);

const UNLOCK_TTL_MS = 20 * 60 * 1000;
const MAX_DENY = 6;

const WRITE_TOOLS = new Set([
  'Write', 'Edit', 'StrReplace', 'Delete', 'DeleteFile', 'EditNotebook',
  'write_to_file', 'replace_in_file', 'multi_edit', 'multiEdit', 'apply_patch',
  'create_file', 'write_file', 'edit_file', 'str_replace', 'str_replace_editor',
  'delete_file', 'deleteFile', 'remove_file', 'move_file', 'rename_file',
  'copy_file', 'save_file', 'notebook_edit'
]);

const NON_FILE_TOOLS = new Set([
  'todo_write', 'todoWrite', 'TodoWrite',
  'plan_create', 'plan_update', 'create_plan', 'update_plan', 'CreatePlan',
  'SwitchMode', 'AskQuestion', 'Task'
]);

const WRITE_KEYWORD = /(write|edit|create|delete|remove|rename|move|patch|apply|save|str_replace)/i;

function isWriteTool(name) {
  if (!name) return false;
  if (NON_FILE_TOOLS.has(name)) return false;
  if (WRITE_TOOLS.has(name)) return true;
  return WRITE_KEYWORD.test(name);
}

const UNLOCK_PROMPT_RE = /(^\s*1\s*$|直接改|直接开始|跳过确认|不用问|开始执行|按计划执行|执行计划|照做|就这么改|可以改|接受计划|批准计划|同意计划|立即开始|开始吧|开始编码)/;
const BLOCK_PROMPT_RE = /(先出计划|先看计划|先出方案|不要改|别改|先别|暂停)/;

const GATE_PATHS = ['/.cursor/hooks.json', '/.cursor/hooks/'];
const EXEMPT_SEGMENTS = ['/tmp/', '/memory/', '/docs/', '/.git/'];
const ALLOW_EXT = new Set(['.md', '.markdown', '.txt', '.log', '.json', '.csv', '.mdc']);

function extOf(p) {
  const i = p.lastIndexOf('.');
  if (i < 0) return '';
  const slash = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\'));
  if (i < slash) return '';
  return p.slice(i).toLowerCase();
}

function isProtected(p) {
  if (!p) return true;
  const norm = p.replace(/\\/g, '/');
  if (GATE_PATHS.some((g) => norm.includes(g))) return true;
  if (EXEMPT_SEGMENTS.some((seg) => norm.includes(seg))) return false;
  if (ALLOW_EXT.has(extOf(norm))) return false;
  return true;
}

function isFresh(file) {
  try {
    return Date.now() - statSync(file).mtimeMs < UNLOCK_TTL_MS;
  } catch {
    return false;
  }
}

function isUnlocked(sid) {
  return isFresh(unlockSession(sid)) || isFresh(UNLOCK_MANUAL);
}

function touchUnlock(file) {
  try {
    writeFileSync(file, String(Date.now()));
    return true;
  } catch {
    return false;
  }
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
    user_message: '编码前确认闸：请先选择 1（直接编码）或 2（先出计划）。',
    agent_message:
      `编码前确认闸：本次操作（${tool}）已被阻止（第 ${times}/${MAX_DENY} 次）。` +
      `请立即停止继续改动文件，先向用户提问：` +
      `「1. 直接开始编码 / 2. 先出执行计划，我确认后再编码」，等用户明确选择。` +
      `若用户已明确放行，请执行：node .cursor/hooks/gate-unlock.mjs on` +
      `（有效期 20 分钟），然后重试。` +
      `（目标：${target || '(未识别路径)'}）`
  });
}

function loadList(file, key) {
  try {
    if (!existsSync(file)) return [];
    return JSON.parse(readFileSync(file, 'utf8'))[key] || [];
  } catch {
    return [];
  }
}

function rememberApproved(file, target) {
  try {
    const list = loadList(file, 'paths');
    if (target && !list.includes(target)) {
      list.push(target);
      writeFileSync(file, JSON.stringify({ ts: Date.now(), paths: list }));
      return true;
    }
  } catch {
    /* ignore */
  }
  return false;
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

try {
  const input = JSON.parse(readFileSync(0, 'utf8'));
  const event = input.hook_event_name || input.event || 'preToolUse';
  const sid = input.session_id;

  if (event === 'UserPromptSubmit' || event === 'beforeSubmitPrompt') {
    const prompt = input.prompt || input.text || '';
    const hit = !BLOCK_PROMPT_RE.test(prompt) && UNLOCK_PROMPT_RE.test(prompt);
    log(`PROMPT hit=${hit} text="${String(prompt).slice(0, 80).replace(/\s+/g, ' ')}"`);
    if (hit) {
      const ok = touchUnlock(unlockSession(sid));
      log(`AUTO-UNLOCK sid=${sid} ok=${ok}`);
    }
    allow();
  }

  const tool = input.tool_name || input.toolName || '';
  const ti = input.tool_input || input.toolInput || input.arguments || {};
  const target =
    ti.filePath || ti.file_path || ti.path || ti.target_file || ti.targetFile || ti.target_notebook || '';

  if (!isWriteTool(tool)) {
    allow();
  }

  if (!WRITE_TOOLS.has(tool)) {
    log(`WARN-NEW-WRITE-TOOL tool=${tool} target=${target}`);
  }

  if (!target) {
    if (WRITE_TOOLS.has(tool)) {
      log(`WARN-NO-PATH-KNOWN-WRITE tool=${tool}`);
    } else {
      log(`SKIP-NO-PATH tool=${tool}`);
      allow();
    }
  }

  if (event === 'PostToolUse' || event === 'postToolUse') {
    log(rememberApproved(approvedStore(sid), target) ? `REMEMBER ${target}` : `POST ${target}`);
    allow();
  }

  if (!isProtected(target)) {
    log(`SKIP-NON-CODE ${target}`);
    allow();
  }

  if (isUnlocked(sid)) {
    log(`UNLOCKED ${target} sid=${sid}`);
    allow();
  }

  if (target && loadList(approvedStore(sid), 'paths').includes(target)) {
    log(`ALREADY-OK ${target}`);
    allow();
  }

  const times = bumpDeny(denyStore(sid), target);
  if (times >= MAX_DENY) {
    log(`FUSE-OPEN ${target} (deny×${times})`);
    allow({
      additional_context: `⚠️ 编码前确认闸：${target} 被拦 ${times} 次后已自动放行。`
    });
  }

  log(`DENY ${target} (${times}/${MAX_DENY}) tool=${tool}`);
  deny(target, times, tool);
} catch (e) {
  log(`ERR ${e && e.message ? e.message : e}`);
  allow();
}
