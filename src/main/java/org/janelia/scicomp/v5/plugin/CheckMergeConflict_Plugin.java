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
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "check_merge_conflict", mixinStandardHelpOptions = true, version = "1.0",
        description = "Check if merge is possible or potential conflict")
class CheckMergeConflict_Plugin implements Callable<Integer> {

    @Option(names = {"-f", "--file"}, required = true, description = "Index matrix Path")
    private File file;

    @Option(names = {"-s", "--source_branch"}, required = true, description = "Source branch to merge")
    private String sourceBranch;

    @Option(names = {"-t", "--target_branch"}, required = true, description = "Target branch to merge")
    private String targetBranch;

    @Override
    public Integer call() throws Exception {
        BranchesMergeManager manager = new BranchesMergeManager(file.getAbsolutePath());
        Map<String, int[][]> conflicts = manager.getConflicts(sourceBranch, targetBranch);
        if (conflicts == null) {
            System.out.println("No conflicts, good to go..");
            return 0;
        }
        System.out.println("WARN: There is " + conflicts.keySet().size() + " conflicts : ");
        for (String k : conflicts.keySet()) {
            System.out.println("- " + k);
        }
        System.out.println("Can't be merged !");

        return 0;
    }

    public static void main(String... args) {
//        args = "-help".split(" ");
        int exitCode = new CommandLine(new CheckMergeConflict_Plugin()).execute(args);
        System.exit(exitCode);
    }
}