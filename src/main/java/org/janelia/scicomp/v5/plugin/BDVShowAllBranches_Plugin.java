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
import org.janelia.scicomp.v5.fs.V5FSReader;
import org.janelia.scicomp.v5.fs.V5FSWriter;
import org.janelia.scicomp.v5.lib.gui.BigDataVersionsViewer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "BigDataViewer show all branches", mixinStandardHelpOptions = true, version = "1.0",
        description = "Show all branches in BigDataViewer")
class BDVShowAllBranches_Plugin implements Callable<Integer> {

    @Option(names = {"-i", "--index_path"}, required = true, description = "Index Matrix Path")
    private String indexPath;

    @Option(names = {"-kv", "--kv_path"}, required = true, description = "Annotation raw Key-Value Path")
    private String rawPath;

    @Option(names = {"-d", "--dataset"}, required = true, description = "Dataset")
    private String dataset;

    @Option(names = {"-r", "--raw"}, description = "RAW n5 path")
    private String raw_path;

    @Option(names = {"-dr", "--dataset_raw"}, description = "RAW Dataset")
    private String raw_dataset;

    @Override
    public Integer call() throws Exception {
        V5FSWriter v5 = new V5FSWriter(indexPath, rawPath);
        BigDataVersionsViewer viewer = new BigDataVersionsViewer(v5);
        if (raw_path != null) {
            viewer.showRaw(new N5FSReader(rawPath), raw_dataset);
        }
        viewer.show(dataset);
        return 0;
    }

    public static void main(String... args) {
//        args = "-help".split(" ");
        new CommandLine(new BDVShowAllBranches_Plugin()).execute(args);
    }
}