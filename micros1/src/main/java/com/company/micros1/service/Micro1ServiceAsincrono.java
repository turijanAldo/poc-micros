package com.company.micros1.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.company.micros1.dto.RecibeSolicitudDto;
import com.company.micros1.repository.Micro1Repository;
import com.company.micros1.utilitaria.Monitores;

import jakarta.transaction.Transactional;


@Service
public class Micro1ServiceAsincrono {
    @Autowired
    Micro1Repository micro1Repository;

    @Async
    @Transactional
    public void enviarASistema1ExternoAsync(RecibeSolicitudDto solicitudDDto) {
        // Aquí puedes implementar la llamada asíncrona al sistema externo
        // armar mensaje
        LocalDateTime horaenvio = LocalDateTime.now();

        Monitores.LOGGER.info("Enviando solicitud id={}, sistema={}, hora={}, datos={} al sistema externo...", 
                              solicitudDDto.getId(), solicitudDDto.getSistemaDestino(), horaenvio, solicitudDDto.getMensaje());
  //      GlobalLogger.debug("Solicitud Envianda correctamente");
        
         try {
            Thread.sleep(6000); // Pause for 1000 milliseconds (1 second)
            // TimeUnit.SECONDS.sleep(1); // Pause for 1 second

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
            System.err.println("Task interrupted during sleep: " + e.getMessage());
        } // Pause for 1000 milliseconds (1 second)

        micro1Repository.updateEnvioDestino1(horaenvio, solicitudDDto.getId());


        // Simula el envío, y luego actualizar estatus, etc.
        
        

        // solicitudRepository.save(solicitud);

        // Puedes agregar lógica para llamar REST u otro protocolo

    }



}
