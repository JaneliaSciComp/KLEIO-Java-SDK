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

package org.janelia.scicomp.v5;

import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.*;
import org.janelia.scicomp.v5.lib.V5Writer;
import org.janelia.scicomp.v5.lib.indexes.N5ZarrIndexWriter;
import org.janelia.scicomp.v5.lib.uri.V5URL;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AbstractV5Writer<I extends N5ZarrIndexWriter, K extends N5FSWriter> extends BasicV5Reader<I,K> implements V5Writer<I, K> {

    public AbstractV5Writer(I indexesWriter, K rawWriter, V5URL url) throws IOException {
        super(indexesWriter,rawWriter,url);
    }

    @Override
    public I getIndexWriter() {
        return indexes;
    }

    @Override
    public K getRawWriter() {
        return raw;
    }

    @Override
    public void createDataset(String pathName, DatasetAttributes datasetAttributes) throws IOException {
        CellGrid grid = new CellGrid(datasetAttributes.getDimensions(), datasetAttributes.getBlockSize());
        getIndexWriter().createDataset(pathName, grid.getGridDimensions());
        getRawWriter().createDataset(pathName, datasetAttributes);
    }

    @Override
    public void createDataset(String pathName, long[] dimensions, int[] blockSize, DataType dataType, Compression compression) throws IOException {
        CellGrid grid = new CellGrid(dimensions, blockSize);
        getIndexWriter().createDataset(pathName, grid.getGridDimensions());
        getRawWriter().createDataset(pathName, dimensions, blockSize, dataType, compression);
    }

    @Override
    public <T> void writeBlock(String pathName, DatasetAttributes datasetAttributes, DataBlock<T> dataBlock) throws IOException {
        long version = getIndexWriter().getCurrentSession().get();
        Path path = Paths.get(pathName, String.valueOf(version));
        getRawWriter().writeBlock(path.toString(), datasetAttributes, dataBlock);
        getIndexWriter().set(pathName, dataBlock.getGridPosition());
    }

    @Override
    public boolean deleteBlock(String pathName, long... gridPosition) throws IOException {

        boolean rawRemoved = getRawWriter().deleteBlock(pathName, gridPosition);
        getIndexWriter().set(pathName, gridPosition, new UnsignedLongType(0));
        if (rawRemoved)
            return true;
        return false;
    }

    @Override
    public boolean exists(String pathName) {
        return super.exists(pathName);
    }


}
