package org.janelia.scicomp.v5.asbtract.lib;


import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.janelia.scicomp.lib.tools.Utils;

import java.io.File;
import java.io.IOException;


public class GitV5VersionManger extends V5VersionManager {
    private final String path;
    protected final Git git;

    public GitV5VersionManger(String path) throws IOException {
        this.path = path;
        this.git = openOrInit(new File(path));
    }

    private static Git openOrInit(File file) throws IOException {
        try {
            return Git.open(file);
        } catch (Exception e) {
            try {
                return Git.init().setDirectory(file).call();
            } catch (GitAPIException ex) {
                throw new IOException(ex);
            }
        }
    }

    public static GitV5VersionManger open(String path) throws IOException {
        return new GitV5VersionManger(path);
    }

    @Override
    public String getCurrentBranch() throws IOException {
        return this.git.getRepository().getBranch();
    }

    public static GitV5VersionManger initRepo(String path) throws IOException {
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

    public static GitV5VersionManger cloneFrom(String mountedFile, String targetDirectory, String username) throws IOException {
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
        return new GitV5VersionManger(targetPath.getAbsolutePath());
    }

    public static void main(String[] args) throws IOException {
        String mountedDirectory = "/groups/scicompsoft/home/zouinkhim/test_versioned";
        String username = "zouinkhim";
        GitV5VersionManger versionControlledDirectory = GitV5VersionManger.cloneFrom(mountedDirectory, new File("").getAbsolutePath(), username);
        System.out.println(versionControlledDirectory.getPath());
    }

    public void addAll() throws IOException {
        try {
            AddCommand add = git.add();
            add.addFilepattern(".");
            add.call();
        }catch (Exception e){
            throw new IOException(e);
        }
    }

    public void commit(String message) throws IOException {
        try {
            CommitCommand commit = git.commit();
            commit.setMessage(message).call();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public void push() throws IOException {
        try{
        PushCommand pushCommand = git.push();
        pushCommand.call();
        }catch (Exception e){
            throw new IOException(e);
        }
    }

    public void pull() throws IOException {
        try{
        PullCommand pullCommand = git.pull();
        pullCommand.call();
        }catch (Exception e){
            throw new IOException(e);
        }
    }

    private void checkout(String name, boolean create) throws IOException {
        try{
        git.checkout().setCreateBranch(create).setName(name).call();
        }catch (Exception e){
            throw new IOException(e);
        }
    }

    @Override
    public void commitAll() throws IOException {
        String message = Utils.format(uncommittedBlocks);
        addAll();
        commit(message);
    }

    @Override
    public void createNewBranch(String branchName) throws IOException {
        checkout(branchName, true);

    }

    @Override
    public void checkoutBranch(String branchName) throws IOException {
        checkout(branchName, false);
    }
}
