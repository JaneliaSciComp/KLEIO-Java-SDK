package org.janelia.scicomp.v5.asbtract;

import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.janelia.saalfeldlab.n5.N5Writer;

import java.io.IOException;

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


    //TODO implement this functions
    @Override
    default boolean remove(String s) throws IOException {
        throw new IOException("Remove is not implemented for versioned storage");
    }

    @Override
    default boolean remove() throws IOException {
        throw new IOException("Remove is not implemented for versioned storage");
    }

    @Override
    default boolean deleteBlock(String s, long... longs) throws IOException {
        throw new IOException("Delete block is not implemented for versioned storage");
    }
}