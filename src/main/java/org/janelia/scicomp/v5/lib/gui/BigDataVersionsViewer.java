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

package org.janelia.scicomp.v5.lib.gui;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.label.LabelMultisetType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.janelia.saalfeldlab.n5.N5FSReader;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.scicomp.v5.AbstractV5Reader;
import org.janelia.scicomp.v5.fs.MultiVersionZarrReader;
import org.janelia.scicomp.v5.fs.V5FSReader;
import org.janelia.scicomp.v5.lib.uri.V5FSURL;
import org.janelia.scicomp.v5.lib.uri.V5URL;
import org.janelia.scicomp.v5.lib.vc.GitUtils;

import java.io.File;
import java.io.IOException;

public class BigDataVersionsViewer {

    private final V5FSReader reader;
    private Bdv bdv = null;
    private final BdvOptions options;

    public BigDataVersionsViewer(V5FSReader reader) {
        this.reader = reader;
        this.options = BdvOptions.options();
    }

    public void showRaw(N5Reader n5, String dataset) throws IOException {
        RandomAccessibleInterval<UnsignedShortType> img = N5Utils.openVolatile(n5, dataset);
        if (bdv == null)
            bdv = BdvFunctions.show(img, dataset, options);
        else
            BdvFunctions.show(img, dataset, options.addTo(bdv));
    }


    public void show(String dataset) throws IOException, GitAPIException {
        String indexPath = reader.getIndexReader().getBasePath();
        N5FSReader rawReader = reader.getRawReader();
        V5URL url = reader.getUrl();

        GitUtils repo = new GitUtils(new File(indexPath));


        for (Ref branch : repo.getBranches()) {
            String branchName = branch.getName().replace("refs/", "");
            System.out.println("Branch: " + branchName);
            RevCommit commit = repo.getLastCommitForBranch(branch);

            AbstractV5Reader<MultiVersionZarrReader, N5FSReader> n5 = new AbstractV5Reader<>(new MultiVersionZarrReader(indexPath, commit), rawReader, url);
            RandomAccessibleInterval<UnsignedShortType> img = LabelMultisetTypeConverter.convertVirtual((CachedCellImg<LabelMultisetType, ?>) N5Utils.openVolatile(n5, dataset));

            if (bdv == null)
                bdv = BdvFunctions.show(img, branchName, options);
            else
                BdvFunctions.show(img, branchName, options.addTo(bdv));
        }
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        V5FSURL url = new V5FSURL("V5:{\"indexesPath\":\"/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/annotation.v5/versionedIndex\",\"keyValueStorePath\":\"/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/annotation.v5/datastore\"}");
        V5FSReader v5 = new V5FSReader(url);
        System.out.println(v5.getIndexReader().getBasePath());
        System.out.println(v5.getRawReader().getBasePath());
        new BigDataVersionsViewer(v5).show("annotation/data/s0");
    }
}
