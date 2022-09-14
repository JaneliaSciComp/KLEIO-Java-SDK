/*
 * *
 *  * Copyright (c) 2022, Janelia
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice,
 *  *    this list of conditions and the following disclaimer.
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation
 *  *    and/or other materials provided with the distribution.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.janelia.scicomp.tests;

import com.google.gson.JsonElement;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.janelia.saalfeldlab.n5.N5FSWriter;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.scicomp.v5.fs.V5FSWriter;

import java.io.IOException;
import java.util.HashMap;

public class TestsWriter {
    final static String inputN5Image = "/Users/zouinkhim/Downloads/car/dataset.n5";
    final static String inputN5Dataset = "setup0/timepoint0";
    final static String masterIndex = "/Users/zouinkhim/Desktop/active_learning/versioned_data/dataset.v5/indexes";
    final static String dataStore = "/Users/zouinkhim/Desktop/active_learning/versioned_data/dataset.v5/dataStore";
    final static String clonedIndex = "/Users/zouinkhim/Desktop/active_learning/versioned_data/clonedIndexes";
    final static String username = "zouinkhim";

    public static void main(String[] args) throws Exception {
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
        V5FSWriter writer = V5FSWriter.convert(reader, inputN5Dataset, masterIndex,dataStore);

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

    private static void cloneMaster() throws Exception {


        V5FSWriter writer = V5FSWriter.cloneFrom(masterIndex, clonedIndex,dataStore, username);
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
