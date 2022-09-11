package org.janelia.scicomp.v5.asbtract;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.eclipse.jgit.api.errors.GitAPIException;
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

public abstract class AbstractV5Reader<I,K,G> implements N5Reader  {
    protected static final String jsonFile = "attributes.json";
//    protected final static String INDEXES_STORE = "indexes";
//    protected final static String KV_STORE = "kv_store";

    protected final String dataStorePath;
    protected final String versionIndexPath;
    protected String currentDataset = "";
//    protected String basePath;

    //TODO fix
    public VersionedN5Reader(String versionIndexPath, String dataStorePath, GsonBuilder gsonBuilder) throws IOException {
        super(gsonBuilder);
        this.versionIndexPath = versionIndexPath;
        this.dataStorePath = dataStorePath;
        if (this.exists("/")) {
            Version version = this.getVersion();
            if (!VERSION.isCompatible(version)) {
                throw new IOException("Incompatible version " + version + " (this is " + VERSION + ").");
            }
        }
    }

    public VersionedN5Reader(V5URI v5URI) throws IOException {
        this(v5URI.getIndexesPath(),v5URI.getKeyValueStorePath());
    }


    public String getVersionedUrl(){
        return new V5URI(versionIndexPath,dataStorePath).getURI();
    }
    public VersionedN5Reader(String versionIndexPath, String dataStorePath) throws IOException {
        this(versionIndexPath, dataStorePath, new GsonBuilder());
    }

    public VersionedN5Reader(String uri) throws IOException {
        this(new V5URI(uri));
    }

    public boolean exists(String pathName) {
        Path path = Paths.get(this.dataStorePath,currentDataset, pathName);
        return Files.exists(path, new LinkOption[0]) && Files.isDirectory(path, new LinkOption[0]);
    }

    public HashMap<String, JsonElement> getAttributes(String pathName) throws IOException {
        Path path = getAttributesPath(pathName);
        System.out.println("getting attribute: "+pathName+" -> "+path.toString());
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


    protected String getDataStorePath() {
        return dataStorePath;
    }

    protected String getCurrentDataset() {
        return currentDataset;
    }

    public void setCurrentDataset(String currentDataset) {
        System.out.println("dataset updated: "+currentDataset);
        this.currentDataset = currentDataset;
    }

    protected String getVersionIndexPath() {
        return versionIndexPath;
    }

    public DataBlock<?> readBlock(String pathName, DatasetAttributes datasetAttributes, long... gridPosition) throws IOException {
        long version = getBlockVersion(pathName, gridPosition);
//        if(version==0) {
//            System.out.println("No block data");
//            return null;
//        }
        String blockPath = getDataBlockPath(pathName, version, gridPosition).toString();

        Path path = Paths.get(this.dataStorePath, currentDataset,blockPath);
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
        //TODO fix hack for paintera
        Path path = Paths.get(this.versionIndexPath,currentDataset);
        N5ZarrReader reader = new N5ZarrReader(path.toString());
        CachedCellImg<UnsignedLongType, ?> img = N5Utils.open(reader, dataset);
        UnsignedLongType p = img.getAt(gridPosition);
        return p.get();
    }

    public String[] list(String pathName) throws IOException {
        Path path = Paths.get(this.dataStorePath,currentDataset, pathName);
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

        pathComponents[0] = Long.toString(version);
        for (int i = 1; i < pathComponents.length; ++i) {
            pathComponents[i] = Long.toString(gridPosition[i - 1]);
        }


        return Paths.get(removeLeadingSlash(datasetPathName), pathComponents);
    }

    protected Path getAttributesPath(String pathName) {
        //TODO fix this is a hack for Paintera
//        Path path;
//        if (removeLeadingSlash(pathName) == "") {
//            path = Paths.get(this.basePath, pathName);
//        } else {
//            path = Paths.get(this.kv_directory, pathName);
//        }
        return Paths.get(this.dataStorePath, currentDataset,pathName, "attributes.json");
    }

    protected static String removeLeadingSlash(String pathName) {
        return !pathName.startsWith("/") && !pathName.startsWith("\\") ? pathName : pathName.substring(1);
    }

    @Override
    public String toString() {
        return "VersionedN5Reader{" +
                "dataStorePath='" + dataStorePath + '\'' +
                ", versionIndexPath='" + versionIndexPath + '\'' +
                '}';
    }

    public String getDatasetPath() {
        return Paths.get(getDataStorePath(),currentDataset).toString();
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

    public static void main(String[] args) throws IOException, GitAPIException {
//        /indexes/s0
        String dataPath = "/Users/zouinkhim/Desktop/active_learning/versioned_data/data";
        String indexesPath = "/Users/zouinkhim/Desktop/active_learning/versioned_data/indexes";
        VersionedN5Reader reader = new VersionedN5Reader(indexesPath, dataPath);
        System.out.println(reader.getVersionedUrl());
//        String[] resolutions = new String[]{"s0", "s1", "s2"};
//        for (String s : resolutions) {
//            HashMap<String, JsonElement> att = reader.getAttributes(s);
//
//            for (String key : att.keySet()) {
//                System.out.println(key + ":" + att.get(key));
//            }
//            CachedCellImg<FloatType, ?> img = N5Utils.open(reader, s);
//            ImageJFunctions.show(img);
//        }


    }
}
