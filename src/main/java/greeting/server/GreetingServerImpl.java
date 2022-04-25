package greeting.server;

import com.mzouink.greeting.GreetingRequest;
import com.mzouink.greeting.GreetingResponse;
import com.mzouink.greeting.GreetingServiceGrpc;
import io.grpc.stub.StreamObserver;

public class GreetingServerImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

    @Override
    public void greet(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
        System.out.println("Server got greet request: " + request.getFirstName());
        responseObserver.onNext(GreetingResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
        responseObserver.onCompleted();
    }
}
