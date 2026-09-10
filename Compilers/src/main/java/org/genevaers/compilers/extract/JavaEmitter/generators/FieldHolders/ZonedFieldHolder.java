package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.LRField;

/**
 * Holds code-generation metadata for a Zoned Decimal (EBCDIC external decimal) field.
 *
 * Maps to the JZOS ExternalDecimal* family via AssemblerDatatypeFactory:
 *
 *   decimals > 0  → ExternalDecimalAsBigDecimalField  getExternalDecimalAsBigDecimalField(length, decimals, signed)
 *   length ≤ 9    → ExternalDecimalAsIntField          getExternalDecimalAsIntField(length, signed)
 *   length ≤ 18   → ExternalDecimalAsLongField         getExternalDecimalAsLongField(length, signed)
 *   length > 18   → ExternalDecimalAsBigIntegerField   getExternalDecimalAsBigIntegerField(length, signed)
 *
 * For Zoned Decimal the field length in bytes equals the number of digits, so
 * precision == length (no packing).
 */
public class ZonedFieldHolder extends FieldHolder {

    private boolean useCompareTo = false;

    public ZonedFieldHolder(LRField f) {
        super(f);
        int length     = f.getLength();
        boolean signed = f.isSigned();
        int numDecimals = f.getNumDecimalPlaces();
        setDetails(f.getName(), length, signed, numDecimals);
    }

    public ZonedFieldHolder(String name, LRField f) {
        super(f);
        int length     = f.getLength();
        boolean signed = f.isSigned();
        int numDecimals = f.getNumDecimalPlaces();
        setDetails(name, length, signed, numDecimals);
    }

    private void setDetails(String name, int length, boolean signed, int numDecimals) {
        if (numDecimals > 0) {
            setAccessor("BigDecimal");
            useCompareTo = true;
            setDefinition(String.format(
                "private static final ExternalDecimalAsBigDecimalField %s = factory.getExternalDecimalAsBigDecimalField(%d, %d, %b);",
                name, length, numDecimals, signed));
        } else if (length <= 9) {
            setAccessor("Int");
            setDefinition(String.format(
                "private static final ExternalDecimalAsIntField %s = factory.getExternalDecimalAsIntField(%d, %b);",
                name, length, signed));
        } else if (length <= 18) {
            setAccessor("Long");
            setDefinition(String.format(
                "private static final ExternalDecimalAsLongField %s = factory.getExternalDecimalAsLongField(%d, %b);",
                name, length, signed));
        } else {
            setAccessor("BigInteger");
            useCompareTo = true;
            setDefinition(String.format(
                "private static final ExternalDecimalAsBigIntegerField %s = factory.getExternalDecimalAsBigIntegerField(%d, %b);",
                name, length, signed));
        }
    }

    @Override
    public String getAssignmentSource(int len) {
        switch (getAccessor()) {
            case "BigDecimal":
                return String.format("String.format(\"%%%d.%df\", %s.getBigDecimal(src))",
                    len, field.getNumDecimalPlaces(), field.getName());
            case "BigInteger":
                return String.format("String.format(\"%%%ds\", %s.getBigInteger(src).toString())",
                    len, field.getName());
            default:
                return String.format("String.format(\"%%%dd\", %s.get%s(src))",
                    len, field.getName(), getAccessor());
        }
    }

    @Override
    public boolean useCompareTo() {
        return useCompareTo;
    }
}
