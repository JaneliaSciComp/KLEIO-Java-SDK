/**
 * Copyright (c) 2017, Stephan Saalfeld
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.janelia.scicomp.v5;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.janelia.saalfeldlab.n5.*;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.scicomp.lib.MultiscaleAttributes;
import org.janelia.scicomp.lib.SessionId;
import org.janelia.scicomp.lib.VersionedDirectory;
import org.janelia.scicomp.lib.tools.Utils;
import org.janelia.scicomp.v5.index.IndexN5ZarrWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.stream.Stream;

/**
 * Versioned Store-based N5 implementation.
 *
 * @author Marwan Zouinkhi
 */
public class VersionedN5Writer extends VersionedN5Reader implements N5Writer {
    private final IndexN5ZarrWriter indexStore;
    private final VersionedDirectory versionedDirectory;
    private UnsignedLongType session;
    private ArrayList<long[]> uncommittedBlocks;

    private String userID;

    //    public VersionedN5Writer(String basePath) throws IOException {
//        this(basePath, true);
//    }
    public VersionedN5Writer(String versionIndexPath, String dataStorePath) throws IOException {
        this(versionIndexPath, dataStorePath, new GsonBuilder());
    }

    public VersionedN5Writer(String versionIndexPath, String dataStorePath, GsonBuilder gsonBuilder) throws IOException {
        super(versionIndexPath, dataStorePath, gsonBuilder);
        new IndexN5ZarrWriter(versionIndexPath);
        createDirectories(Paths.get(dataStorePath));
        this.uncommittedBlocks = new ArrayList<>();

        this.indexStore = new IndexN5ZarrWriter(versionIndexPath);
        this.versionedDirectory = VersionedDirectory.open(versionIndexPath);
        if (!VERSION.equals(this.getVersion())) {
            this.setAttribute("/", "n5", VERSION.toString());
            this.setAttribute("/", "versioned", "true");
//            this.setAttribute("/", "master", "true");
        }
        System.out.println("done..");
    }

    public VersionedN5Writer(URI uri) throws IOException {
        this(new V5URI(uri).getVersionedIndexPath(), new V5URI(uri).getDataStorePath());
    }

//    public static VersionedN5Writer openMaster(String basePath) throws IOException {
//        return new VersionedN5Writer(basePath, false);
//    }

//    public static VersionedN5Writer createMaster(String basePath) throws IOException {
//        return new VersionedN5Writer(basePath, true);
//    }

//    public static VersionedN5Writer openCloned(String remotePath, String localPath) throws IOException {
//        return new VersionedN5Writer(Paths.get(remotePath, KV_STORE).toString(), localPath);
//    }

    public static VersionedN5Writer cloneFrom(String remoteIndexPath, String localIndexPath, String dataStorePath, String username) throws IOException, GitAPIException {
        VersionedDirectory versionedDirectory = VersionedDirectory.cloneFrom(remoteIndexPath, localIndexPath, username);
        versionedDirectory.checkout(username, true);
        return new VersionedN5Writer(localIndexPath, dataStorePath);
    }

    public static VersionedN5Writer convert(N5FSWriter reader, String dataset, String indexPath, String dataStorePath) throws IOException, GitAPIException {

        VersionedN5Writer writer = new VersionedN5Writer(indexPath, dataStorePath);
//        create n5 dataset
        List<MultiscaleAttributes> atts = MultiscaleAttributes.generateFromN5(reader, dataset);
        String outputDataset;
        for (MultiscaleAttributes attributes : atts) {
            outputDataset = attributes.getDataset();
            String inputDataset = Paths.get(dataset, outputDataset).toString();
            System.out.println("Creating : " + outputDataset);
            DatasetAttributes datasetAttributes = reader.getDatasetAttributes(inputDataset);
            writer.createDataset(outputDataset, datasetAttributes);
            //Write blocks
            CachedCellImg<UnsignedLongType, ?> canvas = N5Utils.open(reader, inputDataset);
            long[] gridDimensions = canvas.getCellGrid().getGridDimensions();
            System.out.println("Blocks: " + Arrays.toString(gridDimensions));
            if (gridDimensions.length != 3) {
                throw new InvalidPropertiesFormatException("Grid dimension :" + gridDimensions.length + " not implemented yet!");
            }
            for (int i = 0; i < gridDimensions[0]; ++i) {
                for (int j = 0; j < gridDimensions[1]; ++j) {
                    for (int k = 0; k < gridDimensions[2]; ++k) {

                        long[] gridPosition = new long[]{i, j, k};
                        System.out.println("Writing: " + Arrays.toString(gridPosition));
                        DataBlock<?> block = reader.readBlock(inputDataset, datasetAttributes, gridPosition);
                        writer.writeBlock(outputDataset, datasetAttributes, block);
                    }
                }
            }
        }
        writer.commit();
        return writer;
    }


