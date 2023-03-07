///*
// * *
// *  * Copyright (c) 2022, Janelia
// *  * All rights reserved.
// *  *
// *  * Redistribution and use in source and binary forms, with or without
// *  * modification, are permitted provided that the following conditions are met:
// *  *
// *  * 1. Redistributions of source code must retain the above copyright notice,
// *  *    this list of conditions and the following disclaimer.
// *  * 2. Redistributions in binary form must reproduce the above copyright notice,
// *  *    this list of conditions and the following disclaimer in the documentation
// *  *    and/or other materials provided with the distribution.
// *  *
// *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// *  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// *  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// *  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// *  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// *  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// *  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// *  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// *  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// *  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// *  * POSSIBILITY OF SUCH DAMAGE.
// *
// */
//
//package org.janelia.scicomp.kleio;
//
//import org.janelia.saalfeldlab.n5.N5FSWriter;
//import org.janelia.saalfeldlab.n5.N5Writer;
//import org.janelia.scicomp.kleio.indexes.KleioIndexWriter;
//import org.janelia.scicomp.kleio.indexes.KleioN5FSIndexWriter;
//import org.janelia.scicomp.kleio.tools.FileUtils;
//import org.janelia.scicomp.tests.fs.V5FSWriter;
//
//import java.io.IOException;
//
//public class V5FSClonedTest extends V5FSMasterTest {
//    static private String indexesTestDirPath = System.getProperty("user.home") + "/tmp/n5-test/indexes";
//    static private String clonedIndexesTestDirPath = System.getProperty("user.home") + "/tmp/n5-test/ClonedIndexes";
//    static private String rawTestDirPath = System.getProperty("user.home") + "/tmp/n5-test/raw_data";
//    static private String username = "zouinkhim";
//
//    /**
//     * @throws IOException
//     */
//    @Override
//    protected N5Writer createN5Writer() throws IOException {
//        //Delete if existent
//        FileUtils.forceDeleteAll(indexesTestDirPath, rawTestDirPath, clonedIndexesTestDirPath);
//
//        System.out.println(indexesTestDirPath);
//        KleioWriter<KleioN5FSIndexWriter, N5FSWriter> masterWriter = new KleioWriter<>(new KleioN5FSIndexWriter(indexesTestDirPath),new N5FSWriter(rawTestDirPath));
//        checkGit(masterWriter);
//        return V5FSWriter.cloneFrom(masterWriter, clonedIndexesTestDirPath, username);
//    }
//
////    @Test
////    protected
//
//
//}