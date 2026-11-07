@echo off
setlocal enabledelayedexpansion

echo ========================================
echo KuiklyPerspectiveCrop Ohos shared build
echo ========================================

if defined DEVECO_SDK_HOME (
  for /f "delims=" %%A in ('echo %DEVECO_SDK_HOME%') do set "RESOLVED_SDK_HOME=%%A"
) else if exist "D:\DevEco_Studio_26.0\sdk" (
  set "RESOLVED_SDK_HOME=D:\DevEco_Studio_26.0\sdk"
) else (
  echo [Error] Set DEVECO_SDK_HOME or install DevEco at D:\DevEco_Studio_26.0
  exit /b 1
)

if exist "!RESOLVED_SDK_HOME!\default\openharmony" (
  set "OHOS_SDK_HOME=!RESOLVED_SDK_HOME!\default\openharmony"
) else if exist "!RESOLVED_SDK_HOME!\openharmony" (
  set "OHOS_SDK_HOME=!RESOLVED_SDK_HOME!\openharmony"
) else (
  echo [Error] OpenHarmony SDK not under !RESOLVED_SDK_HOME!
  exit /b 1
)
echo OHOS_SDK_HOME=!OHOS_SDK_HOME!

set KUIKLY_AGP_VERSION=7.4.2
set KUIKLY_KOTLIN_VERSION=2.0.21-KBA-010

echo [1/2] linkSharedDebugSharedOhosArm64...
call gradlew.bat -c settings.ohos.gradle.kts :perspectiveCropDemo:linkSharedDebugSharedOhosArm64 --stacktrace
if %ERRORLEVEL% neq 0 (
  echo [Error] Gradle link failed
  exit /b 1
)

set SO_SRC=perspectiveCropDemo\build\bin\ohosArm64\sharedDebugShared\libshared.so
set HDR_SRC=perspectiveCropDemo\build\bin\ohosArm64\sharedDebugShared\libshared_api.h
set SO_DST=ohosApp\entry\libs\arm64-v8a
set HDR_DST=ohosApp\entry\src\main\cpp

if not exist "%SO_SRC%" (
  echo [Error] missing %SO_SRC%
  exit /b 1
)
if not exist "%SO_DST%" mkdir "%SO_DST%"
copy /y "%SO_SRC%" "%SO_DST%\" >nul
copy /y "%HDR_SRC%" "%HDR_DST%\" >nul
echo [2/2] copied libshared.so + libshared_api.h
echo Done. Next: ohpm install + DevEco assembleHap / hdc install
endlocal
