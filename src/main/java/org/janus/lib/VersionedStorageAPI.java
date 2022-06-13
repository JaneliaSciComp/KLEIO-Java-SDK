package org.janus.lib;

import java.io.IOException;
import java.io.Serializable;


public class VersionedStorageAPI implements Serializable {
    private String localFolder;
    private RemoteDirectory remoteDirectory;

    public VersionedStorageAPI() {


    }

    public void save(){

    }

    public static VersionedStorageAPI load(String configFile){
        return new VersionedStorageAPI();
    }
    public static void main(String[] args) throws IOException {

    }
}
