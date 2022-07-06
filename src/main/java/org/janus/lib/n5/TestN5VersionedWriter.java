package org.janus.lib.n5;

import com.google.gson.JsonElement;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5FSWriter;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

import java.io.IOException;
import java.util.HashMap;

public class TestN5VersionedWriter {
    public static void main(String[] args) throws IOException {
//        /indexes/s0
        String n5_read = "/Users/zouinkhim/Downloads/car/dataset.n5";
        N5FSWriter reader = new N5FSWriter(n5_read);

        HashMap<String, JsonElement> att = reader.getAttributes("setup0/timepoint0/s0");

        for (String key : att.keySet()) {
            System.out.println(key + ":" + att.get(key));
        }

        DatasetAttributes datasetAttributes = reader.getDatasetAttributes("setup0/timepoint0/s0");

        String test_data = "/Users/zouinkhim/Desktop/active_learning/versioned_data/dataset2.v5";
        VersionedN5Writer writer = new VersionedN5Writer(test_data);
        writer.createDataset("s0",datasetAttributes);
//        writer.setAttributes("s0", att);
//        HashMap<String, JsonElement> att = writer.getAttributes("/s0");

//        for( String key : att.keySet()){
//            System.out.println(key+":"+att.get(key));
//        }
//        CachedCellImg<FloatType, ?> img = N5Utils.open(writer, "/s0");
//        ImageJFunctions.show(img);

//        writer.createDataset();

    }

}
