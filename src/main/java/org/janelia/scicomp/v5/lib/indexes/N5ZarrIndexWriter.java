package org.janelia.scicomp.v5.lib.indexes;

import com.google.gson.GsonBuilder;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.*;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.scicomp.v5.lib.vc.GitV5VersionManger;

import java.io.IOException;

public class N5ZarrIndexWriter extends N5ZarrWriter implements V5IndexWriter<GitV5VersionManger> {
    protected UnsignedLongType session;

    private final static int[] indexMatrixBlockSize = new int[]{64, 64, 64};
    private final static DataType indexMatrixDataType = DataType.UINT64;
    private final static Compression indexMatrixCompression = new GzipCompression();

    protected final GitV5VersionManger versionManger;
    public N5ZarrIndexWriter(String basePath, GsonBuilder gsonBuilder, String dimensionSeparator, boolean mapN5DatasetAttributes) throws IOException {
        super(basePath, gsonBuilder, dimensionSeparator, mapN5DatasetAttributes);
        this.versionManger = new GitV5VersionManger(basePath);
    }

    public N5ZarrIndexWriter(String basePath, GsonBuilder gsonBuilder, String dimensionSeparator) throws IOException {
        this(basePath, gsonBuilder, dimensionSeparator, true);
    }

    public N5ZarrIndexWriter(String basePath, String dimensionSeparator, boolean mapN5DatasetAttributes) throws IOException {
        this(basePath, new GsonBuilder(), dimensionSeparator, mapN5DatasetAttributes);
    }

    public N5ZarrIndexWriter(String basePath, boolean mapN5DatasetAttributes) throws IOException {
        this(basePath, new GsonBuilder(), ".", mapN5DatasetAttributes);
    }

    public N5ZarrIndexWriter(String basePath, GsonBuilder gsonBuilder) throws IOException {
        this(basePath, gsonBuilder, ".");
    }

    public N5ZarrIndexWriter(String basePath) throws IOException {
        this(basePath, new GsonBuilder());
    }

    @Override
    public GitV5VersionManger getVersionManager() {
        return versionManger;
    }

    @Override
    public UnsignedLongType getSession() {
        return session;
    }

    @Override
    public void setSession(UnsignedLongType session) {
        this.session = session;
    }

    //TODO optimize this change to read block write block
    public void set(String dataset, long[] gridPosition,UnsignedLongType value) throws IOException {
        CachedCellImg<UnsignedLongType, ?> img = N5Utils.open(this, dataset);
        UnsignedLongType p = img.getAt(gridPosition);
        p.set(value);
        DatasetAttributes attrs = this.getDatasetAttributes(dataset);
        N5Utils.save(img, this, dataset, attrs.getBlockSize(), attrs.getCompression());  }


    @Override
    public void set(String pathName, long[] gridPosition) throws IOException {
        this.set(pathName,gridPosition,getCurrentSession());
    }

    @Override
    public <T> void writeBlock(String pathName, DatasetAttributes datasetAttributes, DataBlock<T> dataBlock) throws IOException {
        super.writeBlock(pathName, datasetAttributes, dataBlock);
        versionManger.addUncommittedBlock(dataBlock.getGridPosition());
    }

    @Override
    public DatasetAttributes getDatasetAttributes(String pathName) throws IOException {
        return super.getDatasetAttributes(pathName);
    }

    public void createDataset(String pathName, long[] gridDimensions) throws IOException {
        super.createDataset(pathName,gridDimensions,indexMatrixBlockSize,indexMatrixDataType,indexMatrixCompression);
    }


}
