package com.company.micros1.beans;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import com.company.solicitud.grpc.RecibeSolicitud;
import com.company.micros1.model.Solicitud; // Add this import if Solicitud exists in this package
import java.time.format.DateTimeFormatter;
// @Mapper(componentModel = "spring",)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SolicitudMapper {
    
    // Factory instance for manual access if needed (useful for debugging)
    SolicitudMapper INSTANCE = Mappers.getMapper(SolicitudMapper.class);
    
    // ======================
    // gRPC -> Entity Mapping
    // ======================
    
    /**
     * Converts a gRPC RecibeSolicitud message to a Solicitud entity.
     * 
     * This mapping explicitly specifies how each field should be converted.
     * The qualifiedByName attributes reference our custom date conversion methods.
     * 
     * If you're still getting "Unknown property" errors, it means MapStruct
     * cannot find the setter methods in your Solicitud class. This usually
     * indicates that Lombok hasn't generated the setters yet when MapStruct
     * tries to create this mapping.
     */
    @Mapping(target = "idSolicitud", source = "id", ignore = true)
    @Mapping(target = "fechaCreacion", source = "fechaCreacion", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "statusString", source = "statusString")
    @Mapping(target = "sistemaDestino", source = "sistemaDestino")
    @Mapping(target = "fechaEnvioS1", source = "fechaEnvioS1", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "fechaRespuestaS1", source = "fechaRespuestaS1", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "sistemaDestino2", source = "sistemaDestino2")
    @Mapping(target = "fechaEnvioS2", source = "fechaEnvioS2", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "fechaRespuestaS2", source = "fechaRespuestaS2", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "fechaActualizacion", source = "fechaActualizacion", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "mensaje", source = "mensaje")
    Solicitud toEntity(RecibeSolicitud grpcMessage);
    
    // ======================
    // Entity -> gRPC Mapping
    // ======================
    
    /**
     * Converts a Solicitud entity to a gRPC RecibeSolicitud message.
     * 
     * This reverse mapping takes your entity objects and converts them back
     * to gRPC messages for transmission over the network.
     */
    @Mapping(source = "idSolicitud", target = "id")
    @Mapping(source = "fechaCreacion", target = "fechaCreacion", qualifiedByName = "localDateTimeToString")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "statusString", target = "statusString")
    @Mapping(source = "sistemaDestino", target = "sistemaDestino")
    @Mapping(source = "fechaEnvioS1", target = "fechaEnvioS1", qualifiedByName = "localDateTimeToString")
    @Mapping(source = "fechaRespuestaS1", target = "fechaRespuestaS1", qualifiedByName = "localDateTimeToString")
    @Mapping(source = "sistemaDestino2", target = "sistemaDestino2")
    @Mapping(source = "fechaEnvioS2", target = "fechaEnvioS2", qualifiedByName = "localDateTimeToString")
    @Mapping(source = "fechaRespuestaS2", target = "fechaRespuestaS2", qualifiedByName = "localDateTimeToString")
    @Mapping(source = "fechaActualizacion", target = "fechaActualizacion", qualifiedByName = "localDateTimeToString")
    @Mapping(source = "mensaje", target = "mensaje")
    RecibeSolicitud toGrpc(Solicitud entity);
    
    // ==================
    // Helper Methods
    // ==================
    
    /**
     * Converts an ISO date string to LocalDateTime with robust error handling.
     * 
     * This method tries to parse dates in multiple formats to handle variations
     * in how your gRPC service might send date strings. It's designed to be
     * defensive programming - better to have a null date than crash the entire
     * mapping process.
     * 
     * Expected formats:
     * - ISO-8601 with timezone: "2023-12-01T10:30:00Z"
     * - ISO-8601 without timezone: "2023-12-01T10:30:00"
     * - Add more formats as needed based on your gRPC service
     */
    @Named("stringToLocalDateTime")
    default LocalDateTime stringToLocalDateTime(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            // First, try parsing as an Instant (expects timezone info like 'Z' or '+00:00')
            if (dateString.contains("Z") || dateString.contains("+") || dateString.contains("-")) {
                Instant instant = Instant.parse(dateString);
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            } else {
                // If no timezone info, assume it's already in local time
                return LocalDateTime.parse(dateString);
            }
        } catch (Exception e) {
            // In production, you'd want to use proper logging here
            System.err.println("Warning: Failed to parse date string '" + dateString + "': " + e.getMessage());
            System.err.println("This will result in a null date value for this field.");
            return null;
        }
    }
    
    /**
     * Converts a LocalDateTime to an ISO date string for gRPC transmission.
     * 
     * This method ensures consistent date formatting when sending data back
     * through gRPC. The resulting string will always include timezone information
     * to prevent ambiguity on the receiving end.
     */
    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        
        try {
            // Convert to UTC Instant for consistent timezone handling
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toString();
        } catch (Exception e) {
            // In production, use proper logging
            System.err.println("Warning: Failed to convert LocalDateTime to string: " + localDateTime + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Alternative date conversion method using a specific formatter.
     * You can use this if your gRPC service uses a non-ISO date format.
     * 
     * To use this method instead of stringToLocalDateTime, change the
     * qualifiedByName parameter in your @Mapping annotations above.
     */
    @Named("customStringToLocalDateTime")
    default LocalDateTime customStringToLocalDateTime(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Example: if your dates come in format "dd/MM/yyyy HH:mm:ss"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return LocalDateTime.parse(dateString, formatter);
        } catch (Exception e) {
            System.err.println("Warning: Failed to parse custom date format '" + dateString + "': " + e.getMessage());
            return null;
        }
    }
}