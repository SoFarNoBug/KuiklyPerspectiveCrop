# Windows: build HAP (if needed) is via DevEco/hvigor; this script installs & starts.
$ErrorActionPreference = "Stop"
$Hdc = if (Test-Path "D:\DevEco_Studio_26.0\sdk\default\openharmony\toolchains\hdc.exe") {
  "D:\DevEco_Studio_26.0\sdk\default\openharmony\toolchains\hdc.exe"
} else {
  "hdc"
}
$Bundle = "com.jlj.kuiklyperspectivecropui"
$HapSigned = Join-Path $PSScriptRoot "entry\build\default\outputs\default\entry-default-signed.hap"
$HapUnsigned = Join-Path $PSScriptRoot "entry\build\default\outputs\default\entry-default-unsigned.hap"

$targets = & $Hdc list targets 2>$null | Where-Object { $_ -and $_ -notmatch "UART" -and $_ -notmatch "^\s*$" }
if (-not $targets) { throw "No hdc TCP target. Start the emulator first." }

$hap = if (Test-Path $HapSigned) { $HapSigned } elseif (Test-Path $HapUnsigned) {
  throw "Only unsigned HAP found. Configure DevEco debug signing, then assembleHap."
} else {
  throw "HAP not found. Run DevEco assembleHap for entry@default."
}

foreach ($t in $targets) {
  Write-Host "install -> $t"
  & $Hdc -t $t shell aa force-stop $Bundle
  & $Hdc -t $t install $hap
  & $Hdc -t $t shell aa start -a EntryAbility -b $Bundle
}
