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

import bdv.ui.BdvDefaultCards;
import bdv.ui.CardPanel;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.janelia.saalfeldlab.n5.N5FSReader;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.scicomp.v5.BasicV5Reader;
import org.janelia.scicomp.v5.fs.MultiVersionZarrReader;
import org.janelia.scicomp.v5.fs.V5FSReader;
import org.janelia.scicomp.v5.fs.V5FSWriter;
import org.janelia.scicomp.v5.lib.gui.panel.BDVCommitsHistoryPanel;
import org.janelia.scicomp.v5.lib.uri.V5FSURL;
import org.janelia.scicomp.v5.lib.uri.V5URL;
import org.janelia.scicomp.v5.lib.vc.GitUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class BigDataViewerHistoryBrowser {

    private final V5FSReader reader;
    private Bdv bdv = null;
    private final BdvOptions options;

    public BigDataViewerHistoryBrowser(V5FSReader reader) {
        this.reader = reader;
        this.options = BdvOptions.options();
    }


    private void showSource(N5Reader n5, String name, String dataset) throws IOException {
        System.out.println("Showing: " + name);
        RandomAccessibleInterval<UnsignedShortType> img;
        if (LabelMultisetTypeConverter.isLabelMultisetType(n5, dataset)) {
            img = LabelMultisetTypeConverter.convertVirtual(N5Utils.openVolatile(n5, dataset));
        } else {
            img = N5Utils.openVolatile(n5, dataset);
        }
        if (bdv == null)
            bdv = BdvFunctions.show(img, name, options);
        else
            BdvFunctions.show(img, name, options.addTo(bdv));
    }

    public void showRaw(N5Reader n5, String dataset) throws IOException {
        showSource(n5, dataset, dataset);
    }


    public void show(String dataset) throws IOException, GitAPIException {
        String indexPath = reader.getIndexReader().getBasePath();
        N5FSReader rawReader = reader.getRawReader();
        V5URL url = reader.getUrl();

        GitUtils repo = new GitUtils(new File(indexPath));

        for (Ref branch : repo.getBranches()) {
            String branchName = branch.getName().replace("refs/", "");

            BasicV5Reader<MultiVersionZarrReader, N5FSReader> n5 = new BasicV5Reader<>(new MultiVersionZarrReader(indexPath, branchName), rawReader, url);
            showSource(n5, branchName, dataset);
        }
        BDVCommitsHistoryPanel bdvCommitHistory = new BDVCommitsHistoryPanel();
        final CardPanel cardPanel = bdv.getBdvHandle().getCardPanel();
        cardPanel.addCard(bdvCommitHistory.getTitle(),
                bdvCommitHistory.getKey(),
                bdvCommitHistory, bdvCommitHistory.isExpend(), new Insets(0, 4, 0, 0));
        cardPanel.setCardExpanded(BdvDefaultCards.DEFAULT_VIEWERMODES_CARD, false);
        cardPanel.setCardExpanded(BdvDefaultCards.DEFAULT_SOURCES_CARD, false);
        cardPanel.setCardExpanded(BdvDefaultCards.DEFAULT_SOURCEGROUPS_CARD, false);
        bdv.getBdvHandle().getViewerPanel().requestRepaint();
        bdv.getBdvHandle().getSplitPanel().repaint();
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        V5FSURL url = new V5FSURL("V5:{\"indexesPath\":\"/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/converted_data/indexes\",\"keyValueStorePath\":\"/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/converted_data/data_store\"}");
        V5FSReader raw = new V5FSReader(url);
        V5FSURL branches_url = new V5FSURL("V5:{\"indexesPath\":\"/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/annotation.v5/versionedIndex\",\"keyValueStorePath\":\"/Users/zouinkhim/Desktop/Klio_presentation/data_multi_branch/annotation.v5/datastore\"}");
        V5FSWriter branches = new V5FSWriter(branches_url);
        BigDataVersionsViewer bdv = new BigDataVersionsViewer(branches);
        bdv.showRaw(raw, "/volumes/crop129/labels/all");
        bdv.show("annotation/data/s0");
    }


}
