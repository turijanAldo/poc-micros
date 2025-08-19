package com.company.micros1.utilitaria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GlobalLogger adaptado para trabajar armoniosamente con Spring Boot.
 * 
 * Esta versión usa SLF4J como abstracción, lo cual es compatible con
 * el sistema de logging predeterminado de Spring Boot (Logback).
 * Esto elimina conflictos durante la inicialización mientras mantiene
 * la misma interfaz simple y consistente para tu aplicación.
 */
public class GlobalLogger {
    
    // Usar SLF4J en lugar de Log4j2 directamente
    private static final Logger logger = LoggerFactory.getLogger("GlobalLogger");
    
    /**
     * Log de información general.
     * Perfectamente compatible con la configuración de logging de Spring Boot.
     */
    public static void info(String message) {
        logger.info(message);
    }
    
    /**
     * Log de advertencias.
     * Se integrará automáticamente con los patrones de logging configurados
     * en tu application.yml, incluyendo formato de timestamp y nivel.
     */
    public static void warn(String message) {
        logger.warn(message);
    }
    
    /**
     * Log de errores.
     * Respetará la configuración de nivel de logging de Spring Boot.
     */
    public static void error(String message) {
        logger.error(message);
    }
    
    /**
     * Log de debugging.
     * Solo aparecerá cuando tengas el nivel DEBUG habilitado en tu configuración.
     */
    public static void debug(String message) {
        logger.debug(message);
    }
    
    /**
     * Log de errores con stack trace.
     * SLF4J maneja exceptiones de manera muy elegante y consistente.
     */
    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}