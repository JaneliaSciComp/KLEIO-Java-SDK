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

package org.janelia.scicomp.kleio.tools;

import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.Compression;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;

import java.io.IOException;
import java.util.List;

public class MultiScaleZarr {
    private final static int[] indexMatrixBlockSize = new int[]{64, 64, 64};
    private final static DataType indexMatrixDataType = DataType.UINT64;
    private final static Compression indexMatrixCompression = new GzipCompression();
    private final String path;

    public MultiScaleZarr(String path) {
        this.path = path;
    }


//TODO
//    public static void main(String[] args) throws IOException {
//        String testDirPath = "/Users/zouinkhim/Desktop/active_learning/tmp/test_zarr";
//        String n5path = "/Users/zouinkhim/Downloads/grid-3d-stitched-h5/dataset.n5";
//        List<MultiscaleAttributes> atts = MultiscaleAttributes.generateFromN5(n5path, "setup0/timepoint0/");
//
//        for (MultiscaleAttributes t : atts) {
//            System.out.println(t);
//        }
//        MultiScaleZarr multiscaleZarr = new MultiScaleZarr(testDirPath);
//        multiscaleZarr.create(atts);
//
//        multiscaleZarr.write(atts.get(0).getDataset(), new long[]{0, 1, 2}, new UnsignedLongType(4));
//    }

    public void write(String dataset, long[] position, UnsignedLongType value) throws IOException {
        N5ZarrWriter writer = new N5ZarrWriter(path);
        CachedCellImg<UnsignedLongType, ?> img = N5Utils.open(writer, dataset);
        UnsignedLongType p = img.getAt(position);
        p.set(value);
        DatasetAttributes attrs = writer.getDatasetAttributes(dataset);
        N5Utils.save(img, writer, dataset, attrs.getBlockSize(), attrs.getCompression());
    }

    public void create(List<MultiscaleAttributes> atts) throws IOException {
        N5ZarrWriter n5 = new N5ZarrWriter(path);
        for (MultiscaleAttributes att : atts)
            create(n5, att);
        n5.close();
    }

    public void create(N5ZarrWriter n5, MultiscaleAttributes att) throws IOException {
        n5.createDataset(att.getDataset(), att.getGridSize(), indexMatrixBlockSize, indexMatrixDataType, indexMatrixCompression);

    }
}
