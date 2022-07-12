package org.janus.n5.tests;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.janus.n5.VersionedN5Writer;

import java.io.IOException;

public class CloneRepo {

    public static void main(String[] args) throws GitAPIException, IOException {
    final String remotePath = "";
    final String localPath = "";
    final  String username = "";
        VersionedN5Writer writer = VersionedN5Writer.cloneFrom(remotePath,localPath,username);
        writer.commit();
    }
}
