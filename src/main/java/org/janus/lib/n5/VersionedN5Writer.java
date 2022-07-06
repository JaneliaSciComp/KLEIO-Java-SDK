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
package org.janus.lib.n5;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.*;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrReader;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janus.lib.SessionId;

/**
 * Versioned Store-based N5 implementation.
 *
 * @author Marwan Zouinkhi
 */
public class VersionedN5Writer extends VersionedN5Reader implements N5Writer {
    private UnsignedLongType session;

    public VersionedN5Writer(String basePath, GsonBuilder gsonBuilder) throws IOException {
        super(basePath, gsonBuilder);
        createDirectories(Paths.get(basePath));
        if (!VERSION.equals(this.getVersion())) {
            this.setAttribute("/", "n5", VERSION.toString());
        }

    }

    public VersionedN5Writer(String basePath) throws IOException {
        this(basePath, new GsonBuilder());
    }

    public void createGroup(String pathName) throws IOException {
        Path path = Paths.get(this.basePath,INDEXES_STORE, pathName);
        createDirectories(path);
    }

    public void setAttributes(String pathName, Map<String, ?> attributes) throws IOException {
        Path path = Paths.get(this.basePath,INDEXES_STORE, getAttributesPath(pathName).toString());
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

        long version = getCurrentSession().get();
        Path path = Paths.get(this.basePath, KV_STORE, getDataBlockPath(pathName, version, dataBlock.getGridPosition()).toString());
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
        setBlockVersion(pathName, dataBlock.getGridPosition(), version);
    }

    private void setBlockVersion(String dataset, long[] gridPosition, long version) throws IOException {
        Path path = Paths.get(this.basePath, INDEXES_STORE);
        N5ZarrWriter writer = new N5ZarrWriter(path.toString());
        CachedCellImg<UnsignedLongType, ?> img = N5Utils.open(writer, dataset);
        UnsignedLongType p = img.getAt(gridPosition);
        p.set(version);
        DatasetAttributes attrs = writer.getDatasetAttributes(dataset);
        N5Utils.save(img, writer, dataset, attrs.getBlockSize(), attrs.getCompression());
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
        Path path = Paths.get(this.basePath, pathName);
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
        Path path = Paths.get(this.basePath, KV_STORE, getDataBlockPath(pathName, version, gridPosition).toString());
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
}
