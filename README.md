# APISIX POC - API Gateway con Docker

## Descripción del Proyecto

Este proyecto implementa una prueba de concepto (POC) de Apache APISIX, un gateway de APIs cloud-native de alto rendimiento, utilizando Docker Compose. APISIX actúa como punto de entrada único para gestionar, enrutar y proteger el tráfico hacia servicios backend.

### ¿Qué es Apache APISIX?

Apache APISIX es un gateway de APIs dinámico, en tiempo real y de alto rendimiento que proporciona funcionalidades como:

- **Enrutamiento dinámico**: Configuración de rutas sin reiniciar el servicio
- **Balanceador de carga**: Distribución inteligente del tráfico entre servicios
- **Autenticación y autorización**: Múltiples métodos de seguridad (JWT, OAuth, API Keys)
- **Rate limiting**: Control de límites de peticiones
- **Monitoreo**: Métricas y observabilidad integrada
- **Plugins extensibles**: Más de 80 plugins disponibles

## Arquitectura del Sistema

```
Cliente → APISIX Gateway → Servicios Backend
                ↕
              etcd (Configuración)
```

### Componentes principales:

1. **APISIX Gateway (Puerto 9080)**: Punto de entrada para todas las peticiones
2. **Admin API (Puerto 9180)**: Interface para gestionar configuraciones
3. **etcd (Puerto 2379)**: Base de datos de configuración distribuida

## Estructura del Proyecto

```
APISIX/
├── docker-compose.yml          # Definición de servicios Docker
├── apisix_conf/
│   └── config.yaml            # Configuración de APISIX
├── route.json                 # Archivo temporal para pruebas
└── README.md                  # Documentación del proyecto
```

## Configuración y Despliegue

### Prerrequisitos

- Docker Desktop instalado
- Docker Compose v3.8+
- curl para pruebas de API

### Instalación

1. **Clonar el repositorio**
```bash
git clone <tu-repositorio>
cd APISIX
```

2. **Iniciar los servicios**
```bash
docker-compose up -d
```

3. **Verificar estado de contenedores**
```bash
docker-compose ps
```

### Verificación de la instalación

```bash
# Verificar Admin API
curl -X GET "http://localhost:9180/apisix/admin/routes" -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f1"

# Verificar estado del gateway
curl http://localhost:9080/apisix/status
```

## Gestión de Rutas

### Crear una nueva ruta

```bash
# Crear archivo JSON con la configuración de la ruta
echo {"uri":"/test","upstream":{"type":"roundrobin","nodes":{"httpbin.org:80":1}}} > route.json

# Aplicar la configuración
curl -X PUT "http://localhost:9180/apisix/admin/routes/1" \
  -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f1" \
  -d @route.json
```

### Listar rutas existentes

```bash
curl -X GET "http://localhost:9180/apisix/admin/routes" \
  -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f1"
```

### Obtener detalles de una ruta específica

```bash
curl -X GET "http://localhost:9180/apisix/admin/routes/1" \
  -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f1"
```

### Probar una ruta

```bash
curl http://localhost:9080/test
```

### Eliminar una ruta

```bash
curl -X DELETE "http://localhost:9180/apisix/admin/routes/1" \
  -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f1"
```

## Comandos de Administración

### Gestión de contenedores

```bash
# Iniciar servicios
docker-compose up -d

# Detener servicios
docker-compose down

# Ver logs
docker-compose logs apisix
docker-compose logs etcd

# Reiniciar un servicio específico
docker-compose restart apisix

# Ver estado de servicios
docker-compose ps
```

### Limpieza del sistema

```bash
# Detener y remover contenedores, redes y volúmenes
docker-compose down -v

# Limpiar sistema Docker
docker system prune -f
```

## Configuración Avanzada

### Estructura del archivo config.yaml

