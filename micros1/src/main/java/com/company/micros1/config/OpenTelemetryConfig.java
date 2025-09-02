// package com.company.micros1.config;

// import io.opentelemetry.api.OpenTelemetry;
// import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
// import io.opentelemetry.sdk.OpenTelemetrySdk;
// import io.opentelemetry.sdk.resources.Resource;
// import io.opentelemetry.sdk.trace.SdkTracerProvider;
// import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;

// import java.time.Duration;

// /**
//  * Configuración de OpenTelemetry para exportar trazas a OTLP
//  */
// @Configuration
// public class OpenTelemetryConfig {
    
//     private static final Logger logger = LoggerFactory.getLogger(OpenTelemetryConfig.class);
    
//     @Value("${management.otlp.tracing.endpoint:http://host.docker.internal:4318/v1/traces}")
//     private String otlpEndpoint;
    
//     @Value("${otel.resource.attributes.service.name:micros1}")
//     private String serviceName;
    
//     @Value("${otel.resource.attributes.service.version:1.0.0}")
//     private String serviceVersion;
    
//     @Value("${otel.resource.attributes.deployment.environment:development}")
//     private String environment;

//     // Constructor para verificar que se carga
//     public OpenTelemetryConfig() {
//         System.out.println("🔧 CONSTRUCTOR: OpenTelemetryConfig se está inicializando...");
//         logger.info("🔧 CONSTRUCTOR: OpenTelemetryConfig se está inicializando...");
//     }

//     @Bean
//     @Primary
//     public OpenTelemetry openTelemetry() {
//         logger.info("🚀 Inicializando OpenTelemetry SDK...");
//         logger.info("📡 Endpoint OTLP: {}", otlpEndpoint);
//         logger.info("🏷️  Service Name: {}", serviceName);
//         logger.info("🏷️  Service Version: {}", serviceVersion);
//         logger.info("🏷️  Environment: {}", environment);
        
//         try {
//             // Crear exportador OTLP HTTP
//             OtlpHttpSpanExporter spanExporter = OtlpHttpSpanExporter.builder()
//                     .setEndpoint(otlpEndpoint)
//                     .setTimeout(Duration.ofSeconds(10))
//                     .setCompression("gzip")
//                     .build();
            
//             logger.info("✅ OTLP HTTP Span Exporter configurado");
            
//             // Crear recurso del servicio (usando strings directamente)
//             Resource resource = Resource.getDefault()
//                     .merge(Resource.builder()
//                             .put("service.name", serviceName)
//                             .put("service.version", serviceVersion)
//                             .put("deployment.environment", environment)
//                             .put("service.namespace", "company")
//                             .put("service.instance.id", java.util.UUID.randomUUID().toString())
//                             .build());
            
//             logger.info("✅ Resource configurado");
            
//             // Crear procesador de spans en batch
//             BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(spanExporter)
//                     .setMaxExportBatchSize(512)
//                     .setScheduleDelay(Duration.ofSeconds(1))
//                     .setMaxQueueSize(2048)
//                     .build();
            
//             logger.info("✅ BatchSpanProcessor configurado");
            
//             // Crear tracer provider
//             SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
//                     .addSpanProcessor(spanProcessor)
//                     .setResource(resource)
//                     .build();
            
//             logger.info("✅ SdkTracerProvider configurado");
            
//             // Crear SDK y registrar globalmente
//             OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
//                     .setTracerProvider(tracerProvider)
//                     .buildAndRegisterGlobal();
            
//             logger.info("🎯 OpenTelemetry SDK inicializado y registrado globalmente");
//             logger.info("🚀 Las trazas se enviarán a: {}", otlpEndpoint);
            
//             return sdk;
            
//         } catch (Exception e) {
//             logger.error("❌ Error inicializando OpenTelemetry: {}", e.getMessage(), e);
//             throw new RuntimeException("Failed to initialize OpenTelemetry", e);
//         }
//     }
// }