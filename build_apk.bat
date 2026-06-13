
@echo off
echo ================================================
echo  Generando APK debug...
echo ================================================


cd /d "%~dp0"

set JAVA_HOME=C:\Program Files\Java\jdk-17

call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: La compilacion fallo. Revisa los errores arriba.
    pause
    exit /b 1
)

echo.
echo ================================================
echo  APK generado correctamente!
echo  Lo encontras en:
echo  app\build\outputs\apk\debug\app-debug.apk
echo ================================================
pause
