package org.janelia.scicomp.v5;

import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.scicomp.v5.fs.V5FSWriter;

import java.io.IOException;

public class V5FSTest extends AbstractN5Test {
    static private String indexesTestDirPath = System.getProperty("user.home") + "/tmp/n5-test/indexes";
    static private String rawTestDirPath = System.getProperty("user.home") + "/tmp/n5-test/raw_data";

    /**
     * @throws IOException
     */
    @Override
    protected N5Writer createN5Writer() throws IOException {
        System.out.println(indexesTestDirPath);

        return new V5FSWriter(indexesTestDirPath,rawTestDirPath);
    }


}
