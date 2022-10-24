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

package org.janelia.scicomp.v5.lib.vc.merge;

import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.scicomp.v5.fs.MultiVersionZarrReader;
import org.janelia.scicomp.v5.lib.tools.Utils;
import org.janelia.scicomp.v5.lib.vc.merge.entities.BlockMergeResult;
import org.janelia.scicomp.v5.lib.vc.merge.entities.Tuple;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockConflictManager {
    public static DataBlock<?> getDelta(MultiVersionZarrReader readerAncestor, MultiVersionZarrReader readerBranchOne, String conflictFile) throws IOException {

        String dataset = conflictFile.substring(0, conflictFile.lastIndexOf("/"));

        String grid = conflictFile.substring(conflictFile.lastIndexOf("/") + 1);
        long[] gridPosition = toPosition(grid);

//        DataType dType = readerAncestor.getDatasetAttributes(dataset).getDataType();

//        System.out.println("Delta: dataset: " + dataset + " | Grid Position: " + Utils.format(gridPosition) + " | Data type: " + dType);

        DatasetAttributes datasetAttributes = readerAncestor.getDatasetAttributes(dataset);
        DataBlock baseBlock = readerAncestor.readBlock(dataset, datasetAttributes, gridPosition);
        DataBlock endBlock = readerBranchOne.readBlock(dataset, datasetAttributes, gridPosition);
        System.out.print("Base   :");
        Utils.printBlock(baseBlock);
        System.out.print("End    :");
        Utils.printBlock(endBlock);
        DataBlock deltaBlock = generateDeltaBlock(baseBlock, endBlock);
        return deltaBlock;
    }

    private static long[] toPosition(String grid) {
        String[] elms = grid.split(("\\."));
        long[] result = new long[elms.length];
        for (int i = 0; i < elms.length; i++)
            result[i] = Long.valueOf(elms[i]).longValue();
        return result;
    }

    private static DataBlock<?> generateDeltaBlock(DataBlock<?> baseBlock, DataBlock<?> endBlock) throws IOException {
        if (endBlock == null)
            throw new IOException("Invalid end block | Null");

        if (baseBlock == null)
            return endBlock;

        LongBuffer base = baseBlock.toByteBuffer().asLongBuffer();
        LongBuffer end = endBlock.toByteBuffer().asLongBuffer();
        long[] result = new long[end.limit()];
        for (int i = 0; i < end.limit(); i++) {
            if (base.get(i) != end.get(i))
                result[i] = end.get(i);
        }

        end.put(result);
        ByteBuffer bb = ByteBuffer.allocate(result.length * 8);
        endBlock.readData(bb);

        return endBlock;
    }

    private static void print(byte[] array) {
        String result = Arrays.toString(array);
        System.out.println(result);
    }

    public static BlockMergeResult mergeDeltas(DataBlock<?> deltaOne, DataBlock<?> deltaTwo) throws IOException {
        List<Long> blockConflicts = new ArrayList<>();
        boolean success = true;
        if (deltaOne == null || deltaTwo == null)
            throw new IOException("ERROR ! Delta is null, can't merge ! ");

        LongBuffer one = deltaOne.toByteBuffer().asLongBuffer();
        LongBuffer two = deltaTwo.toByteBuffer().asLongBuffer();
        long[] result = new long[one.limit()];
        for (int i = 0; i < one.limit(); i++) {
            if (one.get(i) == 0) {
                result[i] = two.get(i);
                continue;
            }
            if (two.get(i) == 0) {
                result[i] = one.get(i);
                continue;
            }
            result[i] = -1;
            success = false;
            blockConflicts.add(Long.valueOf(i));
        }

        two.put(result);
        ByteBuffer bb = ByteBuffer.allocate(result.length * 8);
        deltaTwo.readData(bb);

        return new BlockMergeResult(deltaTwo, blockConflicts.stream().mapToLong(l -> l).toArray(), success);
    }


    public static DataBlock<?> mergeDeltasUsingConflictVote(DataBlock<?> deltaOne, DataBlock<?> deltaTwo, int voteIfConflict) throws IOException {
        if (deltaOne == null || deltaTwo == null)
            throw new IOException("ERROR ! Delta is null, can't merge ! ");

        LongBuffer one = deltaOne.toByteBuffer().asLongBuffer();
        LongBuffer two = deltaTwo.toByteBuffer().asLongBuffer();
        long[] result = new long[one.limit()];
        for (int i = 0; i < one.limit(); i++) {
            if (one.get(i) == 0) {
                result[i] = two.get(i);
                continue;
            }
            if (two.get(i) == 0) {
                result[i] = one.get(i);
                continue;
            }
            if (voteIfConflict == 1){
                result[i] = one.get(i);
            } else if (voteIfConflict == 2) {
                result[i] = two.get(i);
            }else {
                throw new IOException("Invalid conflict vote :"+voteIfConflict);
            }
        }

        two.put(result);
        ByteBuffer bb = ByteBuffer.allocate(result.length * 8);
        deltaTwo.readData(bb);

        return deltaTwo;
    }

    public static Tuple<String, long[]> formatFileInfo(String file) {
        String dataset = file.substring(0, file.lastIndexOf("/"));

        String grid = file.substring(file.lastIndexOf("/") + 1);
        long[] gridPosition = toPosition(grid);

        return new Tuple<>(dataset, gridPosition);
    }

    public static DataBlock<?> overwriteBlock(DataBlock baseBlock, DataBlock<?> endBlock) throws IOException {
        if (endBlock == null)
            throw new IOException("Invalid end block | Null");

        if (baseBlock == null)
            return endBlock;

        LongBuffer base = baseBlock.toByteBuffer().asLongBuffer();
        LongBuffer end = endBlock.toByteBuffer().asLongBuffer();
        long[] result = new long[end.limit()];
        for (int i = 0; i < end.limit(); i++) {
            if (end.get(i) == 0) {
                result[i] = base.get(i);
            } else {
                result[i] = end.get(i);
            }
        }

        end.put(result);
        ByteBuffer bb = ByteBuffer.allocate(result.length * 8);
        endBlock.readData(bb);

        return endBlock;

    }
}
