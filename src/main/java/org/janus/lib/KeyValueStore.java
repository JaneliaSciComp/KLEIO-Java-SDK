package org.janus.lib;

import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

public class KeyValueStore {
    private final String path;
    private final Options options;
    private final String dataset;
    private final long version;

    public KeyValueStore(String path) {
        this(path, "", new UnsignedLongType(0));
    }

    public KeyValueStore(String path, String dataset) {
        this(path, dataset, new UnsignedLongType(0));
    }

    public KeyValueStore(String path, String dataset, UnsignedLongType version) {
        this.path = path;
        this.dataset = dataset;
        this.version = version.get();
        this.options = new Options();
        options.createIfMissing(true);
    }

    public void create() throws IOException {
        DB db = factory.open(new File(path), options);
        db.close();
    }

    public static void main(String[] args) throws IOException {
        KeyValueStore kv = new KeyValueStore("/Users/zouinkhim/Desktop/active_learning/tmp/example", "s0", new UnsignedLongType(10));
        kv.set(new long[]{0, 1, 34}, bytes("rocks"));
        System.out.println(asString(kv.get("Tampa")));
    }

    public void set(long[] position, byte[] value) throws IOException {
        DB db = factory.open(new File(path), options);
        String key = format(position);
        db.put(bytes(key), value);
        db.close();
        System.out.println("Block: " + key + " saved!");
    }

    public byte[] get(String key) throws IOException {
        DB db = factory.open(new File(path), options);
        byte[] value = db.get(bytes(key));
        db.close();
        return value;
    }

    private String format(long[] position) {
        StringBuilder result = new StringBuilder(dataset + "_" + version + "_");
        for (int i = 0; i < position.length; i++) {
            result.append(position[i]);
            if (i < position.length - 1)
                result.append(".");
        }
        return result.toString();
    }
}
