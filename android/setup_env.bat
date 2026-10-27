@echo off
setlocal enabledelayedexpansion

echo ==============================================
echo 颐康宝 Android 开发环境配置脚本
echo ==============================================
echo 本脚本将自动配置：
echo   1. Java JDK 17
echo   2. Gradle 8.5
echo   3. Android SDK (API 34)
echo ==============================================
echo.

:: 检查管理员权限
net session >nul 2>&1
if %errorLevel% == 0 (
    echo [OK] 当前已以管理员身份运行
) else (
    echo [ERROR] 请以管理员身份运行此脚本
    pause
    exit /b 1
)

set "BASE_DIR=%~dp0"
set "TOOL_DIR=%BASE_DIR%tools"
set "ANDROID_SDK_DIR=%TOOL_DIR%\android-sdk"
set "GRADLE_DIR=%TOOL_DIR%\gradle"
set "JAVA_DIR=%TOOL_DIR%\jdk"

mkdir "%TOOL_DIR%" 2>nul
mkdir "%ANDROID_SDK_DIR%" 2>nul
mkdir "%GRADLE_DIR%" 2>nul
mkdir "%JAVA_DIR%" 2>nul

echo.
echo ==============================================
echo 1. 安装 Java JDK 17
echo ==============================================

if not exist "%JAVA_DIR%\bin\java.exe" (
    echo [INFO] 正在下载 JDK 17...
    powershell -Command "Invoke-WebRequest -Uri 'https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.zip' -OutFile '%TOOL_DIR%\jdk.zip'"
    
    echo [INFO] 正在解压 JDK...
    powershell -Command "Expand-Archive -Path '%TOOL_DIR%\jdk.zip' -DestinationPath '%TOOL_DIR%\jdk_temp' -Force"
    for /d %%i in ("%TOOL_DIR%\jdk_temp\jdk-17*") do (
        move "%%i\*" "%JAVA_DIR%\" >nul
    )
    rmdir /s /q "%TOOL_DIR%\jdk_temp"
    del "%TOOL_DIR%\jdk.zip"
    echo [OK] JDK 17 安装完成
) else (
    echo [OK] JDK 17 已安装
)

echo.
echo ==============================================
echo 2. 安装 Gradle 8.5
echo ==============================================

if not exist "%GRADLE_DIR%\bin\gradle.bat" (
    echo [INFO] 正在下载 Gradle 8.5...
    powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.5-bin.zip' -OutFile '%TOOL_DIR%\gradle.zip'"
    
    echo [INFO] 正在解压 Gradle...
    powershell -Command "Expand-Archive -Path '%TOOL_DIR%\gradle.zip' -DestinationPath '%TOOL_DIR%\gradle_temp' -Force"
    move "%TOOL_DIR%\gradle_temp\gradle-8.5\*" "%GRADLE_DIR%\" >nul
    rmdir /s /q "%TOOL_DIR%\gradle_temp"
    del "%TOOL_DIR%\gradle.zip"
    echo [OK] Gradle 8.5 安装完成
) else (
    echo [OK] Gradle 8.5 已安装
)

echo.
echo ==============================================
echo 3. 安装 Android SDK
echo ==============================================

set "SDK_MANAGER=%ANDROID_SDK_DIR%\cmdline-tools\latest\bin\sdkmanager.bat"

if not exist "%SDK_MANAGER%" (
    echo [INFO] 正在下载 Android SDK Command Line Tools...
    powershell -Command "Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/commandlinetools-win-10406996_latest.zip' -OutFile '%TOOL_DIR%\sdk-tools.zip'"
    
    echo [INFO] 正在解压 SDK Tools...
    powershell -Command "Expand-Archive -Path '%TOOL_DIR%\sdk-tools.zip' -DestinationPath '%ANDROID_SDK_DIR%\cmdline-tools' -Force"
    mkdir "%ANDROID_SDK_DIR%\cmdline-tools\latest"
    move "%ANDROID_SDK_DIR%\cmdline-tools\bin" "%ANDROID_SDK_DIR%\cmdline-tools\latest\" >nul
    move "%ANDROID_SDK_DIR%\cmdline-tools\lib" "%ANDROID_SDK_DIR%\cmdline-tools\latest\" >nul
    move "%ANDROID_SDK_DIR%\cmdline-tools\source.properties" "%ANDROID_SDK_DIR%\cmdline-tools\latest\" >nul
    del "%TOOL_DIR%\sdk-tools.zip"
    echo [OK] SDK Tools 安装完成
) else (
    echo [OK] SDK Tools 已安装
)

echo [INFO] 正在安装 Android SDK 组件...
set "JAVA_HOME=%JAVA_DIR%"
set "PATH=%JAVA_DIR%\bin;%PATH%"

"%SDK_MANAGER%" "platform-tools" "platforms;android-34" "build-tools;34.0.0" "extras;google;m2repository" "extras;android;m2repository" --sdk_root=%ANDROID_SDK_DIR%
if %errorLevel% == 0 (
    echo [OK] Android SDK 组件安装完成
) else (
    echo [WARNING] SDK组件安装可能不完整，请手动检查
)

echo.
echo ==============================================
echo 4. 设置环境变量（临时）
echo ==============================================
echo 正在设置环境变量...

set "PATH=%GRADLE_DIR%\bin;%ANDROID_SDK_DIR%\platform-tools;%ANDROID_SDK_DIR%\build-tools\34.0.0;%PATH%"

echo.
echo ==============================================
echo 环境配置完成！
echo ==============================================
echo.
echo 环境变量（当前会话）：
echo   JAVA_HOME=%JAVA_DIR%
echo   ANDROID_HOME=%ANDROID_SDK_DIR%
echo   PATH 已包含 Gradle 和 SDK 路径
echo.
echo 验证配置：
echo   java -version
java -version
echo.
echo   gradle -v
gradle -v
echo.
echo ==============================================
echo 构建项目命令：
echo   cd android
echo   gradle assembleDebug
echo.
echo APK输出路径：
echo   app\build\outputs\apk\debug\app-debug.apk
echo ==============================================

pause
