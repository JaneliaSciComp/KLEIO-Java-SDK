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

package org.janelia.scicomp.v5.plugin;

import org.janelia.scicomp.v5.lib.vc.merge.BranchesMergeManager;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "merge", mixinStandardHelpOptions = true, version = "1.0",
        description = "Merge branches tool, use CheckMergeConflict before ")
class Merge_Plugin implements Callable<Integer> {

    @Option(names = {"-f", "--file"}, required = true, description = "Index matrix Path")
    private File file;

    @Option(names = {"-s", "--source_branch"}, description = "Source branch to merge")
    private String sourceBranch;

    @Option(names = {"-t", "--target_branch"}, description = "Target branch to merge")
    private String targetBranch;

    @Override
    public Integer call() throws Exception {
        BranchesMergeManager manager = new BranchesMergeManager(file.getAbsolutePath());
        manager.mergeAndCommit(sourceBranch, targetBranch);

        System.out.println("SUCCESS !");
        System.out.println("Branch " + sourceBranch + " merged to " + targetBranch + "!");

        return 0;
    }

    public static void main(String... args) {
//        args = "-help".split(" ");
        int exitCode = new CommandLine(new Merge_Plugin()).execute(args);
        System.exit(exitCode);
    }
}