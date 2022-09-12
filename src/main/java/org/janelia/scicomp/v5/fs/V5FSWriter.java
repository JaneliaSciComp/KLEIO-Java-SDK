package org.janelia.scicomp.v5.fs;

import com.google.gson.JsonElement;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.*;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.scicomp.v5.lib.tools.MultiscaleAttributes;
import org.janelia.scicomp.v5.lib.V5Writer;
import org.janelia.scicomp.v5.lib.indexes.N5ZarrIndexWriter;
import org.janelia.scicomp.v5.lib.vc.GitV5VersionManger;
import org.janelia.scicomp.v5.lib.uri.V5FSURL;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class V5FSWriter extends V5FSReader implements V5Writer<N5ZarrIndexWriter, N5FSWriter> {

    public V5FSWriter(String versionIndexPath, String dataStorePath) throws IOException {
        super(new N5ZarrIndexWriter(versionIndexPath), new N5FSWriter(dataStorePath), new V5FSURL(versionIndexPath, dataStorePath));
    }

    public V5FSWriter(V5FSURL v5FSURL) throws IOException {
        this(v5FSURL.getIndexesPath(), v5FSURL.getKeyValueStorePath());
    }

    public static V5FSWriter cloneFrom(String remoteIndexPath, String localIndexPath, String dataStorePath, String username) throws Exception {
        GitV5VersionManger versionManger = GitV5VersionManger.cloneFrom(remoteIndexPath, localIndexPath, username);
        versionManger.createNewBranch(username);
        return new V5FSWriter(localIndexPath, dataStorePath);
    }

    public static V5FSWriter convert(N5FSWriter reader, String dataset, String indexPath, String dataStorePath) throws IOException {
        V5FSWriter writer = new V5FSWriter(indexPath, dataStorePath);
//        create n5 dataset
        List<MultiscaleAttributes> atts = MultiscaleAttributes.generateFromN5(reader, dataset);
        String outputDataset;
        for (MultiscaleAttributes attributes : atts) {
            outputDataset = attributes.getDataset();
            String inputDataset = Paths.get(dataset, outputDataset).toString();
            System.out.println("Creating : " + outputDataset);
            DatasetAttributes datasetAttributes = reader.getDatasetAttributes(inputDataset);
            writer.createDataset(outputDataset, datasetAttributes);
            //Write blocks
            CachedCellImg<UnsignedLongType, ?> canvas = N5Utils.open(reader, inputDataset);
            long[] gridDimensions = canvas.getCellGrid().getGridDimensions();
            System.out.println("Blocks: " + Arrays.toString(gridDimensions));
            if (gridDimensions.length != 3) {
                throw new InvalidPropertiesFormatException("Grid dimension :" + gridDimensions.length + " not implemented yet!");
            }
            for (int i = 0; i < gridDimensions[0]; ++i) {
                for (int j = 0; j < gridDimensions[1]; ++j) {
                    for (int k = 0; k < gridDimensions[2]; ++k) {

                        long[] gridPosition = new long[]{i, j, k};
                        System.out.println("Writing: " + Arrays.toString(gridPosition));
                        DataBlock<?> block = reader.readBlock(inputDataset, datasetAttributes, gridPosition);
                        writer.writeBlock(outputDataset, datasetAttributes, block);
                    }
                }
            }
        }
        writer.commit();
        return writer;
    }

    @Override
    public N5ZarrIndexWriter getIndexWriter() {
        return (N5ZarrIndexWriter) indexReader;
    }

    @Override
    public N5FSWriter getRawWriter() {
        return (N5FSWriter) rawReader;
    }


    @Override
    public void createDataset(String pathName, DatasetAttributes datasetAttributes) throws IOException {
        CellGrid grid = new CellGrid(datasetAttributes.getDimensions(), datasetAttributes.getBlockSize());
        getIndexWriter().createDataset(pathName, grid.getGridDimensions());
        getRawWriter().createDataset(pathName, datasetAttributes);
    }

    @Override
    public void createDataset(String pathName, long[] dimensions, int[] blockSize, DataType dataType, Compression compression) throws IOException {
         CellGrid grid = new CellGrid(dimensions, blockSize);
        getIndexWriter().createDataset(pathName, grid.getGridDimensions());
        getRawWriter().createDataset(pathName, dimensions, blockSize, dataType, compression);
    }

    @Override
    public <T> void writeBlock(String pathName, DatasetAttributes datasetAttributes, DataBlock<T> dataBlock) throws IOException {
        long version = getIndexWriter().getCurrentSession().get();
        Path path = Paths.get(pathName, String.valueOf(version));
        getRawWriter().writeBlock(path.toString(), datasetAttributes, dataBlock);
        getIndexWriter().set(pathName, dataBlock.getGridPosition());
    }

    @Override
    public boolean deleteBlock(String pathName, long... gridPosition) throws IOException {

        boolean rawRemoved = getRawWriter().deleteBlock(pathName, gridPosition);
        getIndexWriter().set(pathName, gridPosition, new UnsignedLongType(0));
        if (rawRemoved)
            return true;
        return false;
    }

    @Override
    public boolean exists(String pathName) {
        return super.exists(pathName);
    }

    public static void main(String[] args) throws Exception {
        String n5_read = "/Users/zouinkhim/Downloads/car/dataset.n5";
        String dataset = "setup0/timepoint0";
        String indexes = "/Users/zouinkhim/Desktop/active_learning/versioned_data/fsdata/indexes";
        String dataStore = "/Users/zouinkhim/Desktop/active_learning/versioned_data/fsdata/dataStore";
        N5FSWriter reader = new N5FSWriter(n5_read);
        V5FSWriter writer = V5FSWriter.convert(reader, dataset, indexes, dataStore);
        System.out.println(writer.getCurrentBranch());
        writer.getIndexWriter().getVersionManager().setUserID("zouinkhim");
        writer.commit();
    }


}
