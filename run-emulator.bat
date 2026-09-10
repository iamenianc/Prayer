@echo off
title Prayer Android Emulator
echo ============================================================
echo Starting Prayer Android Emulator (PrayerPreview)
echo ============================================================

set "JAVA_HOME=C:\Users\ianch\.jdks\jdk-17.0.20.1+1"
set "PATH=%JAVA_HOME%\bin;C:\Users\ianch\android-sdk\emulator;C:\Users\ianch\android-sdk\platform-tools;%PATH%"

start "" "C:\Users\ianch\android-sdk\emulator\emulator.exe" -avd PrayerPreview

echo.
echo Waiting for emulator device to connect...
adb wait-for-device

echo Waiting for Android to finish booting...
adb shell "while [ \"$(getprop sys.boot_completed)\" != \"1\" ]; do sleep 1; done"

echo Installing latest debug APK...
adb install -r "%~dp0android\app\build\outputs\apk\debug\app-debug.apk"

echo Launching Prayer app...
adb shell am start -n au.prayer.app/.MainActivity

echo.
echo App launched successfully!
echo You can close this command prompt window.
timeout /t 5 >nul
