package org.janus.lib.leveldb;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

public class KeyValueStore {
    private final String path;
    private final Options options;

    public KeyValueStore(String path) {
        this.path = path;
        this.options = new Options();
        options.createIfMissing(true);
    }

    public static void main(String[] args) throws IOException {
        KeyValueStore kv = new KeyValueStore("/Users/zouinkhim/Desktop/active_learning/tmp/example");
        kv.set("Tampa", bytes("rocks"));
        System.out.println(asString(kv.get("Tampa")));
    }

    public void set(String key, byte[] value) throws IOException {
        DB db = factory.open(new File(path), options);
        db.put(bytes(key), value);
        db.close();
    }

    public byte[] get(String key) throws IOException {
        DB db = factory.open(new File(path), options);
        byte[] value = db.get(bytes(key));
        db.close();
        return value;
    }
}
