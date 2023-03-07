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

package org.janelia.scicomp.kleio;

import com.google.gson.JsonElement;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.*;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class KleioReader<I extends  GsonAttributesParser, K extends GsonAttributesParser> extends AbstractGsonReader implements N5Reader {
    protected final I indexes;
    protected final K raw;

    public KleioReader(I indexes, K raw) {
        this.indexes = indexes;
        this.raw = raw;
    }

    public I getIndexes() {
        return indexes;
    }

    public K getRaw() {
        return raw;
    }

    //TODO
    public boolean exists(String pathName) {
        return raw.exists(pathName);
    }

    @Override
    public DatasetAttributes getDatasetAttributes(String pathName) throws IOException {
        return raw.getDatasetAttributes(pathName);
    }

    @Override
    public Map<String, Class<?>> listAttributes(String pathName) throws IOException {
        return raw.listAttributes(pathName);
    }

    @Override
    public String[] list(String pathName) throws IOException {
        return Arrays.stream(raw.list(pathName)).filter(x -> !x.contains(".")).toArray(String[]::new);
    }

    public HashMap<String, JsonElement> getAttributes(String pathName) throws IOException {
        return raw.getAttributes(pathName);
    }

    public <T> T getAttribute(String pathName, String key, Class<T> clazz) throws IOException {
        HashMap<String, JsonElement> map = raw.getAttributes(pathName);
        return GsonAttributesParser.parseAttribute(map, key, clazz, raw.getGson());
    }

    public <T> T getAttribute(String pathName, String key, Type type) throws IOException {
        HashMap<String, JsonElement> map = raw.getAttributes(pathName);
        return GsonAttributesParser.parseAttribute(map, key, type, raw.getGson());
    }


    protected long getBlockVersion(String dataset, long[] gridPosition) throws IOException {
        //TODO what's the most optimal way to read the cell and keep it in cache
        return ((UnsignedLongType) N5Utils.openVolatile(indexes, dataset).getAt(gridPosition)).get();
    }

    @Override
    public DataBlock<?> readBlock(String pathName, DatasetAttributes datasetAttributes, long... gridPosition) throws IOException {
        long version = getBlockVersion(pathName, gridPosition);
        if (version == 0) {
            return null;
        }
        Path versionedPath = Paths.get(pathName, String.valueOf(version));
        return raw.readBlock(versionedPath.toString(), datasetAttributes, gridPosition);
    }


}
