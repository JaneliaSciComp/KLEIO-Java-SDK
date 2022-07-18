package org.janelia.scicomp.lib;

import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.Compression;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;

import java.io.IOException;
import java.util.List;

public class MultiScaleZarr {
    private final static int[] indexMatrixBlockSize = new int[]{64, 64, 64};
    private final static DataType indexMatrixDataType = DataType.UINT64;
    private final static Compression indexMatrixCompression = new GzipCompression();
    private final String path;

    public MultiScaleZarr(String path) {
        this.path = path;
    }

    public static void main(String[] args) throws IOException {
        String testDirPath = "/Users/zouinkhim/Desktop/active_learning/tmp/test_zarr";
        String n5path = "/Users/zouinkhim/Downloads/grid-3d-stitched-h5/dataset.n5";
        List<MultiscaleAttributes> atts = MultiscaleAttributes.generateFromN5(n5path, "setup0/timepoint0/");

        for (MultiscaleAttributes t : atts) {
            System.out.println(t);
        }
        MultiScaleZarr multiscaleZarr = new MultiScaleZarr(testDirPath);
        multiscaleZarr.create(atts);

        multiscaleZarr.write(atts.get(0).getDataset(), new long[]{0, 1, 2}, new UnsignedLongType(4));
    }

    public void write(String dataset, long[] position, UnsignedLongType value) throws IOException {
        N5ZarrWriter writer = new N5ZarrWriter(path);
        CachedCellImg<UnsignedLongType, ?> img = N5Utils.open(writer, dataset);
        UnsignedLongType p = img.getAt(position);
        p.set(value);
        DatasetAttributes attrs = writer.getDatasetAttributes(dataset);
        N5Utils.save(img, writer, dataset, attrs.getBlockSize(), attrs.getCompression());
    }

    public void create(List<MultiscaleAttributes> atts) throws IOException {
        N5ZarrWriter n5 = new N5ZarrWriter(path);
        for (MultiscaleAttributes att : atts)
            create(n5, att);
        n5.close();
    }

    public void create(N5ZarrWriter n5, MultiscaleAttributes att) throws IOException {
        n5.createDataset(att.getDataset(), att.getGridSize(), indexMatrixBlockSize, indexMatrixDataType, indexMatrixCompression);

    }
}
