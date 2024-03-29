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
package org.janelia.scicomp.v5.fs;

import com.google.gson.JsonElement;
import net.imglib2.cache.img.CachedCellImg;
//import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.janelia.saalfeldlab.n5.N5FSReader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrReader;
import org.janelia.scicomp.v5.BasicV5Reader;
import org.janelia.scicomp.v5.lib.uri.V5FSURL;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Versioned Store-based N5 implementation.
 *
 * @author Marwan Zouinkhi
 */


//TODO add overwrite annotation
public class V5FSReader extends BasicV5Reader<N5ZarrReader, N5FSReader> {
    public V5FSReader(String versionIndexPath, String dataStorePath) throws IOException {
        this(new N5ZarrReader(versionIndexPath), new N5FSReader(dataStorePath), new V5FSURL(versionIndexPath, dataStorePath));
    }

    //TODO remove
    protected V5FSReader(N5ZarrReader indexStore, N5FSReader rawStore, V5FSURL url) throws IOException {
        super(indexStore, rawStore, url);
    }

    public V5FSReader(V5FSURL v5FSURL) throws IOException {
        this(v5FSURL.getIndexesPath(), v5FSURL.getKeyValueStorePath());
    }

    @Override
    public String[] list(String pathName) throws IOException {
        return Arrays.stream(super.list(pathName)).filter(x -> !x.contains(".")).toArray(String[]::new);
    }

    public HashMap<String, JsonElement> getAttributes(String pathName) throws IOException {
        return getRawReader().getAttributes(pathName);
    }

    public static void main(String[] args) throws Exception {
//        /indexes/s0
        String indexesPath = "/Users/zouinkhim/Desktop/active_learning/versioned_data/fsdata/indexes";
        String dataPath = "/Users/zouinkhim/Desktop/active_learning/versioned_data/fsdata//dataStore";
        V5FSReader reader = new V5FSReader(indexesPath, dataPath);
        System.out.println(reader.getVersionedUrl());
        String[] all = reader.list("/");
        System.out.println("all");
        System.out.println(String.join("-", all));
        String[] resolutions = new String[]{"s0", "s1", "s2"};
        for (String s : resolutions) {
            HashMap<String, JsonElement> att = reader.getAttributes(s);
            for (String key : att.keySet()) {
                System.out.println(key + ":" + att.get(key));
            }
            CachedCellImg<FloatType, ?> img = N5Utils.open(reader, s);
//            ImageJFunctions.show(img);
        }
    }
}


