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

package org.janelia.scicomp.v5.lib;

import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.scicomp.v5.lib.indexes.V5IndexWriter;

import java.io.IOException;
import java.util.Map;

//TODO extends V5Reader<I, K>
public interface V5Writer<I extends V5IndexWriter, K extends N5Writer> extends N5Writer {

    I getIndexWriter();

    K getRawWriter();

    default void commit() throws IOException {
        getIndexWriter().getVersionManager().commitBlocks();
    }

    default void createNewBranch(String branchName) throws IOException {
        getIndexWriter().getVersionManager().createNewBranch(branchName);
    }

    default String[] getBranches() throws IOException {
        return getIndexWriter().getVersionManager().getBranches();
    }

    default void checkoutBranch(String branchName) throws IOException {
        getIndexWriter().getVersionManager().checkoutBranch(branchName);
    }

    default void SetUserID(String userID) {
        getIndexWriter().getVersionManager().setUserID(userID);
    }

    default String getUserID() {
        return getIndexWriter().getVersionManager().getUserID();
    }

    default String getCurrentBranch() throws IOException {
        return getIndexWriter().getVersionManager().getCurrentBranch();
    }

    default UnsignedLongType incrementSession() throws IOException {
        return getIndexWriter().incrementSession();
    }

    default UnsignedLongType getCurrentSession() throws IOException {
        return getIndexWriter().getCurrentSession();
    }


    @Override
    default void createGroup(String pathName) throws IOException {
        getIndexWriter().createGroup(pathName);
        getRawWriter().createGroup(pathName);
    }

    @Override
    default boolean deleteBlock(String s, long... longs) throws IOException {
        throw new IOException("Delete block is not implemented for versioned storage");
    }

    @Override
    default boolean remove() throws IOException {
        boolean indexRemoved = getIndexWriter().remove();
        boolean rawRemoved = getRawWriter().remove();
        if (indexRemoved && rawRemoved)
            return true;
        return false;
    }

    @Override
    default boolean remove(String s) throws IOException {
        boolean indexRemoved = getIndexWriter().remove(s);
        boolean rawRemoved = getRawWriter().remove(s);
        if (indexRemoved && rawRemoved)
            return true;
        return false;
    }

    @Override
    default void setDatasetAttributes(String pathName, DatasetAttributes datasetAttributes) throws IOException {
        getRawWriter().setDatasetAttributes(pathName, datasetAttributes);
    }

    @Override
    default <T> void setAttribute(String pathName, String key, T attribute) throws IOException {
        getRawWriter().setAttribute(pathName, key, attribute);
    }

    @Override
    default boolean exists(String pathName) {
        return getRawWriter().exists(pathName);
    }

    @Override
    default void setAttributes(String pathName, Map<String, ?> attributes) throws IOException {
        getRawWriter().setAttributes(pathName, attributes);
    }

}