package com.example.service_gateway.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.List;

import com.example.service_gateway.beans.SolicitudGrpcAsyncClient;
import com.company.solicitud.grpc.RecibeSolicitud;
import com.company.solicitud.grpc.SolicitudResponse;

/**
 * Servicio de negocio que utiliza el cliente gRPC asíncrono.
 * Este servicio encapsula la lógica de negocio y maneja las operaciones
 * con el cliente gRPC de manera asíncrona.
 */
@Slf4j
@Service
public class SolicitudAsyncService {
    
    @Autowired
    private SolicitudGrpcAsyncClient grpcClient;
    
    /**
     * Procesa una solicitud de forma asíncrona usando CompletableFuture.
     * Este método demuestra cómo usar el cliente para hacer una llamada simple.
     * 
     * @param mensaje El mensaje de la solicitud
     * @param solicitudId El ID de la solicitud
     * @return CompletableFuture con la respuesta procesada
     */
    public CompletableFuture<String> procesarSolicitudSimple(String mensaje, int solicitudId) {
        log.info("Procesando solicitud simple - ID: {}, Mensaje: {}", solicitudId, mensaje);
        
        // Crear la solicitud usando el método de conveniencia del cliente
        RecibeSolicitud solicitud = grpcClient.crearSolicitudEjemplo(mensaje, solicitudId);
        
        // Hacer la llamada asíncrona y transformar la respuesta
        return grpcClient.enviarSolicitudAsync(solicitud)
                .thenApply(response -> {
                    log.info("Respuesta recibida para solicitud {}: {}", 
                            response.getId(), response.getMensaje());
                    
                    // Aquí puedes agregar lógica adicional de procesamiento
                    return String.format("Procesado exitosamente - ID: %d, Éxito: %s, Mensaje: %s",
                            response.getId(), response.getExito(), response.getMensaje());
                })
                .exceptionally(throwable -> {
                    // Manejo de errores
                    log.error("Error procesando solicitud {}: {}", solicitudId, throwable.getMessage());
                    return String.format("Error procesando solicitud %d: %s", solicitudId, throwable.getMessage());
                });
    }
    
    /**
     * Procesa una solicitud con timeout personalizado.
     * Útil cuando necesitas garantizar que la operación no tarde más de un tiempo específico.
     * 
     * @param mensaje El mensaje de la solicitud
     * @param solicitudId El ID de la solicitud
     * @param timeoutSeconds Timeout en segundos
     * @return CompletableFuture con la respuesta
     */
    public CompletableFuture<SolicitudResponse> procesarSolicitudConTimeout(String mensaje, int solicitudId, long timeoutSeconds) {
        log.info("Procesando solicitud con timeout de {} segundos - ID: {}", timeoutSeconds, solicitudId);
        
        RecibeSolicitud solicitud = grpcClient.crearSolicitudEjemplo(mensaje, solicitudId);
        
        return grpcClient.enviarSolicitudAsync(solicitud)
                .orTimeout(timeoutSeconds, TimeUnit.SECONDS) // Aplica timeout
                .whenComplete((response, throwable) -> {
                    if (throwable != null) {
                        if (throwable instanceof java.util.concurrent.TimeoutException) {
                            log.error("Timeout procesando solicitud {}: operación tardó más de {} segundos", 
                                    solicitudId, timeoutSeconds);
                        } else {
                            log.error("Error procesando solicitud {}: {}", solicitudId, throwable.getMessage());
                        }
                    } else {
                        log.info("Solicitud {} procesada exitosamente dentro del timeout", solicitudId);
                    }
                });
    }
    
    /**
     * Procesa múltiples solicitudes en lote de forma asíncrona.
     * Este método demuestra cómo manejar múltiples operaciones paralelas.
     * 
     * @param mensajes Array de mensajes para las solicitudes
     * @return CompletableFuture con la lista de respuestas
     */
    public CompletableFuture<List<String>> procesarLoteDeSolicitudes(String... mensajes) {
        log.info("Procesando lote de {} solicitudes", mensajes.length);
        
        // Crear array de solicitudes
        RecibeSolicitud[] solicitudes = new RecibeSolicitud[mensajes.length];
        for (int i = 0; i < mensajes.length; i++) {
            solicitudes[i] = grpcClient.crearSolicitudEjemplo(mensajes[i], i + 1);
        }
        
        // Procesar todas en paralelo
        return grpcClient.enviarSolicitudesEnParalelo(solicitudes)
                .thenApply(respuestas -> {
                    // Transformar las respuestas a un formato más simple
                    return respuestas.stream()
                            .map(resp -> String.format("ID: %d, Éxito: %s, Mensaje: %s", 
                                    resp.getId(), resp.getExito(), resp.getMensaje()))
                            .collect(java.util.stream.Collectors.toList());
                })
                .whenComplete((resultado, throwable) -> {
                    if (throwable != null) {
                        log.error("Error procesando lote de solicitudes: {}", throwable.getMessage());
                    } else {
                        log.info("Lote de {} solicitudes procesado exitosamente", resultado.size());
                    }
                });
    }
    
