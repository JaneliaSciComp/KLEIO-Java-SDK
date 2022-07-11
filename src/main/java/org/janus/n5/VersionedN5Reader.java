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
package org.janus.n5;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.real.FloatType;
import org.janelia.saalfeldlab.n5.*;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrReader;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * Versioned Store-based N5 implementation.
 *
 * @author Marwan Zouinkhi
 */
public class VersionedN5Reader extends AbstractGsonReader {
    protected static final String jsonFile = "attributes.json";
    final static String INDEXES_STORE = "indexes";
    final static String KV_STORE = "kv_store";
    protected final String basePath;

    public VersionedN5Reader(String basePath, GsonBuilder gsonBuilder) throws IOException {
        super(gsonBuilder);
        this.basePath = basePath;
        if (this.exists("/")) {
            N5Reader.Version version = this.getVersion();
            if (!VERSION.isCompatible(version)) {
                throw new IOException("Incompatible version " + version + " (this is " + VERSION + ").");
            }
        }

    }

    public VersionedN5Reader(String basePath) throws IOException {
        this(basePath, new GsonBuilder());
    }

    public String getBasePath() {
        return this.basePath;
    }

    public boolean exists(String pathName) {
        Path path = Paths.get(this.basePath, pathName);
        return Files.exists(path, new LinkOption[0]) && Files.isDirectory(path, new LinkOption[0]);
    }

    public HashMap<String, JsonElement> getAttributes(String pathName) throws IOException {
        Path path = Paths.get(this.basePath, getAttributesPath(pathName).toString());
        if (this.exists(pathName) && !Files.exists(path, new LinkOption[0])) {
            return new HashMap();
        } else {
            VersionedN5Reader.LockedFileChannel lockedFileChannel = VersionedN5Reader.LockedFileChannel.openForReading(path);
            Throwable var4 = null;

            HashMap var5;
            try {
                var5 = GsonAttributesParser.readAttributes(Channels.newReader(lockedFileChannel.getFileChannel(), StandardCharsets.UTF_8.name()), this.getGson());
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (lockedFileChannel != null) {
                    if (var4 != null) {
                        try {
                            lockedFileChannel.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        lockedFileChannel.close();
                    }
                }

            }

            return var5;
        }
    }

    public DataBlock<?> readBlock(String pathName, DatasetAttributes datasetAttributes, long... gridPosition) throws IOException {
        long version = getBlockVersion(pathName, gridPosition);
//        if(version==0) {
//            System.out.println("No block data");
//            return null;
//        }
        String blockPath = getDataBlockPath(pathName, version, gridPosition).toString();

        Path path = Paths.get(this.basePath, KV_STORE, blockPath);
        if (!Files.exists(path, new LinkOption[0])) {
            System.out.println("block not found");
            return null;
        } else {
            VersionedN5Reader.LockedFileChannel lockedChannel = VersionedN5Reader.LockedFileChannel.openForReading(path);
            Throwable var6 = null;

            DataBlock var7;
            try {
                var7 = DefaultBlockReader.readBlock(Channels.newInputStream(lockedChannel.getFileChannel()), datasetAttributes, gridPosition);
            } catch (Throwable var16) {
                var6 = var16;
                throw var16;
            } finally {
                if (lockedChannel != null) {
                    if (var6 != null) {
                        try {
                            lockedChannel.close();
                        } catch (Throwable var15) {
                            var6.addSuppressed(var15);
                        }
                    } else {
                        lockedChannel.close();
                    }
                }

            }

            return var7;
        }
    }

    long getBlockVersion(String dataset, long[] gridPosition) throws IOException {
        Path path = Paths.get(this.basePath, INDEXES_STORE);
        N5ZarrReader reader = new N5ZarrReader(path.toString());
        CachedCellImg<UnsignedLongType, ?> img = N5Utils.open(reader, dataset);
        UnsignedLongType p = img.getAt(gridPosition);
        return p.get();
    }

    public String[] list(String pathName) throws IOException {
        Path path = Paths.get(this.basePath, pathName);
        Stream<Path> pathStream = Files.list(path);
        Throwable var4 = null;

        String[] var5;
        try {
            var5 = (String[]) pathStream.filter((a) -> {
                return Files.isDirectory(a, new LinkOption[0]);
            }).map((a) -> {
                return path.relativize(a).toString();
            }).toArray((n) -> {
                return new String[n];
            });
        } catch (Throwable var14) {
            var4 = var14;
            throw var14;
        } finally {
            if (pathStream != null) {
                if (var4 != null) {
                    try {
                        pathStream.close();
                    } catch (Throwable var13) {
                        var4.addSuppressed(var13);
                    }
                } else {
                    pathStream.close();
                }
            }

        }

        return var5;
    }

    protected static Path getDataBlockPath(String datasetPathName, long version, long... gridPosition) {
        String[] pathComponents = new String[gridPosition.length + 1];
        int i;
        for (i = 0; i < pathComponents.length - 1; ++i) {
            pathComponents[i] = Long.toString(gridPosition[i]);
        }
        pathComponents[i] = Long.toString(version);


        return Paths.get(removeLeadingSlash(datasetPathName), pathComponents);
    }

    protected static Path getAttributesPath(String pathName) {
        return Paths.get(KV_STORE, removeLeadingSlash(pathName), "attributes.json");
    }

    protected static String removeLeadingSlash(String pathName) {
        return !pathName.startsWith("/") && !pathName.startsWith("\\") ? pathName : pathName.substring(1);
    }

    public String toString() {
        return String.format("%s[basePath=%s]", this.getClass().getSimpleName(), this.basePath);
    }

    protected static class LockedFileChannel implements Closeable {
        private final FileChannel channel;

        public static VersionedN5Reader.LockedFileChannel openForReading(Path path) throws IOException {
            return new VersionedN5Reader.LockedFileChannel(path, true);
        }

        public static VersionedN5Reader.LockedFileChannel openForWriting(Path path) throws IOException {
            return new VersionedN5Reader.LockedFileChannel(path, false);
        }

        private LockedFileChannel(Path path, boolean readOnly) throws IOException {
            OpenOption[] options = readOnly ? new OpenOption[]{StandardOpenOption.READ} : new OpenOption[]{StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE};
            this.channel = FileChannel.open(path, options);
            boolean waiting = true;

            while (waiting) {
                waiting = false;

                try {
                    this.channel.lock(0L, Long.MAX_VALUE, readOnly);
                } catch (OverlappingFileLockException var8) {
                    waiting = true;

                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException var7) {
                        waiting = false;
                        Thread.currentThread().interrupt();
                    }
                } catch (IOException var9) {
                }
            }

        }

        public FileChannel getFileChannel() {
            return this.channel;
        }

        public void close() throws IOException {
            this.channel.close();
        }
    }

    public static void main(String[] args) throws IOException {
//        /indexes/s0
        String test_data = "/Users/zouinkhim/Desktop/active_learning/versioned_data/dataset2.v5";
        VersionedN5Reader reader = new VersionedN5Reader(test_data);
        HashMap<String, JsonElement> att = reader.getAttributes("/s0");

        for (String key : att.keySet()) {
            System.out.println(key + ":" + att.get(key));
        }
        CachedCellImg<FloatType, ?> img = N5Utils.open(reader, "/s0");
        ImageJFunctions.show(img);

    }
}
