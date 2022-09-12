package org.janelia.scicomp.v5.asbtract;

import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.scicomp.v5.asbtract.uri.V5URL;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractV5FSReader<I extends N5Reader, K extends N5Reader> implements V5Reader<I,K> {

    protected final I indexReader;
    protected final K rawReader;
    protected final V5URL url;

    public AbstractV5FSReader(I indexReader, K rawReader, V5URL url) {
        this.indexReader = indexReader;
        this.rawReader = rawReader;
        this.url = url;
    }

    @Override
    public K getRawReader() {
        return rawReader;
    }

    @Override
    public String getVersionedUrl() {
        return url.getURL();
    }

    @Override
    public I getIndexReader() {
        return indexReader;
    }

    @Override
    public V5URL getUrl() {
        return url;
    }

    protected long getBlockVersion(String dataset, long[] gridPosition) throws IOException {
        //TODO what's the most optimal way to read the cell and keep it in cache
        return  ((UnsignedLongType) N5Utils.openVolatile(getIndexReader(),dataset).getAt(gridPosition)).get();
    }

    @Override
    public DataBlock<?> readBlock(String pathName, DatasetAttributes datasetAttributes, long... gridPosition) throws IOException {
        long version = getBlockVersion(pathName, gridPosition);
        if(version==0) {
            return null;
        }
        Path versionedPath = Paths.get(pathName, String.valueOf(version));

        return rawReader.readBlock(versionedPath.toString(),datasetAttributes,gridPosition);
    }
}