    /**
     * Procesa una solicitud usando callbacks en lugar de CompletableFuture.
     * Esta aproximación es útil cuando quieres manejar la respuesta de manera inmediata
     * sin encadenar operaciones adicionales.
     * 
     * @param mensaje El mensaje de la solicitud
     * @param solicitudId El ID de la solicitud
     */
    public void procesarSolicitudConCallback(String mensaje, int solicitudId) {
        log.info("Procesando solicitud con callback - ID: {}", solicitudId);
        
        RecibeSolicitud solicitud = grpcClient.crearSolicitudEjemplo(mensaje, solicitudId);
        
        // Usar el método de callback del cliente
        grpcClient.enviarSolicitudConCallback(
            solicitud,
            // Callback de éxito
            response -> {
                log.info("Éxito - Solicitud {} procesada: {}", response.getId(), response.getMensaje());
                // Aquí puedes agregar lógica adicional que se ejecute al recibir la respuesta
                // Por ejemplo, actualizar una base de datos, enviar notificaciones, etc.
                procesarRespuestaExitosa(response);
            },
            // Callback de error
            throwable -> {
                log.error("Error procesando solicitud {}: {}", solicitudId, throwable.getMessage());
                // Aquí puedes agregar lógica de manejo de errores
                // Por ejemplo, reintentar la operación, notificar al usuario, etc.
                manejarErrorEnProcesamiento(solicitudId, throwable);
            }
        );
    }
    
    /**
     * Encadena múltiples operaciones asíncronas.
     * Este ejemplo muestra cómo puedes combinar múltiples llamadas gRPC
     * donde una depende del resultado de la anterior.
     * 
     * @param mensajeInicial El mensaje de la primera solicitud
     * @return CompletableFuture con el resultado final
     */
    public CompletableFuture<String> procesarSolicitudesEncadenadas(String mensajeInicial) {
        log.info("Iniciando procesamiento de solicitudes encadenadas");
        
        // Primera solicitud
        RecibeSolicitud primeraSolicitud = grpcClient.crearSolicitudEjemplo(mensajeInicial, 1);
        
        return grpcClient.enviarSolicitudAsync(primeraSolicitud)
                .thenCompose(primeraRespuesta -> {
                    // Usar el resultado de la primera solicitud para crear la segunda
                    log.info("Primera solicitud completada, creando segunda solicitud");
                    
                    String mensajeSegundo = "Continuación: " + primeraRespuesta.getMensaje();
                    RecibeSolicitud segundaSolicitud = grpcClient.crearSolicitudEjemplo(mensajeSegundo, 2);
                    
                    // Hacer la segunda llamada usando thenCompose (no thenApply)
                    return grpcClient.enviarSolicitudAsync(segundaSolicitud);
                })
                .thenApply(segundaRespuesta -> {
                    // Procesar el resultado final
                    log.info("Ambas solicitudes completadas exitosamente");
                    return String.format("Procesamiento encadenado completado. Resultado final: %s", 
                            segundaRespuesta.getMensaje());
                })
                .exceptionally(throwable -> {
                    log.error("Error en el procesamiento encadenado: {}", throwable.getMessage());
                    return "Error en el procesamiento encadenado: " + throwable.getMessage();
                });
    }
    
    /**
     * Verifica el estado del cliente gRPC.
     * Útil para health checks y monitoreo.
     * 
     * @return true si el cliente está funcionando correctamente
     */
    public boolean verificarEstadoCliente() {
        boolean conectado = grpcClient.estaConectado();
        log.debug("Estado del cliente gRPC: {}", conectado ? "Conectado" : "Desconectado");
        return conectado;
    }
    
    // Métodos privados auxiliares para el manejo de respuestas y errores
    
    private void procesarRespuestaExitosa(SolicitudResponse response) {
        // Aquí puedes agregar lógica específica para manejar respuestas exitosas
        // Por ejemplo: guardar en base de datos, enviar notificaciones, actualizar caché, etc.
        log.debug("Procesando respuesta exitosa para solicitud ID: {}", response.getId());
    }
    
    private void manejarErrorEnProcesamiento(int solicitudId, Throwable error) {
        // Aquí puedes agregar lógica específica para manejar errores
        // Por ejemplo: logging detallado, reintentos, notificaciones de error, etc.
        log.error("Manejando error para solicitud ID: {} - Error: {}", solicitudId, error.getMessage());
        
        // Ejemplo de lógica adicional: podrías guardar el error en una base de datos
        // o enviar una notificación al sistema de monitoreo
    }
}