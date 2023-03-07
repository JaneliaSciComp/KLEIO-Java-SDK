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

package org.janelia.scicomp.kleio.indexes;

import com.google.gson.GsonBuilder;
import net.imglib2.FinalInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.view.Views;
import org.janelia.saalfeldlab.n5.Compression;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.N5FSReader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.scicomp.kleio.vc.GitVersionManger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class KleioZarrFSIndexWriter extends N5ZarrWriter implements KleioIndexWriter<GitVersionManger> {
    protected UnsignedLongType session;

    private final static int[] indexMatrixBlockSize = new int[]{64, 64, 64};
    private final static DataType indexMatrixDataType = DataType.UINT64;
    private final static Compression indexMatrixCompression = new GzipCompression();

    protected final GitVersionManger versionManger;

    public KleioZarrFSIndexWriter(String basePath, GsonBuilder gsonBuilder, String dimensionSeparator, boolean mapN5DatasetAttributes) throws IOException {
        super(basePath, gsonBuilder, dimensionSeparator, mapN5DatasetAttributes);
        this.versionManger = new GitVersionManger(basePath);
        getVersionManager().commitAll("initial");
    }

    public KleioZarrFSIndexWriter(String basePath, GsonBuilder gsonBuilder, String dimensionSeparator) throws IOException {
        this(basePath, gsonBuilder, dimensionSeparator, true);
    }

    public KleioZarrFSIndexWriter(String basePath, String dimensionSeparator, boolean mapN5DatasetAttributes) throws IOException {
        this(basePath, new GsonBuilder(), dimensionSeparator, mapN5DatasetAttributes);
    }

    public KleioZarrFSIndexWriter(String basePath, boolean mapN5DatasetAttributes) throws IOException {
        this(basePath, new GsonBuilder(), ".", mapN5DatasetAttributes);
    }

    public KleioZarrFSIndexWriter(String basePath, GsonBuilder gsonBuilder) throws IOException {
        this(basePath, gsonBuilder, ".");
    }

    public KleioZarrFSIndexWriter(String basePath) throws IOException {
        this(basePath, new GsonBuilder());
    }

    @Override
    public GitVersionManger getVersionManager() {
        return versionManger;
    }

    @Override
    public UnsignedLongType getSession() {
        return session;
    }

    @Override
    public void setSession(UnsignedLongType session) {
        this.session = session;
    }

    //TODO optimize this change to read block write block
    public void set(String dataset, long[] gridPosition, UnsignedLongType value) throws IOException {

        CachedCellImg<UnsignedLongType, ?> img = N5Utils.open(this, dataset);
        UnsignedLongType p = img.getAt(gridPosition);
        p.set(value);
//        DatasetAttributes attrs = this.getDatasetAttributes(dataset);
        try {
            N5Utils.saveRegion(Views.interval(img, new FinalInterval(gridPosition, new long[]{1, 1, 1})), this, dataset);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        getVersionManager().addUncommittedBlock(gridPosition);

//        N5Utils.save(img, this, dataset, attrs.getBlockSize(), attrs.getCompression());
    }


    @Override
    public void set(String pathName, long[] gridPosition) throws IOException {
        this.set(pathName, gridPosition, getCurrentSession());
    }

    @Override
    public void createDataset(String pathName, long[] dimensions, int[] blockSize, DataType dataType, Compression compression) throws IOException {
        super.createDataset(pathName, dimensions, blockSize, dataType, compression);
        getVersionManager().commitAll("create dataset: " + pathName);
    }

    // overwrite to not remove .git folder and add filter ".git"
    @Override
    public boolean remove(String pathName) throws IOException {
        Path path = Paths.get(this.basePath, pathName);
        if (Files.exists(path, new LinkOption[0])) {
            Stream<Path> pathStream = Files.walk(path).filter(f -> !(f.toString().contains(".git") || f.toString().equals(this.basePath)));
            Throwable var4 = null;

            try {
                pathStream.sorted(Comparator.reverseOrder()).forEach((childPath) -> {
                    if (Files.isRegularFile(childPath, new LinkOption[0])) {
                        try {
                            N5FSReader.LockedFileChannel channel = LockedFileChannel.openForWriting(childPath);
                            Throwable var2 = null;

                            try {
                                Files.delete(childPath);
                            } catch (Throwable var14) {
                                var2 = var14;
                                throw var14;
                            } finally {
                                if (channel != null) {
                                    if (var2 != null) {
                                        try {
                                            channel.close();
                                        } catch (Throwable var12) {
                                            var2.addSuppressed(var12);
                                        }
                                    } else {
                                        channel.close();
                                    }
                                }

                            }
                        } catch (IOException var16) {
                            var16.printStackTrace();
                        }
                    } else {
                        try {
                            Files.delete(childPath);
                        } catch (IOException var13) {
                            var13.printStackTrace();
                        }
                    }

                });
            } catch (Throwable var13) {
                var4 = var13;
                throw var13;
            } finally {
                if (pathStream != null) {
                    if (var4 != null) {
                        try {
                            pathStream.close();
                        } catch (Throwable var12) {
                            var4.addSuppressed(var12);
                        }
                    } else {
                        pathStream.close();
                    }
                }

            }
        }

        getVersionManager().commitAll("remove: " + pathName);
//        boolean result = !Files.exists(path, new LinkOption[0]);
//        System.out.println("Remove : "+pathName + " -> "+result);
        return true;
    }

    @Override
    public boolean remove() throws IOException {
        return this.remove("/");
    }

    @Override
    public void createGroup(String pathName) throws IOException {
        super.createGroup(pathName);
        getVersionManager().commitAll("create group: " + pathName);
    }

    @Override
    public void createDataset(String pathName, long[] gridDimensions) throws IOException {
        this.createDataset(pathName, gridDimensions, indexMatrixBlockSize, indexMatrixDataType, indexMatrixCompression);
    }


}
