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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LocalGitRepo {

    private final File path;
    private final Map<String, ObjectId> files;
    private final Git git;
    private RevCommit commit;


    public LocalGitRepo(File path, RevCommit commit) throws IOException {
        this.path = path;
        this.commit = commit;
        this.git = Git.open(path);
        this.files = mapFiles(git, commit.getTree());
    }

    public void setCommit(RevCommit commit) throws IOException {
        this.commit = commit;
        this.files.clear();
        this.files.putAll(mapFiles(git, this.commit.getTree()));
        System.out.println("Commit updated for :" + commit.getName());
    }

    public static Map<String, ObjectId> mapFiles(Git git, RevTree tree) throws IOException {
        Map<String, ObjectId> result = new HashMap<>();
        TreeWalk treeWalk = new TreeWalk(git.getRepository());
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        while (treeWalk.next()) {
            result.put(treeWalk.getPathString(), treeWalk.getObjectId(0));
        }
        return result;
    }

    public boolean exists(String path) {
        return this.files.containsKey(path);
    }

    public ObjectLoader readFile(String path) throws IOException {
        if (!exists(path))
            throw new IOException("File doesn't exist ! :" + path);

        return git.getRepository().open(files.get(path));
    }


    public Map<String, ObjectId> getFiles() {
        return files;
    }


}
