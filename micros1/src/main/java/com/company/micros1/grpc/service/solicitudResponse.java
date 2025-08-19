package com.company.micros1.grpc.service;

import net.devh.boot.grpc.server.service.GrpcService;
import io.grpc.stub.StreamObserver;


import java.util.concurrent.Executor;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional; // AGREGAR

import com.company.micros1.beans.SolicitudMapper;
import com.company.micros1.model.Solicitud;
import com.company.micros1.repository.Micro1Repository;
import com.company.solicitud.grpc.RecibeSolicitud;
import com.company.solicitud.grpc.SolicitudResponseServiceGrpc; // Import the generated service base class
import com.company.solicitud.grpc.SolicitudResponse; // Import the generated response class

@GrpcService
public class solicitudResponse extends SolicitudResponseServiceGrpc.SolicitudResponseServiceImplBase {

    private final Executor procesamientoExecutor;

    @Autowired
    private Micro1Repository micro1Repository;

    @Autowired
    private SolicitudMapper solicitudMapper;

    @Autowired
    private ProcesamientoAsyncService procesamientoAsyncService;
    
    public solicitudResponse(
        @Qualifier("procesamientoExecutor") Executor procesamientoExecutor) {
        this.procesamientoExecutor = procesamientoExecutor;
    }

    @Override
    public void enviarRespuesta(RecibeSolicitud request, StreamObserver<SolicitudResponse> responseObserver) {
        // Aquí se procesaría la solicitud recibida
        SolicitudResponse solicitudResponse = SolicitudResponse.newBuilder()
            .setMensaje("Respuesta recibida con éxito")
            .setId(request.getId()) // Simulando un ID aleatorio
            .setExito(true)
            .build();
        // Para este ejemplo, simplemente devolvemos la solicitud procesada como respuesta
        responseObserver.onNext(solicitudResponse);
        responseObserver.onCompleted();

        // CORRECCIÓN: Llamar al método transaccional directamente sin CompletableFuture
        // para evitar pérdida del contexto transaccional
        try {
            Thread.sleep(2000); 
            recibirSolicitudTransaccional(request); // MÉTODO CORREGIDO
            System.out.println("Procesada solicitud: ");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Transactional  
    public void recibirSolicitudTransaccional(RecibeSolicitud request) {
        // Lógica para recibir la solicitud
        RecibeSolicitud recibeSolicitud = request.toBuilder()
                .setMensaje("Solicitud recibida con éxito")
                .setStatus(2)
                .setStatusString("RECIBIDA")
                .build();
        
        try {
            Solicitud nuevaSolicitud = micro1Repository.save(this.convertir(recibeSolicitud));
            procesamientoAsyncService.procesarSolicitud(nuevaSolicitud);
        } catch (Exception e) {
            System.err.println("Error al guardar solicitud: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar para que la transacción haga rollback
        }
    }

    public Solicitud convertir(RecibeSolicitud grpcMessage) {
        return solicitudMapper.toEntity(grpcMessage);
    }
}