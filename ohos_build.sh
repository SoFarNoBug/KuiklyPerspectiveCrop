#!/bin/sh
set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

echo "========================================"
echo "KuiklyPerspectiveCrop Ohos shared build"
echo "========================================"

SDK_HOME="${DEVECO_SDK_HOME:-/Applications/DevEco-Studio.app/Contents/sdk}"
if [ -d "$SDK_HOME/default/openharmony" ]; then
  OHOS_SDK_HOME="$SDK_HOME/default/openharmony"
elif [ -d "$SDK_HOME/openharmony" ]; then
  OHOS_SDK_HOME="$SDK_HOME/openharmony"
else
  echo "[Error] OpenHarmony SDK not under $SDK_HOME"
  echo "Set DEVECO_SDK_HOME or install DevEco Studio."
  exit 1
fi
export OHOS_SDK_HOME
export DEVECO_SDK_HOME="${DEVECO_SDK_HOME:-$SDK_HOME}"
echo "OHOS_SDK_HOME=$OHOS_SDK_HOME"

export KUIKLY_AGP_VERSION=7.4.2
export KUIKLY_KOTLIN_VERSION=2.0.21-KBA-010

echo "[1/2] linkSharedDebugSharedOhosArm64..."
./gradlew -c settings.ohos.gradle.kts :perspectiveCropDemo:linkSharedDebugSharedOhosArm64 --stacktrace

SO_SRC="perspectiveCropDemo/build/bin/ohosArm64/sharedDebugShared/libshared.so"
HDR_SRC="perspectiveCropDemo/build/bin/ohosArm64/sharedDebugShared/libshared_api.h"
SO_DST="ohosApp/entry/libs/arm64-v8a"
HDR_DST="ohosApp/entry/src/main/cpp"

if [ ! -f "$SO_SRC" ]; then
  echo "[Error] missing $SO_SRC"
  exit 1
fi

mkdir -p "$SO_DST"
cp -f "$SO_SRC" "$SO_DST/"
cp -f "$HDR_SRC" "$HDR_DST/"

RESFILE_DST="ohosApp/entry/src/main/resources/resfile"
ASSETS_SRC="perspectiveCropDemo/src/commonMain/assets"
if [ -d "$ASSETS_SRC" ]; then
  mkdir -p "$RESFILE_DST"
  cp -f "$ASSETS_SRC"/* "$RESFILE_DST/" 2>/dev/null || true
  echo "copied demo assets -> $RESFILE_DST"
fi

echo "[2/2] copied libshared.so + libshared_api.h"
echo "Done. Next: ensure DevEco 自动签名, then: cd ohosApp && ./runOhosApp.sh"
