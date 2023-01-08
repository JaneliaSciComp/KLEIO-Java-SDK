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

package org.janelia.scicomp.v5.lib.tools;

import org.janelia.saalfeldlab.n5.*;
import org.janelia.saalfeldlab.n5.ij.N5Importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class MultiscaleAttributes {
    final String dataset;
    final long[] dimensions;
    final int[] blockSize;
    final long[] gridSize;

    public MultiscaleAttributes(String dataset, long[] dimensions, int[] blockSize) {
        this.dataset = dataset;
        this.dimensions = dimensions;
        this.blockSize = blockSize;
        this.gridSize = getGridSize(dimensions, blockSize);
    }

    public static long[] getGridSize(long[] dimensions, int[] blockSize) {
        long[] result = new long[dimensions.length];
        for (int i = 0; i < dimensions.length; i++) {
            int x = (int) (dimensions[i] / blockSize[i]);
            if ((long) blockSize[i] * x < dimensions[i])
                x++;
            result[i] = x;
        }
        return result;
    }

    public static List<MultiscaleAttributes> generateFromN5(String n5path, String dataset) throws IOException {
        N5Reader reader = new N5FSReader(n5path);
        return generateFromN5(reader, dataset);
//        return generateFromN5(new N5Factory().openReader(n5path), dataset);
    }

    public static List<MultiscaleAttributes> generateFromN5(N5Reader n5, String dataset) throws IOException {
        List<MultiscaleAttributes> result = new ArrayList<>();

        N5DatasetDiscoverer parsers = new N5DatasetDiscoverer(n5,
                Executors.newSingleThreadExecutor(),
                Arrays.asList(N5Importer.GROUP_PARSERS),
                Arrays.asList(N5Importer.PARSERS));

        //TODO fix doesn't discover if main path is dataset
        N5TreeNode root = parsers.discoverAndParseRecursive(dataset);
//        if(root.isDataset())
//            result.add(new MultiscaleAttributes(dataset,t.getMetadata().getAttributes().getDimensions(), t.getMetadata().getAttributes().getBlockSize()))

        for (N5TreeNode t : root.childrenList()) {
            DatasetAttributes datasetAttributes = n5.getDatasetAttributes(t.getPath());
            result.add(new MultiscaleAttributes(t.getPath(), datasetAttributes.getDimensions(), datasetAttributes.getBlockSize()));
        }
        return result;
    }


    @Override
    public String toString() {
        return "MultiscaleAttributes{" +
                "dataset='" + dataset + '\'' +
                ", dimensions=" + Arrays.toString(dimensions) +
                ", blockSize=" + Arrays.toString(blockSize) +
                ", gridSize=" + Arrays.toString(gridSize) +
                '}';
    }

    public String getDataset() {
        return dataset;
    }

    public long[] getDimensions() {
        return dimensions;
    }

    public int[] getBlockSize() {
        return blockSize;
    }

    public long[] getGridSize() {
        return gridSize;
    }

    public static void main(String[] args) {
        long[] grid = getGridSize(new long[]{600, 600, 600}, new int[]{128, 128, 128});
        for (long i : grid)
            System.out.print(i + " ");

    }
}
