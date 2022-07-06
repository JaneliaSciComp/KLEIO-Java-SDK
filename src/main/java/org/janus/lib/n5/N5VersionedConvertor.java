package org.janus.lib.n5;

import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5FSWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class N5VersionedConvertor {

    public static void main(String[] args) throws IOException {
        String n5_read = "/Users/zouinkhim/Downloads/car/dataset.n5";
        String dataset = "setup0/timepoint0";
        String result = "/Users/zouinkhim/Desktop/active_learning/versioned_data/dataset2.v5";
        N5FSWriter reader = new N5FSWriter(n5_read);
        VersionedN5Writer writer = N5VersionedConvertor.convert(reader,dataset,result);

    }

    public static VersionedN5Writer convert(N5FSWriter reader, String dataset, String result) throws IOException {

        VersionedN5Writer writer = new VersionedN5Writer(result);
        File[] files = new File(reader.getBasePath(), dataset).listFiles();
        for(File f: files){
            if (f.isDirectory()){
            String currentDataset = f.getName();
            System.out.println("Creating : "+currentDataset);
            DatasetAttributes datasetAttributes = reader.getDatasetAttributes(Paths.get(dataset,currentDataset).toString());
             writer.createDataset(currentDataset,datasetAttributes);
        }}
        //TODO create zarr
        // TODO create git
        // TODO write blocks
        return writer;
    }
}
