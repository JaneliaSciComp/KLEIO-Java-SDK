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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TestBranchMergeController implements AbstractBranchMergeController {
    private int randomCase = 0;

    @Override
    public ImgMergeResult merge(MergeBranches mergeBranches) {
        int currentCase = randomCase++;
        System.out.println(currentCase);
        if (randomCase > 2)
            randomCase = 0;
        switch (randomCase) {
            case 0:
                return getSuccessNoConflictCase();
            case 1:
                return getSuccessWithConflictsCase();
            case 2:
                return getConflictCase();
        }

        return null;


    }

    @Override
    public boolean mergeConflicts(MergeBranches mergeBranches, List<BlockConflictEntry> conflicts) throws Exception {
        List<BlockConflictEntry> filteredConflicts = conflicts.stream().filter(e -> !e.isMerged()).collect(Collectors.toList());
        if (!filteredConflicts.stream().filter(e -> e.getSelectedBranch() == 0).collect(Collectors.toList()).isEmpty())
            throw new IOException("Select branch before !");
        return true;
    }


    private ImgMergeResult getConflictCase() {

        List<BlockConflictEntry> conflicts = new ArrayList<>();
        Random rd = new Random();
        for (int i = 0; i < 10; i++) {
            boolean merged = rd.nextBoolean();
            if (merged)
                conflicts.add(new BlockConflictEntry(new long[]{2, 3, 4}));
            else
                conflicts.add(new BlockConflictEntry(new long[]{2, 3, 4}, new long[]{2, 3}, false));
        }


        return new ImgMergeResult(conflicts, ImgMergeResult.Case.CONFLICT_NEED_MANUAL_SELECTION);

    }

    private ImgMergeResult getSuccessWithConflictsCase() {

        List<BlockConflictEntry> conflicts = new ArrayList<>();

        for (int i = 0; i < 5; i++)
            conflicts.add(new BlockConflictEntry(new long[]{2, 3, 4}));
        return new ImgMergeResult(conflicts, ImgMergeResult.Case.CONFLICT_MERGED);
    }

    private ImgMergeResult getSuccessNoConflictCase() {
        return new ImgMergeResult(ImgMergeResult.Case.NO_CONFLICT);
    }
}
