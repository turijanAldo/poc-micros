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

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
public class GreetingController {
    
    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);
    
    private final WebClient webClient;
    private final Tracer tracer;
    
    // Contadores optimizados para diferentes escenarios
    private final Counter greetingRequestCounter;
    private final Counter greetingSuccessCounter;
    private final Counter greetingErrorNullUserCounter;
    private final Counter greetingErrorWebClientCounter;
    private final Counter greetingErrorUnexpectedCounter;
    private final Timer greetingRequestTimer;
    
    // Métricas específicas para interacciones con servicios externos
    private final Counter userServiceCallsCounter;
    private final Counter userServiceHttpErrorCounter;
    private final Counter userServiceTimeoutErrorCounter;
    private final Timer userServiceCallTimer;
    
    public GreetingController(
            MeterRegistry meterRegistry,
            Tracer tracer,
            @Value("${app.service-user.base-url:http://localhost:8081}") String userServiceBaseUrl) {
        
        this.tracer = tracer;
        
        // Configuración de WebClient optimizada para observabilidad
        this.webClient = WebClient.builder()
                .baseUrl(userServiceBaseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        
        // Inicialización de contadores con tags descriptivos
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
        
        this.greetingRequestTimer = Timer.builder("greeting_request_duration_seconds")
                .description("Duration of greeting request processing")
                .tag("service", "gateway")
                .tag("endpoint", "greet-user")
                .register(meterRegistry);
        
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
    
    @Observed(name = "greet_user_operation", contextualName = "greeting-operation")
    @GetMapping("/greet-user")
    public Map<String, Object> greetUser() {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        Timer.Sample sample = Timer.start();
        
        try {
            greetingRequestCounter.increment();
            logger.info("🚀 Starting greet-user request processing with requestId: {}", requestId);
            
            // Crear span para preparación y validación inicial
            Span preparationSpan = tracer.nextSpan()
                    .name("request-preparation")
                    .tag("request.id", requestId)
                    .tag("operation.type", "preparation")
                    .tag("service.name", "gateway")
                    .start();
            
            try (SpanInScope preparationScope = tracer.withSpan(preparationSpan)) {
                logger.info("📋 Preparing request context and validation");
                
                // Simulamos validación y preparación del contexto
                Thread.sleep(10); // Representa tiempo de validación/preparación
                preparationSpan.tag("preparation.completed", "true");
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                preparationSpan.tag("preparation.interrupted", "true");
            } finally {
                preparationSpan.end();
            }
            
            // Crear span principal para la lógica de negocio
            Span businessLogicSpan = tracer.nextSpan()
                    .name("business-logic-greeting")
                    .tag("request.id", requestId)
                    .tag("operation.type", "greeting-generation")
                    .tag("service.name", "gateway")
                    .start();
            
            try (SpanInScope businessScope = tracer.withSpan(businessLogicSpan)) {
                logger.info("📊 Executing core business logic within custom span context");
                
                // Llamada al servicio de usuario con trazado detallado
                User user = callUserServiceWithDetailedTracing(requestId);
                
                if (user == null) {
                    businessLogicSpan.tag("error", "true");
                    businessLogicSpan.tag("error.reason", "null_user");
                    businessLogicSpan.tag("business.outcome", "failure");
                    
                    logger.warn("⚠️ Business logic failed: null user for requestId: {}", requestId);
                    greetingErrorNullUserCounter.increment();
                    return createErrorResponse("User service returned no data", requestId);
                }
                
                // Crear sub-span específico para generación del mensaje
                String greeting = generateGreetingWithEnhancedTracing(user, requestId);
                
                businessLogicSpan.tag("user.name", user.getName());
                businessLogicSpan.tag("greeting.generated", "true");
                businessLogicSpan.tag("business.outcome", "success");
                
                logger.info("✅ Business logic completed successfully for user: {} with requestId: {}", 
                           user.getName(), requestId);
                
                greetingSuccessCounter.increment();
                return createSuccessResponse(greeting, user, requestId);
                
            } finally {
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
    
    /**
     * Método mejorado para llamadas al servicio de usuario con trazado granular.
     * Demuestra cómo crear spans específicos para operaciones externas.
     */
    private User callUserServiceWithDetailedTracing(String requestId) {
        Timer.Sample userServiceSample = Timer.start();
        
        // Crear span específico para la comunicación externa
        Span externalCallSpan = tracer.nextSpan()
                .name("external-service-call")
                .tag("target.service", "user-service")
                .tag("call.type", "http-get")
                .tag("request.id", requestId)
                .start();
        
        try (SpanInScope externalCallScope = tracer.withSpan(externalCallSpan)) {
            
            userServiceCallsCounter.increment();
            logger.info("🔗 Initiating external call to service-user");
            
            externalCallSpan.tag("call.initiated", "true");
            
            User user = webClient
                    .get()
                    .uri("/user")
                    .header("X-Request-ID", requestId)
                    .retrieve()
                    .bodyToMono(User.class)
                    .doOnNext(u -> {
                        logger.info("📦 Successfully received user data: {}", u.getName());
                        externalCallSpan.tag("call.successful", "true");
                        externalCallSpan.tag("response.user.id", String.valueOf(u.getId()));
                        externalCallSpan.tag("response.user.name", u.getName());
                    })
                    .doOnError(error -> {
                        logger.error("❌ Error in external service call: {}", error.getMessage());
                        externalCallSpan.tag("error", "true");
                        externalCallSpan.tag("error.message", error.getMessage());
                        externalCallSpan.tag("call.successful", "false");
                        
                        if (error instanceof WebClientResponseException) {
                            WebClientResponseException webEx = (WebClientResponseException) error;
                            externalCallSpan.tag("http.status_code", String.valueOf(webEx.getStatusCode().value()));
                            userServiceHttpErrorCounter.increment();
                        } else {
                            userServiceTimeoutErrorCounter.increment();
                        }
                    })
                    .block();
            
            return user;
            
        } finally {
            userServiceSample.stop(userServiceCallTimer);
            externalCallSpan.end();
            logger.info("⏱️ External service call completed for requestId: {}", requestId);
        }
    }
    
    /**
     * Método mejorado para generar saludos con trazado específico.
     * Demuestra cómo rastrear lógica de negocio granular.
     */
    private String generateGreetingWithEnhancedTracing(User user, String requestId) {
        
        Span greetingGenerationSpan = tracer.nextSpan()
                .name("greeting-message-generation")
                .tag("user.id", String.valueOf(user.getId()))
                .tag("user.name", user.getName())
                .tag("message.type", "personalized-greeting")
                .start();
        
        try (SpanInScope greetingScope = tracer.withSpan(greetingGenerationSpan)) {
            
            // Lógica de personalización basada en la hora del día
            String timeOfDay = java.time.LocalTime.now().getHour() < 12 ? "Good morning" : 
                              java.time.LocalTime.now().getHour() < 18 ? "Good afternoon" : "Good evening";
            
            String greeting = timeOfDay + ", " + user.getName() + "!";
            
            greetingGenerationSpan.tag("greeting.length", String.valueOf(greeting.length()));
            greetingGenerationSpan.tag("greeting.time_of_day", timeOfDay);
            greetingGenerationSpan.tag("generation.successful", "true");
            
            logger.info("📝 Generated personalized greeting: {} for user: {}", greeting, user.getName());
            
            return greeting;
            
        } finally {
            greetingGenerationSpan.end();
        }
    }
    
    /**
     * Método auxiliar original mantenido para compatibilidad.
     */
    private User callUserService(String requestId) {
        Timer.Sample userServiceSample = Timer.start();
        
        try {
            userServiceCallsCounter.increment();
            logger.info("🔗 Making call to service-user at /user endpoint for requestId: {}", requestId);
            
            User user = webClient
                    .get()
                    .uri("/user")
                    .header("X-Request-ID", requestId)
                    .retrieve()
                    .bodyToMono(User.class)
                    .doOnNext(u -> {
                        logger.info("📦 Successfully received user data: {} for requestId: {}", u.getName(), requestId);
                        
                        Span currentSpan = tracer.currentSpan();
                        if (currentSpan != null) {
                            currentSpan.tag("user.service.response", "success");
                            currentSpan.tag("user.id", String.valueOf(u.getId()));
                        }
                    })
                    .doOnError(error -> {
                        logger.error("❌ Error calling service-user for requestId: {}: {}", requestId, error.getMessage());
                        
                        Span currentSpan = tracer.currentSpan();
                        if (currentSpan != null) {
                            currentSpan.tag("error", "true");
                            currentSpan.tag("error.message", error.getMessage());
                        }
                        
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
        }
    }
    
    private String generateGreeting(User user) {
        return "Hello, " + user.getName() + "!";
    }
    
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
    
    @GetMapping("/generate-load")
    @Observed(name = "generate_load_operation", contextualName = "load-generation")
    public Map<String, Object> generateLoad() {
        String requestId = UUID.randomUUID().toString();
        logger.info("🔥 Load generation requested with requestId: {}", requestId);
        
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