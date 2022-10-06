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

import com.google.common.collect.Lists;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.N5FSWriter;
import org.janelia.saalfeldlab.n5.RawCompression;
import org.janelia.saalfeldlab.n5.s3.N5AmazonS3Reader;
import org.janelia.scicomp.v5.lib.tools.FileUtils;
import org.janelia.scicomp.v5.lib.vc.GitV5VersionManger;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GitMergeConflictTest {
    private final static String path = "/Users/zouinkhim/Desktop/tmp/git_folder";

    public static void main(String[] args) throws IOException, GitAPIException {
        FileUtils.forceDeleteAll(path);
        new File(path).mkdirs();

        GitV5VersionManger manager = new GitV5VersionManger(path);
        N5FSWriter writer = new N5FSWriter(path);
        manager.commitAll("init");

        String masterBranch = manager.getCurrentBranch();
        System.out.println(masterBranch);

        manager.createNewBranch("test");
        manager.checkoutBranch("test");

        writer.createDataset("hello", new long[]{10, 10, 10}, new int[]{2, 2, 2}, DataType.UINT64, new RawCompression());


        Set<String> uncommitted = manager.getGit().status().call().getUncommittedChanges();
        for (String s : uncommitted)
            System.out.println(s);

        manager.commitAll("n5");

        System.out.println(manager.getCurrentBranch());

        manager.checkoutBranch(masterBranch);
////        writer.createDataset("hello", new long[]{110,10,10},new int[]{22,2,2}, DataType.UINT8,new RawCompression());
//
////        manager.commitAll("n5_hello");
////.setSquash() all in one commit
        MergeResult result = manager.getGit().merge().setCommit(false).setFastForward(MergeCommand.FastForwardMode.NO_FF).include(manager.getGit().getRepository().findRef("test")).call();
//
//        System.out.println("conflicts:");
//        Map<String, int[][]> conflicts = result.getConflicts();
//        if (conflicts == null) {
//            System.out.println("NO conflict");
//        } else {
//            for (String k :
//                    conflicts.keySet()) {
//                System.out.println(k + ":" + conflicts.get(k));
//            }
//        }
//        System.out.println("merged !");
//
//        showCommits(manager.getGit());
////reset change
//        manager.getGit().reset().setMode(ResetCommand.ResetType.HARD).call();

//        showCommits(manager.getGit());
    }

    private static void showCommits(Git git) throws GitAPIException {
        Iterable<RevCommit> commits = git.log().call();

        List<RevCommit> commitsList = Lists.newArrayList(commits.iterator());

        System.out.println("--------------------------");
        for (RevCommit commit : commitsList) {
            System.out.println("commit:");
            System.out.println(commit.getName());
            System.out.println(commit.getAuthorIdent().getName());
            System.out.println(new Date(commit.getCommitTime() * 1000L));
            System.out.println(commit.getFullMessage());
        }
    }
}

