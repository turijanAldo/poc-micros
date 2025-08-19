package com.company.solicitud.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.63.0)",
    comments = "Source: solicitud.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class SolicitudServiceGrpc {

  private SolicitudServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "solicitud.SolicitudService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.company.solicitud.grpc.RecibeSolicitud,
      com.company.solicitud.grpc.RecibeSolicitud> getProcesarSolicitudMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ProcesarSolicitud",
      requestType = com.company.solicitud.grpc.RecibeSolicitud.class,
      responseType = com.company.solicitud.grpc.RecibeSolicitud.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.company.solicitud.grpc.RecibeSolicitud,
      com.company.solicitud.grpc.RecibeSolicitud> getProcesarSolicitudMethod() {
    io.grpc.MethodDescriptor<com.company.solicitud.grpc.RecibeSolicitud, com.company.solicitud.grpc.RecibeSolicitud> getProcesarSolicitudMethod;
    if ((getProcesarSolicitudMethod = SolicitudServiceGrpc.getProcesarSolicitudMethod) == null) {
      synchronized (SolicitudServiceGrpc.class) {
        if ((getProcesarSolicitudMethod = SolicitudServiceGrpc.getProcesarSolicitudMethod) == null) {
          SolicitudServiceGrpc.getProcesarSolicitudMethod = getProcesarSolicitudMethod =
              io.grpc.MethodDescriptor.<com.company.solicitud.grpc.RecibeSolicitud, com.company.solicitud.grpc.RecibeSolicitud>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ProcesarSolicitud"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.company.solicitud.grpc.RecibeSolicitud.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.company.solicitud.grpc.RecibeSolicitud.getDefaultInstance()))
              .setSchemaDescriptor(new SolicitudServiceMethodDescriptorSupplier("ProcesarSolicitud"))
              .build();
        }
      }
    }
    return getProcesarSolicitudMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SolicitudServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SolicitudServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SolicitudServiceStub>() {
        @java.lang.Override
        public SolicitudServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SolicitudServiceStub(channel, callOptions);
        }
      };
    return SolicitudServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SolicitudServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SolicitudServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SolicitudServiceBlockingStub>() {
        @java.lang.Override
        public SolicitudServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SolicitudServiceBlockingStub(channel, callOptions);
        }
      };
    return SolicitudServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SolicitudServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SolicitudServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SolicitudServiceFutureStub>() {
        @java.lang.Override
        public SolicitudServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SolicitudServiceFutureStub(channel, callOptions);
        }
      };
    return SolicitudServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void procesarSolicitud(com.company.solicitud.grpc.RecibeSolicitud request,
        io.grpc.stub.StreamObserver<com.company.solicitud.grpc.RecibeSolicitud> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getProcesarSolicitudMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service SolicitudService.
   */
  public static abstract class SolicitudServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return SolicitudServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service SolicitudService.
   */
  public static final class SolicitudServiceStub
      extends io.grpc.stub.AbstractAsyncStub<SolicitudServiceStub> {
    private SolicitudServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SolicitudServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SolicitudServiceStub(channel, callOptions);
    }

    /**
     */
    public void procesarSolicitud(com.company.solicitud.grpc.RecibeSolicitud request,
        io.grpc.stub.StreamObserver<com.company.solicitud.grpc.RecibeSolicitud> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getProcesarSolicitudMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service SolicitudService.
   */
  public static final class SolicitudServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<SolicitudServiceBlockingStub> {
    private SolicitudServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SolicitudServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SolicitudServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.company.solicitud.grpc.RecibeSolicitud procesarSolicitud(com.company.solicitud.grpc.RecibeSolicitud request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getProcesarSolicitudMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service SolicitudService.
   */
  public static final class SolicitudServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<SolicitudServiceFutureStub> {
    private SolicitudServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SolicitudServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SolicitudServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.company.solicitud.grpc.RecibeSolicitud> procesarSolicitud(
        com.company.solicitud.grpc.RecibeSolicitud request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getProcesarSolicitudMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PROCESAR_SOLICITUD = 0;

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
        case METHODID_PROCESAR_SOLICITUD:
          serviceImpl.procesarSolicitud((com.company.solicitud.grpc.RecibeSolicitud) request,
              (io.grpc.stub.StreamObserver<com.company.solicitud.grpc.RecibeSolicitud>) responseObserver);
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
          getProcesarSolicitudMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.company.solicitud.grpc.RecibeSolicitud,
              com.company.solicitud.grpc.RecibeSolicitud>(
                service, METHODID_PROCESAR_SOLICITUD)))
        .build();
  }

  private static abstract class SolicitudServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SolicitudServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.company.solicitud.grpc.solicitudProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("SolicitudService");
    }
  }

  private static final class SolicitudServiceFileDescriptorSupplier
      extends SolicitudServiceBaseDescriptorSupplier {
    SolicitudServiceFileDescriptorSupplier() {}
  }

  private static final class SolicitudServiceMethodDescriptorSupplier
      extends SolicitudServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    SolicitudServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (SolicitudServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new SolicitudServiceFileDescriptorSupplier())
              .addMethod(getProcesarSolicitudMethod())
              .build();
        }
      }
    }
    return result;
  }
}
