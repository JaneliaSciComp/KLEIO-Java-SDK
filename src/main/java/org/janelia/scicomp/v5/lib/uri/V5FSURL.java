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

package org.janelia.scicomp.v5.lib.uri;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class V5FSURL implements V5URL {
    private final String indexesPath;
    private final String keyValueStorePath;

    public V5FSURL(String indexesPath, String keyValueStorePath) {
        this.indexesPath = indexesPath;
        this.keyValueStorePath = keyValueStorePath;
    }

    public V5FSURL(String uri) throws IOException {
        if (!uri.startsWith(PREFIX))
            throw new IOException("Invalid V5 URI: " + uri);
        try {
            uri = uri.substring(PREFIX.length());
            V5FSURL v5uri = new Gson().fromJson(uri, V5FSURL.class);
            this.indexesPath = v5uri.getIndexesPath();
            this.keyValueStorePath = v5uri.getKeyValueStorePath();
        } catch (Exception e) {
            throw new IOException("Invalid V5 URI: " + uri);
        }
    }

    public static boolean isV5(String url) {
        try {
            new V5FSURL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getIndexesPath() {
        return indexesPath;
    }

    public String getKeyValueStorePath() {
        return keyValueStorePath;
    }

    @Deprecated
    public static String format(String indexesPath, String keyValueStorePath) {
        return new V5FSURL(indexesPath, keyValueStorePath).getURL();
    }

    @Override
    public String getURL() {
        return String.format("%s%s", PREFIX, new Gson().toJson(this));
    }

    @Override
    public V5FSURL forDataset(String dataset) {
        Path newIndexesPath = Paths.get(indexesPath, dataset);
        Path newKeyValueStorePath = Paths.get(keyValueStorePath, dataset);
        return new V5FSURL(newIndexesPath.toString(),newKeyValueStorePath.toString());
    }

    @Deprecated
    public String getDataStorePath() {
        return keyValueStorePath;
    }

    @Deprecated
    public String getVersionedIndexPath() {
        return indexesPath;
    }

    public static void main(String[] args) throws IOException {
        V5FSURL v5URI1 = new V5FSURL("Z:\\jonesa\\versioned_data\\jrc_mus-kidney\\versionedIndex", "Z:\\jonesa\\versioned_data\\jrc_mus-kidney\\datastore");
        String uri = v5URI1.getURL();
        System.out.println(uri);
        V5FSURL v5URI2 = new V5FSURL(uri);
        System.out.println(v5URI2.getIndexesPath());
    }
}

