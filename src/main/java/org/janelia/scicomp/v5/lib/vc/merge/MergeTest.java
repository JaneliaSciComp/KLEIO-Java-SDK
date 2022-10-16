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

import org.eclipse.jgit.api.errors.GitAPIException;
import org.janelia.scicomp.v5.fs.V5FSWriter;
import org.janelia.scicomp.v5.lib.tools.Utils;

import java.io.IOException;
import java.util.Map;

public class MergeTest {
    public static void main(String[] args) throws IOException, GitAPIException {
        String indexPath = "/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/annotation.v5/versionedIndex";
        String kvPath = "/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/annotation.v5/datastore";

        V5FSWriter writer = new V5FSWriter(indexPath, kvPath);
        String[] branches = writer.getBranches();
        for (int i = 0; i < branches.length; i++) {
            for (int j = i; j < branches.length; j++) {
                String b1 = branches[i];
                String b2 = branches[j];
                if (!b1.equals(b2)) {
                    System.out.println(b1 + " vs " + b2);
                    Map<String, int[][]> conflicts = new BranchesMergeManager(writer.getIndexWriter().getBasePath()).getConflicts(b1, b2);
                    printConflict(conflicts);
                }
            }
        }


    }

    private static void printConflict(Map<String, int[][]> conflicts) {
        if (conflicts == null)
        {
            System.out.println("No conflicts");
            return;
        }
        for (String k : conflicts.keySet()) {
            int[][] v = conflicts.get(k);
            System.out.println("File : " + k);
            Utils.print2D(v);
        }
    }
}
