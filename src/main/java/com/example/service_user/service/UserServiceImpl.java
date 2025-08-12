package com.example.service_user.service;

import net.devh.boot.grpc.server.service.GrpcService;
import io.grpc.stub.StreamObserver;

// Imports de las clases generadas por protobuf
import com.example.grpc.user.UserServiceGrpc;
import com.example.grpc.user.User;
import com.example.grpc.user.Empty;

@GrpcService
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    
    @Override
    public void getUser(Empty request, StreamObserver<User> responseObserver) {
        // Crear un usuario de ejemplo
        User user = User.newBuilder()
                .setId(1L)
                .setName("Aldo")
                .build();
        
        // Enviar la respuesta
        responseObserver.onNext(user);
        responseObserver.onCompleted();
    }
}