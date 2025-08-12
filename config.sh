#!/bin/bash

# Script para configurar datasources de Grafana automáticamente
# Este script resuelve el problema de provisioning automático creando
# los archivos de configuración necesarios dentro del contenedor de Grafana

echo "🔧 Configurando datasources de Grafana para observabilidad distribuida..."
echo ""

# Función para verificar si el contenedor de Grafana está ejecutándose
check_grafana_running() {
    if ! docker ps | grep -q "grafana"; then
        echo "❌ Error: El contenedor de Grafana no está ejecutándose"
        echo "   Por favor ejecuta 'docker-compose up -d' primero"
        exit 1
    fi
    echo "✅ Contenedor de Grafana detectado y ejecutándose"
}

# Función para crear el archivo de configuración de Prometheus
create_prometheus_datasource() {
    echo "📊 Creando configuración de datasource para Prometheus..."
    
    # Crear el archivo de configuración línea por línea para máxima compatibilidad
    docker exec grafana sh -c "echo 'apiVersion: 1' > /etc/grafana/provisioning/datasources/prometheus.yaml"
    docker exec grafana sh -c "echo 'datasources:' >> /etc/grafana/provisioning/datasources/prometheus.yaml"
    docker exec grafana sh -c "echo '  - name: Prometheus' >> /etc/grafana/provisioning/datasources/prometheus.yaml"
    docker exec grafana sh -c "echo '    type: prometheus' >> /etc/grafana/provisioning/datasources/prometheus.yaml"
    docker exec grafana sh -c "echo '    access: proxy' >> /etc/grafana/provisioning/datasources/prometheus.yaml"
    docker exec grafana sh -c "echo '    url: http://prometheus:9090' >> /etc/grafana/provisioning/datasources/prometheus.yaml"
    docker exec grafana sh -c "echo '    isDefault: true' >> /etc/grafana/provisioning/datasources/prometheus.yaml"
    docker exec grafana sh -c "echo '    editable: true' >> /etc/grafana/provisioning/datasources/prometheus.yaml"
    docker exec grafana sh -c "echo '    basicAuth: false' >> /etc/grafana/provisioning/datasources/prometheus.yaml"
    
    # Verificar que el archivo se creó correctamente
    if docker exec grafana test -f /etc/grafana/provisioning/datasources/prometheus.yaml; then
        echo "   ✅ Configuración de Prometheus creada exitosamente"
    else
        echo "   ❌ Error creando configuración de Prometheus"
        return 1
    fi
}

# Función para crear el archivo de configuración de Zipkin
create_zipkin_datasource() {
    echo "🔍 Creando configuración de datasource para Zipkin..."
    
    # Crear el archivo de configuración para Zipkin
    docker exec grafana sh -c "echo 'apiVersion: 1' > /etc/grafana/provisioning/datasources/zipkin.yaml"
    docker exec grafana sh -c "echo 'datasources:' >> /etc/grafana/provisioning/datasources/zipkin.yaml"
    docker exec grafana sh -c "echo '  - name: Zipkin' >> /etc/grafana/provisioning/datasources/zipkin.yaml"
    docker exec grafana sh -c "echo '    type: zipkin' >> /etc/grafana/provisioning/datasources/zipkin.yaml"
    docker exec grafana sh -c "echo '    access: proxy' >> /etc/grafana/provisioning/datasources/zipkin.yaml"
    docker exec grafana sh -c "echo '    url: http://zipkin:9411' >> /etc/grafana/provisioning/datasources/zipkin.yaml"
    docker exec grafana sh -c "echo '    editable: true' >> /etc/grafana/provisioning/datasources/zipkin.yaml"
    
    # Verificar que el archivo se creó correctamente
    if docker exec grafana test -f /etc/grafana/provisioning/datasources/zipkin.yaml; then
        echo "   ✅ Configuración de Zipkin creada exitosamente"
    else
        echo "   ❌ Error creando configuración de Zipkin"
        return 1
    fi
}

# Función para verificar el contenido de los archivos creados
verify_configurations() {
    echo "🔍 Verificando contenido de las configuraciones creadas..."
    echo ""
    
    echo "📄 Contenido de la configuración de Prometheus:"
    echo "   ---"
    docker exec grafana cat /etc/grafana/provisioning/datasources/prometheus.yaml | sed 's/^/   /'
    echo "   ---"
    echo ""
    
    echo "📄 Contenido de la configuración de Zipkin:"
    echo "   ---"
    docker exec grafana cat /etc/grafana/provisioning/datasources/zipkin.yaml | sed 's/^/   /'
    echo "   ---"
    echo ""
}

# Función para recargar la configuración de Grafana
reload_grafana() {
    echo "🔄 Recargando configuración de Grafana..."
    
    # Intentar primero con señal HUP (recarga sin reinicio)
    if docker exec grafana kill -HUP 1 2>/dev/null; then
        echo "   ✅ Configuración recargada usando señal HUP"
        sleep 3
    else
        echo "   ⚠️  Señal HUP no funcionó, reiniciando contenedor..."
        docker restart grafana
        echo "   ✅ Contenedor de Grafana reiniciado"
        
        # Esperar a que Grafana esté completamente disponible
        echo "   ⏳ Esperando que Grafana esté completamente disponible..."
        sleep 10
    fi
}

# Función para verificar que los datasources están disponibles
test_datasources() {
    echo "🧪 Probando conectividad de datasources..."
    
    # Dar tiempo adicional para que Grafana procese las configuraciones
    echo "   ⏳ Esperando que Grafana procese las nuevas configuraciones..."
    sleep 5
    
    echo "   📊 Los datasources deberían estar disponibles en:"
    echo "      http://localhost:3000/datasources"
    echo "   🔑 Credenciales: admin / admin"
    echo ""
    echo "   💡 Para verificar que funcionan correctamente:"
    echo "      1. Ve a Configuration > Data Sources en Grafana"
    echo "      2. Deberías ver 'Prometheus' y 'Zipkin' listados"
    echo "      3. Haz clic en cada uno y usa 'Save & Test' para verificar conectividad"
}

# Función principal que ejecuta todo el proceso
main() {
    echo "🚀 Iniciando configuración automática de datasources de Grafana"
    echo "==============================================================="
    echo ""
    
    # Ejecutar cada paso del proceso con verificación de errores
    check_grafana_running
    echo ""
    
    create_prometheus_datasource
    if [ $? -ne 0 ]; then
        echo "❌ Falló la configuración de Prometheus, abortando"
        exit 1
    fi
    echo ""
    
    create_zipkin_datasource
    if [ $? -ne 0 ]; then
        echo "❌ Falló la configuración de Zipkin, abortando"
        exit 1
    fi
    echo ""
    
    verify_configurations
    
    reload_grafana
    echo ""
    
    test_datasources
    
    echo ""
    echo "🎉 ¡Configuración completada exitosamente!"
    echo ""
    echo "📋 Próximos pasos recomendados:"
    echo "   1. Abre http://localhost:3000 en tu navegador"
    echo "   2. Inicia sesión con admin/admin"
    echo "   3. Ve a Configuration > Data Sources para verificar los datasources"
    echo "   4. Inicia tus aplicaciones Spring Boot para generar datos"
    echo "   5. Crea dashboards para visualizar métricas y trazas"
    echo ""
    echo "🔧 Si necesitas ejecutar este script nuevamente:"
    echo "   chmod +x setup-grafana-datasources.sh"
    echo "   ./setup-grafana-datasources.sh"
}

# Verificar si el script se está ejecutando directamente (no siendo sourced)
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi