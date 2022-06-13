package org.janus.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class RemoteDirectory implements Serializable {
    private final String host;
    private final String path;

    private String username;
    private transient byte[] password;

    public RemoteDirectory(String host, String path) {
        this.host = host;
        this.path = path;
    }

    public RemoteDirectory setUsername(String username) {
        this.username = username;
        return this;
    }

    public RemoteDirectory setPassword(byte[] password) {
        this.password = password;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public String getHost() {
        return host;
    }

    public byte[] getPassword() {
        if (password == null || password.length == 0)
            throw new NullPointerException("Password missing!");
        return password;
    }

    public String getPath() {
        return path;
    }

    public String getUri() {
        if (host == null || host.length() == 0)
            throw new NullPointerException("Host can't be null ! ");
        if (path == null || path.length() == 0)
            throw new NullPointerException("Remote path can't be null");
        if (username == null || username.length() == 0)
            throw new NullPointerException("Username can't be null");
        return String.format("%s@%s:%s", username, host, path);
    }


    public static void main(String[] args) throws IOException {
        RemoteDirectory remoteDirectory = new RemoteDirectory("c13u06.int.janelia.org", "/groups/scicompsoft/home/zouinkhim/test_versioned").setUsername("zouinkhim");
        remoteDirectory.setPassword(args[0].getBytes());
        remoteDirectory.toJson("/Users/zouinkhim/Desktop/active_learning/tmp/remote.json");
        RemoteDirectory r2 = RemoteDirectory.fromJson("/Users/zouinkhim/Desktop/active_learning/tmp/remote.json");
        System.out.println(r2.getUri());
        System.out.println(r2.getPassword());

    }

    public static RemoteDirectory fromJson(String file) throws FileNotFoundException {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(new FileReader(file), RemoteDirectory.class);
    }

    public void toJson(String file) throws IOException {
        Writer writer = new FileWriter(file);
        Gson gson = new GsonBuilder().create();
        gson.toJson(this,writer);
        writer.flush();
        writer.close();
        System.out.println("Remote saved: "+ gson.toJson(this));
    }
}