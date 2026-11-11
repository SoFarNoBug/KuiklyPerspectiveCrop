#!/bin/bash
#
# 发布 KuiklyPerspectiveCropCore 到 Maven Central（别人可 mavenCentral() 直接依赖）
#
# 本阶段：主 settings（Android AAR + iOS/JS KMP 变体）；鸿蒙另议。
#
# 用法：
#   CENTRAL_USERNAME=… CENTRAL_PASSWORD=… GPG_KEY_PASSWORD=… ./publish-maven.sh
#
# 凭证（均走环境变量，不落盘）：
#   CENTRAL_USERNAME / CENTRAL_PASSWORD  — central.sonatype.com User Token
#   GPG_KEY_PASSWORD                     — 本机密钥口令
#   GPG_KEY_ID（可选）                   — 默认 E03218FD37956AC6AE94223E041C34E7F024F7A0
#   MAVEN_VERSION（可选）                — 默认 1.0.0-2.30.0

set -euo pipefail

MODULE=KuiklyPerspectiveCropCore
VERSION="${MAVEN_VERSION:-1.0.0-2.30.0}"
GPG_KEY_ID="${GPG_KEY_ID:-E03218FD37956AC6AE94223E041C34E7F024F7A0}"
GPG_BIN="${GPG_BIN:-$(command -v gpg || true)}"
if [ -z "$GPG_BIN" ]; then
  for c in /opt/homebrew/bin/gpg /usr/local/bin/gpg; do
    [ -x "$c" ] && GPG_BIN=$c && break
  done
fi
if [ -z "$GPG_BIN" ]; then
  echo "错误：找不到 gpg，请安装 gnupg 或设置 GPG_BIN" >&2
  exit 1
fi

CENTRAL_USER="${CENTRAL_USERNAME:-}"
CENTRAL_PASS="${CENTRAL_PASS:-${CENTRAL_PASSWORD:-}}"

if [ -z "$CENTRAL_USER" ] || [ -z "$CENTRAL_PASS" ]; then
  echo "错误：请设置 CENTRAL_USERNAME / CENTRAL_PASSWORD（central.sonatype.com User Token）" >&2
  exit 1
fi
if [ -z "${GPG_KEY_PASSWORD:-}" ]; then
  echo "错误：请设置 GPG_KEY_PASSWORD（GPG 密钥口令）" >&2
  exit 1
fi

SIGNING_KEY=$("$GPG_BIN" --batch --pinentry-mode loopback --passphrase "$GPG_KEY_PASSWORD" \
  --armor --export-secret-keys "$GPG_KEY_ID")
if [ -z "$SIGNING_KEY" ]; then
  echo "错误：导出 GPG 私钥失败，请检查 GPG_KEY_ID / GPG_KEY_PASSWORD" >&2
  exit 1
fi
export ORG_GRADLE_PROJECT_signingInMemoryKey="$SIGNING_KEY"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="$GPG_KEY_PASSWORD"

echo "=============================================="
echo "Publishing to Maven Central -> ${VERSION}"
echo "=============================================="
./gradlew ":${MODULE}:publishAllPublicationsToMavenCentralRepository" \
  -PmavenVersion="${VERSION}" \
  -PmavenCentralUsername="${CENTRAL_USER}" \
  -PmavenCentralPassword="${CENTRAL_PASS}"
echo "Maven Central 发布完成：io.github.sofarnobug:${MODULE}:${VERSION}"
echo "消费方：implementation(\"io.github.sofarnobug:${MODULE}:${VERSION}\")"
