package greeting.client;

import com.mzouink.greeting.GreetingRequest;
import com.mzouink.greeting.GreetingResponse;
import com.mzouink.greeting.GreetingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GreetingClient {
    public static void main(String[] args) {

        if (args.length==0){
            System.out.println("Need one argument to work");
        return;}
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext().build();

        switch (args[0]){
            case "greet" : doGreet(channel);break;
            default:
                System.out.println("Invalid keyword: "+args[0]);
        }

        System.out.println("Shutting down");
        channel.shutdown();
    }

    private static void doGreet(ManagedChannel channel) {
        System.out.println("Enter doGreet");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
        GreetingResponse response = stub.greet(GreetingRequest.newBuilder().setFirstName("Marwan").build());

        System.out.println("Greeting: " + response.getResult());
    }
}
