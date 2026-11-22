@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   Scroll the Gita - Store Screenshot Capture Tool
echo ===================================================
echo.

:: Locate adb
set ADB=adb
if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
)

:: Verify ADB connection
"%ADB%" get-state >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] No active Android device or emulator detected.
    echo Please start the emulator or connect a device with USB debugging enabled.
    exit /b 1
)

echo [OK] Connected to Android device/emulator.

:: Create destination folder
if not exist "docs\screenshots" (
    mkdir "docs\screenshots"
)

:: Ensure python is available
python --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Python is required to run the automated screenshot capture script.
    exit /b 1
)

echo [INFO] Running screenshot capture script...
python scripts/capture_screenshots.py

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Screenshot capture encountered an error.
    exit /b %ERRORLEVEL%
)

echo.
echo ===================================================
echo [SUCCESS] All screenshots captured to docs\screenshots\
echo ===================================================
dir /b docs\screenshots\*.png

exit /b 0
