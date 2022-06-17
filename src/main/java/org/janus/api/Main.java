package org.janus.api;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.janus.lib.MultiScaleZarr;
import org.janus.lib.MultiscaleAttributes;
import org.janus.lib.SessionId;
import org.janus.lib.VersionedDirectory;
import org.janus.lib.KeyValueStore;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws GitAPIException, IOException {
        //  Clone empty project
//        String mountedDirectory = "/groups/scicompsoft/home/zouinkhim/test_versioned";
        String mountedDirectory = "/Users/zouinkhim/Desktop/active_learning/tmp/test_pipeline/main_repo";
        String kvstore = "/Users/zouinkhim/Desktop/active_learning/tmp/test_pipeline/kv";
        String localFolder = "/Users/zouinkhim/Desktop/active_learning/tmp/test_pipeline/cloned_project";
        String username = "zouinkhim";
        VersionedDirectory versionControlledDirectory = VersionedDirectory.cloneFrom(mountedDirectory,localFolder, username);

        // Create dataset
        String n5path = "/Users/zouinkhim/Downloads/grid-3d-stitched-h5/dataset.n5";
        List<MultiscaleAttributes> atts = MultiscaleAttributes.generateFromN5(n5path, "setup0/timepoint0/");

        for (MultiscaleAttributes t : atts) {
            System.out.println(t);
        }
        MultiScaleZarr versionZarr = new MultiScaleZarr(localFolder);
        versionZarr.create(atts);

        //  push it
        versionControlledDirectory.addAll();
        versionControlledDirectory.commit("initial");
        versionControlledDirectory.push();


        //  store annotation in key store
        KeyValueStore kv = new KeyValueStore(kvstore, "s0", SessionId.get());
        long[] position = new long[]{1, 3, 1};
        kv.set(position, new byte[]{});

        // update index matrix
        versionZarr.write("s0", position, SessionId.get());
        versionZarr.write("s1", position, SessionId.get());

        // push again
        versionControlledDirectory.addAll();
        versionControlledDirectory.commit("update");
        versionControlledDirectory.push();
    }
}