```yaml
apisix:
  node_listen: 9080              # Puerto del gateway
  enable_admin: true             # Habilitar Admin API

deployment:
  role: traditional
  role_traditional:
    config_provider: etcd        # Usar etcd como backend de configuración
  admin:
    allow_admin:                 # IPs permitidas para Admin API
      - 127.0.0.0/24            # Localhost
      - 172.16.0.0/12           # Redes Docker
    admin_key:                   # Claves de administración
      - name: admin
        key: edd1c9f034335f136f87ad84b625c8f1
        role: admin
    admin_listen:
      ip: 0.0.0.0
      port: 9180
```

### Plugins habilitados

El proyecto incluye los siguientes plugins esenciales:
- `real-ip`: Obtener IP real del cliente
- `cors`: Manejo de CORS
- `proxy-rewrite`: Reescritura de requests
- `basic-auth`, `key-auth`, `jwt-auth`: Autenticación
- `api-breaker`: Circuit breaker
- `limit-req`, `limit-conn`, `limit-count`: Rate limiting
- `proxy-cache`: Caché de respuestas
- `prometheus`: Métricas
- `http-logger`: Logging de requests

## Ejemplos de Uso

### Ejemplo 1: Ruta simple con balanceador

```json
{
  "uri": "/api/*",
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "backend1.example.com:8080": 1,
      "backend2.example.com:8080": 1
    }
  }
}
```

### Ejemplo 2: Ruta con autenticación por API Key

```json
{
  "uri": "/secure/*",
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "secure-backend.example.com:443": 1
    }
  },
  "plugins": {
    "key-auth": {}
  }
}
```

### Ejemplo 3: Ruta con rate limiting

```json
{
  "uri": "/limited/*",
  "upstream": {
    "type": "roundrobin", 
    "nodes": {
      "api.example.com:80": 1
    }
  },
  "plugins": {
    "limit-req": {
      "rate": 10,
      "burst": 5,
      "rejected_code": 429
    }
  }
}
```

## Troubleshooting

### Problemas comunes

1. **Error 403 Forbidden en Admin API**
   - Verificar configuración de `allow_admin` en config.yaml
   - Confirmar que se está usando la clave correcta

2. **Contenedor en estado "Restarting"**
   - Revisar logs: `docker-compose logs apisix`
   - Verificar sintaxis del archivo config.yaml

3. **No se puede conectar a etcd**
   - Verificar que etcd esté healthy: `docker-compose ps`
   - Confirmar configuración de red en docker-compose.yml

### Logs y debugging

```bash
# Ver logs en tiempo real
docker-compose logs -f apisix

# Acceder al contenedor para debugging
docker exec -it apisix-gateway sh

# Verificar conectividad con etcd
docker exec -it apisix-gateway ping etcd
```

## Seguridad

### Consideraciones para producción

1. **Cambiar claves por defecto**
   - Usar claves seguras generadas aleatoriamente
   - Implementar rotación de claves

2. **Configurar allow_admin restrictivo**
   - Remover `0.0.0.0/0` de allow_admin
   - Usar rangos de IP específicos de la red de administración

3. **Habilitar HTTPS**
   - Configurar certificados SSL/TLS
   - Usar `https_admin: true` para Admin API

4. **Monitoreo y auditoría**
   - Habilitar logging detallado
   - Implementar monitoreo de métricas
   - Configurar alertas para accesos no autorizados

## Recursos adicionales

- [Documentación oficial de APISIX](https://apisix.apache.org/docs/)
- [Plugin Hub](https://apisix.apache.org/plugins/)
- [GitHub del proyecto](https://github.com/apache/apisix)
- [Guía de configuración de Docker](https://apisix.apache.org/docs/docker/manual/)

## Versiones utilizadas

- APISIX: 3.6.0-debian
- etcd: 3.5.12 (bitnami)
- Docker Compose: 3.8

## Licencia

Este proyecto utiliza Apache APISIX bajo la licencia Apache 2.0.
