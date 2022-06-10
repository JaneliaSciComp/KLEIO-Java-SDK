package org.janus.lib;

import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.real.FloatType;
import org.janelia.saalfeldlab.n5.ByteArrayDataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.LongArrayDataBlock;
import org.janelia.saalfeldlab.n5.ij.N5IJUtils;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;

import java.io.IOException;
import java.util.HashMap;

public class Zarr {
    public static<T extends NumericType<T> & NativeType<T>>   void main(String[] args) throws IOException {

//        new CLibrary();
        N5ZarrWriter writer = new N5ZarrWriter("/Users/zouinkhim/Desktop/test_compress");
        CachedCellImg<UnsignedLongType, ?> img = N5Utils.open(writer, "");
        img.getAt(0, 0, 0).set(new UnsignedLongType(2));


        DatasetAttributes attrs = writer.getDatasetAttributes("");
        N5Utils.save(img, writer, "", attrs.getBlockSize(), attrs.getCompression());


        DatasetAttributes attributes = writer.getDatasetAttributes("");
//        if (p instanceof UnsignedLongType){
//            System.out.println("UnsignedLongType");
//        }

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
