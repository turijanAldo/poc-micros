@echo off
set API_KEY=edd1c9f034335f136f87ad84b625c8f1
set ADMIN_URL=http://localhost:9180/apisix/admin


echo Configurando ruta de Usuarios...
curl -X PUT "%ADMIN_URL%/routes/2" -H "X-API-KEY: %API_KEY%" -d "{\"uri\":\"/api/users/*\",\"upstream\":{\"type\":\"roundrobin\",\"nodes\":{\"host.docker.internal:8082\":1}}}"

echo Configurando ruta de user...
curl -X PUT "%ADMIN_URL%/routes/3" -H "X-API-KEY: %API_KEY%" -d "{\"uri\":\"/user/*\",\"upstream\":{\"type\":\"roundrobin\",\"nodes\":{\"host.docker.internal:8081\":1}}}"

echo Configurando ruta de Pagos...
curl -X PUT "%ADMIN_URL%/routes/4" -H "X-API-KEY: %API_KEY%" -d "{\"uri\":\"/api/micro1/*\",\"upstream\":{\"type\":\"roundrobin\",\"nodes\":{\"host.docker.internal:8084\":1}}}"

echo Rutas configuradas!