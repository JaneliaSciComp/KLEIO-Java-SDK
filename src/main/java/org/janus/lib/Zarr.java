package org.janus.lib;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;

import java.io.IOException;
import java.util.HashMap;

public class Zarr {
    public static<T extends NumericType<T> & NativeType<T>>   void main(String[] args) throws IOException {

//        new CLibrary();
        N5ZarrWriter writer = new N5ZarrWriter("/Users/zouinkhim/Desktop/test_compress");
        DatasetAttributes attributes = writer.getDatasetAttributes("");

        HashMap<String, Object> mp = attributes.asMap();
        for (String k : mp.keySet())
            System.out.println(k+":"+mp.get(k));
        for ( long d : attributes.getDimensions()){
            System.out.print(d+" ");
        }
//        N5Utils.save

//        System.out.println("hello");
////        System.out.println(reader.getGson());
////        N5Utils.open(reader,"");
//        ImagePlus imp = N5IJUtils.load(reader, "");
//        imp.show();

    }
}
