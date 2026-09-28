@echo off
REM ==============================================================================
REM Rewardly - Release APK Build Script for Windows
REM ==============================================================================

echo ==========================================================
echo   Rewardly - Building Release APK for Android Devices
echo ==========================================================

set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

if "%KEYSTORE_PATH%"=="" set KEYSTORE_PATH=%SCRIPT_DIR%my-upload-key.jks
if "%STORE_PASSWORD%"=="" set STORE_PASSWORD=rewardlypass123
if "%KEY_PASSWORD%"=="" set KEY_PASSWORD=rewardlypass123

if not exist "%KEYSTORE_PATH%" (
    echo [>>] Generating release keystore...
    keytool -genkeypair -v -keystore "%KEYSTORE_PATH%" -alias upload -keyalg RSA -keysize 2048 -validity 10000 -storepass %STORE_PASSWORD% -keypass %KEY_PASSWORD% -dname "CN=Rewardly, OU=Mobile, O=Rewardly, L=San Francisco, ST=CA, C=US"
)

if exist gradlew.bat (
    set GRADLE_CMD=gradlew.bat
) else (
    set GRADLE_CMD=gradle
)

echo [>>] Building release APK...
call %GRADLE_CMD% :app:assembleRelease

set RELEASE_APK=%SCRIPT_DIR%app\build\outputs\apk\release\app-release.apk

if exist "%RELEASE_APK%" (
    echo ==========================================================
    echo   SUCCESS! Release APK generated at:
    echo   %RELEASE_APK%
    echo ==========================================================
    echo.
    echo Installation on Android:
    echo   adb install -r "%RELEASE_APK%"
    echo   or copy the APK file directly to your phone.
    echo ==========================================================
) else (
    echo Error: Release APK not found.
    exit /b 1
)
