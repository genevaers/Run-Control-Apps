package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.ViewColumn;

public class ColumnFieldHolder extends ComponentFieldHolder {

    private ViewColumn viewcolumn;

    public ColumnFieldHolder(ViewColumn c) {
        super(c);
        setDetails(c);
        viewcolumn = c;
    }

    private void setDetails(ViewColumn c) {
        String colName = "COL_" + c.getColumnNumber();
        int len = c.getFieldLength();
        int dec = c.getDecimalCount();
        boolean signed = c.isSigned();
        int precision = len * 2 - 1;
        switch(c.getDataType()) {
            case ALPHANUMERIC:
            case ALPHA:
            case EDITED:
            case MASKED: {
                setAccessor("putString");
                setDefinition(String.format(
                    "private static final StringField %s = factory.getStringField(%d)", colName, len));
                break;
            }
            case PACKED:
            case BCD: {
                // Output column holds packed decimal bytes; use the appropriate typed field.
                if (dec != 0) {
                    setAccessor("putBigDecimal");
                    setDefinition(String.format(
                        "private static final PackedDecimalAsBigDecimalField %s = factory.getPackedDecimalAsBigDecimalField(%d, %d, %b)",
                        colName, len, dec, signed));
                } else if (precision <= 9) {
                    setAccessor("putInt");
                    setDefinition(String.format(
                        "private static final PackedDecimalAsIntField %s = factory.getPackedDecimalAsIntField(%d, %b)",
                        colName, len, signed));
                } else if (precision <= 18) {
                    setAccessor("putLong");
                    setDefinition(String.format(
                        "private static final PackedDecimalAsLongField %s = factory.getPackedDecimalAsLongField(%d, %b)",
                        colName, len, signed));
                } else {
                    setAccessor("putBigInteger");
                    setDefinition(String.format(
                        "private static final PackedDecimalAsBigIntegerField %s = factory.getPackedDecimalAsBigIntegerField(%d, %d, %b)",
                        colName, len, dec, signed));
                }
                break;
            }
            case ZONED: {
                // ZONED output columns: write using ExternalDecimal* field types,
                // mirroring the same length/decimal branching used by ZonedFieldHolder.
                if (dec != 0) {
                    setAccessor("putBigDecimal");
                    setDefinition(String.format(
                        "private static final ExternalDecimalAsBigDecimalField %s = factory.getExternalDecimalAsBigDecimalField(%d, %d, %b)",
                        colName, len, dec, signed));
                } else if (len <= 9) {
                    setAccessor("putInt");
                    setDefinition(String.format(
                        "private static final ExternalDecimalAsIntField %s = factory.getExternalDecimalAsIntField(%d, %b)",
                        colName, len, signed));
                } else if (len <= 18) {
                    setAccessor("putLong");
                    setDefinition(String.format(
                        "private static final ExternalDecimalAsLongField %s = factory.getExternalDecimalAsLongField(%d, %b)",
                        colName, len, signed));
                } else {
                    setAccessor("putBigInteger");
                    setDefinition(String.format(
                        "private static final ExternalDecimalAsBigIntegerField %s = factory.getExternalDecimalAsBigIntegerField(%d, %b)",
                        colName, len, signed));
                }
                break;
            }
            case BINARY: {
                // Output column holds binary bytes; use the appropriate typed field.
                // Mirrors BinaryFieldHolder length logic: lengths 1–4 (signed special-case) → Int, 5–8 → Long.
                if ((len <= 0 || len >= 4) && (len != 4 || !signed)) {
                    if (len <= 8) {
                        setAccessor("putLong");
                        setDefinition(String.format(
                            "private static final BinaryAsLongField %s = factory.getBinaryAsLongField(%d, %b)",
                            colName, len, signed));
                    } else {
                        // Lengths > 8 not supported by JZOS binary fields; fall back to string.
                        setAccessor("putString");
                        setDefinition(String.format(
                            "// WARNING: column %s binary length %d > 8 — using StringField fallback\n    private static final StringField %s = factory.getStringField(%d)",
                            colName, len, colName, len));
                    }
                } else {
                    setAccessor("putInt");
                    setDefinition(String.format(
                        "private static final BinaryAsIntField %s = factory.getBinaryAsIntField(%d, %b)",
                        colName, len, signed));
                }
                break;
            }
            default: {
                setAccessor("putString");
                setDefinition(String.format(
                    "// WARNING: column %s has unsupported data type %s — using StringField fallback\n    private static final StringField %s = factory.getStringField(%d)",
                    colName, c.getDataType(), colName, len));
                break;
            }
        }
    }

    public String getAssignmentTarget() {
        return String.format("COL_%d.%s", viewcolumn.getColumnNumber(), accessor);
    }

    @Override
    public String getName() {
        return "COL_" + viewcolumn.getColumnNumber();
    }

}
