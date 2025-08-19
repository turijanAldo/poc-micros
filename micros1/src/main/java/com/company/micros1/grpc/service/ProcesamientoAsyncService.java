package com.company.micros1.grpc.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

// Import SolicitudRepository (adjust the package if needed)
// Update the import path to the correct package where Micro1Repository is located
import com.company.micros1.repository.Micro1Repository;
import com.company.micros1.model.Solicitud;

@Service
public class ProcesamientoAsyncService {

    private static final Logger log = LoggerFactory.getLogger(ProcesamientoAsyncService.class);

    @Autowired
    private Micro1Repository solicitudRepository;

    @Async // Esta anotación hace que el método se ejecute en un hilo separado
    public void procesarSolicitud(Solicitud solicitud) {
        log.info("Iniciando procesamiento para la solicitud ID: {}", solicitud.getIdSolicitud());

        try {
            // 1. Cambiar estado a "PROCESANDO"
            solicitud.setStatus(2);
            solicitud.setFechaActualizacion(LocalDateTime.now());
            solicitudRepository.save(solicitud);
            log.info("Solicitud ID: {} -> PROCESANDO", solicitud.getIdSolicitud());

            // 2. Simular un proceso largo y variable
            long tiempoDeEsperaMs = ThreadLocalRandom.current().nextLong(30000, 120001); // Entre 30s y 2min
            log.info("Solicitud ID: {} - Simulación de trabajo por {} segundos.", solicitud.getIdSolicitud(), TimeUnit.MILLISECONDS.toSeconds(tiempoDeEsperaMs));
            Thread.sleep(tiempoDeEsperaMs);

            // 3. Cambiar estado a "COMPLETADO"
            solicitud.setStatus(3);
            solicitud.setFechaActualizacion(LocalDateTime.now());
            solicitud.setMensaje("El proceso ha finalizado exitosamente.");
            solicitudRepository.save(solicitud);
            log.info("Solicitud ID: {} -> COMPLETADO", solicitud.getIdSolicitud());

        } catch (InterruptedException e) {
            log.error("El hilo para la solicitud ID: {} fue interrumpido.", solicitud.getIdSolicitud());
            solicitud.setStatus(5);
            solicitud.setMensaje("El proceso fue interrumpido.");
            solicitudRepository.save(solicitud);
            Thread.currentThread().interrupt();
        }
    }
}