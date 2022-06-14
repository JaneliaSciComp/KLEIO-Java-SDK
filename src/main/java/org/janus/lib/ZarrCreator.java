package org.janus.lib;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5DatasetDiscoverer;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5TreeNode;
import org.janelia.saalfeldlab.n5.bdv.N5Viewer;
import org.janelia.saalfeldlab.n5.ij.N5Factory;
import org.janelia.saalfeldlab.n5.ij.N5Importer;
import org.janelia.saalfeldlab.n5.metadata.N5Metadata;
import org.janelia.saalfeldlab.n5.ui.DataSelection;
import java.util.Arrays;
import java.util.Collections;

public class ZarrCreator {
    private static String testDirPath = "/Users/zouinkhim/Desktop/active_learning/tmp/test_zarr";
    private static long[] dimensions = new long[]{600,600,600};
    private static int[] blockSize = new int[]{128,128,128};

    private static String n5path = "/Users/zouinkhim/Downloads/grid-3d-stitched-h5/dataset.n5";

    public static void main(String[] args) throws IOException {
//        N5FSReader n5 = new N5FSReader(n5path);
        List<MultiscaleAttributes> atts = MultiscaleAttributes.generateFromN5(n5path, "setup0/timepoint0/");

        for(MultiscaleAttributes t : atts){
            System.out.println(t);
        }
//        N5Metadata multiscaleMetadata = root.childrenList().stream()
//                .filter( x -> x.getPath().equals(multiscaleBaseDataset)).findFirst().get().getMetadata();

        // run n5 viewer
//        N5Viewer.exec(new DataSelection( n5, Collections.singletonList( multiscaleMetadata )));


//        N5DatasetDiscoverer
//        N5ZarrWriter n5 = new N5ZarrWriter(testDirPath);
//                n5.remove();
//        n5.createDataset("s0", new long[]{600,600,600}, blockSize, DataType.UINT64, new GzipCompression());
//        n5.createDataset("s1", new long[]{200,200,200}, blockSize, DataType.UINT64, new GzipCompression());
//        n5.createDataset("s2", new long[]{100,100,100}, blockSize, DataType.UINT64, new GzipCompression());
////        n5.remove();
//        n5.close();
    }
}
