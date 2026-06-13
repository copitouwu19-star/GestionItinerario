# Compilar e instalar la app en el celular conectado por USB
# Uso: .\correr_app.ps1

$ADB      = "C:\Users\sofia\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$PACKAGE  = "com.gestion.itinerario"
$ACTIVITY = "$PACKAGE/.MainActivity"

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  App Gestion Itinerario - Build and Run" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# 1. Verificar ADB
if (-not (Test-Path $ADB)) {
    Write-Host "`n[ERROR] No se encontro adb.exe en: $ADB" -ForegroundColor Red
    pause
    exit 1
}

# 2. Verificar celular conectado
Write-Host "`n[1/3] Verificando dispositivo..." -ForegroundColor Yellow
$devices = & $ADB devices | Select-String "device$"
if (-not $devices) {
    Write-Host "[ERROR] No se detecto ningun celular." -ForegroundColor Red
    Write-Host "- Conecta el cable USB" -ForegroundColor Yellow
    Write-Host "- Activa Depuracion USB en el celular" -ForegroundColor Yellow
    Write-Host "- Acepta el cartel 'Permitir depuracion' en la pantalla" -ForegroundColor Yellow
    pause
    exit 1
}
Write-Host "  OK - $($devices[0].ToString().Trim())" -ForegroundColor Green

# 3. Compilar
Write-Host "`n[2/3] Compilando APK..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
& "$PSScriptRoot\gradlew.bat" assembleDebug
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilacion fallida." -ForegroundColor Red
    pause
    exit 1
}
Write-Host "  OK - Compilacion exitosa" -ForegroundColor Green

# 4. Instalar e iniciar
Write-Host "`n[3/3] Instalando en el celular..." -ForegroundColor Yellow
$APK = "$PSScriptRoot\app\build\outputs\apk\debug\app-debug.apk"
& $ADB install -r $APK
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] No se pudo instalar." -ForegroundColor Red
    pause
    exit 1
}

& $ADB shell am start -n $ACTIVITY | Out-Null

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Listo! La app se abrio en tu celular." -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
pause
