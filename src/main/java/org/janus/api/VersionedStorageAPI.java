package org.janus.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.janus.lib.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class VersionedStorageAPI implements Serializable {

    private final static String GIT_FOLDER = "git_indexes";
    private final static String KV_STORE = "data_kv";
    private static Logger LOG = LoggerFactory.getLogger(VersionedStorageAPI.class);
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

        LOG.info("Config saved: " + gson.toJson(this));
    }

    public static void createEmptyLabelDataset(String username, String name, String projectPath, String localPath, String dataset,
                                               long[] dimensions, int[] blockSize, double[] resolution,
                                               double[] offset, double[][] relativeScaleFactors,
                                               int[] mipmapLevelsMaxNumEntries) throws GitAPIException, IOException {

        LOG.info("Username :" + username);
        LOG.info("projectPath :" + projectPath);
        LOG.info("dataset :" + dataset);
        LOG.info("name :" + name);
        LOG.info("dimensions :" + Arrays.toString(dimensions));

        LOG.info("blockSize :" + Arrays.toString(blockSize));
        LOG.info("resolution :" + Arrays.toString(resolution));
        LOG.info("offset :" + Arrays.toString(offset));
        LOG.info("relativeScaleFactors :" + Utils.format(relativeScaleFactors));

        List<MultiscaleAttributes> multiscaleAttributes = new ArrayList<>();
        final long[] scaledDimensions = dimensions.clone();
        final double[] accumulatedFactors = new double[]{1.0, 1.0, 1.0};
        for (int scaleLevel = 0, downscaledLevel = -1; downscaledLevel < relativeScaleFactors.length; ++scaleLevel, ++downscaledLevel) {
            final double[] scaleFactors = downscaledLevel < 0 ? null : relativeScaleFactors[downscaledLevel];

            if (scaleFactors != null) {
                Arrays.setAll(scaledDimensions, dim -> (long) Math.ceil(scaledDimensions[dim] / scaleFactors[dim]));
                Arrays.setAll(accumulatedFactors, dim -> accumulatedFactors[dim] * scaleFactors[dim]);
            }

            MultiscaleAttributes attrs = new MultiscaleAttributes("s" + scaleLevel, scaledDimensions, blockSize);
            LOG.info("level:" + scaleLevel);
            LOG.info("Attrs: " + attrs);
            multiscaleAttributes.add(attrs);
        }

        VersionedDirectory versionControlledDirectory = VersionedDirectory.cloneFrom(new File(projectPath, GIT_FOLDER).getAbsolutePath(), localPath, username);
        LOG.info("Cloned to : " + localPath);
        MultiScaleZarr versionZarr = new MultiScaleZarr(localPath);
        versionZarr.create(multiscaleAttributes);

        LOG.info("Multi scale index dataset created.");
        //  push it
        versionControlledDirectory.addAll();
        versionControlledDirectory.commit("initial");
        versionControlledDirectory.push();

        LOG.info("Pushed to remote");
    }


}

