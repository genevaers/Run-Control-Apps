package org.genevaers.engine.runcolumns;

import java.nio.charset.Charset;

import org.genevaers.utilities.GersConfigration;

public class Bin2ToEdited extends RunColumn {

    public Bin2ToEdited(int columnNumber, int offset, int length, int srcOffset, int srcLength) {
        this.columnNumber = columnNumber;
        this.offset = offset;
        this.length = length;
        this.srcOffset = srcOffset;
        this.srcLength = srcLength;
    }

    public static void transformField(byte[] src, int srcOffset, int srcLength, int offset, int length) {
        int value = ((src[srcOffset] & 0xFF) << 8) |
                        ((src[srcOffset + 1] & 0xFF));
        System.arraycopy(String.format("%" + length + "d", value).getBytes(Charset.forName(GersConfigration.getZosCodePage())), 0, target, offset, length);
    }

}
