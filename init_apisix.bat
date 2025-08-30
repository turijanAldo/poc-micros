@echo off
SETLOCAL ENABLEDELAYEDEXPANSION

echo =============================================
echo    🚀 Iniciando APISIX con Docker Compose
echo =============================================

REM Levantar contenedores
docker-compose down -v
docker-compose up -d

echo.
echo ⏳ Esperando a que etcd esté listo...

REM Obtener nombre del contenedor de etcd
for /f "tokens=*" %%i in ('docker ps --filter "ancestor=bitnami/etcd" --format "{{.Names}}"') do (
    set ETCD_CONTAINER=%%i
)

echo Contenedor ETCD detectado: !ETCD_CONTAINER!

REM Esperar hasta que etcd responda
:WAIT_ETCD
docker exec !ETCD_CONTAINER! etcdctl endpoint status >nul 2>&1
if errorlevel 1 (
    timeout /t 2 >nul
    goto WAIT_ETCD
)

echo ✅ ETCD listo.

REM Obtener nombre del contenedor de APISIX
for /f "tokens=*" %%i in ('docker ps --filter "ancestor=apache/apisix" --format "{{.Names}}"') do (
    set APISIX_CONTAINER=%%i
)

echo Contenedor APISIX detectado: !APISIX_CONTAINER!

REM Inicializar ETCD para APISIX
echo.
echo Inicializando ETCD para APISIX...
docker exec !APISIX_CONTAINER! /usr/local/openresty/luajit/bin/luajit ./apisix/cli/apisix.lua init_etcd

REM Inicializar APISIX
echo.
echo Inicializando APISIX...
docker exec !APISIX_CONTAINER! /usr/local/openresty/luajit/bin/luajit ./apisix/cli/apisix.lua init

echo.
echo =============================================
echo    ✅ APISIX y ETCD inicializados correctamente
echo    Dashboard: http://localhost:9000
echo    API Gateway: http://localhost:9080
echo =============================================

pause
