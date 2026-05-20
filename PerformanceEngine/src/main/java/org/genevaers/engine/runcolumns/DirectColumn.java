package org.genevaers.engine.runcolumns;

public class DirectColumn extends RunColumn {

    public DirectColumn(int columnNumber, int offset, int length, int srcOffset, int srcLength) {
        this.columnNumber = columnNumber;
        this.offset = offset;
        this.length = length;
        this.srcOffset = srcOffset;
        this.srcLength = srcLength;
    }

    public static void transformField(int srcOffset, int srcLength,  int offset, int length ) {
        // Copy directly from source to target
        System.arraycopy(src.bytes.array(), srcOffset, target.bytes.array(), offset, length);
    }

}
