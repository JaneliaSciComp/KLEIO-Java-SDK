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

package org.janelia.scicomp.v5.lib.vc.merge.entities;

import org.janelia.scicomp.v5.fs.V5FSWriter;
import org.janelia.scicomp.v5.lib.vc.merge.BranchesMergeManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BranchMergeController implements AbstractBranchMergeController {
    private final BranchesMergeManager manager;
    private final V5FSWriter writer;
    private final AbstractCallBack callback;

    public BranchMergeController(V5FSWriter writer,AbstractCallBack callback) throws IOException {
        String indexPath = writer.getIndexWriter().getBasePath();
        this.manager = new BranchesMergeManager(indexPath);
        this.writer = writer;
        this.callback = callback;
    }

    @Override
    public ImgMergeResult merge(MergeBranches mergeBranches) throws Exception {
        ImgMergeResult result = manager.mergeBlocks(writer, mergeBranches.getSourceBranch(), mergeBranches.getTargetBranch());
        if(!result.getResult().equals(ImgMergeResult.Case.CONFLICT_NEED_MANUAL_SELECTION)){
            callback.call(null);
        }
        return result;
    }

    @Override
    public boolean mergeConflicts(MergeBranches mergeBranches, List<BlockConflictEntry> conflicts) throws Exception {
        List<BlockConflictEntry> filteredConflicts = conflicts.stream().filter(e -> !e.isMerged()).collect(Collectors.toList());
        if (!filteredConflicts.stream().filter(e-> e.getSelectedBranch() == 0).collect(Collectors.toList()).isEmpty())
            throw new IOException("Select branch before !");
        boolean result = manager.mergeConflictBlocks(writer, mergeBranches.getSourceBranch(), mergeBranches.getTargetBranch(), filteredConflicts);
        if(result){
            callback.call(null);
        }
        return result;
    }
}
