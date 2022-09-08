package org.janelia.scicomp.v5;

import java.io.IOException;
import java.net.URI;

public class V5URI {
    private final String indexesPath;
    private final String keyValueStorePath;

    public V5URI(String indexesPath, String keyValueStorePath) {
        this.indexesPath = indexesPath;
        this.keyValueStorePath = keyValueStorePath;
    }

    public V5URI(URI uri) throws IOException {
        try {
            if (uri.getScheme().equals("v5")) {
                // "::" added to fix windows bug Z:/
                String[] parts = uri.toString().split("::");
                this.indexesPath = parts[1];
                this.keyValueStorePath = parts[2];
            } else {
                throw new IOException("Invalid Versioned URI pattern V5:INDEX_PATH:DATA_PATH yours: " + uri);
            }
        } catch (Exception e) {
            throw new IOException("Invalid Versioned URI pattern V5:INDEX_PATH:DATA_PATH yours: " + uri + "\nexception: " + e);
        }
    }

    @Deprecated
    public static String format(String indexesPath, String keyValueStorePath){
        return new V5URI(indexesPath,keyValueStorePath).get();
    }

    public String get() {
        return String.format("v5::%s::%s", indexesPath, keyValueStorePath);
    }

    @Deprecated
    public String getDataStorePath() {
        return keyValueStorePath;
    }

    @Deprecated
    public String getVersionedIndexPath() {
        return indexesPath;
    }
}
