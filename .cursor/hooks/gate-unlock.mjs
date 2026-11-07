#!/usr/bin/env node
/**
 * 编码前确认闸 · 解锁 / 开关（Cursor）
 *
 *   node .cursor/hooks/gate-unlock.mjs on|off|disable|enable
 */
import { writeFileSync, rmSync, readdirSync, statSync, existsSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { createHash } from 'node:crypto';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const PROJECT_ROOT = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const OFF_FILE = join(PROJECT_ROOT, '.cursor', 'hooks', '.gate-off');

const projectDir =
  process.env.CURSOR_PROJECT_DIR ||
  process.env.CODEBUDDY_PROJECT_DIR ||
  process.env.CLAUDE_PROJECT_DIR ||
  PROJECT_ROOT;
const TAG = createHash('md5').update(projectDir).digest('hex').slice(0, 8);
const TMP = tmpdir();
const PREFIX = `cursor-gate-${TAG}-`;
const UNLOCK_MANUAL = join(TMP, `${PREFIX}unlock`);
const TTL_MS = 20 * 60 * 1000;

const mode = (process.argv[2] || '').toLowerCase();

function listFiles() {
  try {
    return readdirSync(TMP).filter((f) => f.startsWith(PREFIX));
  } catch {
    return [];
  }
}

function statusOf(file) {
  try {
    const left = Math.round((TTL_MS - (Date.now() - statSync(file).mtimeMs)) / 1000);
    return left > 0 ? `有效，剩余约 ${Math.ceil(left / 60)} 分钟` : '已过期';
  } catch {
    return '不存在';
  }
}

if (mode === 'on') {
  writeFileSync(UNLOCK_MANUAL, String(Date.now()));
  console.log('✅ 已解锁：20 分钟内本项目写入不再被拦');
  console.log(`   标记文件：${UNLOCK_MANUAL}`);
} else if (mode === 'off') {
  let n = 0;
  for (const f of listFiles()) {
    try {
      rmSync(join(TMP, f));
      n++;
    } catch {
      /* ignore */
    }
  }
  console.log(`🔒 已恢复拦截，清理了 ${n} 个标记文件`);
} else if (mode === 'disable') {
  writeFileSync(OFF_FILE, `${new Date().toISOString()}\n关闭者：本机用户\n`);
  console.log('⛔ 已彻底关闭本项目的编码前确认闸（本机生效）');
  console.log(`   开关文件：${OFF_FILE}（应 gitignore）`);
  console.log('   重新开启：node .cursor/hooks/gate-unlock.mjs enable');
} else if (mode === 'enable') {
  try {
    rmSync(OFF_FILE);
    console.log('✅ 已重新开启拦截');
  } catch {
    console.log('拦截本来就是开启状态');
  }
} else {
  const envOff = (process.env.CODING_GATE || '').toLowerCase() === 'off';
  const fileOff = existsSync(OFF_FILE);
  console.log('编码前确认闸 · 状态（Cursor）');
  console.log(`  项目目录：${projectDir}`);
  console.log(`  闸门开关：${envOff || fileOff ? '⛔ 已关闭' : '🟢 开启'}`);
  console.log(`  手动解锁：${statusOf(UNLOCK_MANUAL)}`);
  console.log('  用法：gate-unlock.mjs on | off | disable | enable');
}
