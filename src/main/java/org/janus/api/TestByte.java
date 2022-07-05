package org.janus.api;

import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.DefaultBlockWriter;
import org.janelia.saalfeldlab.n5.s3.N5AmazonS3Writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class TestByte {
    public static void main(String[] args) throws IOException {
//        byte test = 68;


//        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//        DatasetAttributes datasetAttributes = null;
//        DataBlock<?> dataBlock = null;
//        long[] gridPossition = dataBlock.getGridPosition();
//        DefaultBlockWriter.writeBlock(byteStream, datasetAttributes, dataBlock);
        
        UnsignedLongType j = new UnsignedLongType(111111111l);
//        N5AmazonS3Writer
        BigInteger k = j.getBigInteger();
        byte[] x = k.toByteArray();
        System.out.println("Total bytes: "+x.length);
        for(byte test : x){
            System.out.println("byte: (" +Integer.toBinaryString(test)+") - "+Short.toUnsignedInt(test));
        }


    }
}
