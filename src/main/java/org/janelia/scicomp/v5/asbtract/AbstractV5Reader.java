package org.janelia.scicomp.v5.asbtract;

import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.scicomp.v5.asbtract.uri.V5URL;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractV5Reader<I extends N5Reader, K extends N5Reader>  implements N5Reader  {

    protected final I indexStore;
    protected final K rawData;
    protected final V5URL url;

    public AbstractV5Reader(I indexStore, K rawData, V5URL url) throws IOException {
        super();
        this.indexStore = indexStore;
        this.rawData = rawData;
        this.url = url;
    }

    public boolean exists(String pathName) {
        return rawData.exists(pathName);
    }

    @Override
    public DatasetAttributes getDatasetAttributes(String pathName) throws IOException {
        return rawData.getDatasetAttributes(pathName);
    }


    @Override
    public Map<String, Class<?>> listAttributes(String pathName) throws IOException {
        return rawData.listAttributes(pathName);
    }

    public String[] list(String pathName) throws IOException {
        return rawData.list(pathName);
    }

    protected long getBlockVersion(String dataset, long[] gridPosition) throws IOException {
        //TODO what's the most optimal way to read the cell and keep it in cache
        return  ((UnsignedLongType) N5Utils.openVolatile(indexStore,dataset).getAt(gridPosition)).get();
    }

    abstract public String getVersionedUrl();

}
