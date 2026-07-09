package org.genevaers.engine.runcolumns;

import java.nio.charset.Charset;

import com.ibm.jzos.fields.ExternalDecimalAsLongField;
import com.ibm.jzos.fields.DatatypeFactory;

import org.genevaers.utilities.GersConfigration;

public class ZonedToEdited extends RunColumn {

    public ZonedToEdited(int columnNumber, int offset, int length, int srcOffset, int srcLength) {
        this.columnNumber = columnNumber;
        this.offset = offset;
        this.length = length;
        this.srcOffset = srcOffset;
        this.srcLength = srcLength;
    }

    public static void transformField(byte[] src, int srcOffset, int srcLength, int offset, int length) {
        ExternalDecimalAsLongField decimalField = new ExternalDecimalAsLongField(srcOffset, srcLength*2, false, false, false, false);
        long value = decimalField.getLong(src);  
        byte[] bytes = String.format("%" + length + "s", value).getBytes(Charset.forName(GersConfigration.getZosCodePage()));
        System.arraycopy(bytes, 0, target, offset, length);
    }
}
