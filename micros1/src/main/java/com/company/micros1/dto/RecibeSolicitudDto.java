package com.company.micros1.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RecibeSolicitudDto {
    private Long id;
    private LocalDateTime fechaCreacion;
    private Integer status;
    private String  statusString;

    private String sistemaDestino;
    private LocalDateTime fechaEnvioS1;
    private LocalDateTime fechaRespuestaS1;

    private String sistemaDestino2;
    private LocalDateTime fechaEnvioS2;
    private LocalDateTime fechaRespuestaS2;

    private LocalDateTime fechaActualizacion;
	private String mensaje;

}
