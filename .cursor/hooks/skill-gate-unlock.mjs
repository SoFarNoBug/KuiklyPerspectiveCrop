#!/usr/bin/env node
/**
 * 编码 Skill 硬闸 · 解锁 / 开关
 *
 *   node .cursor/hooks/skill-gate-unlock.mjs on|off|disable|enable|status
 */
import { writeFileSync, rmSync, readdirSync, statSync, existsSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { createHash } from 'node:crypto';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const PROJECT_ROOT = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const OFF_FILE = join(PROJECT_ROOT, '.cursor', 'hooks', '.skill-gate-off');

const projectDir =
  process.env.CURSOR_PROJECT_DIR ||
  process.env.CODEBUDDY_PROJECT_DIR ||
  process.env.CLAUDE_PROJECT_DIR ||
  PROJECT_ROOT;
const TAG = createHash('md5').update(projectDir).digest('hex').slice(0, 8);
const TMP = tmpdir();
const PREFIX = `cursor-skill-gate-${TAG}-`;
const ARMED_MANUAL = join(TMP, `${PREFIX}armed-manual`);
const TTL_MS = 20 * 60 * 1000;

const mode = (process.argv[2] || 'status').toLowerCase();

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
  writeFileSync(ARMED_MANUAL, String(Date.now()));
  console.log('✅ Skill 硬闸已手动武装：20 分钟内可写 .kt/.java/.kts');
  console.log(`   标记：${ARMED_MANUAL}`);
  console.log('   正式流程仍应 Read .cursor/skills/kuikly-android-coding/SKILL.md');
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
  console.log(`🔒 已清除 Skill 武装标记（${n} 个），写业务源码需重新 Read SKILL`);
} else if (mode === 'disable') {
  writeFileSync(OFF_FILE, `${new Date().toISOString()}\n关闭者：本机用户\n`);
  console.log('⛔ 已彻底关闭本项目编码 Skill 硬闸（本机）');
  console.log(`   ${OFF_FILE}`);
  console.log('   重新开启：node .cursor/hooks/skill-gate-unlock.mjs enable');
} else if (mode === 'enable') {
  try {
    rmSync(OFF_FILE);
    console.log('✅ 已重新开启 Skill 硬闸');
  } catch {
    console.log('Skill 硬闸本来就是开启状态');
  }
} else {
  const envOff = (process.env.CODING_SKILL_GATE || '').toLowerCase() === 'off';
  const fileOff = existsSync(OFF_FILE);
  console.log('编码 Skill 硬闸 · 状态');
  console.log(`  项目：${projectDir}`);
  console.log(`  开关：${envOff || fileOff ? '⛔ 已关闭' : '🟢 开启'}`);
  console.log(`  手动武装：${statusOf(ARMED_MANUAL)}`);
  console.log('  用法：on | off | disable | enable | status');
}
