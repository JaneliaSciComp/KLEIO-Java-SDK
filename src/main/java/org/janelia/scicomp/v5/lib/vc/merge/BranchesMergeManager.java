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
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.scicomp.v5.fs.MultiVersionZarrReader;
import org.janelia.scicomp.v5.fs.V5FSWriter;
import org.janelia.scicomp.v5.lib.indexes.N5ZarrIndexWriter;
import org.janelia.scicomp.v5.lib.tools.Utils;
import org.janelia.scicomp.v5.lib.vc.GitV5VersionManger;
import org.janelia.scicomp.v5.lib.vc.merge.entities.BlockConflictEntry;
import org.janelia.scicomp.v5.lib.vc.merge.entities.BlockMergeResult;
import org.janelia.scicomp.v5.lib.vc.merge.entities.ImgMergeResult;
import org.janelia.scicomp.v5.lib.vc.merge.entities.Tuple;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BranchesMergeManager {
    private final GitV5VersionManger manager;

    public BranchesMergeManager(String indexMatrix) throws IOException {
        this.manager = new N5ZarrIndexWriter(indexMatrix).getVersionManager();
    }

    public BranchesMergeManager(V5FSWriter writer) {
        this.manager = writer.getIndexWriter().getVersionManager();
    }

    public Map<String, int[][]> getConflicts(String sourceBranch, String targetBranch) throws IOException {
        try {
            String originalBranch = checkAndCheckout(targetBranch);
            Map<String, int[][]> conflicts = getBranchChangesToCurrent(sourceBranch);
            manager.getGit().reset().setMode(ResetCommand.ResetType.HARD).call();
            return conflicts;
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
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

    public RevCommit getCommonAncestor(String branch1, String branch2) {
        try {
            Map<String, ObjectId> branches = manager.getBranchesMap();
            ObjectId obj1 = bgetObjectID(branches, branch1);
            ObjectId obj2 = bgetObjectID(branches, branch2);
            RevWalk walk = new RevWalk(manager.getGit().getRepository());
            RevCommit revA = walk.lookupCommit(obj1);
            RevCommit revB = walk.lookupCommit(obj2);
            return getCommonAncestor(revA, revB);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RevCommit getLastCommit(String branch) throws IOException, GitAPIException {
        String branchHeader = "refs/heads/";
        if (!branch.contains(branchHeader))
            branch = branchHeader + branch;
        return manager.getGit().log().add(manager.getGit().getRepository().resolve(branch)).call().iterator().next();
    }

    private ObjectId bgetObjectID(Map<String, ObjectId> branches, String branch) throws IOException {
        ObjectId obj = branches.get(branch);
        if (obj == null)
            throw new IOException("ERROR! " + branch + " not found !");
        return obj;
    }

    public RevCommit getCommonAncestor(RevCommit commit1, RevCommit commit2) throws IOException {
        Repository repo = manager.getGit().getRepository();
        RevWalk walk = new RevWalk(repo);
        walk.setRevFilter(RevFilter.MERGE_BASE);
        walk.markStart(commit1);
        walk.markStart(commit2);
        RevCommit mergeBase = walk.next();
        return mergeBase;
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

    public Repository getRepo() {
        return manager.getGit().getRepository();
    }

    public List<DiffEntry> getDifferences(RevCommit originCommit, RevCommit newCommit) throws IOException {
        DiffFormatter df = new DiffFormatter(new ByteArrayOutputStream()); // use NullOutputStream.INSTANCE if you don't need the diff output
        df.setRepository(manager.getGit().getRepository());
        List<DiffEntry> entries = df.scan(originCommit, newCommit);
        return entries;
    }

    public ImgMergeResult mergeBlocks(V5FSWriter writer, String sourceBranch, String targetBranch) throws IOException, GitAPIException {

        Map<String, int[][]> conflicts = getConflicts(sourceBranch, targetBranch);
        if (conflicts == null) {
            System.out.println("No conflict");
            mergeAndCommit(sourceBranch, targetBranch);
            return new ImgMergeResult(ImgMergeResult.Case.NO_CONFLICT);
        }
        print(conflicts.keySet());

        String originalBranch = checkAndCheckout(targetBranch);
        String indexPath = writer.getIndexWriter().getBasePath();

        RevCommit originCommit = getCommonAncestor(sourceBranch, targetBranch);
        RevCommit branchSourceCommit = getLastCommit(sourceBranch);
//                System.out.println(branchOneCommit.getFullMessage());
//                System.out.println(branchOneCommit.getTree());
        RevCommit branchTargetCommit = getLastCommit(targetBranch);

        MultiVersionZarrReader readerBranchSource = new MultiVersionZarrReader(indexPath, branchSourceCommit);
        MultiVersionZarrReader readerBranchTarget = new MultiVersionZarrReader(indexPath, branchTargetCommit);
        MultiVersionZarrReader readerAncestor = new MultiVersionZarrReader(indexPath, originCommit);


        boolean allSuccess = true;
        List<BlockConflictEntry> errorBlocks = new ArrayList<>();

        for (String conflictFile : conflicts.keySet()) {
            System.out.println("Conflict file: " + conflictFile);
            Tuple<String, long[]> blockInfo = BlockConflictManager.formatFileInfo(conflictFile);
            String dataset = blockInfo.getA();
            long[] gridPosition = blockInfo.getB();

            DatasetAttributes datasetAttributes = readerAncestor.getDatasetAttributes(dataset);
            DataBlock baseBlock = readerAncestor.readBlock(dataset, datasetAttributes, gridPosition);

            DataBlock<?> deltaOne = BlockConflictManager.getDelta(readerAncestor, readerBranchSource, conflictFile);
            DataBlock<?> deltaTwo = BlockConflictManager.getDelta(readerAncestor, readerBranchTarget, conflictFile);

//            System.out.print("Delta 1:");
//            Utils.printBlock(deltaOne);
//            System.out.print("Delta 2:");
//            Utils.printBlock(deltaTwo);

            BlockMergeResult blockMergeResult = BlockConflictManager.mergeDeltas(deltaOne, deltaTwo);
            if (blockMergeResult.isSuccess()) {
//                System.out.println("success!");
                DataBlock<?> resultBlock = BlockConflictManager.overwriteBlock(baseBlock, blockMergeResult.getResultBlock());
                writer.writeBlock(dataset, datasetAttributes, resultBlock);
                errorBlocks.add(new BlockConflictEntry(gridPosition));
            } else {
                allSuccess = false;
//                System.out.println("Error ! "+Utils.format(blockMergeResult.getBlockConflicts()));
                errorBlocks.add(new BlockConflictEntry(gridPosition, blockMergeResult.getBlockConflicts()));
            }

        }
        ImgMergeResult.Case result;
        if (allSuccess) {
            manager.commitAll("merge " + sourceBranch);
            System.out.println("Success, Block and Branches merged ! ");
            result = ImgMergeResult.Case.CONFLICT_MERGED;
        } else {
            manager.getGit().reset().setMode(ResetCommand.ResetType.HARD).call();
            System.out.println("Merge didn't succeed, check conflict blocks");
            result = ImgMergeResult.Case.CONFLICT_NEED_MANUAL_SELECTION;
        }

        if (originalBranch != null)
            manager.checkoutBranch(originalBranch);


        return new ImgMergeResult(errorBlocks, result);
    }

    private void print(Set<String> keySet) {
        System.out.println("Conflict files found: ");
        for (String k : keySet)
            System.out.println("Conflict  :   " + k);
    }

    public boolean mergeConflictBlocks(V5FSWriter writer, String sourceBranch, String targetBranch, List<BlockConflictEntry> conflictsVote) throws IOException, GitAPIException {

        Map<String, int[][]> conflicts = getConflicts(sourceBranch, targetBranch);
        if (conflicts == null) {
            System.out.println("No conflict");
            mergeAndCommit(sourceBranch, targetBranch);
            return true;
        }

        String originalBranch = checkAndCheckout(targetBranch);
        String indexPath = writer.getIndexWriter().getBasePath();

        RevCommit originCommit = getCommonAncestor(sourceBranch, targetBranch);
        RevCommit branchSourceCommit = getLastCommit(sourceBranch);
//                System.out.println(branchOneCommit.getFullMessage());
//                System.out.println(branchOneCommit.getTree());
        RevCommit branchTargetCommit = getLastCommit(targetBranch);

        MultiVersionZarrReader readerBranchSource = new MultiVersionZarrReader(indexPath, branchSourceCommit);
        MultiVersionZarrReader readerBranchTarget = new MultiVersionZarrReader(indexPath, branchTargetCommit);
        MultiVersionZarrReader readerAncestor = new MultiVersionZarrReader(indexPath, originCommit);


        for (String conflictFile : conflicts.keySet()) {
            System.out.println("Conflict file: " + conflictFile);
            Tuple<String, long[]> blockInfo = BlockConflictManager.formatFileInfo(conflictFile);
            String dataset = blockInfo.getA();
            long[] gridPosition = blockInfo.getB();

            DatasetAttributes datasetAttributes = readerAncestor.getDatasetAttributes(dataset);
            DataBlock baseBlock = readerAncestor.readBlock(dataset, datasetAttributes, gridPosition);

            DataBlock<?> deltaOne = BlockConflictManager.getDelta(readerAncestor, readerBranchSource, conflictFile);
            DataBlock<?> deltaTwo = BlockConflictManager.getDelta(readerAncestor, readerBranchTarget, conflictFile);

//            System.out.print("Delta 1:");
//            Utils.printBlock(deltaOne);
//            System.out.print("Delta 2:");
//            Utils.printBlock(deltaTwo);

            BlockMergeResult blockMergeResult = BlockConflictManager.mergeDeltas(deltaOne, deltaTwo);
            DataBlock<?> block;
            if (blockMergeResult.isSuccess()) {
                block = blockMergeResult.getResultBlock();
            } else {
                List<BlockConflictEntry> votes = conflictsVote.stream().filter(e -> Utils.isEqual(e.getGridPosition(),gridPosition)).collect(Collectors.toList());
                if (votes.isEmpty()) {
                    throw new IOException("Vote grid position " + Utils.format(gridPosition) + " not found!");
                }

                BlockConflictEntry vote = votes.get(0);
                block = BlockConflictManager.mergeDeltasUsingConflictVote(deltaOne, deltaTwo, vote.getSelectedBranch());
            }
//                System.out.println("success!");
            DataBlock<?> resultBlock = BlockConflictManager.overwriteBlock(baseBlock, block);
            writer.writeBlock(dataset, datasetAttributes, resultBlock);
        }
        manager.commitAll("merge " + sourceBranch);
        System.out.println("Success, Block and Branches merged ! ");

        if (originalBranch != null)
            manager.checkoutBranch(originalBranch);

        return true;
    }
}
