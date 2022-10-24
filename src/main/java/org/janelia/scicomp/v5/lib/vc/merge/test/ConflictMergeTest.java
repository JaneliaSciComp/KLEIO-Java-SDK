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

package org.janelia.scicomp.v5.lib.vc.merge.test;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.scicomp.v5.fs.MultiVersionZarrReader;
import org.janelia.scicomp.v5.lib.tools.Utils;
import org.janelia.scicomp.v5.lib.vc.merge.BlockConflictManager;
import org.janelia.scicomp.v5.lib.vc.merge.BranchesMergeManager;
import org.janelia.scicomp.v5.lib.vc.merge.entities.BlockMergeResult;
import org.janelia.scicomp.v5.lib.vc.merge.entities.Tuple;

import java.io.IOException;
import java.util.Map;

public class ConflictMergeTest {
    private static String branch1 = "annotator_1";
    private static String branch2 = "annotator_2";

    public static void main(String[] args) throws IOException, GitAPIException {
        String indexPath = "/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/annotation.v5/versionedIndex";
        BranchesMergeManager manager = new BranchesMergeManager(indexPath);
        Map<String, int[][]> conflicts = manager.getConflicts(branch1, branch2);

        RevCommit originCommit = manager.getCommonAncestor(branch1,branch2);
        System.out.println(originCommit.getFullMessage());
        System.out.println(originCommit.getTree());
        RevCommit branchOneCommit = manager.getLastCommit(branch1);
        System.out.println(branchOneCommit.getFullMessage());
        System.out.println(branchOneCommit.getTree());
        RevCommit branchTwoCommit = manager.getLastCommit(branch2);

        MultiVersionZarrReader readerBranchOne = new MultiVersionZarrReader(indexPath,branchOneCommit);
        MultiVersionZarrReader readerBranchTwo = new MultiVersionZarrReader(indexPath,branchTwoCommit);
        MultiVersionZarrReader readerAncestor = new MultiVersionZarrReader(indexPath,originCommit);


        for (String conflictFile: conflicts.keySet()) {
            System.out.println("Conflict file: "+conflictFile);
            Tuple<String,long[]> blockInfo = BlockConflictManager.formatFileInfo(conflictFile);
            String dataset = blockInfo.getA();
            long[] gridPosition = blockInfo.getB();

            DatasetAttributes datasetAttributes = readerAncestor.getDatasetAttributes(dataset);
            DataBlock baseBlock = readerAncestor.readBlock(dataset, datasetAttributes, gridPosition);

            DataBlock<?> deltaOne = BlockConflictManager.getDelta(readerAncestor, readerBranchOne, conflictFile);
            DataBlock<?> deltaTwo = BlockConflictManager.getDelta(readerAncestor, readerBranchTwo, conflictFile);

            System.out.println();
            System.out.print("Delta 1:");
            Utils.printBlock(deltaOne);
            System.out.print("Delta 2:");
            Utils.printBlock(deltaTwo);

            BlockMergeResult blockMergeResult  = BlockConflictManager.mergeDeltas(deltaOne,deltaTwo);
            if (blockMergeResult.isSuccess()){
                System.out.println("success!");
                DataBlock<?> resultBlock = BlockConflictManager.overwriteBlock(baseBlock,blockMergeResult.getResultBlock());
            }else {
                System.out.println("Error ! "+Utils.format(blockMergeResult.getBlockConflicts()));
            }
            System.out.println();
            System.out.println();

        }

    }
}
