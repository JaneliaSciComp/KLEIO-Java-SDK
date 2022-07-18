package org.janelia.scicomp.lib;


import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;

import java.io.File;
import java.io.IOException;

public class VersionedDirectory {
    private final String path;
    protected final Git git;

    protected VersionedDirectory(String path) throws IOException {
        this.path = path;

        this.git = Git.open(new File(path));
    }

    public static VersionedDirectory open(String path) throws IOException {
        return new VersionedDirectory(path);
    }

    public static VersionedDirectory initRepo(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("Folder " + path + " doesn't exist !");
        }
        try {
            Git.init().setDirectory(file).call();
        } catch (GitAPIException e) {
            throw new IOException(e);
        }
        return open(path);
    }

    public String getPath() {
        return path;
    }

    public static VersionedDirectory cloneFrom(String mountedFile, String targetDirectory, String username) throws IOException {
        System.out.println("cloning:" + mountedFile);
        File targetPath;
//        if (new File(targetDirectory).exists())
//            targetPath = new File(targetDirectory, FilenameUtils.getBaseName(mountedFile));
//        else
        targetPath = new File(targetDirectory);

        System.out.println("Target Path: " + targetPath.getAbsolutePath());
        try {
            Git git = Git.cloneRepository().setDirectory(targetPath).setURI(mountedFile).call();
            StoredConfig cfg = git.getRepository().getConfig();
            cfg.setString("user", null, "name", username);
            cfg.save();
        } catch (GitAPIException e) {
            System.out.println("Couldn't clone :" + mountedFile);
            throw new IOException(e);
        }
        return new VersionedDirectory(targetPath.getAbsolutePath());
    }

    public static void main(String[] args) throws GitAPIException, IOException {
        String mountedDirectory = "/groups/scicompsoft/home/zouinkhim/test_versioned";
        String username = "zouinkhim";
        VersionedDirectory versionControlledDirectory = VersionedDirectory.cloneFrom(mountedDirectory, new File("").getAbsolutePath(), username);
        System.out.println(versionControlledDirectory.getPath());
    }

    public void addAll() throws GitAPIException {
        AddCommand add = git.add();
        add.addFilepattern(".");
        add.call();
    }

    public void commit(String message) throws GitAPIException {
        CommitCommand commit = git.commit();
        commit.setMessage(message).call();
    }

    public void push() throws GitAPIException {
        PushCommand pushCommand = git.push();
        pushCommand.call();
    }

    public void pull() throws GitAPIException {
        PullCommand pullCommand = git.pull();
        pullCommand.call();
    }
}
