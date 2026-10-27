<#
.SYNOPSIS
颐康宝 Android 开发环境配置脚本

.DESCRIPTION
本脚本将自动配置 Android 开发所需的环境：
1. Java JDK 17
2. Gradle 8.5
3. Android SDK (API 34)

.NOTES
需要以管理员身份运行此脚本
#>

param(
    [string]$BaseDir = $PWD.Path
)

$ErrorActionPreference = "Stop"

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "颐康宝 Android 开发环境配置脚本" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "本脚本将自动配置：" -ForegroundColor White
Write-Host "  1. Java JDK 17" -ForegroundColor White
Write-Host "  2. Gradle 8.5" -ForegroundColor White
Write-Host "  3. Android SDK (API 34)" -ForegroundColor White
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
$isAdmin = $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "[ERROR] 请以管理员身份运行此脚本" -ForegroundColor Red
    Write-Host "方法：右键 PowerShell -> 以管理员身份运行" -ForegroundColor Yellow
    Read-Host "按任意键退出..."
    exit 1
}

Write-Host "[OK] 当前已以管理员身份运行" -ForegroundColor Green

# 设置目录
$ToolDir = Join-Path $BaseDir "tools"
$AndroidSdkDir = Join-Path $ToolDir "android-sdk"
$GradleDir = Join-Path $ToolDir "gradle"
$JavaDir = Join-Path $ToolDir "jdk"

# 创建目录
New-Item -ItemType Directory -Path $ToolDir -Force | Out-Null
New-Item -ItemType Directory -Path $AndroidSdkDir -Force | Out-Null
New-Item -ItemType Directory -Path $GradleDir -Force | Out-Null
New-Item -ItemType Directory -Path $JavaDir -Force | Out-Null

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "1. 安装 Java JDK 17" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

$JavaExe = Join-Path $JavaDir "bin\java.exe"

