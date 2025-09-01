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

# Docker Compose APISIX - Explicación línea por línea

## ¿Qué hace este archivo?

Este `docker-compose.yml` crea una infraestructura de API Gateway usando dos componentes principales:
- **etcd**: Base de datos que guarda las configuraciones
- **APISIX**: El gateway que procesa las peticiones

## Anatomía del archivo

### Declaración de versión
```yaml
version: '3.8'
```
**¿Qué hace?** Especifica la versión del formato Docker Compose que usaremos. La 3.8 es estable y soporta todas las funciones que necesitamos.

---

### Inicio de servicios
```yaml
services:
```
**¿Qué hace?** Declara que vamos a definir contenedores. Todo lo que sigue son los diferentes servicios (contenedores) que queremos ejecutar.

---

## Servicio etcd (Base de datos de configuración)

### Definición del servicio
```yaml
  etcd:
    image: bitnami/etcd:3.5.12
```
**¿Qué hace?** 
- Crea un servicio llamado `etcd`
- Usa la imagen oficial de Bitnami (versión 3.5.12)
- etcd es como una agenda compartida donde APISIX guarda sus configuraciones

### Nombre del contenedor
```yaml
    container_name: apisix-etcd
```
**¿Qué hace?** Le da un nombre fijo al contenedor. Sin esto, Docker inventaría un nombre aleatorio. Con este nombre puedes hacer `docker logs apisix-etcd`.

### Política de reinicio
```yaml
    restart: always
```
**¿Qué hace?** Si el contenedor se cierra por cualquier razón (error, reinicio del sistema), Docker automáticamente lo vuelve a iniciar.

### Variables de entorno
```yaml
    environment:
      ALLOW_NONE_AUTHENTICATION: "yes"
      ETCD_ADVERTISE_CLIENT_URLS: "http://0.0.0.0:2379"
      ETCD_LISTEN_CLIENT_URLS: "http://0.0.0.0:2379"
      ETCD_ENABLE_V2: "true"
```
**¿Qué hace cada variable?**
- `ALLOW_NONE_AUTHENTICATION: "yes"` → No pide contraseña (solo para desarrollo)
- `ETCD_ADVERTISE_CLIENT_URLS: "http://0.0.0.0:2379"` → Le dice a otros servicios "conéctate a mí en el puerto 2379"
- `ETCD_LISTEN_CLIENT_URLS: "http://0.0.0.0:2379"` → "Escucho conexiones en todos los IPs en puerto 2379"
- `ETCD_ENABLE_V2: "true"` → Habilita la versión 2 de la API (APISIX la necesita)

### Mapeo de puertos
```yaml
    ports:
      - "2379:2379"
```
**¿Qué hace?** Conecta el puerto 2379 de tu computadora con el puerto 2379 del contenedor. Ahora puedes acceder a etcd desde `localhost:2379`.

### Red de comunicación
```yaml
    networks:
      - apisix
```
**¿Qué hace?** Pone este contenedor en una red llamada `apisix`. Los contenedores en la misma red pueden "hablarse" entre ellos usando sus nombres.

### Almacenamiento persistente
```yaml
    volumes:
      - etcd_data:/bitnami/etcd
```
**¿Qué hace?** Crea un espacio de almacenamiento llamado `etcd_data` que sobrevive aunque borres el contenedor. Así no pierdes las configuraciones.

### Health check
```yaml
    healthcheck:
      test: ["CMD-SHELL", "etcdctl --endpoints=http://127.0.0.1:2379 endpoint health || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 3
      start_period: 10s
```
**¿Qué hace cada línea?**
- `test: [...]` → Comando que verifica si etcd está funcionando bien
- `interval: 10s` → Ejecuta la prueba cada 10 segundos
- `timeout: 5s` → Si la prueba toma más de 5 segundos, falla
- `retries: 3` → Intenta 3 veces antes de marcar como "unhealthy"
- `start_period: 10s` → Espera 10 segundos antes de empezar las pruebas

---

## Servicio APISIX (API Gateway)

### Definición del servicio
```yaml
  apisix:
    image: apache/apisix:3.6.0-debian
    container_name: apisix-gateway
```
**¿Qué hace?** Crea el servicio principal usando la imagen oficial de Apache APISIX versión 3.6.0 basada en Debian.

### Dependencias
```yaml
    depends_on:
      etcd:
        condition: service_healthy
```
**¿Qué hace?** 
- APISIX no se inicia hasta que etcd esté completamente sano
- `service_healthy` significa que espera hasta que el health check de etcd pase

### Puertos expuestos
```yaml
    ports:
      - "9080:9080"  # Gateway público
      - "9443:9443"  # HTTPS (si se configura)
      - "9180:9180"  # Admin API
```
**¿Qué hace cada puerto?**
- `9080` → Puerto principal donde llegan las peticiones de los clientes
- `9443` → Puerto para HTTPS (encriptado)
- `9180` → Puerto para administrar APISIX (crear rutas, configurar plugins)

### Conectividad con el host
```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```
**¿Qué hace?** Permite que APISIX se conecte a servicios que corren en tu computadora (fuera de Docker). Crea un alias especial llamado `host.docker.internal`.

### Variables de configuración
```yaml
    environment:
      - APISIX_STAND_ALONE=false
```
**¿Qué hace?** Le dice a APISIX que use etcd para guardar configuraciones (no modo autónomo).

### Archivo de configuración
```yaml
    volumes:
      - ./apisix_conf/config.yaml:/usr/local/apisix/conf/config.yaml:ro
```
**¿Qué hace?**
- Monta el archivo `config.yaml` de tu carpeta al contenedor
- `:ro` significa "read-only" (solo lectura)
- Este archivo contiene la configuración principal de APISIX

---

## Configuración de red

```yaml
networks:
  apisix:
    driver: bridge
```
**¿Qué hace?**
- Crea una red privada llamada `apisix`
- `bridge` es el tipo de red (los contenedores pueden comunicarse entre ellos)
- Es como crear una LAN virtual solo para estos servicios

---

## Almacenamiento persistente

```yaml
volumes:
  etcd_data:
    driver: local
```
**¿Qué hace?**
- Define un volumen llamado `etcd_data`
- `local` significa que se guarda en el disco duro de tu computadora
- Aquí se almacenan las configuraciones de APISIX permanentemente

---

## Flujo de comunicación

```
Tu aplicación → Puerto 9080 → APISIX → host.docker.internal:8001-8004 → Tus microservicios
                     ↕
                   etcd (configuraciones)
```

## Comandos útiles con esta configuración

```bash
# Iniciar todo
docker-compose up -d

# Ver estado de servicios
docker-compose ps

# Ver logs de APISIX
docker-compose logs apisix

# Ver logs de etcd
docker-compose logs etcd

# Detener todo
docker-compose down

# Detener y eliminar datos
docker-compose down -v
```

## Lo que obtienes al ejecutar este archivo

1. **Un gateway funcional** en `localhost:9080`
2. **Una interfaz de administración** en `localhost:9180`  
3. **Conexión automática entre servicios** mediante la red `apisix`
4. **Persistencia de datos** a través del volumen `etcd_data`
5. **Reinicio automático** si algo falla
6. **Health monitoring** para garantizar que etcd esté sano

Este archivo crea la infraestructura completa para que puedas enrutar peticiones desde un punto único hacia tus 4 microservicios externos.

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
