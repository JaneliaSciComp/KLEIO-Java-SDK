package org.janus.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janus.lib.RemoteDirectory;

import java.io.*;


public class VersionedStorageAPI implements Serializable {
    private String localFolder;
    private String currentAnnotationN5File;

    private UnsignedLongType currentVersion;

    private VersionedStorageAPI(String localFolder, RemoteDirectory remoteDirectory) {
        this.localFolder = localFolder;
    }



    public VersionedStorageAPI startNewSession() {

        return null;
    }

    public UnsignedLongType getCurrentVersion() {
        return currentVersion;
    }

    public static VersionedStorageAPI open(String file) throws FileNotFoundException {
        Gson gson = new GsonBuilder().create();
        VersionedStorageAPI vsi = gson.fromJson(new FileReader(file), VersionedStorageAPI.class);
        return vsi;
    }

    public void save(String file) throws IOException {
        Writer writer = new FileWriter(file);
        Gson gson = new GsonBuilder().create();
        gson.toJson(this, writer);
        writer.flush();
        writer.close();
        System.out.println("Config saved: " + gson.toJson(this));
    }
}
