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

package org.janelia.scicomp.kleio;

import net.imglib2.img.cell.CellGrid;
import org.janelia.saalfeldlab.n5.*;
import org.janelia.scicomp.kleio.indexes.KleioIndexWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class KleioWriter<I extends KleioIndexWriter, K extends GsonAttributesParser & N5Writer> extends KleioReader<I, K> implements N5Writer {

    public KleioWriter(I indexes, K raw) {
        super(indexes, raw);
    }


    public void commit() throws IOException {
        indexes.getVersionManager().commitBlocks();
        indexes.incrementSession();
    }

    public void push() throws IOException {
        indexes.getVersionManager().push();
    }

    public void createNewBranch(String branchName) throws IOException {
        indexes.getVersionManager().createNewBranch(branchName);
    }

    public void checkoutBranch(String branchName) throws IOException {
        indexes.getVersionManager().checkoutBranch(branchName);
    }

    public void SetUserID(String userID) {
        indexes.getVersionManager().setUserID(userID);
    }

    public String getUserID() {
        return indexes.getVersionManager().getUserID();
    }

    public String getCurrentBranch() throws IOException {
        return indexes.getVersionManager().getCurrentBranch();
    }


    @Override
    public <T> void writeBlock(String pathName, DatasetAttributes datasetAttributes, DataBlock<T> dataBlock) throws IOException {
        long version = indexes.getCurrentSession().get();
        Path path = Paths.get(pathName, String.valueOf(version));
        raw.writeBlock(path.toString(), datasetAttributes, dataBlock);
        indexes.set(pathName, dataBlock.getGridPosition());
    }


    @Override
    public void createGroup(String pathName) throws IOException {
        indexes.createGroup(pathName);
        raw.createGroup(pathName);
    }

    @Override
    public boolean deleteBlock(String s, long... longs) throws IOException {
        throw new IOException("Delete block is not implemented for versioned storage");
    }

    @Override
    public boolean remove() throws IOException {
        boolean indexRemoved = indexes.remove();
        boolean rawRemoved = raw.remove();
        if (indexRemoved && rawRemoved)
            return true;
        return false;
    }

    @Override
    public boolean remove(String s) throws IOException {
        boolean indexRemoved = indexes.remove(s);
        boolean rawRemoved = raw.remove(s);
        if (indexRemoved && rawRemoved)
            return true;
        return false;
    }

    @Override
    public void setDatasetAttributes(String pathName, DatasetAttributes datasetAttributes) throws IOException {
        raw.setDatasetAttributes(pathName, datasetAttributes);
    }

    @Override
    public <T> void setAttribute(String pathName, String key, T attribute) throws IOException {
        raw.setAttribute(pathName, key, attribute);
    }

    @Override
    public void setAttributes(String pathName, Map<String, ?> attributes) throws IOException {
        raw.setAttributes(pathName, attributes);
    }


    @Override
    public void createDataset(String pathName, DatasetAttributes datasetAttributes) throws IOException {
        CellGrid grid = new CellGrid(datasetAttributes.getDimensions(), datasetAttributes.getBlockSize());
        indexes.createDataset(pathName, grid.getGridDimensions());
        raw.createDataset(pathName, datasetAttributes);
    }

    @Override
    public void createDataset(String pathName, long[] dimensions, int[] blockSize, DataType dataType, Compression compression) throws IOException {
        CellGrid grid = new CellGrid(dimensions, blockSize);
        indexes.createDataset(pathName, grid.getGridDimensions());
        raw.createDataset(pathName, dimensions, blockSize, dataType, compression);
    }

}