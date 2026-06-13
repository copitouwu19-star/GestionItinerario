# Uso: .\commit_push.ps1 [-mensaje "tu mensaje"]

param(
    [string]$mensaje = ""
)

Set-Location $PSScriptRoot

# Limpiar build del indice por si acaso
git rm -r --cached --ignore-unmatch app/build/ 2>$null | Out-Null

# Agregar solo codigo fuente
git add app/src/ app/build.gradle.kts app/google-services.json app/proguard-rules.pro .gitignore build.gradle.kts settings.gradle.kts gradle.properties gradle/ gradlew gradlew.bat 2>$null

# Verificar si hay cambios
$staged = git diff --cached --name-only 2>$null
if (-not $staged) {
    Write-Host "No hay cambios para commitear." -ForegroundColor Cyan
    exit 0
}

Write-Host "Archivos a commitear:" -ForegroundColor Cyan
$staged | ForEach-Object { Write-Host "  + $_" -ForegroundColor Green }

# Mensaje automatico si no se paso uno
if (-not $mensaje) {
    $fecha = Get-Date -Format "yyyy-MM-dd"
    $tieneNuevos = git diff --cached --name-only --diff-filter=A | Select-Object -First 1
    $tieneModif  = git diff --cached --name-only --diff-filter=M | Select-Object -First 1
    if ($tieneNuevos -and $tieneModif) {
        $mensaje = "feat: agregar y actualizar codigo fuente [$fecha]"
    } elseif ($tieneNuevos) {
        $mensaje = "feat: agregar nuevos archivos [$fecha]"
    } else {
        $mensaje = "update: actualizar codigo fuente [$fecha]"
    }
}

Write-Host "`nCommit: '$mensaje'" -ForegroundColor Cyan
git commit -m $mensaje
if ($LASTEXITCODE -ne 0) { Write-Host "[ERROR] Fallo el commit." -ForegroundColor Red; exit 1 }

Write-Host "Haciendo push..." -ForegroundColor Cyan
git push origin main
if ($LASTEXITCODE -ne 0) { Write-Host "[ERROR] Fallo el push." -ForegroundColor Red; exit 1 }

Write-Host "[OK] Listo. Cambios subidos a GitHub." -ForegroundColor Green
