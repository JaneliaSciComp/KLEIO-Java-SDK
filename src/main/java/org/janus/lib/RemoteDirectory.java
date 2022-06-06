package org.janus.lib;

public class RemoteDirectory {
    private final String host;
    private final String path;

    private String username;
    private byte[] password;

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

    public static void main(String[] args) {
        RemoteDirectory remoteDirectory = new RemoteDirectory("c13u06.int.janelia.org", "/groups/scicompsoft/home/zouinkhim/test_versioned").setUsername("zouinkhim");
//        .setPassword(args[0].getBytes());
        System.out.println(remoteDirectory.getUri());
    }
}