package com.example.service_gateway.beans;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;

// Imports para las clases generadas por protobuf
import com.company.solicitud.grpc.SolicitudResponseServiceGrpc;
import com.company.solicitud.grpc.RecibeSolicitud;
import com.company.solicitud.grpc.SolicitudResponse;

@Slf4j
@Component
public class SolicitudGrpcAsyncClient {
    
    // Canal de comunicación con el servidor gRPC
    private ManagedChannel channel;
    
    // Stub asíncrono para hacer las llamadas
    private SolicitudResponseServiceGrpc.SolicitudResponseServiceStub asyncStub;
    
    // Configuración desde application.properties
    @Value("${grpc.client.solicitud.host:localhost}")
    private String grpcHost;
    
    @Value("${grpc.client.solicitud.port:9095}")
    private int grpcPort;
    
    /**
     * Inicializa el canal y el stub después de que Spring cree el bean.
     * Este método se ejecuta automáticamente cuando Spring inicializa el componente.
     */
    @PostConstruct
    public void init() {
        log.info("Inicializando cliente gRPC para {}:{}", grpcHost, grpcPort);
        
        // Construye el canal de comunicación
        channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                .usePlaintext() // Para desarrollo, en producción usar TLS
                .keepAliveTime(30, TimeUnit.SECONDS) // Mantiene la conexión viva
                .keepAliveTimeout(5, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .maxInboundMessageSize(1024 * 1024) // Tamaño máximo de mensaje: 1MB
                .build();
                
        // Crea el stub asíncrono que usaremos para las llamadas
        asyncStub = SolicitudResponseServiceGrpc.newStub(channel);
        
        log.info("Cliente gRPC inicializado correctamente");
    }
    
    /**
     * Envía una solicitud de forma asíncrona usando CompletableFuture.
     * Este es el método más fácil de usar para operaciones asíncronas.
     * 
     * @param solicitud La solicitud a enviar
     * @return CompletableFuture que se completará con la respuesta
     */
    public CompletableFuture<SolicitudResponse> enviarSolicitudAsync(RecibeSolicitud solicitud) {
        log.debug("Enviando solicitud asíncrona con ID: {}", solicitud.getId());
        
        // CompletableFuture que se completará cuando recibamos la respuesta
        CompletableFuture<SolicitudResponse> future = new CompletableFuture<>();
        
        // StreamObserver que maneja la respuesta del servidor
        StreamObserver<SolicitudResponse> responseObserver = new StreamObserver<SolicitudResponse>() {
            @Override
            public void onNext(SolicitudResponse response) {
                // Se ejecuta cuando recibimos la respuesta del servidor
                log.debug("Respuesta recibida para solicitud ID: {}", response.getId());
                future.complete(response); // Completa el CompletableFuture con la respuesta
            }
            
            @Override
            public void onError(Throwable throwable) {
                // Se ejecuta si hay un error en la comunicación
                log.error("Error en la comunicación gRPC: {}", throwable.getMessage(), throwable);
                future.completeExceptionally(throwable); // Completa el future con error
            }
            
            @Override
            public void onCompleted() {
                // Se ejecuta cuando el servidor termina de enviar datos
                log.debug("Comunicación gRPC completada");
                // No necesitamos hacer nada aquí para llamadas unarias
            }
        };
        
        // Hace la llamada asíncrona al servidor
        asyncStub.enviarRespuesta(solicitud, responseObserver);
        
        return future;
    }
    
    /**
     * Envía una solicitud con callback personalizado.
     * Útil cuando quieres manejar la respuesta de una manera específica.
     * 
     * @param solicitud La solicitud a enviar
     * @param onSuccess Función que se ejecuta cuando la operación es exitosa
     * @param onError Función que se ejecuta cuando hay un error
     */
    public void enviarSolicitudConCallback(RecibeSolicitud solicitud, 
                                         java.util.function.Consumer<SolicitudResponse> onSuccess,
                                         java.util.function.Consumer<Throwable> onError) {
        
        log.debug("Enviando solicitud con callback para ID: {}", solicitud.getId());
        
        StreamObserver<SolicitudResponse> responseObserver = new StreamObserver<SolicitudResponse>() {
            @Override
            public void onNext(SolicitudResponse response) {
                try {
                    onSuccess.accept(response); // Ejecuta el callback de éxito
                } catch (Exception e) {
                    log.error("Error ejecutando callback de éxito: {}", e.getMessage(), e);
                }
            }
            
            @Override
            public void onError(Throwable throwable) {
                log.error("Error en comunicación gRPC: {}", throwable.getMessage(), throwable);
                try {
                    onError.accept(throwable); // Ejecuta el callback de error
                } catch (Exception e) {
                    log.error("Error ejecutando callback de error: {}", e.getMessage(), e);
                }
            }
            
            @Override
            public void onCompleted() {
                log.debug("Comunicación completada para solicitud ID: {}", solicitud.getId());
            }
        };
        
        asyncStub.enviarRespuesta(solicitud, responseObserver);
    }
    
    /**
     * Envía múltiples solicitudes en paralelo.
     * Útil cuando necesitas procesar varias solicitudes al mismo tiempo.
     * 
     * @param solicitudes Array de solicitudes a enviar
     * @return CompletableFuture que se completa cuando todas las solicitudes terminan
     */
    public CompletableFuture<java.util.List<SolicitudResponse>> enviarSolicitudesEnParalelo(RecibeSolicitud... solicitudes) {
        log.info("Enviando {} solicitudes en paralelo", solicitudes.length);
        
        // Convierte cada solicitud en un CompletableFuture
        CompletableFuture<SolicitudResponse>[] futures = new CompletableFuture[solicitudes.length];
        
        for (int i = 0; i < solicitudes.length; i++) {
            futures[i] = enviarSolicitudAsync(solicitudes[i]);
        }
        
        // Combina todos los futures en uno solo que se completa cuando todos terminan
        return CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    // Recolecta todas las respuestas
                    java.util.List<SolicitudResponse> respuestas = new java.util.ArrayList<>();
                    for (CompletableFuture<SolicitudResponse> future : futures) {
                        try {
                            respuestas.add(future.get()); // get() es seguro aquí porque allOf ya terminó
                        } catch (Exception e) {
                            log.error("Error obteniendo respuesta: {}", e.getMessage(), e);
                            // Podrías decidir si continuar o fallar completamente
                        }
                    }
                    return respuestas;
                });
    }
    
    /**
     * Método de conveniencia para crear una solicitud de prueba.
     * Útil para testing y desarrollo.
     */
    public RecibeSolicitud crearSolicitudEjemplo(String mensaje, int id) {
        return RecibeSolicitud.newBuilder()
                .setId(id)
                .setMensaje(mensaje)
                .setStatus(1)
                .setStatusString("ENVIADA")
                .build();
    }
    
    /**
     * Verifica si el cliente está listo para hacer llamadas.
     * Útil para health checks.
     */
    public boolean estaConectado() {
        if (channel == null) {
            return false;
        }
        
        return !channel.isShutdown() && !channel.isTerminated();
    }
    
    /**
     * Cierra el canal de comunicación cuando Spring destruye el bean.
     * Es importante cerrar recursos para evitar memory leaks.
     */
    @PreDestroy
    public void cleanup() {
        log.info("Cerrando cliente gRPC...");
        
        if (channel != null && !channel.isShutdown()) {
            try {
                // Intenta cerrar gracefully
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("Interrupción durante el cierre del canal gRPC", e);
                Thread.currentThread().interrupt();
            } finally {
                // Fuerza el cierre si no se cerró gracefully
                if (!channel.isTerminated()) {
                    channel.shutdownNow();
                }
            }
        }
        
        log.info("Cliente gRPC cerrado");
    }
}