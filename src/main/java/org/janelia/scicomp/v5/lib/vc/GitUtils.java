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
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GitUtils {
    private final Git git;

    public GitUtils(File path) throws IOException {
        this.git = Git.open(path);
    }

    public List<Ref> getBranches() throws GitAPIException {
        return git.branchList().call();
    }

    public String[] getBranchesNames() throws GitAPIException {
        List<String> branches = getBranches().stream().map(b -> b.getName().replace("refs/", "").replace("heads/", "")).collect(Collectors.toList());
        return branches.toArray(new String[branches.size()]);
    }

    public Ref getBranch(String branchName) throws GitAPIException, IOException {
        List<Ref> branches = getBranches();
        for (Ref b:branches)
            if(b.getName().contains(branchName))
                return b;
        throw new IOException("ERROR : branch "+branchName+ "not found !" );
    }

    public RevCommit getLastCommitForBranch(Ref branch) throws IncorrectObjectTypeException, MissingObjectException, GitAPIException {
        return git.log().add(branch.getObjectId()).call().iterator().next();
    }

    public List<RevCommit> getLastCommitForAllBranches() throws IncorrectObjectTypeException, MissingObjectException, GitAPIException {
        List<RevCommit> commits = new ArrayList<>();
        for (Ref branch : getBranches())
            commits.add(getLastCommitForBranch(branch));
        return commits;
    }
}
