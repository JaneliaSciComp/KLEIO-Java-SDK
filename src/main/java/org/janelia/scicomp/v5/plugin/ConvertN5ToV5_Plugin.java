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

package org.janelia.scicomp.v5.plugin;

import org.janelia.saalfeldlab.n5.N5FSReader;
import org.janelia.scicomp.v5.fs.V5FSWriter;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "N5 to V5 Converter", mixinStandardHelpOptions = true, version = "1.0",
        description = "Convert N5 dataset to V5")
class ConvertN5ToV5_Plugin implements Callable<Integer> {
    @CommandLine.Option(names = {"-i", "--input"}, required = true,description = "Input n5 path")
    private String input_path;

    @CommandLine.Option(names = {"-id", "--input_dataset"}, required = true,description = "Input Dataset")
    private String input_dataset;

    @CommandLine.Option(names = {"-oi", "--output_indexes"}, required = true, description = "Output V5 Indexes Matrix Path")
    private String output_indexes;

    @CommandLine.Option(names = {"-ok", "--output_kv"}, required = true, description = "Output V5 raw Key-Value Path")
    private String output_kv;


    @Override
    public Integer call() throws Exception {
        N5FSReader reader = new N5FSReader(input_path);
        V5FSWriter writer = new V5FSWriter(output_indexes, output_kv);
        V5FSWriter.convert(reader,input_dataset,writer);
        System.out.println("Converted!");
        return 0;
    }

    public static void main(String[] args) throws IOException {
//        String path = "/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/test_data.n5";
//        String indexes = "/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/converted_data/indexes";
//        String dataStore = "/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/converted_data/data_store";
//        String dataset = "/volumes/crop129/labels/";
//        String[] arg = ("-i " + path + " -id " + dataset + " -oi " + indexes + " -ok " + dataStore).split(" ");
//        args = "-help".split(" ");
        int exitCode = new CommandLine(new ConvertN5ToV5_Plugin()).execute(args);
        System.exit(exitCode);
    }
}
