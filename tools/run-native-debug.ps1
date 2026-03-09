[CmdletBinding()]
param(
    [string]$DeviceId,
    [switch]$SkipInstall,
    [switch]$SkipBuild
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Write-Step([string]$Message) {
    Write-Host "`n==> $Message" -ForegroundColor Cyan
}

function Resolve-RepoRoot {
    $scriptDir = Split-Path -Parent $PSCommandPath
    return (Resolve-Path (Join-Path $scriptDir '..')).Path
}

function Get-LocalPropertiesValue {
    param(
        [string]$FilePath,
        [string]$Key
    )

    if (-not (Test-Path $FilePath)) {
        return $null
    }

    foreach ($line in Get-Content $FilePath) {
        if ($line -match "^\s*$([regex]::Escape($Key))=(.*)$") {
            return $Matches[1].Trim()
        }
    }

    return $null
}

function Convert-LocalPropertiesPath([string]$Value) {
    if ([string]::IsNullOrWhiteSpace($Value)) {
        return $null
    }

    return ($Value -replace '\\\\', '\')
}

function Resolve-AdbPath([string]$RepoRoot) {
    $adbCommand = Get-Command adb -ErrorAction SilentlyContinue
    if ($adbCommand) {
        return $adbCommand.Source
    }

    $localPropertiesPath = Join-Path $RepoRoot 'local.properties'
    $sdkDirValue = Get-LocalPropertiesValue -FilePath $localPropertiesPath -Key 'sdk.dir'
    $sdkDir = Convert-LocalPropertiesPath $sdkDirValue
    if ($sdkDir) {
        $adbPath = Join-Path $sdkDir 'platform-tools\adb.exe'
        if (Test-Path $adbPath) {
            return $adbPath
        }
    }

    return $null
}

function Get-ConnectedDevices([string]$AdbPath) {
    if (-not $AdbPath) {
        return @()
    }

    & $AdbPath start-server | Out-Null
    $lines = & $AdbPath devices
    $devices = @()

    foreach ($line in $lines) {
        if ($line -match '^(?<id>\S+)\s+device$') {
            $devices += $Matches['id']
        }
    }

    return $devices
}

function Get-GoogleServicesStatus([string]$RepoRoot) {
    $paths = @(
        'app\google-services.json',
        'app\src\main\google-services.json',
        'app\src\debug\google-services.json',
        'app\src\release\google-services.json'
    )

    $found = @()
    foreach ($relativePath in $paths) {
        $fullPath = Join-Path $RepoRoot $relativePath
        if (Test-Path $fullPath) {
            $found += $relativePath
        }
    }

    return $found
}

function Get-DebugApk([string]$RepoRoot) {
    $apkRoot = Join-Path $RepoRoot 'app\build\outputs\apk'
    if (-not (Test-Path $apkRoot)) {
        return $null
    }

    $apks = Get-ChildItem -Path $apkRoot -Recurse -Filter '*.apk' | Sort-Object LastWriteTimeUtc -Descending
    if (-not $apks) {
        return $null
    }

    $preferred = $apks | Where-Object { $_.Name -match 'universal' } | Select-Object -First 1
    if ($preferred) {
        return $preferred.FullName
    }

    return ($apks | Select-Object -First 1).FullName
}

$repoRoot = Resolve-RepoRoot
$gradleWrapper = Join-Path $repoRoot 'gradlew.bat'
if (-not (Test-Path $gradleWrapper)) {
    throw "Could not find gradlew.bat at $gradleWrapper"
}

Write-Step "Repository root: $repoRoot"

$googleServicesFiles = Get-GoogleServicesStatus -RepoRoot $repoRoot
if ($googleServicesFiles.Count -gt 0) {
    Write-Host "Firebase config detected:" -ForegroundColor Green
    $googleServicesFiles | ForEach-Object { Write-Host " - $_" }
} else {
    Write-Host "No google-services.json found. Debug builds will continue without Firebase-backed features." -ForegroundColor Yellow
}

if (-not $SkipBuild) {
    Write-Step 'Building native debug APK'
    Push-Location $repoRoot
    try {
        & $gradleWrapper ':app:assembleDebug' '--console=plain'
    } finally {
        Pop-Location
    }
}

$apkPath = Get-DebugApk -RepoRoot $repoRoot
if (-not $apkPath) {
    throw 'Debug APK was not found under app\build\outputs\apk after the build.'
}

Write-Host "Debug APK: $apkPath" -ForegroundColor Green

if ($SkipInstall) {
    Write-Step 'Skipping install as requested'
    exit 0
}

$adbPath = Resolve-AdbPath -RepoRoot $repoRoot
if (-not $adbPath) {
    Write-Host 'ADB was not found on PATH or under the Android SDK. Install skipped.' -ForegroundColor Yellow
    exit 0
}

$devices = Get-ConnectedDevices -AdbPath $adbPath
if ($devices.Count -eq 0) {
    Write-Host 'No connected devices detected. Build succeeded; install skipped.' -ForegroundColor Yellow
    exit 0
}

$targetDevice = $DeviceId
if (-not $targetDevice) {
    $targetDevice = $devices[0]
    if ($devices.Count -gt 1) {
        Write-Host "Multiple devices detected. Using the first one: $targetDevice" -ForegroundColor Yellow
    }
}

Write-Step "Installing APK on device $targetDevice"
& $adbPath -s $targetDevice install -r $apkPath

Write-Host "Native debug flow completed successfully." -ForegroundColor Green

