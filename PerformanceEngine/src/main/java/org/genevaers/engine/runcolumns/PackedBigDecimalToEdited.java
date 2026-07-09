package org.genevaers.engine.runcolumns;

import java.math.BigDecimal;
import java.nio.charset.Charset;

import org.genevaers.utilities.GersConfigration;

import com.ibm.jzos.fields.BigDecimalAccessor;

//Do we need different versions based on the field accessor type
//Eg PackecBigDecimalToEdited
//At run time we want to avoid the if on type for probably
public class PackedBigDecimalToEdited extends RunColumn {

    public PackedBigDecimalToEdited(int columnNumber, int offset, int length, int srcOffset, int srcLength) {
        this.columnNumber = columnNumber;
        this.offset = offset;
        this.length = length;
        this.srcOffset = srcOffset;
        this.srcLength = srcLength;
    }

    public static void transformField(byte[] src, BigDecimalAccessor fld, int offset, int length) {
        BigDecimal value = fld.getBigDecimal(src);
        System.out.println("PackedToEdited " + value.toString());
        System.arraycopy(String.format("%" + length + "f", value).getBytes(Charset.forName(GersConfigration.getZosCodePage())), 0, target, offset, length);
    }

}
