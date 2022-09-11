package org.janelia.scicomp.v5.old.index;

import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.Compression;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.scicomp.lib.VersionedDirectory;

import java.io.IOException;

public class IndexN5ZarrWriter extends N5ZarrWriter {
    private final static int[] indexMatrixBlockSize = new int[]{64, 64, 64};
    private final static DataType indexMatrixDataType = DataType.UINT64;
    private final static Compression indexMatrixCompression = new GzipCompression();



    public IndexN5ZarrWriter(String basePath) throws IOException {
        super(basePath);
        VersionedDirectory.initRepo(basePath);
    }

    public void createDataset(String pathName, long[] gridDimensions) throws IOException {
        super.createDataset(pathName,gridDimensions,indexMatrixBlockSize,indexMatrixDataType,indexMatrixCompression);
    }

    public void set(String dataset, long[] gridPosition, long version) throws IOException {
        CachedCellImg<UnsignedLongType, ?> img = N5Utils.open(this, dataset);
        UnsignedLongType p = img.getAt(gridPosition);
        p.set(version);
        DatasetAttributes attrs = this.getDatasetAttributes(dataset);
        N5Utils.save(img, this, dataset, attrs.getBlockSize(), attrs.getCompression());
    }
}