if (-not (Test-Path $JavaExe)) {
    Write-Host "[INFO] 正在下载 JDK 17..." -ForegroundColor Yellow
    
    $JdkZip = Join-Path $ToolDir "jdk.zip"
    $JdkTemp = Join-Path $ToolDir "jdk_temp"
    
    try {
        Invoke-WebRequest -Uri "https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.zip" -OutFile $JdkZip -UseBasicParsing
        Write-Host "[INFO] 正在解压 JDK..." -ForegroundColor Yellow
        
        New-Item -ItemType Directory -Path $JdkTemp -Force | Out-Null
        Expand-Archive -Path $JdkZip -DestinationPath $JdkTemp -Force
        
        # 移动解压后的文件
        $ExtractedDir = Get-ChildItem -Path $JdkTemp -Directory | Select-Object -First 1
        Move-Item -Path (Join-Path $ExtractedDir.FullName "*") -Destination $JavaDir -Force
        
        Remove-Item -Path $JdkTemp -Recurse -Force
        Remove-Item -Path $JdkZip -Force
        
        Write-Host "[OK] JDK 17 安装完成" -ForegroundColor Green
    }
    catch {
        Write-Host "[ERROR] JDK 下载失败: $_" -ForegroundColor Red
        Read-Host "按任意键退出..."
        exit 1
    }
}
else {
    Write-Host "[OK] JDK 17 已安装" -ForegroundColor Green
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "2. 安装 Gradle 8.5" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

$GradleBat = Join-Path $GradleDir "bin\gradle.bat"

if (-not (Test-Path $GradleBat)) {
    Write-Host "[INFO] 正在下载 Gradle 8.5..." -ForegroundColor Yellow
    
    $GradleZip = Join-Path $ToolDir "gradle.zip"
    $GradleTemp = Join-Path $ToolDir "gradle_temp"
    
    try {
        Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-8.5-bin.zip" -OutFile $GradleZip -UseBasicParsing
        Write-Host "[INFO] 正在解压 Gradle..." -ForegroundColor Yellow
        
        New-Item -ItemType Directory -Path $GradleTemp -Force | Out-Null
        Expand-Archive -Path $GradleZip -DestinationPath $GradleTemp -Force
        
        # 移动解压后的文件
        $ExtractedDir = Get-ChildItem -Path $GradleTemp -Directory | Select-Object -First 1
        Move-Item -Path (Join-Path $ExtractedDir.FullName "*") -Destination $GradleDir -Force
        
        Remove-Item -Path $GradleTemp -Recurse -Force
        Remove-Item -Path $GradleZip -Force
        
        Write-Host "[OK] Gradle 8.5 安装完成" -ForegroundColor Green
    }
    catch {
        Write-Host "[ERROR] Gradle 下载失败: $_" -ForegroundColor Red
        Read-Host "按任意键退出..."
        exit 1
    }
}
else {
    Write-Host "[OK] Gradle 8.5 已安装" -ForegroundColor Green
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "3. 安装 Android SDK" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

$SdkManager = Join-Path $AndroidSdkDir "cmdline-tools\latest\bin\sdkmanager.bat"

if (-not (Test-Path $SdkManager)) {
    Write-Host "[INFO] 正在下载 Android SDK Command Line Tools..." -ForegroundColor Yellow
    
    $SdkZip = Join-Path $ToolDir "sdk-tools.zip"
    $CmdLineToolsDir = Join-Path $AndroidSdkDir "cmdline-tools"
    $LatestDir = Join-Path $CmdLineToolsDir "latest"
    
    try {
        Invoke-WebRequest -Uri "https://dl.google.com/android/repository/commandlinetools-win-10406996_latest.zip" -OutFile $SdkZip -UseBasicParsing
        Write-Host "[INFO] 正在解压 SDK Tools..." -ForegroundColor Yellow
        
        New-Item -ItemType Directory -Path $CmdLineToolsDir -Force | Out-Null
        Expand-Archive -Path $SdkZip -DestinationPath $CmdLineToolsDir -Force
        
        # 重命名目录结构
        New-Item -ItemType Directory -Path $LatestDir -Force | Out-Null
        Move-Item -Path (Join-Path $CmdLineToolsDir "bin") -Destination $LatestDir -Force
        Move-Item -Path (Join-Path $CmdLineToolsDir "lib") -Destination $LatestDir -Force
        Move-Item -Path (Join-Path $CmdLineToolsDir "source.properties") -Destination $LatestDir -Force -ErrorAction SilentlyContinue
        
        Remove-Item -Path $SdkZip -Force
        
        Write-Host "[OK] SDK Tools 安装完成" -ForegroundColor Green
    }
    catch {
        Write-Host "[ERROR] SDK Tools 下载失败: $_" -ForegroundColor Red
        Read-Host "按任意键退出..."
        exit 1
    }
}
else {
    Write-Host "[OK] SDK Tools 已安装" -ForegroundColor Green
}

# 安装 SDK 组件
Write-Host "[INFO] 正在安装 Android SDK 组件..." -ForegroundColor Yellow

$env:JAVA_HOME = $JavaDir
$env:PATH = "$JavaDir\bin;$env:PATH"

try {
    & $SdkManager "platform-tools" "platforms;android-34" "build-tools;34.0.0" "extras;google;m2repository" "extras;android;m2repository" --sdk_root=$AndroidSdkDir
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Android SDK 组件安装完成" -ForegroundColor Green
    }
    else {
        Write-Host "[WARNING] SDK组件安装可能不完整，请手动检查" -ForegroundColor Yellow
    }
}
catch {
    Write-Host "[WARNING] SDK组件安装遇到问题: $_" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "4. 配置环境变量" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

Write-Host "[INFO] 设置临时环境变量..." -ForegroundColor Yellow

$env:ANDROID_HOME = $AndroidSdkDir
$env:ANDROID_SDK_ROOT = $AndroidSdkDir
$env:PATH = "$GradleDir\bin;$AndroidSdkDir\platform-tools;$AndroidSdkDir\build-tools\34.0.0;$env:PATH"

Write-Host ""
Write-Host "==============================================" -ForegroundColor Green
Write-Host "环境配置完成！" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green
Write-Host ""
Write-Host "环境变量（当前会话）：" -ForegroundColor White
Write-Host "  JAVA_HOME = $JavaDir" -ForegroundColor Cyan
Write-Host "  ANDROID_HOME = $AndroidSdkDir" -ForegroundColor Cyan
Write-Host "  Gradle 和 SDK 路径已添加到 PATH" -ForegroundColor Cyan
Write-Host ""
Write-Host "验证配置：" -ForegroundColor White

try {
    Write-Host "`n  java -version" -ForegroundColor Yellow
    java -version
    
    Write-Host "`n  gradle -v" -ForegroundColor Yellow
    gradle -v
}
catch {
    Write-Host "[WARNING] 验证时出现问题，请手动验证" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "构建项目命令：" -ForegroundColor White
Write-Host "  cd android" -ForegroundColor Cyan
Write-Host "  gradle assembleDebug" -ForegroundColor Cyan
Write-Host ""
Write-Host "APK输出路径：" -ForegroundColor White
Write-Host "  app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

Read-Host "按任意键退出..."
