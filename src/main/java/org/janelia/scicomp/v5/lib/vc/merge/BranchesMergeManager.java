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

package org.janelia.scicomp.v5.lib.vc.merge;

import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.janelia.scicomp.v5.fs.V5FSWriter;
import org.janelia.scicomp.v5.lib.vc.GitV5VersionManger;

import java.io.IOException;
import java.util.Map;

public class BranchesMergeManager {
    private final GitV5VersionManger manager;

    public BranchesMergeManager(V5FSWriter writer) {
        this.manager = writer.getIndexWriter().getVersionManager();
    }

    public Map<String, int[][]> getConflicts(String sourceBranch, String targetBranch) throws IOException, GitAPIException {
        String originalBranch = checkAndCheckout(targetBranch);
        Map<String, int[][]> conflicts = getBranchChangesToCurrent(sourceBranch);
        manager.getGit().reset().setMode(ResetCommand.ResetType.HARD).call();
        return conflicts;

    }

    public boolean mergeAndCommit(String sourceBranch, String targetBranch) throws IOException, GitAPIException {
        String originalBranch = checkAndCheckout(targetBranch);
        Map<String, int[][]> conflicts = getBranchChangesToCurrent(sourceBranch);
        if (conflicts != null) {
            manager.getGit().reset().setMode(ResetCommand.ResetType.HARD).call();
            throw new IOException("ERROR! Can't merge, there is conflicts. Use getConflict() to figure out !");
        }
        manager.commitAll("merge " + sourceBranch);

        if (originalBranch != null)
            manager.checkoutBranch(originalBranch);
        return true;
    }

    private Map<String, int[][]> getBranchChangesToCurrent(String sourceBranch) throws IOException, GitAPIException {
        MergeResult result = manager
                .getGit()
                .merge()
                .setCommit(false)
                .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                .include(manager.getGit().getRepository().findRef(sourceBranch))
                .call();
        Map<String, int[][]> conflicts = result.getConflicts();
        return conflicts;
    }

    private String checkAndCheckout(String targetBranch) throws IOException {
        if (!manager.getCurrentBranch().equals(targetBranch)) {
            String originalBranch = manager.getCurrentBranch();
            manager.checkoutBranch(targetBranch);
            return originalBranch;
        }
        return null;
    }
}
