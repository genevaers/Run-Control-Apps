package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.LookupPathAST;
import org.genevaers.repository.Repository;

public class LookupInfo {
    private int lookupId;
    private String lookupName;
    private int level;
    private boolean hidden;
    private LookupPathAST lkast;
    private int recLength;
    private int keyLength;
    private int fileid;

    public LookupInfo(LookupPathAST lkast, int depth) {
        this.lookupId = lkast.getNewJoinId();
        this.lookupName = lkast.getLookup().getName();
        this.level = depth;
        this.hidden = false;
        this.lkast = lkast;
        recLength = Repository.getLRLength(lkast.getLookup().getTargetLRID());
        keyLength = Repository.getLrKeyLen(lkast.getLookup().getTargetLRID());
        fileid = lkast.getNewJoinId();
    }

    public int getLookupId() {
        return lookupId;
    }

    public String getLookupName() {
        return lookupName;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public LookupPathAST getLkast() {
        return lkast;
    }

    public String getNewid() {
        return String.format("%d/%d", lkast.getLookup().getTargetLFID(), lkast.getLookup().getTargetLRID());
    }

    public int getTargetLf() {
        return lkast.getLookup().getTargetLFID();
    }

    public int getTargetLr() {
        return lkast.getLookup().getTargetLRID();
    }

    public int getRecLength() {
        return recLength;
    }   

    public int getKeyLength() {
        return keyLength;
    }

    public int getFileid() {
        return fileid;
    }
}
