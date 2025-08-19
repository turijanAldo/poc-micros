package com.company.micros1.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.company.micros1.dto.RecibeSolicitudDto;
import com.company.micros1.repository.Micro1Repository;
import com.company.micros1.utilitaria.GlobalLogger;
import com.company.micros1.utilitaria.Monitores;
import com.company.micros1.model.Solicitud;


@Service
public class Micro1Service {
    @Autowired
    Micro1Repository micro1Repository;

    @Autowired
    Micro1ServiceAsincrono micro1ServiceAsincrono;

    @Autowired
    Monitores monitores;

    public ResponseEntity<RecibeSolicitudDto> recibeSolicitud(RecibeSolicitudDto solicitudDDto) {
          Solicitud solicitud = new Solicitud();

          solicitud.setSistemaDestino(solicitudDDto.getSistemaDestino());
          solicitud.setSistemaDestino2(solicitudDDto.getSistemaDestino2());
          solicitud.setFechaCreacion(LocalDateTime.now());
          solicitud.setStatusString("Solicitud recibida");
          solicitud.setStatus(10);
          solicitud.setMensaje(solicitudDDto.getMensaje());

          solicitud = micro1Repository.save(solicitud); 

          // escribe en log
          Monitores.LOGGER.info("Solicitud RECIBIDA: id={}, sistema={}, hora={}, datos={}", solicitudDDto.getId(), solicitudDDto.getFechaCreacion(), "Micro1Servicio", solicitudDDto.getMensaje());
          GlobalLogger.info("Procesando solicitud con ID: " + solicitud.getIdSolicitud());

          try {
                solicitudDDto.setStatus(solicitud.getStatus());
                solicitudDDto.setStatusString(solicitud.getStatusString());
                solicitudDDto.setId(solicitud.getIdSolicitud());
                solicitudDDto.setFechaCreacion(solicitud.getFechaCreacion());

                // envía la solictud al sistema destino (Asincrono)
                micro1ServiceAsincrono.enviarASistema1ExternoAsync(solicitudDDto);
                GlobalLogger.debug("Solicitud procesada correctamente");

          } catch (Exception e) {
                GlobalLogger.error("Error procesando la solicitud ID: " + solicitud.getIdSolicitud(), e);
          }

          return ResponseEntity.ok().body(solicitudDDto);
    }


}
