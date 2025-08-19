package com.company.micros1.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    Long idSolicitud;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "status")
    private Integer status;

    @Column(name = "status_string")
    private String statusString;

    @Column(name = "sistema_destino")
    String sistemaDestino;

    @Column(name = "fecha_envio_s1")
    private LocalDateTime fechaEnvioS1;

    @Column(name = "fecha_respuesta_s1")
    private LocalDateTime fechaRespuestaS1;

    @Column(name = "sistema_destino2")
    String sistemaDestino2;

    @Column(name = "fecha_envio_s2")
    private LocalDateTime fechaEnvioS2;

    @Column(name = "fecha_respuesta_s2")
    private LocalDateTime fechaRespuestaS2;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "mensaje")
    private String mensaje;


}
