/**
 * 一次性：把 yarn-v1.22.17 放到 Gradle Kotlin/JS 可读的本地 ivy 布局
 *（绕过 GitHub releases 超时）。
 *
 *   node static_server/fetch-yarn-ivy.js
 */
const https = require('https')
const fs = require('fs')
const path = require('path')
const { execSync } = require('child_process')
const os = require('os')

const VERSION = '1.22.17'
const URL = `https://mirrors.cloud.tencent.com/npm/yarn/-/yarn-${VERSION}.tgz`
const ivyDir = path.join(os.homedir(), '.gradle', 'caches', 'manual-yarn', 'ivy', `v${VERSION}`)
const outTar = path.join(ivyDir, `yarn-v${VERSION}.tar.gz`)

fs.mkdirSync(ivyDir, { recursive: true })
const tmp = path.join(os.tmpdir(), `yarn-${VERSION}.tgz`)

function download(url, dest) {
  return new Promise((resolve, reject) => {
    const file = fs.createWriteStream(dest)
    https
      .get(url, (res) => {
        if (res.statusCode && res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          file.close()
          fs.unlinkSync(dest)
          download(res.headers.location, dest).then(resolve, reject)
          return
        }
        if (res.statusCode !== 200) {
          reject(new Error(`HTTP ${res.statusCode}`))
          return
        }
        res.pipe(file)
        file.on('finish', () => file.close(resolve))
      })
      .on('error', reject)
  })
}

;(async () => {
  console.log('download', URL)
  await download(URL, tmp)
  const extract = path.join(os.tmpdir(), `yarn-extract-${VERSION}`)
  fs.rmSync(extract, { recursive: true, force: true })
  fs.mkdirSync(extract, { recursive: true })
  execSync(`tar -xzf "${tmp}" -C "${extract}"`, { stdio: 'inherit' })
  // tencent tgz 解压后已是 yarn-v1.22.17/
  const folder = path.join(extract, `yarn-v${VERSION}`)
  if (!fs.existsSync(folder)) {
    throw new Error(`expected ${folder}`)
  }
  execSync(`tar -czf "${outTar}" -C "${extract}" yarn-v${VERSION}`, { stdio: 'inherit' })
  console.log('wrote', outTar, fs.statSync(outTar).size)
})().catch((e) => {
  console.error(e)
  process.exit(1)
})
