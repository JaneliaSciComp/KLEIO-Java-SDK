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

import org.janelia.scicomp.v5.lib.tools.Utils;

public class BlockConflictEntry {
    private final long[] gridPosition;
    private final long[] conflicts;
    private final boolean merged;

    // 0 Nothing selected | 1 Branch Source | 2 Branch target
    private int selectedBranch = 0;

    public void setSelectedBranch(int selectedBranch) {
        this.selectedBranch = selectedBranch;
    }

    public int getSelectedBranch() {
        return selectedBranch;
    }

    public String getBranchString() {
        switch (selectedBranch) {
            case 0:
                return "_";
            case 1:
                return "S";
            case 2:
                return "T";
            default:
                throw new RuntimeException("ERROR! Invalid Branch " + selectedBranch);
        }
    }

    public void getNextBranch() {
        if (selectedBranch == 2)
            selectedBranch = 0;
        else selectedBranch = selectedBranch + 1;
    }

    public BlockConflictEntry(long[] gridPosition, long[] conflicts) {
        this.gridPosition = gridPosition;
        this.conflicts = conflicts;
        this.merged = false;
    }

    public BlockConflictEntry(long[] gridPosition) {
        this.gridPosition = gridPosition;
        this.conflicts = null;
        this.merged = true;
    }

    public BlockConflictEntry(long[] gridPosition, long[] conflicts, boolean merged) {
        this.gridPosition = gridPosition;
        this.conflicts = conflicts;
        this.merged = merged;
    }

    public boolean isMerged() {
        return merged;
    }

    public long[] getGridPosition() {
        return gridPosition;
    }

    public String getStringGridPosition(){
        return Utils.format(gridPosition);
    }

    public long[] getConflicts() {
        return conflicts;
    }
}
