package sum.client;

import com.mzouink.sum.SumRequest;
import com.mzouink.sum.SumResponse;
import com.mzouink.sum.SumServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculateSumClient {
    public static void main(String[] args) {
        System.out.println("started client");
        int x  = 5;
        int y = 6 ;

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",50051).usePlaintext().build();

        SumServiceGrpc.SumServiceBlockingStub stub = SumServiceGrpc.newBlockingStub(channel);
        SumResponse result = stub.calculateSum(SumRequest.newBuilder().setX(x).setY(y).build());

        System.out.println("got result "+result.getResult());
    }
}
