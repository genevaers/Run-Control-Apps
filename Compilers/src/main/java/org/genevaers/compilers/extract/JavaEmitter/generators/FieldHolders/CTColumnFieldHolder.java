package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.ViewColumn;

/**
 * Field holder for CT (Calculation) columns.
 * CT columns are always PACKED; fields are named CT_N where N is the column number.
 */
public class CTColumnFieldHolder extends ComponentFieldHolder {

    private final int columnNumber;

    public CTColumnFieldHolder(ViewColumn vc) {
        super(vc);
        this.columnNumber = vc.getColumnNumber();
        int len = vc.getFieldLength();
        int dec = vc.getDecimalCount();
        boolean signed = vc.isSigned();
        int precision = len * 2 - 1;
        String ctName = "CT_" + columnNumber;
        if (dec != 0) {
            setAccessor("putBigDecimal");
            setDefinition(String.format(
                "private static final PackedDecimalAsBigDecimalField %s = factory.getPackedDecimalAsBigDecimalField(%d, %d, %b)",
                ctName, len, dec, signed));
        } else if (precision <= 9) {
            setAccessor("putInt");
            setDefinition(String.format(
                "private static final PackedDecimalAsIntField %s = factory.getPackedDecimalAsIntField(%d, %b)",
                ctName, len, signed));
        } else if (precision <= 18) {
            setAccessor("putLong");
            setDefinition(String.format(
                "private static final PackedDecimalAsLongField %s = factory.getPackedDecimalAsLongField(%d, %b)",
                ctName, len, signed));
        } else {
            setAccessor("putBigInteger");
            setDefinition(String.format(
                "private static final PackedDecimalAsBigIntegerField %s = factory.getPackedDecimalAsBigIntegerField(%d, %d, %b)",
                ctName, len, dec, signed));
        }
    }

    public String getAssignmentTarget() {
        return String.format("CT_%d.%s", columnNumber, accessor);
    }

    @Override
    public String getName() {
        return "CT_" + columnNumber;
    }
}
