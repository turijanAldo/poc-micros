package com.company.solicitud.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.63.0)",
    comments = "Source: solicitud.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class SolicitudResponseServiceGrpc {

  private SolicitudResponseServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "solicitud.SolicitudResponseService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.company.solicitud.grpc.RecibeSolicitud,
      com.company.solicitud.grpc.SolicitudResponse> getEnviarRespuestaMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "EnviarRespuesta",
      requestType = com.company.solicitud.grpc.RecibeSolicitud.class,
      responseType = com.company.solicitud.grpc.SolicitudResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.company.solicitud.grpc.RecibeSolicitud,
      com.company.solicitud.grpc.SolicitudResponse> getEnviarRespuestaMethod() {
    io.grpc.MethodDescriptor<com.company.solicitud.grpc.RecibeSolicitud, com.company.solicitud.grpc.SolicitudResponse> getEnviarRespuestaMethod;
    if ((getEnviarRespuestaMethod = SolicitudResponseServiceGrpc.getEnviarRespuestaMethod) == null) {
      synchronized (SolicitudResponseServiceGrpc.class) {
        if ((getEnviarRespuestaMethod = SolicitudResponseServiceGrpc.getEnviarRespuestaMethod) == null) {
          SolicitudResponseServiceGrpc.getEnviarRespuestaMethod = getEnviarRespuestaMethod =
              io.grpc.MethodDescriptor.<com.company.solicitud.grpc.RecibeSolicitud, com.company.solicitud.grpc.SolicitudResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "EnviarRespuesta"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.company.solicitud.grpc.RecibeSolicitud.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.company.solicitud.grpc.SolicitudResponse.getDefaultInstance()))
              .setSchemaDescriptor(new SolicitudResponseServiceMethodDescriptorSupplier("EnviarRespuesta"))
              .build();
        }
      }
    }
    return getEnviarRespuestaMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SolicitudResponseServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SolicitudResponseServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SolicitudResponseServiceStub>() {
        @java.lang.Override
        public SolicitudResponseServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SolicitudResponseServiceStub(channel, callOptions);
        }
      };
    return SolicitudResponseServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SolicitudResponseServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SolicitudResponseServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SolicitudResponseServiceBlockingStub>() {
        @java.lang.Override
        public SolicitudResponseServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SolicitudResponseServiceBlockingStub(channel, callOptions);
        }
      };
    return SolicitudResponseServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SolicitudResponseServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SolicitudResponseServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SolicitudResponseServiceFutureStub>() {
        @java.lang.Override
        public SolicitudResponseServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SolicitudResponseServiceFutureStub(channel, callOptions);
        }
      };
    return SolicitudResponseServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void enviarRespuesta(com.company.solicitud.grpc.RecibeSolicitud request,
        io.grpc.stub.StreamObserver<com.company.solicitud.grpc.SolicitudResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getEnviarRespuestaMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service SolicitudResponseService.
   */
  public static abstract class SolicitudResponseServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return SolicitudResponseServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service SolicitudResponseService.
   */
  public static final class SolicitudResponseServiceStub
      extends io.grpc.stub.AbstractAsyncStub<SolicitudResponseServiceStub> {
    private SolicitudResponseServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SolicitudResponseServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SolicitudResponseServiceStub(channel, callOptions);
    }

    /**
     */
    public void enviarRespuesta(com.company.solicitud.grpc.RecibeSolicitud request,
        io.grpc.stub.StreamObserver<com.company.solicitud.grpc.SolicitudResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getEnviarRespuestaMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service SolicitudResponseService.
   */
  public static final class SolicitudResponseServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<SolicitudResponseServiceBlockingStub> {
    private SolicitudResponseServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SolicitudResponseServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SolicitudResponseServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.company.solicitud.grpc.SolicitudResponse enviarRespuesta(com.company.solicitud.grpc.RecibeSolicitud request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getEnviarRespuestaMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service SolicitudResponseService.
   */
  public static final class SolicitudResponseServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<SolicitudResponseServiceFutureStub> {
    private SolicitudResponseServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SolicitudResponseServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SolicitudResponseServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.company.solicitud.grpc.SolicitudResponse> enviarRespuesta(
        com.company.solicitud.grpc.RecibeSolicitud request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getEnviarRespuestaMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_ENVIAR_RESPUESTA = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ENVIAR_RESPUESTA:
          serviceImpl.enviarRespuesta((com.company.solicitud.grpc.RecibeSolicitud) request,
              (io.grpc.stub.StreamObserver<com.company.solicitud.grpc.SolicitudResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getEnviarRespuestaMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.company.solicitud.grpc.RecibeSolicitud,
              com.company.solicitud.grpc.SolicitudResponse>(
                service, METHODID_ENVIAR_RESPUESTA)))
        .build();
  }

  private static abstract class SolicitudResponseServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SolicitudResponseServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.company.solicitud.grpc.solicitudProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("SolicitudResponseService");
    }
  }

  private static final class SolicitudResponseServiceFileDescriptorSupplier
      extends SolicitudResponseServiceBaseDescriptorSupplier {
    SolicitudResponseServiceFileDescriptorSupplier() {}
  }

  private static final class SolicitudResponseServiceMethodDescriptorSupplier
      extends SolicitudResponseServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    SolicitudResponseServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (SolicitudResponseServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new SolicitudResponseServiceFileDescriptorSupplier())
              .addMethod(getEnviarRespuestaMethod())
              .build();
        }
      }
    }
    return result;
  }
}
