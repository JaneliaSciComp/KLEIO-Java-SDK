package org.janelia.scicomp.v5.old.index;

import com.google.gson.GsonBuilder;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrReader;

import java.io.IOException;

public class IndexN5ZarrReader extends N5ZarrReader {
    public IndexN5ZarrReader(String basePath, GsonBuilder gsonBuilder, String dimensionSeparator, boolean mapN5DatasetAttributes) throws IOException {
        super(basePath, gsonBuilder, dimensionSeparator, mapN5DatasetAttributes);
    }

    public IndexN5ZarrReader(String basePath, GsonBuilder gsonBuilder, String dimensionSeparator) throws IOException {
        super(basePath, gsonBuilder, dimensionSeparator);
    }

    public IndexN5ZarrReader(String basePath, String dimensionSeparator, boolean mapN5DatasetAttributes) throws IOException {
        super(basePath, dimensionSeparator, mapN5DatasetAttributes);
    }

    public IndexN5ZarrReader(String basePath, boolean mapN5DatasetAttributes) throws IOException {
        super(basePath, mapN5DatasetAttributes);
    }

    public IndexN5ZarrReader(String basePath, GsonBuilder gsonBuilder) throws IOException {
        super(basePath, gsonBuilder);
    }

    public IndexN5ZarrReader(String basePath) throws IOException {
        super(basePath);
    }

}
