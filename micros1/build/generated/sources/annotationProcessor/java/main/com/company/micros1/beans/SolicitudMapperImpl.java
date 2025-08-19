package com.company.micros1.beans;

import com.company.micros1.model.Solicitud;
import com.company.solicitud.grpc.RecibeSolicitud;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-19T14:15:58-0600",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class SolicitudMapperImpl implements SolicitudMapper {

    @Override
    public Solicitud toEntity(RecibeSolicitud grpcMessage) {
        if ( grpcMessage == null ) {
            return null;
        }

        Solicitud solicitud = new Solicitud();

        solicitud.setFechaCreacion( stringToLocalDateTime( grpcMessage.getFechaCreacion() ) );
        solicitud.setStatus( grpcMessage.getStatus() );
        solicitud.setStatusString( grpcMessage.getStatusString() );
        solicitud.setSistemaDestino( grpcMessage.getSistemaDestino() );
        solicitud.setFechaEnvioS1( stringToLocalDateTime( grpcMessage.getFechaEnvioS1() ) );
        solicitud.setFechaRespuestaS1( stringToLocalDateTime( grpcMessage.getFechaRespuestaS1() ) );
        solicitud.setSistemaDestino2( grpcMessage.getSistemaDestino2() );
        solicitud.setFechaEnvioS2( stringToLocalDateTime( grpcMessage.getFechaEnvioS2() ) );
        solicitud.setFechaRespuestaS2( stringToLocalDateTime( grpcMessage.getFechaRespuestaS2() ) );
        solicitud.setFechaActualizacion( stringToLocalDateTime( grpcMessage.getFechaActualizacion() ) );
        solicitud.setMensaje( grpcMessage.getMensaje() );

        return solicitud;
    }

    @Override
    public RecibeSolicitud toGrpc(Solicitud entity) {
        if ( entity == null ) {
            return null;
        }

        RecibeSolicitud.Builder recibeSolicitud = RecibeSolicitud.newBuilder();

        if ( entity.getIdSolicitud() != null ) {
            recibeSolicitud.setId( entity.getIdSolicitud() );
        }
        recibeSolicitud.setFechaCreacion( localDateTimeToString( entity.getFechaCreacion() ) );
        if ( entity.getStatus() != null ) {
            recibeSolicitud.setStatus( entity.getStatus() );
        }
        recibeSolicitud.setStatusString( entity.getStatusString() );
        recibeSolicitud.setSistemaDestino( entity.getSistemaDestino() );
        recibeSolicitud.setFechaEnvioS1( localDateTimeToString( entity.getFechaEnvioS1() ) );
        recibeSolicitud.setFechaRespuestaS1( localDateTimeToString( entity.getFechaRespuestaS1() ) );
        recibeSolicitud.setSistemaDestino2( entity.getSistemaDestino2() );
        recibeSolicitud.setFechaEnvioS2( localDateTimeToString( entity.getFechaEnvioS2() ) );
        recibeSolicitud.setFechaRespuestaS2( localDateTimeToString( entity.getFechaRespuestaS2() ) );
        recibeSolicitud.setFechaActualizacion( localDateTimeToString( entity.getFechaActualizacion() ) );
        recibeSolicitud.setMensaje( entity.getMensaje() );

        return recibeSolicitud.build();
    }
}
