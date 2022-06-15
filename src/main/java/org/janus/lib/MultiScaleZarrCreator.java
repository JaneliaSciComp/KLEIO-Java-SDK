package org.janus.lib;

import org.janelia.saalfeldlab.n5.Compression;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;

import java.io.IOException;
import java.util.List;

public class MultiScaleZarrCreator {
    private final static int[] indexMatrixBlockSize = new int[]{64, 64, 64};
    private final static DataType indexMatrixDataType = DataType.UINT64;
    private final static Compression indexMatrixCompression = new GzipCompression();

    public static void main(String[] args) throws IOException {
        String testDirPath = "/Users/zouinkhim/Desktop/active_learning/tmp/test_zarr";
        String n5path = "/Users/zouinkhim/Downloads/grid-3d-stitched-h5/dataset.n5";
        List<MultiscaleAttributes> atts = MultiscaleAttributes.generateFromN5(n5path, "setup0/timepoint0/");

        for (MultiscaleAttributes t : atts) {
            System.out.println(t);
        }
        MultiScaleZarrCreator.create(testDirPath, atts);

    }

    private static void create(String testDirPath, List<MultiscaleAttributes> atts) throws IOException {
        N5ZarrWriter n5 = new N5ZarrWriter(testDirPath);
        for (MultiscaleAttributes att : atts)
            n5.createDataset(att.getDataset(), att.getGridSize(), indexMatrixBlockSize, indexMatrixDataType, indexMatrixCompression);
        n5.close();
    }
}
