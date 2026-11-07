/**
 * 本仓精简 H5 业务 JS 静态服（无 whistle、无 npm 依赖）。
 * 在 8083 提供 packLocalJsBundle* 产出的 nativevue2.js，
 * 满足 h5App/src/jsMain/resources/index.html 的开发期引用。
 *
 * 用法：node static_server/serve/index.js
 * 可选环境变量：H5_BUNDLE_PORT、H5_BUNDLE_DIR（绝对或相对仓库根）
 */
const http = require('http')
const fs = require('fs')
const path = require('path')

const PORT = Number(process.env.H5_BUNDLE_PORT || 8083)
const REPO_ROOT = path.join(__dirname, '..', '..')

const CANDIDATE_DIRS = [
  process.env.H5_BUNDLE_DIR,
  // webpack / distribution（packLocalJsBundleDebug 实际可读的 js）
  'perspectiveCropDemo/build/dist/js/developmentExecutable',
  'perspectiveCropDemo/build/kotlin-webpack/js/developmentExecutable',
  'perspectiveCropDemo/build/dist/js/productionExecutable',
  'perspectiveCropDemo/build/outputs/kuikly/js/debug/local',
  'perspectiveCropDemo/build/outputs/kuikly/js/release/local',
].filter(Boolean)

function resolveBundleDir() {
  for (const rel of CANDIDATE_DIRS) {
    const abs = path.isAbsolute(rel) ? rel : path.join(REPO_ROOT, rel)
    const js = path.join(abs, 'nativevue2.js')
    if (fs.existsSync(js) && fs.statSync(js).size > 100_000) {
      return abs
    }
  }
  return null
}

function contentType(filePath) {
  if (filePath.endsWith('.js')) return 'application/javascript; charset=utf-8'
  if (filePath.endsWith('.map')) return 'application/json; charset=utf-8'
  if (filePath.endsWith('.wasm')) return 'application/wasm'
  return 'application/octet-stream'
}

const bundleDir = resolveBundleDir()

const server = http.createServer((req, res) => {
  if (!bundleDir) {
    res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' })
    res.end(
      'nativevue2.js not found. Run:\n' +
        '  ./gradlew :perspectiveCropDemo:packLocalJsBundleDebug\n' +
        'then restart this server.\n' +
        `Looked under:\n${CANDIDATE_DIRS.map((d) => '  - ' + d).join('\n')}\n`,
    )
    return
  }

  const urlPath = decodeURIComponent((req.url || '/').split('?')[0])
  const rel = urlPath === '/' ? '/nativevue2.js' : urlPath
  const safeRel = path.normalize(rel).replace(/^(\.\.[/\\])+/, '')
  const filePath = path.join(bundleDir, safeRel)

  if (!filePath.startsWith(bundleDir)) {
    res.writeHead(403)
    res.end('forbidden')
    return
  }

  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' })
      res.end(`not found: ${rel}\nroot=${bundleDir}\n`)
      return
    }
    res.writeHead(200, {
      'Content-Type': contentType(filePath),
      'Access-Control-Allow-Origin': '*',
      'Cache-Control': 'no-store',
    })
    res.end(data)
  })
})

server.listen(PORT, '127.0.0.1', () => {
  if (bundleDir) {
    console.log(`[h5-bundle-serve] root=${bundleDir}`)
  } else {
    console.warn('[h5-bundle-serve] nativevue2.js missing — 404 until pack completes')
  }
  console.log(`[h5-bundle-serve] http://127.0.0.1:${PORT}/nativevue2.js`)
})
