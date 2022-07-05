package org.janus.lib.n5;

import com.google.gson.JsonElement;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

import java.io.IOException;
import java.util.HashMap;

public class TestReader {
    public static void main(String[] args) throws IOException {
        String test_data = "/Users/zouinkhim/Downloads/car/dataset.n5";
    VersionedN5Reader reader = new VersionedN5Reader(test_data);

        HashMap<String, JsonElement> att = reader.getAttributes("/");

        for( String key : att.keySet()){
            System.out.println(key+":"+att.get(key));
        }
        CachedCellImg<FloatType, ?> img = N5Utils.open(reader, "setup0/timepoint0/s0");
        ImageJFunctions.show(img);

//        N5Utils.op

    }
}
