package com.example.service_gateway.controller;

import com.example.grpc.user.User;
import com.example.grpc.user.Empty;
import com.example.grpc.user.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Service;

@Service
public class GrpcClientService {

    private final UserServiceGrpc.UserServiceBlockingStub stub;

    public GrpcClientService() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        this.stub = UserServiceGrpc.newBlockingStub(channel);
    }

    public User getUser() {
        // Construye el mensaje Empty
        Empty emptyRequest = Empty.newBuilder().build();
        
        // Llama al servicio gRPC
        return stub.getUser(emptyRequest);
    }
}