package org.janelia.scicomp.v5.lib.tools;

import org.janelia.saalfeldlab.n5.N5DatasetDiscoverer;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5TreeNode;
import org.janelia.saalfeldlab.n5.ij.N5Factory;
import org.janelia.saalfeldlab.n5.ij.N5Importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class MultiscaleAttributes {
    final String dataset;
    final long[] dimensions;
    final int[] blockSize;
    final long[] gridSize;

    public MultiscaleAttributes(String dataset, long[] dimensions, int[] blockSize) {
        this.dataset = dataset;
        this.dimensions = dimensions;
        this.blockSize = blockSize;
        this.gridSize = getGridSize(dimensions, blockSize);
    }

    public static long[] getGridSize(long[] dimensions, int[] blockSize) {
        long[] result = new long[dimensions.length];
        for (int i = 0; i < dimensions.length; i++) {
            int x = (int) (dimensions[i] / blockSize[i]);
            if ((long) blockSize[i] * x < dimensions[i])
                x++;
            result[i] = x;
        }
        return result;
    }

    public static List<MultiscaleAttributes> generateFromN5(String n5path, String dataset) throws IOException {
        return generateFromN5(new N5Factory().openReader(n5path), dataset);
    }

    public static List<MultiscaleAttributes> generateFromN5(N5Reader n5, String dataset) throws IOException {
        List<MultiscaleAttributes> result = new ArrayList<>();

        N5DatasetDiscoverer parsers = new N5DatasetDiscoverer(
                Executors.newSingleThreadExecutor(),
                N5Importer.GROUP_PARSERS,
                N5Importer.PARSERS);

        N5TreeNode root = parsers.discoverRecursive(n5, dataset);

        for (N5TreeNode t : root.childrenList()) {
            result.add(new MultiscaleAttributes(t.getPath().replace(dataset, ""), t.getMetadata().getAttributes().getDimensions(), t.getMetadata().getAttributes().getBlockSize()));
        }
        return result;
    }


    @Override
    public String toString() {
        return "MultiscaleAttributes{" +
                "dataset='" + dataset + '\'' +
                ", dimensions=" + Arrays.toString(dimensions) +
                ", blockSize=" + Arrays.toString(blockSize) +
                ", gridSize=" + Arrays.toString(gridSize) +
                '}';
    }

    public String getDataset() {
        return dataset;
    }

    public long[] getDimensions() {
        return dimensions;
    }

    public int[] getBlockSize() {
        return blockSize;
    }

    public long[] getGridSize() {
        return gridSize;
    }

    public static void main(String[] args) {
        long[] grid = getGridSize(new long[]{600, 600, 600}, new int[]{128, 128, 128});
        for (long i : grid)
            System.out.print(i + " ");

    }
}
