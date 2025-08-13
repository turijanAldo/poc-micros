package com.example.service_gateway.controller;

import com.example.grpc.user.User;
import com.example.service_gateway.model.HealthResponse;
import com.example.service_gateway.model.UserResponse;

import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Este controlador es el "recepcionista bilingüe" de tu sistema.
 * 
 * Su trabajo es recibir peticiones HTTP del mundo exterior (navegadores, apps móviles, 
 * otros servicios) y traducirlas a llamadas gRPC hacia servicios internos especializados.
 * 
 * Piensa en esto como la recepción de un hospital: cuando llegas, el recepcionista
 * entiende tu problema en lenguaje cotidiano, pero luego traduce esa información
 * al lenguaje médico especializado para comunicarse con los doctores internos.
 */
@RestController
@RequestMapping("/api/users")  // Todas las rutas empiezan con /api/users
@CrossOrigin(origins = "*")    // Permite peticiones desde cualquier origen para desarrollo
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private GrpcClientService grpcClientService;


        /**
     * Endpoint para obtener información del usuario actual.
     * 
     * La anotación @NewSpan crea automáticamente un nuevo span de tracing,
     * lo que te permite seguir esta operación específica en Zipkin y ver
     * exactamente cuánto tiempo toma cada parte del proceso.
     * 
     * URL de prueba: GET http://localhost:8080/api/users/current
     */
    @GetMapping("/current")
    @NewSpan("get-current-user")  // Nombre del span para identificarlo en Zipkin
    public ResponseEntity<UserResponse> getCurrentUser() {
        try{
            logger.info("Recibiendo solicitud para obtener el usuario actual");

            User grpcUser = grpcClientService.getUser();

            UserResponse httpResponse = new UserResponse(
                grpcUser.getId(),
                grpcUser.getName()
                );
            logger.info("Usuario actual obtenido: {}", httpResponse);
            return ResponseEntity.ok(httpResponse);

        }catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(UserController.class);
            logger.error("Error al obtener el usuario actual", e);
            return ResponseEntity.status(500).body(new UserResponse("Error al obtener el usuario actual"));
        }

    }
        /**
     * Endpoint adicional para obtener información de un usuario específico por ID.
     * 
     * Este endpoint demuestra cómo puedes pasar parámetros desde HTTP hacia gRPC.
     * Aunque tu servicio gRPC actual no maneja IDs específicos, este ejemplo
     * te muestra el patrón para cuando expandas tu API.
     * 
     * URL de prueba: GET http://localhost:8080/api/users/12345
     */
    @GetMapping("/{userId}")
    @NewSpan("get-user-by-id")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable @SpanTag("user.id") Long userId) {
        try {
            User grpcUser = grpcClientService.getUser();

            if(grpcUser.getId() != userId) {
                return ResponseEntity.notFound().build();
            }
            UserResponse httpResponse = new UserResponse(
                grpcUser.getId(),
                grpcUser.getName()
            );
            return ResponseEntity.ok(httpResponse);
        } catch (Exception e) {
            logger.error("Error al obtener el usuario por ID", e);
            return ResponseEntity.status(500).body(new UserResponse("Error al obtener el usuario por ID"));
        }
    }
        @GetMapping("/health")
    @NewSpan("grpc-health-check")
    public ResponseEntity<HealthResponse> checkGrpcHealth() {
        try {
            logger.info("🔍 Verificando salud de la comunicación gRPC...");
            
            long startTime = System.currentTimeMillis();
            User testUser = grpcClientService.getUser();
            long responseTime = System.currentTimeMillis() - startTime;
            
            HealthResponse health = new HealthResponse(
                "healthy",
                "Comunicación gRPC funcionando correctamente",
                responseTime + "ms",
                testUser.getId()
            );
            
            logger.info("✅ Health check exitoso - Tiempo de respuesta: {}ms", responseTime);
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("❌ Health check falló: {}", e.getMessage(), e);
            
            HealthResponse health = new HealthResponse(
                "unhealthy",
                "Error en comunicación gRPC: " + e.getMessage(),
                "N/A",
                null
            );
            
            return ResponseEntity.status(503).body(health);
        }
    }

}
