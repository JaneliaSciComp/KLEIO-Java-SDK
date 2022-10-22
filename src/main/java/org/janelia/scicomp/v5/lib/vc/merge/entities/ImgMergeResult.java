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

import java.util.ArrayList;
import java.util.List;

public class ImgMergeResult {

    public enum Case {
        NO_CONFLICT("Success: Merged without conflict"),
        CONFLICT_MERGED("Success: Index conflicts but fixed"),
        CONFLICT_NEED_MANUAL_SELECTION("ERROR: conflict need to be fixed manually");

        private final String message;

        public String getMessage() {
            return message;
        }

        Case(String message) {
            this.message = message;
        }
    }

    final List<BlockConflictEntry> conflicts;
    final Case result;
    public ImgMergeResult(List<BlockConflictEntry> conflicts, Case result) {
        this.conflicts = conflicts;
        this.result = result;
    }

    public ImgMergeResult() {
        this.result = null;
        this.conflicts = new ArrayList<>();
    }

    public ImgMergeResult(Case result) {
        this.result = result;
        this.conflicts = new ArrayList<>();
    }

    public List<BlockConflictEntry> getConflicts() {
        return conflicts;
    }

    public Case getResult() {
        return result;
    }

}


