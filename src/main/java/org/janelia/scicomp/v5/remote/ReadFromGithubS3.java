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
//package org.janelia.scicomp.v5.remote;
//
//import bdv.util.BdvFunctions;
//import bdv.util.BdvOptions;
//import net.imglib2.cache.img.CachedCellImg;
//import net.imglib2.type.numeric.real.FloatType;
//import org.janelia.saalfeldlab.n5.github.N5GithubReader;
//import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
//import org.janelia.saalfeldlab.n5.s3.N5AmazonS3Reader;
//import org.janelia.scicomp.v5.BasicV5Reader;
//import org.janelia.scicomp.v5.lib.uri.V5FSURL;
//import org.janelia.scicomp.v5.lib.uri.V5URL;
//
//import java.io.IOException;
//
//public class ReadFromGithubS3 {
//    public static void main(String[] args) throws IOException {
//        N5GithubReader indexReader = null;
//        N5AmazonS3Reader rawReader = null;
//        String dataset = "";
//        V5URL url = new V5FSURL(indexReader.serialize(), rawReader.toString());
//        BasicV5Reader<N5GithubReader, N5AmazonS3Reader> reader =
//                new BasicV5Reader<>(indexReader, rawReader, url);
//
//        CachedCellImg<FloatType, ?> img = N5Utils.openVolatile(reader, dataset);
//        BdvFunctions.show(img, dataset, new BdvOptions());
//    }
//}
