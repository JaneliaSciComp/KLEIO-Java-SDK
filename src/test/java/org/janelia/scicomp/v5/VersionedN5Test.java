///**
// * Copyright (c) 2019, Stephan Saalfeld
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// * 1. Redistributions of source code must retain the above copyright notice,
// *    this list of conditions and the following disclaimer.
// * 2. Redistributions in binary form must reproduce the above copyright notice,
// *    this list of conditions and the following disclaimer in the documentation
// *    and/or other materials provided with the distribution.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// * POSSIBILITY OF SUCH DAMAGE.
// */
//package org.janelia.scicomp.v5;
//
//import net.imglib2.RandomAccessibleInterval;
//import net.imglib2.type.numeric.IntegerType;
//import net.imglib2.type.numeric.RealType;
//import net.imglib2.view.Views;
//import org.janelia.saalfeldlab.n5.*;
//import org.janelia.saalfeldlab.n5.N5Reader.Version;
//import org.janelia.saalfeldlab.n5.blosc.BloscCompression;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//import org.junit.Assert;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import java.io.*;
//import java.util.Map;
//
//import static org.junit.Assert.*;
//
///**
// *
// *
// * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
// */
//public class VersionedN5Test extends AbstractN5Test {
//
//    static private String testDirPath = System.getProperty("user.home") + "/tmp/n5-test.v5";
//    static private String testV5DirPath = System.getProperty("user.home") + "/tmp/v5-test.v5";
//    static private String testV5NestedDirPath = System.getProperty("user.home") + "/tmp/v5-test-nested.v5";
//    static private String testV5DatasetName = "/test/data";
//
//
//    /**
//     * @throws IOException
//     */
//    @Override
//    protected VersionedN5Writer createN5Writer() throws IOException {
//
//        return new VersionedN5Writer(testDirPath);
//    }
//
//    @Override
//    protected Compression[] getCompressions() {
//
//        return new Compression[] {
//                new Bzip2Compression(),
//                new GzipCompression(),
//                new GzipCompression(5, true),
//                new BloscCompression(),
//                new BloscCompression("lz4", 6, BloscCompression.BITSHUFFLE, 0, 4),
//                new RawCompression()
//        };
//    }
//
//    @Override
//    @Test
//    public void testCreateDataset() {
//
//        try {
//            AbstractN5Test.n5.createDataset(AbstractN5Test.datasetName, AbstractN5Test.dimensions, AbstractN5Test.blockSize, DataType.UINT64, getCompressions()[0]);
//        } catch (final IOException e) {
//            fail(e.getMessage());
//        }
//
//        if (!AbstractN5Test.n5.exists(AbstractN5Test.datasetName))
//            fail("Dataset does not exist");
//
//        try {
//            final DatasetAttributes info = AbstractN5Test.n5.getDatasetAttributes(AbstractN5Test.datasetName);
//            Assert.assertArrayEquals(AbstractN5Test.dimensions, info.getDimensions());
//            Assert.assertArrayEquals(AbstractN5Test.blockSize, info.getBlockSize());
//            Assert.assertEquals(DataType.UINT64, info.getDataType());
//            Assert.assertEquals(getCompressions()[0].getClass(), info.getCompression().getClass());
//        } catch (final IOException e) {
//            fail("Dataset info cannot be opened");
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testCreateNestedDataset() {
//
//        final String datasetName = "/test/nested/data";
//        try {
//            VersionedN5Writer n5Nested = new VersionedN5Writer(testDirPath );
//            n5Nested.createDataset(datasetName, AbstractN5Test.dimensions, AbstractN5Test.blockSize, DataType.UINT64, getCompressions()[0]);
//            assertEquals( "/", n5Nested.getGroupSeparator());
//
//            n5Nested.remove(datasetName);
//            n5Nested.close();
//        } catch (IOException e) {
//            fail(e.getMessage());
//        }
//    }
//
//    @Test
//    public void testCreateDatasetNameEmpty() {
//        try {
//            VersionedN5Writer n5 = new VersionedN5Writer(testDirPath );
//            n5.createDataset("", AbstractN5Test.dimensions, AbstractN5Test.blockSize, DataType.UINT64, getCompressions()[0]);
//            n5.remove();
//            n5.close();
//        } catch (IOException e) {
//            fail(e.getMessage());
//        }
//    }
//
//    @Test
//    public void testCreateDatasetNameSlash() {
//        try {
//            VersionedN5Writer n5 = new VersionedN5Writer(testDirPath );
//            n5.createDataset("", AbstractN5Test.dimensions, AbstractN5Test.blockSize, DataType.UINT64, getCompressions()[0]);
//            n5.remove();
//            n5.close();
//        } catch (IOException e) {
//            fail(e.getMessage());
//        }
//    }
//
//    @Override
//    @Test
//    public void testVersion() throws NumberFormatException, IOException {
//
//        AbstractN5Test.n5.createGroup("/");
//
//        final Version n5Version = AbstractN5Test.n5.getVersion();
//
//        System.out.println(n5Version);
//
//        Assert.assertTrue(n5Version.equals(VersionedN5Reader.VERSION));
//
//        Assert.assertTrue(VersionedN5Reader.VERSION.isCompatible(AbstractN5Test.n5.getVersion()));
//    }
//
//    @Override
//    @Test
//    public void testExists() {
//
//        final String groupName2 = AbstractN5Test.groupName + "-2";
//        final String datasetName2 = AbstractN5Test.datasetName + "-2";
//        final String notExists = AbstractN5Test.groupName + "-notexists";
//        try {
//            AbstractN5Test.n5.createDataset(datasetName2, AbstractN5Test.dimensions, AbstractN5Test.blockSize, DataType.UINT64, getCompressions()[0]);
//            Assert.assertTrue(AbstractN5Test.n5.exists(datasetName2));
//            Assert.assertTrue(AbstractN5Test.n5.datasetExists(datasetName2));
//
//            AbstractN5Test.n5.createGroup(groupName2);
//            Assert.assertTrue(AbstractN5Test.n5.exists(groupName2));
//            Assert.assertFalse(AbstractN5Test.n5.datasetExists(groupName2));
//
//            Assert.assertFalse(AbstractN5Test.n5.exists(notExists));
//            Assert.assertFalse(AbstractN5Test.n5.datasetExists(notExists));
//
//            Assert.assertTrue(AbstractN5Test.n5.remove(datasetName2));
//            Assert.assertTrue(AbstractN5Test.n5.remove(groupName2));
//        } catch (final IOException e) {
//            fail(e.getMessage());
//        }
//    }
//
//    @Override
//    @Test
//    public void testListAttributes() {
//
//        final String groupName2 = AbstractN5Test.groupName + "-2";
//        final String datasetName2 = AbstractN5Test.datasetName + "-2";
//        try {
//            AbstractN5Test.n5.createDataset(datasetName2, AbstractN5Test.dimensions, AbstractN5Test.blockSize, DataType.UINT64, getCompressions()[0]);
//            AbstractN5Test.n5.setAttribute(datasetName2, "attr1", new double[] {1, 2, 3});
//            AbstractN5Test.n5.setAttribute(datasetName2, "attr2", new String[] {"a", "b", "c"});
//            AbstractN5Test.n5.setAttribute(datasetName2, "attr3", 1.0);
//            AbstractN5Test.n5.setAttribute(datasetName2, "attr4", "a");
//
//            Map<String, Class<?>> attributesMap = AbstractN5Test.n5.listAttributes(datasetName2);
//            Assert.assertTrue(attributesMap.get("attr1") == double[].class);
//            Assert.assertTrue(attributesMap.get("attr2") == String[].class);
//            Assert.assertTrue(attributesMap.get("attr3") == double.class);
//            Assert.assertTrue(attributesMap.get("attr4") == String.class);
//
//            AbstractN5Test.n5.createGroup(groupName2);
//            AbstractN5Test.n5.setAttribute(groupName2, "attr1", new double[] {1, 2, 3});
//            AbstractN5Test.n5.setAttribute(groupName2, "attr2", new String[] {"a", "b", "c"});
//            AbstractN5Test.n5.setAttribute(groupName2, "attr3", 1.0);
//            AbstractN5Test.n5.setAttribute(groupName2, "attr4", "a");
//
//            attributesMap = AbstractN5Test.n5.listAttributes(datasetName2);
//            Assert.assertTrue(attributesMap.get("attr1") == double[].class);
//            Assert.assertTrue(attributesMap.get("attr2") == String[].class);
//            Assert.assertTrue(attributesMap.get("attr3") == double.class);
//            Assert.assertTrue(attributesMap.get("attr4") == String.class);
//        } catch (final IOException e) {
//            fail(e.getMessage());
//        }
//    }
//
//    @Override
//    @Test
//    @Ignore("Zarr does not currently support mode 1 data blocks.")
//    public void testMode1WriteReadByteBlock() {
//    }
//
//    @Override
//    @Test
//    @Ignore("Zarr does not currently support mode 2 data blocks and serialized objects.")
//    public void testWriteReadSerializableBlock() {
//    }
//
//    private boolean runPythonTest(String script) throws IOException, InterruptedException {
//
//        final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
//        Process process;
//        if (isWindows) {
//            process = Runtime.getRuntime().exec("cmd.exe /c python3 src\\test\\python\\" + script);
//        } else {
//            process = Runtime.getRuntime().exec("python3 src/test/python/" + script );
//        }
//        final int exitCode = process.waitFor();
//        new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().forEach(System.out::println);
//        process.destroy();
//
//        return (exitCode == 0 );
//    }
//
//    private static <T extends IntegerType<T>> void assertIsSequence(
//            final RandomAccessibleInterval<T> source,
//            final T ref) {
//
//        ref.setZero();
//        for (final T t : Views.flatIterable(source)) {
//
//            if (!t.valueEquals(ref))
//                throw new AssertionError("values not equal: expected " + ref + ", actual " + t);
//            ref.inc();
//        }
//    }
//
//    private static <T extends RealType<T>> void assertIsSequence(
//            final RandomAccessibleInterval<T> source,
//            final T ref) {
//
//        ref.setReal(0);
//        for (final T t : Views.flatIterable(source)) {
//
//            if (!t.valueEquals(ref))
//                throw new AssertionError("values not equal: expected " + ref + ", actual " + t);
//            ref.inc();
//        }
//    }
//
//
////	/**
////	 * @throws IOException
////	 */
////	@AfterClass
////	public static void rampDownAfterClass() throws IOException {
////
//////		Assert.assertTrue(n5.remove());
//////		initialized = false;
////		n5 = null;
////	}
//
//}
