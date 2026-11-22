@echo off
setlocal
cd /d "%~dp0"
title Launching Scroll the Gita (Pixel 7 Android 15)...

set "EMULATOR=%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe"
set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
set "APK=%~dp0app\build\outputs\apk\debug\app-debug.apk"

echo ========================================================
echo        Starting Pixel 7 (Android 15) Emulator
echo ========================================================
echo.

if not exist "%EMULATOR%" (
    echo [ERROR] Emulator not found at: %EMULATOR%
    pause
    exit /b 1
)

start "" "%EMULATOR%" -avd Pixel_7_API_35

echo Waiting for emulator to start...
"%ADB%" wait-for-device

echo Emulator connected. Waiting for system boot to complete...
:wait_boot
for /f "tokens=*" %%A in ('"%ADB%" shell getprop sys.boot_completed 2^>nul') do set "BOOT=%%A"
if not "%BOOT%"=="1" (
    timeout /t 2 /nobreak >nul
    goto wait_boot
)

echo System booted successfully!
echo Installing Scroll the Gita APK...
"%ADB%" install -r "%APK%"

echo Launching Scroll the Gita...
"%ADB%" shell am start -n com.scrollthegita/.app.MainActivity

echo.
echo ========================================================
echo   Scroll the Gita is running on the Pixel 7 emulator!
echo ========================================================
echo You can interact with the app in the phone window on your desktop.
echo (Press any key to close this launcher console window)
pause >nul
