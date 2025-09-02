package com.company.micros1.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * Configuración de OpenTelemetry con exportador OTLP
 * ✅ Verificado que Spring Boot carga esta configuración correctamente
 */
@Configuration
public class TelemetryConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(TelemetryConfiguration.class);
    
    @Value("${management.otlp.tracing.endpoint:http://host.docker.internal:4318/v1/traces}")
    private String otlpEndpoint;
    
    @Value("${otel.resource.attributes.service.name:micros1}")
    private String serviceName;
    
    @Value("${otel.resource.attributes.service.version:1.0.0}")
    private String serviceVersion;

    public TelemetryConfiguration() {
        System.out.println("🔧 CONSTRUCTOR: TelemetryConfiguration inicializándose...");
        logger.info("🔧 CONSTRUCTOR: TelemetryConfiguration inicializándose...");
    }

    @PostConstruct
    public void init() {
        System.out.println("🚀 POST-CONSTRUCT: TelemetryConfiguration configurada");
        System.out.println("📡 Endpoint: " + otlpEndpoint);
        System.out.println("🏷️  Service: " + serviceName + " v" + serviceVersion);
        
        logger.info("🚀 POST-CONSTRUCT: TelemetryConfiguration configurada");
        logger.info("📡 Endpoint: {}", otlpEndpoint);
        logger.info("🏷️  Service: {} v{}", serviceName, serviceVersion);
    }

    @Bean("customOpenTelemetry")
    @Primary
    public OpenTelemetry customOpenTelemetry() {
        try {
            logger.info("🔧 Configurando OpenTelemetry SDK...");
            
            // 1. Crear exportador OTLP
            OtlpHttpSpanExporter spanExporter = OtlpHttpSpanExporter.builder()
                    .setEndpoint(otlpEndpoint)
                    .setTimeout(Duration.ofSeconds(10))
                    .setCompression("gzip")
                    .build();
            
            logger.info("✅ OTLP HTTP Span Exporter creado: {}", otlpEndpoint);
            
            // 2. Crear recurso del servicio
            Resource resource = Resource.getDefault()
                    .merge(Resource.builder()
                            .put("service.name", serviceName)
                            .put("service.version", serviceVersion)
                            .put("deployment.environment", "development")
                            .put("service.namespace", "company")
                            .put("service.instance.id", java.util.UUID.randomUUID().toString())
                            .build());
            
            logger.info("✅ Resource creado con service.name: {}", serviceName);
            
            // 3. Crear procesador de spans
            BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(spanExporter)
                    .setMaxExportBatchSize(512)
                    .setScheduleDelay(Duration.ofSeconds(1))
                    .setMaxQueueSize(2048)
                    .build();
            
            logger.info("✅ BatchSpanProcessor creado");
            
            // 4. Crear tracer provider
            SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                    .addSpanProcessor(spanProcessor)
                    .setResource(resource)
                    .build();
            
            logger.info("✅ SdkTracerProvider creado");
            
            // 5. Crear y registrar SDK
            OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                    .setTracerProvider(tracerProvider)
                    .buildAndRegisterGlobal();
            
            System.out.println("🎯 ¡OpenTelemetry SDK inicializado exitosamente!");
            System.out.println("📤 Las trazas se enviarán a: " + otlpEndpoint);
            
            logger.info("🎯 ¡OpenTelemetry SDK inicializado exitosamente!");
            logger.info("📤 Las trazas se enviarán a: {}", otlpEndpoint);
            
            return sdk;
            
        } catch (Exception e) {
            System.err.println("❌ Error configurando OpenTelemetry: " + e.getMessage());
            logger.error("❌ Error configurando OpenTelemetry: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to configure OpenTelemetry", e);
        }
    }
}