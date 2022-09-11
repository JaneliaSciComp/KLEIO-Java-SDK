package org.janelia.scicomp.v5.old.tests;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.janelia.scicomp.v5.old.VersionedN5Writer;

import java.io.IOException;

public class CloneRepo {

    public static void main(String[] args) throws GitAPIException, IOException {
    final String remoteIndexPath = "";
        final String localIndexPath = "";
        final String dataStorePath = "";
    final  String username = "";
        VersionedN5Writer writer = VersionedN5Writer.cloneFrom(remoteIndexPath,localIndexPath,dataStorePath,username);
        writer.commit();
    }
}
