/*
 * *
 *  * Copyright (c) 2022, Janelia
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice,
 *  *    this list of conditions and the following disclaimer.
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation
 *  *    and/or other materials provided with the distribution.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.janelia.scicomp.v5.lib.vc;


import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.janelia.scicomp.v5.lib.tools.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Set;


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

    @Override
    public Set<String> getUncommittedChanges() throws IOException {
        try {
            return this.git.status().call().getUncommittedChanges();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> getUntrackedChanges() throws IOException {
        try {
            return this.git.status().call().getUntracked();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
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
        File targetPath = new File(targetDirectory);
//        if (new File(targetDirectory).exists())
//            targetPath = new File(targetDirectory, FilenameUtils.getBaseName(mountedFile));
//        else
//        targetPath = new File(targetDirectory);

        System.out.println("Target Path: " + targetPath.getAbsolutePath());
        try {
            Git git = Git.cloneRepository().setDirectory(targetPath).setURI(mountedFile).call();
            StoredConfig cfg = git.getRepository().getConfig();
            cfg.setString("user", null, "name", username);
            cfg.save();
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        try {
            PushCommand pushCommand = git.push();
            pushCommand.call();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public void pull() throws IOException {
        try {
            PullCommand pullCommand = git.pull();
            pullCommand.call();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void checkout(String name, boolean create) throws IOException {
        try {
            git.checkout().setCreateBranch(create).setName(name).call();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void commitAll(String message) throws IOException {
        addAll();
        commit(message);
    }

    @Override
    public void commitBlocks() throws IOException {
        String message = "";
        if (!uncommittedBlocks.isEmpty())
            message = Utils.format(uncommittedBlocks);
        this.commitAll(message);
    }

    @Override
    public void createNewBranch(String branchName) throws IOException {
        try {
            git.branchCreate().setName(branchName).call();
        } catch (Exception e) {
            throw new IOException(e);
        }

    }

    @Override
    public void checkoutBranch(String branchName) throws IOException {
        checkout(branchName, false);
    }

    public Git getGit() {
        return git;
    }
}
