package com.company.micros1.controller;

import org.springframework.core.io.ByteArrayResource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;

import com.company.micros1.dto.RecibeSolicitudDto;
import com.company.micros1.service.Micro1Service;
import com.company.micros1.utilitaria.Monitores;


@RestController
public class Micro1Controller {
    @Autowired
    Micro1Service micro1Service;

    @Autowired
    Monitores monitores;

    @PostMapping("/solicitud") 
    public ResponseEntity<RecibeSolicitudDto> recibeSolicitud(@RequestBody RecibeSolicitudDto solicitudDDto) {
        return micro1Service.recibeSolicitud(solicitudDDto);

    }

    @GetMapping("/logs/download")
    public ResponseEntity<ByteArrayResource> downloadLog(@RequestParam(required = false) Integer lines)  throws IOException {
        return monitores.download(lines);
    
    }

     @PostMapping("/respuesta") 
    public ResponseEntity<RecibeSolicitudDto> recibeRespuesta(@RequestBody RecibeSolicitudDto solicitudDDto) {
        return micro1Service.recibeSolicitud(solicitudDDto);

    }

}
