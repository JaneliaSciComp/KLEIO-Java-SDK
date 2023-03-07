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

package org.janelia.scicomp.kleio;

import org.janelia.saalfeldlab.n5.*;
import org.janelia.scicomp.kleio.indexes.KleioIndexWriter;
import org.janelia.scicomp.kleio.indexes.KleioN5FSIndexWriter;
import org.janelia.scicomp.kleio.tools.FileUtils;
import org.janelia.scicomp.kleio.tools.SessionId;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.fail;

public class KleioFSMasterTest extends AbstractN5Test {
    static private String indexesTestDirPath = System.getProperty("user.home") + "/tmp/n5-test/indexes";
    static private String rawTestDirPath = System.getProperty("user.home") + "/tmp/n5-test/raw_data";

    /**
     * @throws IOException
     */
    @Override
    protected KleioWriter<KleioN5FSIndexWriter, N5FSWriter> createN5Writer() throws IOException {
        System.out.println(indexesTestDirPath);
        SessionId.TESTING = true;
        FileUtils.forceDeleteAll(indexesTestDirPath, rawTestDirPath);
        KleioWriter<KleioN5FSIndexWriter, N5FSWriter> masterWriter = new KleioWriter<>(new KleioN5FSIndexWriter(indexesTestDirPath), new N5FSWriter(rawTestDirPath));
        return masterWriter;
    }

    private KleioWriter<KleioN5FSIndexWriter, N5FSWriter> getN5() {
        return (KleioWriter<KleioN5FSIndexWriter, N5FSWriter>) n5;
    }

    @Test
    public void TestAPI() throws IOException{
        SessionId.TESTING = false;
        SessionId.getNextId();
    }

    @Test
    public void testGit() throws IOException {
        KleioWriter<KleioN5FSIndexWriter, N5FSWriter> writer = createN5Writer();
        String branch = writer.getIndexes().getVersionManager().getCurrentBranch();
        System.out.println("Current branch: " + branch);
        Assert.assertEquals(branch, "master");
        checkGit();
    }

    @Test
    public void testCreateNewBranchEmptyRepo() throws IOException {
        KleioWriter<KleioN5FSIndexWriter, N5FSWriter> writer = createN5Writer();
        createNewBranch(writer);
        n5 = createN5Writer();
    }

    protected void checkGit() throws IOException {
        checkGit((KleioWriter) n5);
    }


    @Test
    public void testCreateNewBranch() throws IOException {
        createNewBranch((KleioWriter) n5);
    }

    public void createNewBranch(KleioWriter writer) throws IOException {
        String name = "test_branch";
        writer.createNewBranch(name);
        writer.checkoutBranch(name);
        String currentBranch = writer.getCurrentBranch();
        Assert.assertEquals(name, currentBranch);
    }

    protected void checkGit(KleioWriter writer) throws IOException {
        KleioIndexWriter indexes = (KleioIndexWriter) writer.getIndexes();
        Set<String> uncommittedChanges = indexes.getVersionManager().getUncommittedChanges();
        Set<String> untrackedChanges = indexes.getVersionManager().getUntrackedChanges();

        print(uncommittedChanges);
        print(untrackedChanges);

        Assert.assertTrue(uncommittedChanges.isEmpty());
        Assert.assertTrue(untrackedChanges.isEmpty());
    }

    private void print(Set<String> elements) {
        if (elements.isEmpty())
            return;
        System.out.println("Elements: ");
        for (String s : elements)
            System.out.println("e: " + s);
    }

    @Override
    public void testCreateDataset() {
        try {

            super.testCreateDataset();
            checkGit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void testCreateGroup() {
        try {
            n5 = createN5Writer();
            super.testCreateGroup();
            checkGit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testWriteReadByteBlockCommit() {

        for (final Compression compression : getCompressions()) {
            for (final DataType dataType : new DataType[]{
                    DataType.UINT8,
                    DataType.INT8}) {

                System.out.println("Testing " + compression.getType() + " " + dataType);
                try {
                    n5.createDataset(datasetName, dimensions, blockSize, dataType, compression);
                    final DatasetAttributes attributes = n5.getDatasetAttributes(datasetName);
                    final ByteArrayDataBlock dataBlock = new ByteArrayDataBlock(blockSize, new long[]{0, 0, 0}, byteBlock);
                    n5.writeBlock(datasetName, attributes, dataBlock);
                    ((KleioWriter) n5).commit();
                    checkGit();

                    final DataBlock<?> loadedDataBlock = n5.readBlock(datasetName, attributes, new long[]{0, 0, 0});

                    Assert.assertArrayEquals(byteBlock, (byte[]) loadedDataBlock.getData());

                    Assert.assertTrue(n5.remove(datasetName));

                } catch (final IOException e) {
                    e.printStackTrace();
                    fail("Block cannot be written.");
                }
            }
        }
    }

    @Override
    public void testExists() {
        super.testExists();
        try {
            checkGit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
