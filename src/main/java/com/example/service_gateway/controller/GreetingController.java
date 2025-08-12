package com.example.service_gateway.controller;

import com.example.service_gateway.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer.SpanInScope;
// import io.micrometer.tracing.Scope;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
public class GreetingController {
    
    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);
    
    // ═══════════════════════════════════════════════════════════════
    // CONFIGURACIÓN DE WEBCLIENT CON OBSERVABILIDAD COMPLETA
    // ═══════════════════════════════════════════════════════════════
    
    private final WebClient webClient;
    private final Tracer tracer;  // Para crear spans manuales cuando necesitemos granularidad extra
    
    // ═══════════════════════════════════════════════════════════════
    // MÉTRICAS GRANULARES - ENFOQUE CORRECTO CON CONTADORES ESPECÍFICOS
    // En lugar de un contador general que se incrementa con diferentes tags,
    // creamos contadores específicos para cada caso. Esto es más eficiente
    // y evita problemas con cardinalidad alta de métricas.
    // ═══════════════════════════════════════════════════════════════
    
    // Contadores generales del endpoint de greeting
    private final Counter greetingRequestCounter;
    private final Counter greetingSuccessCounter;
    
    // Contadores específicos para diferentes tipos de errores
    // Cada tipo de error tiene su propio contador para mejor granularidad
    private final Counter greetingErrorNullUserCounter;
    private final Counter greetingErrorWebClientCounter;
    private final Counter greetingErrorUnexpectedCounter;
    
    // Timers para medir duración
    private final Timer greetingRequestTimer;
    
    // Métricas específicas para llamadas al service-user
    private final Counter userServiceCallsCounter;
    private final Counter userServiceHttpErrorCounter;
    private final Counter userServiceTimeoutErrorCounter;
    private final Timer userServiceCallTimer;
    
    public GreetingController(
            MeterRegistry meterRegistry,
            Tracer tracer,
            @Value("${app.service-user.base-url:http://localhost:8081}") String userServiceBaseUrl,
            @Value("${app.service-user.timeout.connect:2s}") Duration connectTimeout,
            @Value("${app.service-user.timeout.read:5s}") Duration readTimeout) {
        
        this.tracer = tracer;
        
        // Configuramos WebClient con timeouts y observabilidad automática
        this.webClient = WebClient.builder()
                .baseUrl(userServiceBaseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB buffer
                .build();
        
        // ═══════════════════════════════════════════════════════════════
        // INICIALIZACIÓN DE CONTADORES ESPECÍFICOS
        // Cada contador tiene un propósito específico y tags fijos.
        // Esto es mucho más eficiente que usar increment(tag, value) dinámicamente.
        // ═══════════════════════════════════════════════════════════════
        
        // Contadores principales del endpoint
        this.greetingRequestCounter = Counter.builder("greeting_requests_total")
                .description("Total number of greeting requests received")
                .tag("service", "gateway")
                .tag("endpoint", "greet-user")
                .register(meterRegistry);
        
        this.greetingSuccessCounter = Counter.builder("greeting_success_total")
                .description("Total number of successful greeting responses")
                .tag("service", "gateway")
                .tag("endpoint", "greet-user")
                .register(meterRegistry);
        
        // Contadores específicos para cada tipo de error
        // Esto nos da granularidad sin crear problemas de cardinalidad
        this.greetingErrorNullUserCounter = Counter.builder("greeting_errors_total")
                .description("Greeting errors due to null user response")
                .tag("service", "gateway")
                .tag("error_type", "null_user")
                .register(meterRegistry);
        
        this.greetingErrorWebClientCounter = Counter.builder("greeting_errors_total")
                .description("Greeting errors due to WebClient issues")
                .tag("service", "gateway")
                .tag("error_type", "webclient_error")
                .register(meterRegistry);
        
        this.greetingErrorUnexpectedCounter = Counter.builder("greeting_errors_total")
                .description("Greeting errors due to unexpected exceptions")
                .tag("service", "gateway")
                .tag("error_type", "unexpected")
                .register(meterRegistry);
        
        // Timer principal para duración de requests
        this.greetingRequestTimer = Timer.builder("greeting_request_duration_seconds")
                .description("Duration of greeting request processing")
                .tag("service", "gateway")
                .tag("endpoint", "greet-user")
                .register(meterRegistry);
        
        // Métricas específicas para interacciones con user service
        this.userServiceCallsCounter = Counter.builder("user_service_calls_total")
                .description("Total calls made to user service")
                .tag("source_service", "gateway")
                .tag("target_service", "user-service")
                .register(meterRegistry);
        
        this.userServiceHttpErrorCounter = Counter.builder("user_service_errors_total")
                .description("HTTP errors from user service calls")
                .tag("source_service", "gateway")
                .tag("target_service", "user-service")
                .tag("error_category", "http")
                .register(meterRegistry);
        
        this.userServiceTimeoutErrorCounter = Counter.builder("user_service_errors_total")
                .description("Timeout errors from user service calls")
                .tag("source_service", "gateway")
                .tag("target_service", "user-service")
                .tag("error_category", "timeout")
                .register(meterRegistry);
        
        this.userServiceCallTimer = Timer.builder("user_service_call_duration_seconds")
                .description("Duration of calls to user service")
                .tag("source_service", "gateway")
                .tag("target_service", "user-service")
                .register(meterRegistry);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ENDPOINT PRINCIPAL CON OBSERVABILIDAD CORREGIDA
    // ═══════════════════════════════════════════════════════════════
    
@GetMapping("/greet-user")
public Map<String, Object> greetUser() {
    String requestId = UUID.randomUUID().toString();
    MDC.put("requestId", requestId);
    
    Timer.Sample sample = Timer.start();
    
    try {
        greetingRequestCounter.increment();
        logger.info("🚀 Starting greet-user request processing with requestId: {}", requestId);
        
        // ═══════════════════════════════════════════════════════════════
        // PASO 1: Crear el span usando el Tracer
        // El tracer.nextSpan() crea un nuevo span hijo del span actual
        // ═══════════════════════════════════════════════════════════════
        Span businessLogicSpan = tracer.nextSpan()
                .name("business-logic-greeting")  // Nombre descriptivo del span
                .tag("request.id", requestId)      // Tags para filtrar/buscar
                .tag("operation.type", "greeting-generation")
                .start();  // ¡Importante! start() hace que el span comience a medir tiempo
        
        // ═══════════════════════════════════════════════════════════════
        // PASO 2: Hacer el span "actual" en el contexto del hilo
        // businessLogicSpan.makeCurrent() es el método correcto en Micrometer Tracing
        // Esto es equivalente al antiguo tracer.withSpanInScope() de Sleuth
        // ═══════════════════════════════════════════════════════════════
        // try (Tracer.SpanInScope spanInScope = businessLogicSpan.makeCurrent()) {
        try (SpanInScope spanInScope = tracer.withSpan(businessLogicSpan)) {
            
            // ═══════════════════════════════════════════════════════════════
            // PASO 3: Lógica de negocio dentro del contexto del span
            // Mientras este try-with-resources esté activo, cualquier nueva traza
            // o span que se cree automáticamente será hijo de businessLogicSpan
            // ═══════════════════════════════════════════════════════════════
            
            logger.info("📊 Executing business logic within custom span context");
            
            // Esta llamada automáticamente heredará el contexto de traza
            User user = callUserService(requestId);
            
            if (user == null) {
                // Añadimos información de error al span para debugging
                businessLogicSpan.tag("error", "true");
                businessLogicSpan.tag("error.reason", "null_user");
                
                logger.warn("⚠️ Received null user from service-user for requestId: {}", requestId);
                greetingErrorNullUserCounter.increment();
                return createErrorResponse("User service returned no data", requestId);
            }
            
            // Generamos el greeting y añadimos información útil al span
            String greeting = generateGreeting(user);
            businessLogicSpan.tag("user.name", user.getName());
            businessLogicSpan.tag("greeting.generated", "true");
            businessLogicSpan.tag("business.outcome", "success");
            
            logger.info("✅ Successfully generated greeting for user: {} with requestId: {}", 
                       user.getName(), requestId);
            
            greetingSuccessCounter.increment();
            return createSuccessResponse(greeting, user, requestId);
            
        } finally {
            // ═══════════════════════════════════════════════════════════════
            // PASO 4: Cerrar explícitamente el span
            // El try-with-resources se encarga del spanInScope automáticamente,
            // pero necesitamos cerrar manualmente el span para registrar su duración
            // ═══════════════════════════════════════════════════════════════
            businessLogicSpan.end();
            logger.info("🔚 Business logic span completed for requestId: {}", requestId);
        }
        
    } catch (WebClientResponseException e) {
        logger.error("🔥 WebClient error during greet-user processing. Status: {}, Body: {}, RequestId: {}", 
                    e.getStatusCode(), e.getResponseBodyAsString(), requestId, e);
        
        greetingErrorWebClientCounter.increment();
        return createErrorResponse("User service error: " + e.getStatusCode(), requestId);
        
    } catch (Exception e) {
        logger.error("💥 Unexpected error during greet-user processing with requestId: {}", requestId, e);
        greetingErrorUnexpectedCounter.increment();
        return createErrorResponse("Failed to process greeting request: " + e.getMessage(), requestId);
        
    } finally {
        sample.stop(greetingRequestTimer);
        MDC.remove("requestId");
        logger.info("🏁 Completed greet-user request processing for requestId: {}", requestId);
    }
}

// ═══════════════════════════════════════════════════════════════
// MÉTODO AUXILIAR PARA DEMOSTRAR PROPAGACIÓN AUTOMÁTICA DE CONTEXTO
// ═══════════════════════════════════════════════════════════════
private User callUserService(String requestId) {
    Timer.Sample userServiceSample = Timer.start();
    
    try {
        userServiceCallsCounter.increment();
        logger.info("🔗 Making call to service-user at /user endpoint for requestId: {}", requestId);
        
        // ═══════════════════════════════════════════════════════════════
        // NOTA IMPORTANTE: WebClient automáticamente propaga el contexto de traza
        // No necesitamos hacer nada especial aquí - Spring Boot se encarga
        // de que el trace ID y span ID se propaguen automáticamente al service-user
        // ═══════════════════════════════════════════════════════════════
        
        User user = webClient
                .get()
                .uri("/user")
                .header("X-Request-ID", requestId)  // Header personalizado para correlación adicional
                .retrieve()
                .bodyToMono(User.class)
                .doOnNext(u -> {
                    logger.info("📦 Successfully received user data: {} for requestId: {}", u.getName(), requestId);
                    
                    // Podemos añadir información al span actual desde aquí también
                    Span currentSpan = tracer.currentSpan();
                    if (currentSpan != null) {
                        currentSpan.tag("user.service.response", "success");
                        currentSpan.tag("user.id", String.valueOf(u.getId()));
                    }
                })
                .doOnError(error -> {
                    logger.error("❌ Error calling service-user for requestId: {}: {}", requestId, error.getMessage());
                    
                    // También podemos marcar errores en el span actual
                    Span currentSpan = tracer.currentSpan();
                    if (currentSpan != null) {
                        currentSpan.tag("error", "true");
                        currentSpan.tag("error.message", error.getMessage());
                    }
                    
                    // Incrementamos contadores de error según el tipo
                    if (error instanceof WebClientResponseException) {
                        userServiceHttpErrorCounter.increment();
                    } else {
                        userServiceTimeoutErrorCounter.increment();
                    }
                })
                .block();
        
        return user;
        
    } finally {
        userServiceSample.stop(userServiceCallTimer);
        logger.info("⏱️ User service call completed in {} for requestId: {}", 
                   userServiceSample.stop(userServiceCallTimer), requestId);
    }
}
    
    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES CON MANEJO CORRECTO DE MÉTRICAS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Realiza la llamada al servicio de usuario con métricas detalladas.
     * Este método demuestra cómo manejar diferentes tipos de errores
     * con contadores específicos en lugar de tags dinámicos.
     */

    
    /**
     * Genera el mensaje de saludo. Aquí podríamos añadir lógica más compleja
     * como personalización basada en la hora del día, preferencias del usuario, etc.
     */
    private String generateGreeting(User user) {
        // En un caso real, aquí podríamos tener lógica más sofisticada:
        // - Personalización basada en la hora del día
        // - Diferentes saludos según el perfil del usuario
        // - Internacionalización basada en locale
        return "Hello, " + user.getName() + "!";
    }
    
    /**
     * Crea una respuesta de éxito estructurada y consistente
     */
    private Map<String, Object> createSuccessResponse(String greeting, User user, String requestId) {
        return Map.of(
            "greeting", greeting,
            "user", Map.of(
                "id", user.getId(),
                "name", user.getName()
            ),
            "metadata", Map.of(
                "timestamp", java.time.Instant.now().toString(),
                "source_service", "service-gateway",
                "request_id", requestId,
                "status", "success"
            )
        );
    }
    
    /**
     * Crea una respuesta de error estructurada y consistente
     */
    private Map<String, Object> createErrorResponse(String errorMessage, String requestId) {
        return Map.of(
            "error", true,
            "message", errorMessage,
            "metadata", Map.of(
                "timestamp", java.time.Instant.now().toString(),
                "source_service", "service-gateway", 
                "request_id", requestId,
                "status", "error"
            )
        );
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ENDPOINTS ADICIONALES PARA TESTING Y OBSERVABILIDAD
    // ═══════════════════════════════════════════════════════════════
    
    @GetMapping("/health-check")
    public Map<String, Object> healthCheck() {
        String requestId = UUID.randomUUID().toString();
        logger.info("🏥 Health check requested with requestId: {}", requestId);
        
        return Map.of(
            "status", "UP",
            "service", "service-gateway",
            "timestamp", java.time.Instant.now().toString(),
            "version", "1.0.0",
            "request_id", requestId,
            "dependencies", Map.of(
                "service-user", checkUserServiceHealth()
            )
        );
    }
    
    /**
     * Endpoint útil para generar carga de prueba y observar el comportamiento
     * de las métricas bajo diferentes condiciones de tráfico
     */
    @GetMapping("/generate-load")
    @Observed(name = "generate_load_operation", contextualName = "load-generation")
    public Map<String, Object> generateLoad() {
        String requestId = UUID.randomUUID().toString();
        logger.info("🔥 Load generation requested with requestId: {}", requestId);
        
        // Simulamos múltiples llamadas para generar métricas interesantes
        int numberOfCalls = 10;
        int successCount = 0;
        int errorCount = 0;
        
        for (int i = 0; i < numberOfCalls; i++) {
            try {
                greetUser();
                successCount++;
            } catch (Exception e) {
                errorCount++;
            }
            
            // Pequeña pausa entre llamadas para simular tráfico real
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        return Map.of(
            "load_test_completed", true,
            "total_calls", numberOfCalls,
            "successful_calls", successCount,
            "failed_calls", errorCount,
            "request_id", requestId
        );
    }
    
    /**
     * Verifica la salud del servicio de usuario.
     * Útil para health checks dependency y circuit breaker patterns.
     */
    private String checkUserServiceHealth() {
        try {
            webClient.get()
                    .uri("/health-check")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(2));
            return "UP";
        } catch (Exception e) {
            logger.warn("User service health check failed: {}", e.getMessage());
            return "DOWN";
        }
    }
}