package org.janelia.scicomp.v5.lib.indexes;

import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.scicomp.v5.lib.tools.SessionId;
import org.janelia.scicomp.v5.lib.vc.V5VersionManager;

import java.io.IOException;

public interface V5IndexWriter<G extends V5VersionManager> extends N5Writer, N5Reader {

    G getVersionManager();

    UnsignedLongType getSession();

    void setSession(UnsignedLongType session);

    default UnsignedLongType incrementSession() {
        setSession(SessionId.getNextId());
        return getSession();
    }
    default UnsignedLongType getCurrentSession() {
        if (getSession() == null)
            return incrementSession();
        return getSession();
    }

    void set(String dataset, long[] gridPosition) throws IOException;

}
