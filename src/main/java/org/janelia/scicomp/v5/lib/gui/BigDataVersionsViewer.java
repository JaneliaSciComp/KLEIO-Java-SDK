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
import net.imglib2.type.NativeType;
import net.imglib2.type.label.LabelMultisetType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.janelia.saalfeldlab.n5.N5FSReader;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.scicomp.v5.BasicV5Reader;
import org.janelia.scicomp.v5.fs.MultiVersionZarrReader;
import org.janelia.scicomp.v5.fs.V5FSReader;
import org.janelia.scicomp.v5.fs.V5FSWriter;
import org.janelia.scicomp.v5.lib.gui.panel.BDVMergePanel;
import org.janelia.scicomp.v5.lib.uri.V5FSURL;
import org.janelia.scicomp.v5.lib.uri.V5URL;
import org.janelia.scicomp.v5.lib.vc.GitUtils;
import org.janelia.scicomp.v5.lib.vc.merge.entities.AbstractCallBack;
import org.janelia.scicomp.v5.lib.vc.merge.entities.BranchMergeController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BigDataVersionsViewer<T extends NativeType<T>> implements AbstractCallBack<Boolean> {

    private final V5FSReader writer;
    private final String[] branches;
    private Bdv bdv = null;
    private final BdvOptions options;
    private final BranchMergeController controller;
    private final List<BasicV5Reader<MultiVersionZarrReader, N5FSReader>> branchesReaders;
    private final List<CachedCellImg<T, ?> > imgs;

    public BigDataVersionsViewer(V5FSWriter writer) throws IOException, GitAPIException {
        this.writer = writer;
        this.options = BdvOptions.options();
        this.branches = new GitUtils(new File(writer.getIndexReader().getBasePath())).getBranchesNames();
        this.controller = new BranchMergeController(writer,this);
        this.branchesReaders = new ArrayList<>();
        this.imgs = new ArrayList<>();
    }


    private void showSource(N5Reader n5, String name, String dataset) throws IOException {
        System.out.println("Showing: " + name);
        RandomAccessibleInterval<UnsignedShortType> img;
        if (LabelMultisetTypeConverter.isLabelMultisetType(n5, dataset)) {
            CachedCellImg<T, ?> imp = N5Utils.openVolatile(n5, dataset);
           this.imgs.add(imp);
            img = LabelMultisetTypeConverter.convertVirtual((RandomAccessibleInterval<LabelMultisetType>) imp);
        } else {
            img = N5Utils.openVolatile(n5, dataset);
            this.imgs.add((CachedCellImg<T, ?>) img);
        }
        if (bdv == null) {
            bdv = BdvFunctions.show(img, name, options);
            setupBDV(bdv);
        } else
            BdvFunctions.show(img, name, options.addTo(bdv));
    }

    private void setupBDV(Bdv bdv) {
        BDVMergePanel mergePanel = new BDVMergePanel(branches, controller);
        bdv.getBdvHandle().getCardPanel().addCard(mergePanel.getKey(), mergePanel.getTitle(), mergePanel, mergePanel.isExpend());
    }

    public void showRaw(N5Reader n5, String dataset) throws IOException {
        showSource(n5, dataset, dataset);
    }


    public void show(String dataset) throws IOException, GitAPIException {
        String indexPath = writer.getIndexReader().getBasePath();
        N5FSReader rawReader = writer.getRawReader();
        V5URL url = writer.getUrl();

        for (String branchName : branches) {


            BasicV5Reader<MultiVersionZarrReader, N5FSReader> n5 = new BasicV5Reader<>(
                    new MultiVersionZarrReader(indexPath, branchName),
                    rawReader,
                    url);

            showSource(n5, n5.getIndexReader().getBranchName(), dataset);
            branchesReaders.add(n5);
        }

//        BDVUtils.randomColor(bdv);
//        bdv.getBdvHandle().getSetupAssignments().getConverterSetups().get(0).setDisplayRange();
    }


    public static void main(String[] args) throws IOException, GitAPIException {
        V5FSURL url = new V5FSURL("V5:{\"indexesPath\":\"/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/converted_data/indexes\",\"keyValueStorePath\":\"/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/converted_data/data_store\"}");
        V5FSReader raw = new V5FSReader(url);
        V5FSURL branches_url = new V5FSURL("V5:{\"indexesPath\":\"/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/annotation.v5/versionedIndex\",\"keyValueStorePath\":\"/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/annotation.v5/datastore\"}");
        V5FSWriter branchesReader = new V5FSWriter(branches_url);
        BigDataVersionsViewer bdv = new BigDataVersionsViewer(branchesReader);
        bdv.showRaw(raw, "/volumes/crop129/labels/all");
        bdv.show("annotation/data/s0");
    }


    @Override
    public void call(Boolean aBoolean) throws Exception {
        System.out.println("call back refresh");
        branchesReaders.forEach(reader -> {
            try {
                reader.getIndexReader().updateCommit();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
        });
        this.imgs.forEach(e -> e.getCache().invalidateAll());

    }
}
