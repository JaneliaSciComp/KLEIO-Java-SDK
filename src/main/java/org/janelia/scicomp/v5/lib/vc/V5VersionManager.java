package org.janelia.scicomp.v5.lib.vc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class V5VersionManager {
    protected List<long[]> uncommittedBlocks = new ArrayList<>();

    protected String userID;

    public String getUserID() {
        return userID;
    }

    public List<long[]> getUncommittedBlocks() {
        return uncommittedBlocks;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void addUncommittedBlock(long[] position) {
        this.uncommittedBlocks.add(position);
    }

    public void resetUncommittedBlock() {
        this.uncommittedBlocks.clear();
    }

    public abstract void commitAll() throws IOException;

    public abstract void createNewBranch(String branchName) throws IOException;

    public abstract void checkoutBranch(String branchName) throws IOException;

    public abstract String getCurrentBranch() throws IOException;

}
