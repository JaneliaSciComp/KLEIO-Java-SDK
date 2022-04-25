package sum.server;

import com.mzouink.sum.SumRequest;
import com.mzouink.sum.SumResponse;
import com.mzouink.sum.SumServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;

public class CalculateSumServer extends SumServiceGrpc.SumServiceImplBase {

    @Override
    public void calculateSum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        int x = request.getX();
        int y = request.getY();
        int result = x + y;
        System.out.println("Calculate " + x + "-" + y);
        responseObserver.onNext(SumResponse.newBuilder().setResult(result).build());
        responseObserver.onCompleted();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        int port = 50051;

        Server server = ServerBuilder.forPort(port).addService(new CalculateSumServer()).build();

        server.start();
        System.out.println("Server started");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("request server shutdown");
            server.shutdown();
            System.out.println("server is down");
        }));
        server.awaitTermination();
    }
}
