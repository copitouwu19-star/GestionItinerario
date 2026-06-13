@echo off
echo ================================================
echo  Compilando e instalando app en el emulador...
echo ================================================

cd /d "%~dp0"

echo.
echo [1/2] Compilando APK debug...
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: La compilacion fallo. Revisa los errores arriba.
    pause
    exit /b 1
)

echo.
echo [2/2] Instalando en dispositivo/emulador...
call gradlew.bat installDebug
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: No se pudo instalar. Asegurate de tener el emulador abierto o el celular conectado.
    pause
    exit /b 1
)

echo.
echo ================================================
echo  App instalada correctamente!
echo  Busca "Gestion Itinerario" en el emulador.
echo ================================================
pause
