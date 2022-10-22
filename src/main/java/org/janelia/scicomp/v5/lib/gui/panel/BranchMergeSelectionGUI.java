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

package org.janelia.scicomp.v5.lib.gui.panel;

import org.janelia.scicomp.v5.lib.vc.merge.entities.MergeBranches;

import javax.swing.*;

public class BranchMergeSelectionGUI {

    private final String[] branches;

    public BranchMergeSelectionGUI(String[] branches) {
        this.branches = branches;

    }

    public static void main(String[] args) {
        String s1[] = {"Jalpaiguri", "Mumbai", "Noida", "Kolkata", "New Delhi"};
        MergeBranches mergeBranches = new BranchMergeSelectionGUI(s1).getBranches();
        System.out.println(mergeBranches);
    }

    public MergeBranches getBranches() {
        String sourceBranch = (String) JOptionPane.showInputDialog(
                null,
                "Select Source branch: ",
                "Source branch",
                JOptionPane.PLAIN_MESSAGE,
                null,
                branches,
                branches[branches.length - 1]);

        if (sourceBranch == null)
            return null;

        String targetBranch = (String) JOptionPane.showInputDialog(
                null,
                "Select Target branch: ",
                "Target branch",
                JOptionPane.PLAIN_MESSAGE,
                null,
                branches,
                branches[0]);

        if (targetBranch == null)
            return null;

        return new MergeBranches(sourceBranch, targetBranch);
    }
}

