package org.genevaers.engine.runcolumns;

import org.genevaers.genevaio.recordreader.FileRecord;

public abstract class RunColumn {
    protected int columnNumber;
    protected int offset;
    protected int length;
    protected int srcOffset;
    protected int srcLength;

    protected static byte[] src;
    protected static byte[] target;

    //public abstract void transformField(int offset, int length, int srcOffset, int srcLength);
}
