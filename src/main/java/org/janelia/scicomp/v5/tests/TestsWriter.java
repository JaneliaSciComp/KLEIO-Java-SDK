package org.janelia.scicomp.v5.tests;

import com.google.gson.JsonElement;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.janelia.saalfeldlab.N5Factory;
import org.janelia.saalfeldlab.n5.N5DatasetDiscoverer;
import org.janelia.saalfeldlab.n5.N5FSWriter;
import org.janelia.saalfeldlab.n5.ij.N5IJUtils;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.scicomp.v5.VersionedN5Writer;

import java.io.IOException;
import java.util.HashMap;

public class TestsWriter {
    final static String inputN5Image = "/Users/zouinkhim/Downloads/car/dataset.n5";
    final static String inputN5Dataset = "setup0/timepoint0";
    final static String masterV5 = "/Users/zouinkhim/Desktop/active_learning/versioned_data/master.v5";
    final static String clonedLocalV5 = "/Users/zouinkhim/Desktop/active_learning/versioned_data/cloned.v5";
    final static String username = "zouinkhim";

    public static void main(String[] args) throws GitAPIException, IOException {
//        convertN5ToVersioned();
        cloneMaster();
    }


    private static void convertN5ToVersioned() throws IOException, GitAPIException {

//        //Read versioned N5
//        N5Reader reader= new VersionedN5Reader(versionedN5Image);
//
//
//        // Open writer
//        VersionedN5Writer writer = new VersionedN5Writer(versionedN5Image,true);
//
//        // Write block
//        writer.writeBlock( ..... );
//
//        // Commit changes
//        writer.commit();

        // Checkout new branch
//        writer.checkoutBranch("new_branch",true);
//
//        // Increment current version ID
//        writer.incrementSession();

        N5FSWriter reader = new N5FSWriter(inputN5Image);
        VersionedN5Writer writer = VersionedN5Writer.convert(reader, inputN5Dataset, masterV5);

        String[] resolutions = new String[]{"s0", "s1", "s2"};
        for (String s:resolutions){
            HashMap<String, JsonElement> att = writer.getAttributes(s);

            for (String key : att.keySet()) {
                System.out.println(key + ":" + att.get(key));
            }
            CachedCellImg<FloatType, ?> img = N5Utils.open(writer, s);
            ImageJFunctions.show(img);
        }
    }

    private static void cloneMaster() throws IOException {


        VersionedN5Writer writer = VersionedN5Writer.cloneFrom(masterV5, clonedLocalV5, username);
        String[] resolutions = new String[]{"s0", "s1", "s2"};
        for (String s:resolutions){
            HashMap<String, JsonElement> att = writer.getAttributes(s);

            for (String key : att.keySet()) {
                System.out.println(key + ":" + att.get(key));
            }
            CachedCellImg<FloatType, ?> img = N5Utils.open(writer, s);
            ImageJFunctions.show(img);
        }
    }
}
