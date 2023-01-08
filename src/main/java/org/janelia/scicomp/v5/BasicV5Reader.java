/*
 * *
 *  * Copyright (c) 2022, Janelia
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice,
 *  *    this list of conditions and the following disclaimer.
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation
 *  *    and/or other materials provided with the distribution.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.janelia.scicomp.v5;

import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.scicomp.v5.lib.V5Reader;
import org.janelia.scicomp.v5.lib.uri.V5URL;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BasicV5Reader<I extends N5Reader, K extends N5Reader> implements V5Reader<I, K> {

    protected final I indexes;
    protected final K raw;
    protected final V5URL url;

    public BasicV5Reader(I indexReader, K rawReader, V5URL url) throws IOException {
        this.indexes = indexReader;
        this.raw = rawReader;
        this.url = url;
        if (this.exists("/")) {
            Version version = this.getVersion();
            if (!VERSION.isCompatible(version)) {
                throw new IOException("Incompatible version " + version + " (this is " + VERSION + ").");
            }
        }
    }

    @Override
    public K getRawReader() {
        return raw;
    }

    @Override
    public String getVersionedUrl() {
        return url.getURL();
    }

    @Override
    public I getIndexReader() {
        return indexes;
    }

    @Override
    public V5URL getUrl() {
        return url;
    }

    protected long getBlockVersion(String dataset, long[] gridPosition) throws IOException {
        //TODO what's the most optimal way to read the cell and keep it in cache
        return ((UnsignedLongType) N5Utils.openVolatile(getIndexReader(), dataset).getAt(gridPosition)).get();
    }

    @Override
    public <T> T getAttribute(String pathName, String key, Class<T> clazz) throws IOException {
        return getRawReader().getAttribute(pathName, key, clazz);
    }

    @Override
    public <T> T getAttribute(String pathName, String key, Type type) throws IOException {
        return getRawReader().getAttribute(pathName, key, type);
    }

    @Override
    public DatasetAttributes getDatasetAttributes(String pathName) throws IOException {
        return getRawReader().getDatasetAttributes(pathName);
    }

    @Override
    public DataBlock<?> readBlock(String pathName, DatasetAttributes datasetAttributes, long... gridPosition) throws IOException {
        long version = getBlockVersion(pathName, gridPosition);
        if (version == 0)
            return null;
        Path versionedPath = Paths.get(pathName, String.valueOf(version));
        return getRawReader().readBlock(versionedPath.toString(), datasetAttributes, gridPosition);
    }
}
