package com.example.service_gateway.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

import com.example.service_gateway.service.SolicitudAsyncService;


/**
 * Controlador REST que demuestra cómo usar el cliente gRPC asíncrono.
 * 
 * Este controlador proporciona varios endpoints que muestran diferentes
 * patrones de uso del cliente asíncrono. En una aplicación real, estos
 * endpoints representarían las diferentes operaciones de negocio que tu
 * aplicación necesita realizar.
 */
@Slf4j
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {
    
    @Autowired
    private SolicitudAsyncService solicitudService;
    
    /**
     * Endpoint para procesar una solicitud simple de forma asíncrona.
     * 
     * Este endpoint demuestra el patrón más básico: recibir una petición HTTP,
     * procesarla usando el cliente gRPC asíncrono, y devolver la respuesta.
     * 
     * La ventaja de usar CompletableFuture es que Spring Boot automáticamente
     * maneja la respuesta asíncrona y libera el hilo del servidor mientras
     * espera la respuesta del servicio gRPC.
     * 
     * Ejemplo de uso:
     * POST /api/solicitudes/procesar
     * {
     *   "mensaje": "Mi primera solicitud",
     *   "id": 123
     * }
     */
    @PostMapping("/procesar")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> procesarSolicitud(
            @RequestBody Map<String, Object> request) {
        
        String mensaje = (String) request.get("mensaje");
        Integer id = (Integer) request.get("id");
        
        log.info("Recibida petición para procesar solicitud - ID: {}, Mensaje: {}", id, mensaje);
        
        // Hacer la llamada asíncrona al servicio
        return solicitudService.procesarSolicitudSimple(mensaje, id)
                .thenApply(resultado -> {
                    // Crear la respuesta cuando la operación termine
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("resultado", resultado);
                    response.put("timestamp", java.time.Instant.now().toString());
                    
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    // Manejar errores y devolver una respuesta de error
                    log.error("Error procesando solicitud: {}", throwable.getMessage());
                    
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("mensaje", "Error procesando la solicitud: " + throwable.getMessage());
                    errorResponse.put("timestamp", java.time.Instant.now().toString());
                    
                    return ResponseEntity.internalServerError().body(errorResponse);
                });
    }
    
    /**
     * Endpoint para procesar una solicitud con timeout personalizado.
     * 
     * Este endpoint muestra cómo puedes controlar el tiempo máximo de espera
     * para una operación. Esto es especialmente útil en servicios que necesitan
     * garantizar tiempos de respuesta específicos.
     * 
     * Ejemplo de uso:
     * POST /api/solicitudes/procesar-con-timeout/10
     * {
     *   "mensaje": "Solicitud con timeout",
     *   "id": 456
     * }
     */
    @PostMapping("/procesar-con-timeout/{timeoutSeconds}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> procesarConTimeout(
            @PathVariable long timeoutSeconds,
            @RequestBody Map<String, Object> request) {
        
        String mensaje = (String) request.get("mensaje");
        Integer id = (Integer) request.get("id");
        
        log.info("Procesando solicitud con timeout de {} segundos - ID: {}", timeoutSeconds, id);
        
        return solicitudService.procesarSolicitudConTimeout(mensaje, id, timeoutSeconds)
                .thenApply(response -> {
                    Map<String, Object> resultado = new HashMap<>();
                    resultado.put("status", "success");
                    resultado.put("solicitudId", response.getId());
                    resultado.put("mensaje", response.getMensaje());
                    resultado.put("exito", response.getExito());
                    resultado.put("timeoutUsado", timeoutSeconds + " segundos");
                    
                    return ResponseEntity.ok(resultado);
                })
                .exceptionally(throwable -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    
                    if (throwable.getCause() instanceof java.util.concurrent.TimeoutException) {
                        errorResponse.put("status", "timeout");
                        errorResponse.put("mensaje", "La operación tardó más de " + timeoutSeconds + " segundos");
                    } else {
                        errorResponse.put("status", "error");
                        errorResponse.put("mensaje", "Error: " + throwable.getMessage());
                    }
                    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
                    // return ResponseEntity.requestTimeout().body(errorResponse);
                });
    }
    
    /**
     * Endpoint para procesar múltiples solicitudes en paralelo.
     * 
     * Este endpoint demuestra cómo puedes procesar múltiples operaciones
     * al mismo tiempo, lo que puede mejorar significativamente el rendimiento
     * cuando necesitas hacer varias llamadas independientes.
     * 
     * Ejemplo de uso:
     * POST /api/solicitudes/procesar-lote
     * {
     *   "mensajes": ["Mensaje 1", "Mensaje 2", "Mensaje 3"]
     * }
     */
    @PostMapping("/procesar-lote")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> procesarLote(
            @RequestBody Map<String, List<String>> request) {
        
        List<String> mensajes = request.get("mensajes");
        log.info("Procesando lote de {} solicitudes", mensajes.size());
        
        // Convertir la lista a array para el método del servicio
        String[] mensajesArray = mensajes.toArray(new String[0]);
        
        return solicitudService.procesarLoteDeSolicitudes(mensajesArray)
                .thenApply(resultados -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("totalProcesadas", resultados.size());
                    response.put("resultados", resultados);
                    response.put("tiempoInicio", java.time.Instant.now().toString());
                    
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("mensaje", "Error procesando lote: " + throwable.getMessage());
                    
                    return ResponseEntity.internalServerError().body(errorResponse);
                });
    }
    
    /**
     * Endpoint para procesar una solicitud usando callbacks.
     * 
     * Este endpoint usa un enfoque diferente: no espera la respuesta de manera
     * síncrona, sino que devuelve inmediatamente una confirmación de que
     * la solicitud fue enviada. El resultado se procesará en segundo plano.
     * 
     * Este patrón es útil para operaciones "fire-and-forget" donde el cliente
     * no necesita esperar el resultado inmediatamente.
     * 
     * Ejemplo de uso:
     * POST /api/solicitudes/procesar-async
     * {
     *   "mensaje": "Procesamiento en segundo plano",
     *   "id": 789
     * }
     */
    @PostMapping("/procesar-async")
    public ResponseEntity<Map<String, Object>> procesarConCallback(
            @RequestBody Map<String, Object> request) {
        
        String mensaje = (String) request.get("mensaje");
        Integer id = (Integer) request.get("id");
        
        log.info("Iniciando procesamiento asíncrono - ID: {}", id);
        
        // Enviar la solicitud usando callbacks (no esperamos la respuesta)
        solicitudService.procesarSolicitudConCallback(mensaje, id);
        
        // Devolver respuesta inmediata
        Map<String, Object> response = new HashMap<>();
        response.put("status", "accepted");
        response.put("mensaje", "Solicitud enviada para procesamiento asíncrono");
        response.put("solicitudId", id);
        response.put("timestamp", java.time.Instant.now().toString());
        
        return ResponseEntity.accepted().body(response);
    }
    
    /**
     * Endpoint para procesar solicitudes encadenadas.
     * 
     * Este endpoint demuestra cómo hacer múltiples llamadas gRPC donde
     * una depende del resultado de la anterior. Es útil para workflows
     * complejos donde necesitas procesar datos paso a paso.
     * 
     * Ejemplo de uso:
     * POST /api/solicitudes/procesar-encadenadas
     * {
     *   "mensajeInicial": "Inicio del workflow"
     * }
     */
    @PostMapping("/procesar-encadenadas")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> procesarEncadenadas(
            @RequestBody Map<String, String> request) {
        
        String mensajeInicial = request.get("mensajeInicial");
        log.info("Iniciando procesamiento encadenado con mensaje: {}", mensajeInicial);
        
        return solicitudService.procesarSolicitudesEncadenadas(mensajeInicial)
                .thenApply(resultado -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("resultadoFinal", resultado);
                    response.put("tipoOperacion", "encadenada");
                    response.put("timestamp", java.time.Instant.now().toString());
                    
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("mensaje", "Error en procesamiento encadenado: " + throwable.getMessage());
                    errorResponse.put("tipoOperacion", "encadenada");
                    
                    return ResponseEntity.internalServerError().body(errorResponse);
                });
    }
    
    /**
     * Endpoint de health check para verificar el estado del cliente gRPC.
     * 
     * Este endpoint es útil para monitoreo y diagnóstico. Te permite
     * verificar si el cliente gRPC está funcionando correctamente.
     * 
     * Ejemplo de uso:
     * GET /api/solicitudes/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        boolean clienteConectado = solicitudService.verificarEstadoCliente();
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", clienteConectado ? "UP" : "DOWN");
        health.put("componente", "SolicitudGrpcClient");
        health.put("timestamp", java.time.Instant.now().toString());
        health.put("detalles", Map.of(
            "grpcClientConectado", clienteConectado,
            "servicioDisponible", true
        ));
        
        return clienteConectado ? 
            ResponseEntity.ok(health) : 
            ResponseEntity.status(503).body(health); // Service Unavailable
    }
    
    /**
     * Endpoint para obtener ejemplos de uso de la API.
     * 
     * Este endpoint devuelve documentación sobre cómo usar cada endpoint
     * de la API. Es útil para desarrollo y testing.
     * 
     * Ejemplo de uso:
     * GET /api/solicitudes/ejemplos
     */
    @GetMapping("/ejemplos")
    public ResponseEntity<Map<String, Object>> obtenerEjemplos() {
        Map<String, Object> ejemplos = new HashMap<>();
        
        ejemplos.put("procesar", Map.of(
            "method", "POST",
            "url", "/api/solicitudes/procesar",
            "body", Map.of("mensaje", "Mi primera solicitud", "id", 123),
            "descripcion", "Procesa una solicitud de forma asíncrona y espera la respuesta"
        ));
        
        ejemplos.put("procesarConTimeout", Map.of(
            "method", "POST", 
            "url", "/api/solicitudes/procesar-con-timeout/10",
            "body", Map.of("mensaje", "Solicitud con timeout", "id", 456),
            "descripcion", "Procesa una solicitud con un timeout específico"
        ));
        
        ejemplos.put("procesarLote", Map.of(
            "method", "POST",
            "url", "/api/solicitudes/procesar-lote",
            "body", Map.of("mensajes", List.of("Mensaje 1", "Mensaje 2", "Mensaje 3")),
            "descripcion", "Procesa múltiples solicitudes en paralelo"
        ));
        
        ejemplos.put("procesarAsync", Map.of(
            "method", "POST",
            "url", "/api/solicitudes/procesar-async", 
            "body", Map.of("mensaje", "Procesamiento en segundo plano", "id", 789),
            "descripcion", "Envía una solicitud para procesamiento asíncrono sin esperar respuesta"
        ));
        
        ejemplos.put("procesarEncadenadas", Map.of(
            "method", "POST",
            "url", "/api/solicitudes/procesar-encadenadas",
            "body", Map.of("mensajeInicial", "Inicio del workflow"),
            "descripcion", "Procesa solicitudes encadenadas donde una depende de la anterior"
        ));
        
        ejemplos.put("health", Map.of(
            "method", "GET",
            "url", "/api/solicitudes/health",
            "descripcion", "Verifica el estado del cliente gRPC"
        ));
        
        return ResponseEntity.ok(ejemplos);
    }
}