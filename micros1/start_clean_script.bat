@echo off
echo =====================================================
echo 🚀 INICIANDO MICROS1 SIN VARIABLES DE ENTORNO OTEL
echo =====================================================

echo.
echo 🧹 Limpiando variables de entorno OTEL...
set OTEL_EXPORTER_OTLP_ENDPOINT=
set OTEL_SERVICE_NAME=
set OTEL_RESOURCE_ATTRIBUTES=
set OTEL_EXPORTER_OTLP_PROTOCOL=
set OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=
set OTEL_PROPAGATORS=
set OTEL_TRACES_SAMPLER=
set OTEL_LOGS_EXPORTER=

echo ✅ Variables OTEL limpiadas

echo.
echo 🔍 Verificando que no queden variables OTEL...
set | findstr /i OTEL
if %ERRORLEVEL% == 0 (
    echo ⚠️  ADVERTENCIA: Aún quedan variables OTEL definidas
) else (
    echo ✅ No hay variables OTEL definidas - perfecto
)

echo.
echo 📋 Configuración que se usará (desde application.yml):
echo    - Endpoint: http://host.docker.internal:4318/v1/traces
echo    - Service: micros1
echo    - Sampling: 1.0 (100%%)

echo.
echo 🔗 Verificando conectividad con el collector...
curl -s -o nul -w "HTTP Status: %%{http_code}" -X POST http://host.docker.internal:4318/v1/traces
if %ERRORLEVEL% == 0 (
    echo.
    echo ✅ Collector OTLP accesible
) else (
    echo.
    echo ⚠️  ADVERTENCIA: No se puede conectar al collector
    echo    Verifica que tu docker-compose esté ejecutándose
)

echo.
echo 🎯 Iniciando aplicación Spring Boot...
echo    Busca estos logs importantes:
echo    ✅ "OTLP trace exporter initialized"
echo    ✅ "Using endpoint: http://host.docker.internal:4318/v1/traces"
echo    ✅ "Exporting spans" (después de llamadas gRPC)

echo.
echo =====================================================
echo           INICIANDO GRADLEW BOOTRUN
echo =====================================================
echo.

gradlew bootRun