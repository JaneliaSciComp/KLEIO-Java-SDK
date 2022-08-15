package org.janelia.scicomp.v5;

import java.io.IOException;
import java.net.URI;

public class V5URI {
    private final String versionedIndexPath;
    private final String dataStorePath;

    public V5URI(URI uri) throws IOException {
        try {
            if (uri.getScheme().equals("v5")) {
                String[] parts = uri.toString().split(":");
                this.versionedIndexPath = parts[1];
                this.dataStorePath = parts[2];
            } else {
                throw new IOException("Invalid Versioned URI pattern V5:INDEX_PATH:DATA_PATH yours: " + uri);
            }
        } catch (Exception e) {
            throw new IOException("Invalid Versioned URI pattern V5:INDEX_PATH:DATA_PATH yours: " + uri + "\nexception: " + e);
        }
    }

    public static String format(String versionIndexPath, String dataStorePath) {
        return String.format("v5:%s:%s", versionIndexPath, dataStorePath);
    }

    public String getDataStorePath() {
        return dataStorePath;
    }

    public String getVersionedIndexPath() {
        return versionedIndexPath;
    }
}
