package org.janelia.scicomp.api;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.janelia.scicomp.lib.KeyValueStore;
import org.janelia.scicomp.lib.MultiScaleZarr;
import org.janelia.scicomp.lib.MultiscaleAttributes;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CreateVersionedDataset {
    private final static String DATASET_NAME = "dataset.v5";
    private final static String INDEXES_STORE = "indexes";
    private final static String KV_STORE = "kv_store";

    public static void main(String[] args) throws GitAPIException, IOException {
        String path = "/Users/zouinkhim/Desktop/active_learning/versioned_data";
        String n5_file = "/Users/zouinkhim/Downloads/car/dataset.n5";
        String dataset = "setup0/timepoint0";
        File versioned_dataset_path = new File(path, DATASET_NAME);

//        String n5path = "/Users/zouinkhim/Downloads/grid-3d-stitched-h5/dataset.n5";
        List<MultiscaleAttributes> atts = MultiscaleAttributes.generateFromN5(n5_file, "setup0/timepoint0/");

        for (MultiscaleAttributes t : atts) {
            System.out.println(t);
        }
        MultiScaleZarr versionZarr = new MultiScaleZarr(new File(versioned_dataset_path, INDEXES_STORE).getAbsolutePath());
        versionZarr.create(atts);

        //  push it
//        versionControlledDirectory.addAll();
//        versionControlledDirectory.commit("initial");
//        versionControlledDirectory.push();


        //  store annotation in key store
        KeyValueStore kv = new KeyValueStore(new File(versioned_dataset_path, KV_STORE).getAbsolutePath());
        kv.create();
//        long[] position = new long[]{1, 3, 1};
//        kv.set(position, new byte[]{});
//
//        // update index matrix
//        versionZarr.write("s0", position, SessionId.get());
//        versionZarr.write("s1", position, SessionId.get());

        // push again
//        versionControlledDirectory.addAll();
//        versionControlledDirectory.commit("update");
//        versionControlledDirectory.push();
    }
}
