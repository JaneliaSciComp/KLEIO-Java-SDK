package org.janelia.scicomp.v5.lib;

import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.scicomp.v5.lib.indexes.V5IndexWriter;

import java.io.IOException;
import java.util.Map;

public interface V5Writer<I extends V5IndexWriter, K extends N5Writer> extends N5Writer {

    I getIndexWriter();

    K getRawWriter();

    default void commit() throws IOException {
        getIndexWriter().getVersionManager().commitAll();
    }

    default void createNewBranch(String branchName) throws IOException {
        getIndexWriter().getVersionManager().createNewBranch(branchName);
    }

    default void checkoutBranch(String branchName) throws IOException {
        getIndexWriter().getVersionManager().checkoutBranch(branchName);
    }

    default void SetUserID(String userID) {
        getIndexWriter().getVersionManager().setUserID(userID);
    }

    default String getUserID() {
        return getIndexWriter().getVersionManager().getUserID();
    }

    default String getCurrentBranch() throws IOException {
       return getIndexWriter().getVersionManager().getCurrentBranch();
    }

    default UnsignedLongType incrementSession() {
        return getIndexWriter().incrementSession();
    }

    default UnsignedLongType getCurrentSession() {
        return getIndexWriter().getCurrentSession();
    }


    @Override
    default void createGroup(String pathName) throws IOException {
        getIndexWriter().createGroup(pathName);
        getRawWriter().createGroup(pathName);
    }

    @Override
    default boolean deleteBlock(String s, long... longs) throws IOException {
        throw new IOException("Delete block is not implemented for versioned storage");
    }

    @Override
    default boolean remove() throws IOException {
        boolean indexRemoved = getIndexWriter().remove();
        boolean rawRemoved = getRawWriter().remove();
        if (indexRemoved && rawRemoved)
            return true;
        return false;
    }

    @Override
    default boolean remove(String s) throws IOException {
        boolean indexRemoved = getIndexWriter().remove(s);
        boolean rawRemoved = getRawWriter().remove(s);
        if (indexRemoved && rawRemoved)
            return true;
        return false;
    }

    @Override
    default void setDatasetAttributes(String pathName, DatasetAttributes datasetAttributes) throws IOException {
        getRawWriter().setDatasetAttributes(pathName, datasetAttributes);
    }

    @Override
    default <T> void setAttribute(String pathName, String key, T attribute) throws IOException {
        getRawWriter().setAttribute(pathName, key, attribute);
    }

    @Override
    default boolean exists(String pathName) {
        return getRawWriter().exists(pathName);
    }

    @Override
    default void setAttributes(String pathName, Map<String, ?> attributes) throws IOException {
        getRawWriter().setAttributes(pathName, attributes);
    }

}