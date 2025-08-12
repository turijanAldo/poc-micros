# start-observability.ps1 - PowerShell script
Write-Host "🚀 Iniciando Stack de Observabilidad..." -ForegroundColor Green

# Crear directorios si no existen
if (!(Test-Path "logs")) { New-Item -ItemType Directory -Path "logs" }

Write-Host "📁 Directorios verificados" -ForegroundColor Yellow

# Verificar que Docker está corriendo
try {
    docker version | Out-Null
    Write-Host "✅ Docker está corriendo" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker no está corriendo. Por favor inicia Docker Desktop" -ForegroundColor Red
    exit 1
}

# Verificar archivos de configuración
$configFiles = @("prometheus.yml", "docker-compose.yml")
foreach ($file in $configFiles) {
    if (!(Test-Path $file)) {
        Write-Host "❌ Archivo faltante: $file" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✅ Archivos de configuración verificados" -ForegroundColor Green

# Levantar servicios
Write-Host "🐳 Iniciando contenedores..." -ForegroundColor Blue
docker-compose up -d

# Esperar que los servicios estén listos
Write-Host "⏳ Esperando que los servicios estén listos..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Verificar servicios
$services = @(
    @{Name="Zipkin"; Url="http://localhost:9411/health"},
    @{Name="Prometheus"; Url="http://localhost:9090/-/ready"},
    @{Name="Grafana"; Url="http://localhost:3000/api/health"}
)

foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri $service.Url -TimeoutSec 5 -UseBasicParsing
        Write-Host "✅ $($service.Name) está disponible" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  $($service.Name) no está disponible aún" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "🎉 Stack de Observabilidad iniciado!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 URLs de acceso:" -ForegroundColor Cyan
Write-Host "  - Grafana:    http://localhost:3000 (admin/admin)"
Write-Host "  - Prometheus: http://localhost:9090"
Write-Host "  - Zipkin:     http://localhost:9411"

---
@echo off
REM start-observability.bat - Batch script alternativo

echo 🚀 Iniciando Stack de Observabilidad...

REM Crear directorio logs si no existe
if not exist "logs" mkdir logs
echo 📁 Directorios verificados

REM Verificar Docker
docker version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker no está corriendo. Por favor inicia Docker Desktop
    pause
    exit /b 1
)
echo ✅ Docker está corriendo

REM Verificar archivos
if not exist "prometheus.yml" (
    echo ❌ Archivo faltante: prometheus.yml
    pause
    exit /b 1
)
if not exist "docker-compose.yml" (
    echo ❌ Archivo faltante: docker-compose.yml
    pause
    exit /b 1
)
echo ✅ Archivos de configuración verificados

REM Levantar servicios
echo 🐳 Iniciando contenedores...
docker-compose up -d

REM Esperar
echo ⏳ Esperando 30 segundos...
timeout /t 30 /nobreak >nul

echo.
echo 🎉 Stack iniciado!
echo.
echo 📊 URLs de acceso:
echo   - Grafana:    http://localhost:3000 (admin/admin)
echo   - Prometheus: http://localhost:9090
echo   - Zipkin:     http://localhost:9411
echo.
pause

---
# test-services.ps1 - Script de pruebas en PowerShell
Write-Host "🧪 Probando microservicios..." -ForegroundColor Green

$gatewayUrl = "http://localhost:8082"
$userUrl = "http://localhost:8081"

Write-Host ""
Write-Host "🔍 1. Verificando servicios..." -ForegroundColor Yellow

# Health checks
try {
    $response = Invoke-RestMethod -Uri "$gatewayUrl/health-check" -TimeoutSec 5
    Write-Host "✅ service-gateway: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ service-gateway no responde" -ForegroundColor Red
}

try {
    $response = Invoke-RestMethod -Uri "$userUrl/health-check" -TimeoutSec 5
    Write-Host "✅ service-user: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ service-user no responde" -ForegroundColor Red
}

Write-Host ""
Write-Host "🔄 2. Generando trazas..." -ForegroundColor Yellow

# Hacer peticiones
for ($i = 1; $i -le 5; $i++) {
    Write-Host "📡 Petición $i`: /greet-user" -ForegroundColor Cyan
    try {
        $response = Invoke-RestMethod -Uri "$gatewayUrl/greet-user" -TimeoutSec 10
        Write-Host "Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor White
    } catch {
        Write-Host "Error en petición $i" -ForegroundColor Red
    }
    Start-Sleep -Seconds 1
}

Write-Host ""
Write-Host "✅ Pruebas completadas!" -ForegroundColor Green
Write-Host ""
Write-Host "🔍 Revisa los resultados en:" -ForegroundColor Cyan
Write-Host "  - Zipkin: http://localhost:9411"
Write-Host "  - Prometheus: http://localhost:9090"
Write-Host "  - Grafana: http://localhost:3000"