    @Override
    public void createDataset(String pathName, DatasetAttributes datasetAttributes) throws IOException {
        N5Writer.super.createDataset(pathName, datasetAttributes);
        CellGrid grid = new CellGrid(datasetAttributes.getDimensions(), datasetAttributes.getBlockSize());
        indexStore.createDataset(pathName, grid.getGridDimensions());
    }

    public void createGroup(String pathName) throws IOException {
        Path path = Paths.get(this.dataStorePath, pathName);
        createDirectories(path);
    }

    public void setAttributes(String pathName, Map<String, ?> attributes) throws IOException {
//        Path path = Paths.get(this.kv_directory, getAttributesPath(pathName).toString());
        Path path = getAttributesPath(pathName);
        HashMap<String, JsonElement> map = new HashMap();
        VersionedN5Reader.LockedFileChannel lockedFileChannel = LockedFileChannel.openForWriting(path);
        Throwable var6 = null;

        try {
            map.putAll(GsonAttributesParser.readAttributes(Channels.newReader(lockedFileChannel.getFileChannel(), StandardCharsets.UTF_8.name()), this.getGson()));
            GsonAttributesParser.insertAttributes(map, attributes, this.gson);
            lockedFileChannel.getFileChannel().truncate(0L);
            GsonAttributesParser.writeAttributes(Channels.newWriter(lockedFileChannel.getFileChannel(), StandardCharsets.UTF_8.name()), map, this.getGson());
        } catch (Throwable var15) {
            var6 = var15;
            throw var15;
        } finally {
            if (lockedFileChannel != null) {
                if (var6 != null) {
                    try {
                        lockedFileChannel.close();
                    } catch (Throwable var14) {
                        var6.addSuppressed(var14);
                    }
                } else {
                    lockedFileChannel.close();
                }
            }
        }
    }

    public <T> void writeBlock(String pathName, DatasetAttributes datasetAttributes, DataBlock<T> dataBlock) throws IOException {
        uncommittedBlocks.add(dataBlock.getGridPosition());
        long version = getCurrentSession().get();
        Path path = Paths.get(this.dataStorePath, getDataBlockPath(pathName, version, dataBlock.getGridPosition()).toString());
        createDirectories(path.getParent());
        VersionedN5Reader.LockedFileChannel lockedChannel = LockedFileChannel.openForWriting(path);
        Throwable var6 = null;

        try {
            lockedChannel.getFileChannel().truncate(0L);
            DefaultBlockWriter.writeBlock(Channels.newOutputStream(lockedChannel.getFileChannel()), datasetAttributes, dataBlock);
        } catch (Throwable var15) {
            var6 = var15;
            throw var15;
        } finally {
            if (lockedChannel != null) {
                if (var6 != null) {
                    try {
                        lockedChannel.close();
                    } catch (Throwable var14) {
                        var6.addSuppressed(var14);
                    }
                } else {
                    lockedChannel.close();
                }
            }

        }
        // Modify Zarr index
        updateBlockIndex(pathName, dataBlock.getGridPosition(), version);
    }

    private void updateBlockIndex(String dataset, long[] gridPosition, long version) throws IOException {
        indexStore.set(dataset, gridPosition, version);
    }

    private UnsignedLongType getCurrentSession() {
        if (this.session == null)
            this.session = SessionId.get();
        return this.session;
    }

    public UnsignedLongType incrementSession() {
        this.session = SessionId.incrementAndGet();
        return session;
    }

    public boolean remove() throws IOException {
        return this.remove("/");
    }

