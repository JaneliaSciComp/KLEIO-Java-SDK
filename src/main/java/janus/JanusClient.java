package janus;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.janus.api.ChunkIndexerGrpc;
import org.janus.api.DataFormat;
import org.janus.api.Dataset;
import org.janus.api.DatasetSpec;
import org.janus.api.NewDatasetRequest;
import org.janus.api.NewDatasetResponse;
import org.janus.api.SessionRequest;

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

    public ChunkIndexerGrpc.ChunkIndexerBlockingStub getStub() {
        return stub;
    }

    public static void main(String[] args) {

        JanusClient.get();

//        rpc NewDataset(NewDatasetRequest) returns (NewDatasetResponse);
        NewDatasetResponse result = JanusClient.get().getStub().newDataset(
                NewDatasetRequest.newBuilder().setSpec(
                        DatasetSpec.newBuilder()
                                .setDataset(Dataset.newBuilder().setPath("test_path"))
                                .setLayout(DataFormat.N5)
                                .setNumDims(3)
                                .addDimBits(1000).addDimBits(1000).addDimBits(1000)
                                .setMetadata("")
                                .build()).build());

//        rpc OpenSession(SessionRequest) returns (Session);
        NewDatasetResponse result = JanusClient.get().getStub().openSession(
                SessionRequest.newBuilder()
                        .setClient("zouinkhim@hhmi.org")
                        .setDataset(Dataset.newBuilder().setPath("test_path"))
                .build())

//        // Commits and closes the session.
//        rpc CommitSession(CommitRequest) returns (CommitResponse);
//
//        // Write chunks in a session.
//        rpc WriteChunks(WriteRequest) returns (WriteResponse);
//
//        // Read chunks in a session.
//        rpc ReadChunks(ReadRequest) returns (Chunks);
//
//        // Read chunks from any given version.
//        rpc ReadVersionChunks(ReadVersionRequest) returns (Chunks);
//
//        // ---- For advanced clients that read/write directly to backing store.
//
//        // Get location of chunks, i.e., (version, chunk coord) tuples, for set of chunk coords at a given version.
//        rpc GetVersionChunkRefs(ReadVersionRequest) returns (ChunkRefs);
//
//        // Update the Janus server index for chunks that have been altered using direct writes to chunk storage.
//        rpc MarkIngestedChunks(MarkIngestedRequest) returns (MarkIngestedResponse);

    }
}
