package com.company.micros1.grpc.service;

import net.devh.boot.grpc.server.service.GrpcService;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional; // AGREGAR

import com.company.micros1.beans.SolicitudMapper;
import com.company.micros1.model.Solicitud;
import com.company.micros1.repository.Micro1Repository;
import com.company.solicitud.grpc.RecibeSolicitud;
import com.company.solicitud.grpc.SolicitudServiceGrpc; // Import the generated service base class
import com.company.micros1.service.Micro1Service; // Import the service class

@GrpcService
public class SolicitudServiceImpl extends SolicitudServiceGrpc.SolicitudServiceImplBase { // Use the correct base class

    private final Executor procesamientoExecutor;

    @Autowired
    private Micro1Service micro1Service;

    @Autowired
    private Micro1Repository micro1Repository;

    @Autowired
    private ProcesamientoAsyncService procesamientoAsyncService;

    @Autowired
    private SolicitudMapper solicitudMapper;

    public SolicitudServiceImpl(
        @Qualifier("procesamientoExecutor") Executor procesamientoExecutor) {
        this.procesamientoExecutor = procesamientoExecutor;
    }

    @Override
    public void procesarSolicitud(RecibeSolicitud request, StreamObserver<RecibeSolicitud> responseObserver) {
        // Aquí se procesaría la solicitud recibida
        RecibeSolicitud recibeSolicitud = request.toBuilder()
            .setMensaje("Solicitud recibida con éxito")
            .setStatus(2)
            .setStatusString("RECIBIDA")
            .build();
        // Para este ejemplo, simplemente devolvemos la solicitud procesada como respuesta
        responseObserver.onNext(recibeSolicitud);
        responseObserver.onCompleted();
        
        // CORRECCIÓN: Llamar método transaccional directamente
        try {
            Thread.sleep(2000); 
            System.out.println("Procesada solicitud: ");
            procesarSolicitudTransaccional(request); // MÉTODO CORREGIDO
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // NUEVO MÉTODO: Separar la lógica transaccional  
    @Transactional  // AGREGAR @Transactional
    public void procesarSolicitudTransaccional(RecibeSolicitud request) {
        try {
            Solicitud nuevaSolicitud = micro1Repository.save(this.convertir(request));
            procesamientoAsyncService.procesarSolicitud(nuevaSolicitud);
        } catch (Exception e) {
            System.err.println("Error al procesar solicitud: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar para rollback
        }
    }

    public Solicitud convertir(RecibeSolicitud grpcMessage) {
        return solicitudMapper.toEntity(grpcMessage);
    }
}