    public boolean remove(String pathName) throws IOException {
        Path path = Paths.get(this.dataStorePath, pathName);
        if (Files.exists(path, new LinkOption[0])) {
            Stream<Path> pathStream = Files.walk(path);
            Throwable var4 = null;

            try {
                pathStream.sorted(Comparator.reverseOrder()).forEach((childPath) -> {
                    if (Files.isRegularFile(childPath, new LinkOption[0])) {
                        try {
                            VersionedN5Reader.LockedFileChannel channel = LockedFileChannel.openForWriting(childPath);
                            Throwable var2 = null;

                            try {
                                Files.delete(childPath);
                            } catch (Throwable var20) {
                                var2 = var20;
                                throw var20;
                            } finally {
                                if (channel != null) {
                                    if (var2 != null) {
                                        try {
                                            channel.close();
                                        } catch (Throwable var15) {
                                            var2.addSuppressed(var15);
                                        }
                                    } else {
                                        channel.close();
                                    }
                                }

                            }
                        } catch (IOException var22) {
                            var22.printStackTrace();
                        }
                    } else {
                        try {
                            Files.delete(childPath);
                        } catch (DirectoryNotEmptyException var18) {
                            try {
                                Thread.sleep(100L);
                                Files.delete(childPath);
                            } catch (InterruptedException var16) {
                                var18.printStackTrace();
                                Thread.currentThread().interrupt();
                            } catch (IOException var17) {
                                var17.printStackTrace();
                            }
                        } catch (IOException var19) {
                            var19.printStackTrace();
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

        return !Files.exists(path, new LinkOption[0]);
    }

    public boolean deleteBlock(String pathName, long... gridPosition) throws IOException {
        long version = getBlockVersion(pathName, gridPosition);
        Path path = Paths.get(this.dataStorePath, getDataBlockPath(pathName, version, gridPosition).toString());
        if (Files.exists(path, new LinkOption[0])) {
            VersionedN5Reader.LockedFileChannel channel = LockedFileChannel.openForWriting(path);
            Throwable var5 = null;

            try {
                Files.deleteIfExists(path);
            } catch (Throwable var14) {
                var5 = var14;
                throw var14;
            } finally {
                if (channel != null) {
                    if (var5 != null) {
                        try {
                            channel.close();
                        } catch (Throwable var13) {
                            var5.addSuppressed(var13);
                        }
                    } else {
                        channel.close();
                    }
                }

            }
        }

        return !Files.exists(path, new LinkOption[0]);
    }

    private static Path createDirectories(Path dir, FileAttribute<?>... attrs) throws IOException {
        try {

            createAndCheckIsDirectory(dir, attrs);
            return dir;
        } catch (FileAlreadyExistsException var9) {
            throw var9;
        } catch (IOException var10) {
            SecurityException se = null;

            try {
                dir = dir.toAbsolutePath();
            } catch (SecurityException var7) {
                se = var7;
            }

            Path parent = dir.getParent();

            while (parent != null) {
                try {
                    parent.getFileSystem().provider().checkAccess(parent, new AccessMode[0]);
                    break;
                } catch (NoSuchFileException var8) {
                    parent = parent.getParent();
                }
            }

            if (parent == null) {
                if (se == null) {
                    throw new FileSystemException(dir.toString(), (String) null, "Unable to determine if root directory exists");
                } else {
                    throw se;
                }
            } else {
                Path child = parent;
                Iterator var5 = parent.relativize(dir).iterator();

                while (var5.hasNext()) {
                    Path name = (Path) var5.next();
                    child = child.resolve(name);
                    createAndCheckIsDirectory(child, attrs);
                }

                return dir;
            }
        }
    }

    private static void createAndCheckIsDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        try {
            Files.createDirectory(dir, attrs);
        } catch (FileAlreadyExistsException var3) {
            if (!Files.isDirectory(dir, new LinkOption[0])) {
                throw var3;
            }
        }
    }

    public void commit() throws GitAPIException {
        String message = Utils.format(uncommittedBlocks);
        //TODO add only uncommitted
        versionedDirectory.addAll();
        versionedDirectory.commit(message);
    }

    public void checkoutNewBranch(String name) throws GitAPIException {
        versionedDirectory.checkout(name, true);
    }

    public void checkoutBranch(String name, boolean create) throws GitAPIException {
        versionedDirectory.checkout(name, create);
    }

    public String getUserID() {
        return userID;
    }

    public String getCurrentBranch() throws IOException {
       return versionedDirectory.getCurrentBranch();
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        String n5_read = "/Users/zouinkhim/Downloads/car/dataset.n5";
        String dataset = "setup0/timepoint0";
        String indexes = "/Users/zouinkhim/Desktop/active_learning/versioned_data/data.v5/indexes";
        String dataStore = "/Users/zouinkhim/Desktop/active_learning/versioned_data/data.v5/dataStore";
        N5FSWriter reader = new N5FSWriter(n5_read);
        VersionedN5Writer writer = VersionedN5Writer.convert(reader, dataset, indexes, dataStore);
        System.out.println(writer.getCurrentBranch());
        writer.setUserID("zouinkhim");
        writer.commit();
    }

    @Override
    public String toString() {
        return "VersionedN5Writer{" +
                "indexStore=" + indexStore +
                ", dataStorePath=" + dataStorePath +
                ", currentDataset=" + currentDataset +
                ", session=" + session +
                ", uncommittedBlocks=" + uncommittedBlocks +
                ", userID='" + userID + '\'' +
                '}';
    }
}
