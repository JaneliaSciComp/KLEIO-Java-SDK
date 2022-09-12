package org.janelia.scicomp.v5.lib;

import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.scicomp.v5.lib.uri.V5URL;

import java.io.IOException;
import java.util.Map;


public interface V5Reader<I extends N5Reader, K extends N5Reader>  extends N5Reader  {


    I getIndexReader();

    K getRawReader();

    V5URL getUrl();


//    TODO explain with example what i expect
//    pathName
    default boolean exists(String pathName) {
        return getRawReader().exists(pathName);
    }

    @Override
    default DatasetAttributes getDatasetAttributes(String pathName) throws IOException {
        return getRawReader().getDatasetAttributes(pathName);
    }


    @Override
    default Map<String, Class<?>> listAttributes(String pathName) throws IOException {
        return getRawReader().listAttributes(pathName);
    }

    @Override
    default String[] list(String pathName) throws IOException {
        return getRawReader().list(pathName);
    }

    String getVersionedUrl();

}
