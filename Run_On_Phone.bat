@echo off
setlocal
cd /d "%~dp0"
title Run Scroll the Gita on Phone (USB Debugging)

set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

echo ========================================================
echo       Running Scroll the Gita on Connected Phone
echo ========================================================
echo.

if not exist "%ADB%" (
    echo [ERROR] ADB not found at: %ADB%
    pause
    exit /b 1
)

echo Checking connected USB devices...
"%ADB%" devices

echo.
echo Building and installing app to phone...
call .\gradlew.bat installDebug
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Build or installation failed.
    echo Please make sure:
    echo  1. Your phone is connected via USB.
    echo  2. USB debugging is enabled in Settings -^> Developer options.
    echo  3. You accepted the "Allow USB debugging" prompt on your phone screen.
    echo.
    pause
    exit /b 1
)

echo.
echo Launching Scroll the Gita on your phone...
"%ADB%" shell am start -n com.scrollthegita/.app.MainActivity

echo.
echo ========================================================
echo   Application successfully launched on your phone!
echo ========================================================
echo You can now use the app directly on your device.
pause

