package org.janus.lib;

import java.io.File;
import java.io.IOException;

public class MainTest {
    public static void main(String[] args) throws IOException {
        // 1 - Clone
//        RemoteDirectory remoteDirectory = new RemoteDirectory("c13u06.int.janelia.org",
//                "/groups/scicompsoft/home/zouinkhim/test_versioned")
//                .setUsername("zouinkhim")
//                .setPassword(args[0].getBytes());
//        System.out.println(remoteDirectory.getUri());
//
//        VersionControlledDirectory versionControlledDirectory = VersionControlledDirectory.cloneFrom(remoteDirectory, new File("").getAbsolutePath());
//        System.out.println(versionControlledDirectory.getPath());
    // 2- Open
        String path = "/Users/zouinkhim/Desktop/active_learning/janus_jclient/test_versioned";
        VersionControlledDirectory versionControlledDirectory = VersionControlledDirectory.open(path);
    }
}