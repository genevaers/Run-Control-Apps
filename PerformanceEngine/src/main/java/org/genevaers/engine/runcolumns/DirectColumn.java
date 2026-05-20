package org.genevaers.engine.runcolumns;

import java.nio.ByteBuffer;

import org.genevaers.genevaio.recordreader.FileRecord;

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
        System.arraycopy(src, srcOffset, target, offset, length);
    }

    public static void setSource(byte[] src) {
        DirectColumn.src = src;
    }

    public static void setTarget(byte[] target) {
        DirectColumn.target = target;
    }

}
