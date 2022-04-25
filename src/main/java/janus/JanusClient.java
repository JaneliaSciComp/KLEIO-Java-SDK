package janus;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.janus.api.ChunkIndexerGrpc;

public class JanusClient {
    private static final String path = "localhost";
    private static final int port = 50051;

    ChunkIndexerGrpc.ChunkIndexerBlockingStub stub;

    private static JanusClient instance;

    private JanusClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(path, port)
                .usePlaintext()
                .build();
        this.stub = ChunkIndexerGrpc.newBlockingStub(channel);
    }

    public static JanusClient get() {
        if (instance == null) {
            instance = new JanusClient();
        }
        return instance;
    }

    public static void main(String[] args) {


//        Session.newBuilder().set
//        SumResponse result = stub.calculateSum(SumRequest.newBuilder().setX(x).setY(y).build());
//
//        System.out.println("got result "+result.getResult());
    }
}
