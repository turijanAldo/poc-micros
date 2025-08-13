package com.example.service_gateway.controller;

import com.example.grpc.user.User;
import com.example.grpc.user.Empty;
import com.example.grpc.user.UserServiceGrpc;
import io.grpc.StatusRuntimeException;
import io.micrometer.tracing.annotation.NewSpan;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Este servicio es tu "especialista en comunicaciones gRPC".
 * 
 * Su única responsabilidad es manejar la comunicación con el servidor gRPC de usuarios.
 * Es como tener un empleado experto que habla perfectamente el idioma técnico de gRPC
 * y sabe exactamente cómo comunicarse con cada servicio específico de tu arquitectura.
 * 
 * La belleza del patrón que estás usando es que este servicio se enfoca únicamente
 * en la comunicación gRPC, mientras que el controlador REST se enfoca únicamente
 * en manejar peticiones HTTP. Cada clase tiene una responsabilidad muy clara.
 */
@Service
public class GrpcClientService {

    private static final Logger logger = LoggerFactory.getLogger(GrpcClientService.class);

    /**
     * Esta es la línea más importante de toda la comunicación gRPC.
     * 
     * La anotación @GrpcClient le dice a Spring Boot: "Por favor, crea automáticamente
     * una conexión gRPC hacia el servicio llamado 'user-service'". Spring Boot busca
     * la configuración para 'user-service' en tu application.yml, encuentra la dirección
     * localhost:9090, y automáticamente maneja toda la complejidad de crear canales,
     * manejar conexiones, balanceo de carga, timeouts, y limpieza de recursos.
     * 
     * Es como tener un asistente súper competente que maneja todos los detalles
     * técnicos de hacer llamadas telefónicas de larga distancia, mientras tú solo
     * te enfocas en qué mensaje quieres transmitir.
     */
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    /**
     * Este método realiza la comunicación gRPC real con tu servidor de usuarios.
     * 
     * La anotación @NewSpan crea automáticamente un span de tracing específico
     * para esta operación, lo que te permite ver en Zipkin exactamente cuánto
     * tiempo toma la comunicación gRPC versus el procesamiento en el controlador REST.
     * 
     * Esta separación de concerns es fundamental: el controlador se encarga de
     * la lógica HTTP, y este método se encarga únicamente de la comunicación gRPC.
     */
    @NewSpan("grpc-call-get-user")
    public User getUser() {
        try {
            logger.debug("📞 Iniciando llamada gRPC a user-service...");
            
            // Construimos el mensaje Empty requerido por tu servicio
            // En protobuf, incluso los mensajes "vacíos" necesitan ser construidos explícitamente
            Empty emptyRequest = Empty.newBuilder().build();
            
            // Esta es la línea donde realmente ocurre la magia de la red
            // Aquí tu Gateway envía el mensaje por la red hacia tu servidor gRPC
            logger.debug("📡 Enviando petición gRPC...");
            
            User response = userServiceStub.getUser(emptyRequest);
            
            // Log detallado de la respuesta recibida
            logger.info("✅ Respuesta gRPC recibida exitosamente");
            logger.debug("📋 Detalles del usuario: ID={}, Nombre='{}'", 
                        response.getId(), response.getName());
            
            return response;
            
        } catch (StatusRuntimeException e) {
            /*
             * Los errores de gRPC vienen con códigos de estado muy específicos que te dan
             * información precisa sobre qué salió mal. Estos códigos son estándar en toda
             * la industria y te ayudan a diagnosticar problemas rápidamente.
             * 
             * Algunos códigos comunes:
             * - UNAVAILABLE: El servidor no está corriendo o no es alcanzable
             * - DEADLINE_EXCEEDED: La operación tomó demasiado tiempo
             * - INVALID_ARGUMENT: Los parámetros enviados son incorrectos
             * - NOT_FOUND: El recurso solicitado no existe
             * - PERMISSION_DENIED: Problemas de autenticación/autorización
             */
            
            logger.error("❌ Error de comunicación gRPC:");
            logger.error("   🔸 Código de estado: {}", e.getStatus().getCode());
            logger.error("   🔸 Descripción: {}", e.getStatus().getDescription());
            logger.error("   🔸 Causa: {}", e.getStatus().getCause());
            
            // Creamos una excepción más amigable para el resto de la aplicación
            String userFriendlyMessage = mapGrpcErrorToUserMessage(e);
            throw new RuntimeException(userFriendlyMessage, e);
            
        } catch (Exception e) {
            // Para cualquier otro tipo de error no relacionado directamente con gRPC
            logger.error("❌ Error inesperado en cliente gRPC: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno del sistema de comunicación", e);
        }
    }

    /**
     * Método auxiliar para traducir códigos de error técnicos de gRPC
     * a mensajes más comprensibles para usuarios o sistemas cliente.
     * 
     * Esta traducción es importante porque los códigos de error gRPC son muy técnicos,
     * pero los consumidores de tu API REST necesitan mensajes más claros sobre qué salió mal.
     */
    private String mapGrpcErrorToUserMessage(StatusRuntimeException e) {
        switch (e.getStatus().getCode()) {
            case UNAVAILABLE:
                return "El servicio de usuarios no está disponible temporalmente. Intente nuevamente en unos momentos.";
            
            case DEADLINE_EXCEEDED:
                return "El servicio de usuarios está respondiendo lentamente. Intente nuevamente.";
            
            case NOT_FOUND:
                return "El usuario solicitado no fue encontrado.";
            
            case INVALID_ARGUMENT:
                return "Los parámetros de la petición son incorrectos.";
            
            case PERMISSION_DENIED:
                return "No tiene permisos para acceder a esta información.";
            
            default:
                return "Error temporal en el servicio de usuarios. Código: " + e.getStatus().getCode();
        }
    }

    /**
     * Método para probar la conectividad con el servidor gRPC.
     * 
     * Este método es especialmente útil durante el desarrollo y para health checks.
     * Te permite verificar rápidamente si la comunicación gRPC está funcionando
     * sin tener que ir a través de todo el flujo HTTP completo.
     */
    @NewSpan("grpc-ping-test")
    public boolean testConnection() {
        try {
            logger.info("🏓 Probando conectividad con user-service...");
            
            User response = getUser();
            
            logger.info("✅ Test de conectividad exitoso - Servidor respondió con usuario ID: {}", 
                       response.getId());
            return true;
            
        } catch (Exception e) {
            logger.warn("⚠️ Test de conectividad falló: {}", e.getMessage());
            return false;
        }
    }

    /*
     * Nota importante sobre el ciclo de vida de conexiones:
     * 
     * Con el starter de net.devh, no necesitas manejar manualmente el ciclo de vida
     * de las conexiones gRPC. Spring Boot automáticamente:
     * 
     * 1. Crea las conexiones cuando la aplicación inicia
     * 2. Mantiene las conexiones vivas de manera eficiente
     * 3. Maneja reconexiones automáticas si hay problemas de red temporales
     * 4. Cierra todas las conexiones limpiamente cuando la aplicación se apaga
     * 
     * Esta abstracción te permite enfocarte en la lógica de negocio en lugar de
     * los detalles técnicos de manejo de conexiones de red.
     */
}