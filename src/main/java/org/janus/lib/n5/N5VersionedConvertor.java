package org.janus.lib.n5;

import org.janelia.saalfeldlab.n5.N5Reader;

import java.io.IOException;

public class N5VersionedConvertor {
    public static void main(String[] args) throws IOException {
        String test_data = "/Users/zouinkhim/Downloads/car/dataset.n5";
        VersionedN5Reader reader = new VersionedN5Reader(test_data);
        N5VersionedConvertor.convert(reader,test_data);
    }

    private static void convert(N5Reader reader, String output) {

    }
}
