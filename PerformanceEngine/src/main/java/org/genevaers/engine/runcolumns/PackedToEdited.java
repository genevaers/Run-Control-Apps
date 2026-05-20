package org.genevaers.engine.runcolumns;

import java.nio.charset.Charset;

import org.genevaers.utilities.GersConfigration;

import com.ibm.jzos.fields.PackedDecimalAsLongField;

public class PackedToEdited extends RunColumn {

    public PackedToEdited(int columnNumber, int offset, int length, int srcOffset, int srcLength) {
        this.columnNumber = columnNumber;
        this.offset = offset;
        this.length = length;
        this.srcOffset = srcOffset;
        this.srcLength = srcLength;
    }

    public static void transformField(int srcOffset, int srcLength, int offset, int length) {
        int precision = (srcLength * 2) - 1;
        PackedDecimalAsLongField packedField = new PackedDecimalAsLongField(srcOffset, precision, true);
        long value = packedField.getLong(src.bytes.array());
        System.arraycopy(String.format("%" + length + "d", value).getBytes(Charset.forName(GersConfigration.getZosCodePage())), 0, target.bytes.array(), offset, length);
    }

}
