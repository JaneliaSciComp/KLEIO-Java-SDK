package org.janelia.scicomp.v5.lib.uri;

import com.google.gson.Gson;

import java.io.IOException;

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

