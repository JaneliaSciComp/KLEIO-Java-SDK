package org.janus.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.janus.lib.AnnotationSession;
import org.janus.lib.RemoteDirectory;
import org.janus.lib.VersionControlledDirectory;

import java.io.*;


public class VersionedStorageAPI implements Serializable {
    private String localFolder;
    private RemoteDirectory remoteDirectory;
    private String currentAnnotationN5File;

    private UnsignedLongType currentVersion;

    private VersionedStorageAPI(String localFolder, RemoteDirectory remoteDirectory) {
        this.localFolder = localFolder;
        this.remoteDirectory = remoteDirectory;
    }

    public RemoteDirectory getRemoteDirectory() {
        return remoteDirectory;
    }

    public static VersionedStorageAPI init(String localDirectory, RemoteDirectory remoteDirectory) throws GitAPIException, IOException {
        System.out.println("Start cloning version control repo. This my take a while..");
        VersionControlledDirectory vcd = VersionControlledDirectory.cloneFrom(remoteDirectory, localDirectory);
        System.out.println("Version control repo cloned!");

        return new VersionedStorageAPI(vcd.getPath(),remoteDirectory);
    }

    public AnnotationSession startNewSession(long[] startPosition, long[] endPosition){

        return null;
    }

    public static VersionedStorageAPI open(String file,byte[] password) throws FileNotFoundException {
        Gson gson = new GsonBuilder().create();
        VersionedStorageAPI vsi = gson.fromJson(new FileReader(file), VersionedStorageAPI.class);
        vsi.getRemoteDirectory().setPassword(password);
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

    public void commit(){

    }
}
