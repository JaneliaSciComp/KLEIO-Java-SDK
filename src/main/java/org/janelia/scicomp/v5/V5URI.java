package org.janelia.scicomp.v5;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class V5URI {
    public static final String PREFIX = "V5:" ;
    private final String indexesPath;
    private final String keyValueStorePath;

    public V5URI(String indexesPath, String keyValueStorePath) {
        this.indexesPath = indexesPath;
        this.keyValueStorePath = keyValueStorePath;
    }

    public V5URI(String uri) throws IOException {
        if (!uri.startsWith(PREFIX))
            throw new IOException("Invalid V5 URI: "+uri);
        try {
            uri = uri.substring(PREFIX.length());
            V5URI v5uri = new Gson().fromJson(uri, V5URI.class);
            this.indexesPath = v5uri.getIndexesPath();
            this.keyValueStorePath = v5uri.getKeyValueStorePath();
        }catch (Exception e){
            throw new IOException("Invalid V5 URI: "+uri);
        }
    }

    public static boolean isV5(String url) {
        try {
            new V5URI(url);
            return true;
        }catch (Exception e){
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
    public static String format(String indexesPath, String keyValueStorePath){
        return new V5URI(indexesPath,keyValueStorePath).getURI();
    }

    public String getURI() {
        return String.format("%s%s",PREFIX, new Gson().toJson(this));
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
        V5URI v5URI1 = new V5URI("Z:\\jonesa\\versioned_data\\jrc_mus-kidney\\versionedIndex","Z:\\jonesa\\versioned_data\\jrc_mus-kidney\\datastore");
        String uri = v5URI1.getURI();
        System.out.println(uri);
        V5URI v5URI2 = new V5URI(uri);
        System.out.println(v5URI2.getIndexesPath());

    }
}
