package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.LRField;

/**
 * Holds code-generation metadata for an unsigned Packed / BCD (Binary Coded Decimal) field.
 *
 * In GenevaERS / mainframe data formats:
 * - BCD is unsigned packed decimal (each byte stores two BCD digits 0-9 without a sign nibble,
 *   or treated similarly to unsigned packed decimal).
 * - Maps to the JZOS PackedDecimal* family with signed = false.
 *
 *   decimals > 0  → PackedDecimalAsBigDecimalField  getPackedDecimalAsBigDecimalField(length, decimals, false)
 *   precision ≤ 9 → PackedDecimalAsIntField         getPackedDecimalAsIntField(length, false)
 *   precision ≤ 18→ PackedDecimalAsLongField        getPackedDecimalAsLongField(length, false)
 *   precision ≤ 31→ PackedDecimalAsBigIntegerField  getPackedDecimalAsBigIntegerField(length, decimals, false)
 */
public class BCDFieldHolder extends FieldHolder {

    private boolean useCompareTo = false;

    public BCDFieldHolder(LRField f) {
        super(f);
        field = f;
        int length = f.getLength();
        int numDecimals = f.getNumDecimalPlaces();
        int precision = length * 2;
        setDetails(f.getName(), length, numDecimals, precision);
    }

    public BCDFieldHolder(String name, LRField f) {
        super(f);
        field = f;
        int length = f.getLength();
        int numDecimals = f.getNumDecimalPlaces();
        int precision = length * 2;
        setDetails(name, length, numDecimals, precision);
    }

    private void setDetails(String name, int length, int numDecimals, int precision) {
        if (numDecimals > 0) {
            setAccessor("BigDecimal");
            useCompareTo = true;
            setDefinition(String.format(
                "private static final PackedDecimalAsBigDecimalField %s = factory.getPackedDecimalAsBigDecimalField(%d, %d, false);",
                name, length, numDecimals));
        } else if (numDecimals < 0) {
            setAccessor("BigDecimal");
            useCompareTo = true;
            setDefinition(String.format(
                "private static final PackedDecimalAsBigDecimalField %s = factory.getPackedDecimalAsBigDecimalField(%d, %d, false);",
                name, length, numDecimals));
        } else if (precision <= 9) {
            setAccessor("Int");
            setDefinition(String.format(
                "private static final PackedDecimalAsIntField %s = factory.getPackedDecimalAsIntField(%d, false);",
                name, length));
        } else if (precision <= 18) {
            setAccessor("Long");
            setDefinition(String.format(
                "private static final PackedDecimalAsLongField %s = factory.getPackedDecimalAsLongField(%d, false);",
                name, length));
        } else if (precision <= 31) {
            setAccessor("BigInteger");
            useCompareTo = true;
            setDefinition(String.format(
                "private static final PackedDecimalAsBigIntegerField %s = factory.getPackedDecimalAsBigIntegerField(%d, %d, false);",
                name, length, numDecimals));
        } else {
            setAccessor("length too long");
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
