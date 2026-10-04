# Helper: run gradlew detached, tee output to a log, print exit code.
# Gradle args are written verbatim into a temp .cmd so cmd.exe cannot mangle them.
#
# Usage:
#   pwsh -NoProfile -File scripts/gw.ps1 <logfile> [-TimeoutSec N] [gradle args...]
#
# On timeout the whole process tree is killed (cmd -> gradlew -> gradle daemon).
param(
    [Parameter(Mandatory = $true, Position = 0)][string]$LogFile,
    [int]$TimeoutSec = 120,
    [Parameter(ValueFromRemainingArguments = $true)][string[]]$GradleArgs
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $root

if ($TimeoutSec -le 0) { $TimeoutSec = 120 }
$pollSec = 10

$logAbs = Join-Path $root $LogFile
$logDir = Split-Path -Parent $logAbs
if ($logDir -and -not (Test-Path -LiteralPath $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force | Out-Null
}
$errAbs = "$logAbs.err"
foreach ($f in @($logAbs, $errAbs)) { if (Test-Path -LiteralPath $f) { Remove-Item -LiteralPath $f -Force } }

$cmdFile = Join-Path $env:TEMP ("exdeorum-gw-" + [Guid]::NewGuid().ToString('N') + ".cmd")
$lines = New-Object System.Collections.Generic.List[string]
$lines.Add('@echo off')
$lines.Add('set "GRADLE_USER_HOME=E:\stv-tools\gradle-8.14.2\cache"')
$lines.Add('set "JAVA_HOME=E:\Java_JDK\J26"')
$lines.Add("gradlew.bat " + ($GradleArgs -join ' '))
$lines.Add("exit /b %ERRORLEVEL%")
Set-Content -LiteralPath $cmdFile -Value $lines -Encoding ASCII

$p = Start-Process -FilePath 'cmd.exe' -ArgumentList @('/c', $cmdFile) -WorkingDirectory $root `
    -RedirectStandardOutput $logAbs -RedirectStandardError $errAbs -NoNewWindow -PassThru

$deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSec)
$sw = [System.Diagnostics.Stopwatch]::StartNew()
$lastLen = -1
$timedOut = $false

while ($true) {
    if ($p.HasExited) { break }
    if ([DateTime]::UtcNow -gt $deadline) { $timedOut = $true; break }

    $remaining = [int]($deadline - [DateTime]::UtcNow).TotalSeconds
    Start-Sleep -Seconds ([Math]::Min($pollSec, [Math]::Max(1, $remaining)))

    if (Test-Path -LiteralPath $logAbs) {
        $len = (Get-Item -LiteralPath $logAbs).Length
        if ($len -ne $lastLen) {
            Write-Output ("  ... {0} bytes, {1}s" -f $len, [int]$sw.Elapsed.TotalSeconds)
            $lastLen = $len
        }
    }
}

if ($timedOut) {
    Write-Output "TIMEOUT after $TimeoutSec s - killing process tree"
    # Kill children first (gradlew/gradle daemon), then the wrapper cmd.
    & taskkill.exe /PID $p.Id /T /F 2>&1 | Out-Null
    Start-Sleep -Seconds 2
    if (-not $p.HasExited) { try { $p.Kill() } catch { } }
    & taskkill.exe /IM java.exe /FI "WINDOWTITLE eq *GradleDaemon*" /F 2>&1 | Out-Null
    # Best effort: stop any leftover single-use daemon for this project.
    & taskkill.exe /IM java.exe /FI "COMMANDLINE eq *GradleDaemon*" /F 2>&1 | Out-Null
}

Remove-Item -LiteralPath $cmdFile -Force -ErrorAction SilentlyContinue
Write-Output ("EXITCODE={0}  ELAPSED={1}s  TIMEOUT={2}  LOG={3}" -f $p.ExitCode, [int]$sw.Elapsed.TotalSeconds, $timedOut, $logAbs)
if ($timedOut) { exit 124 }
