package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewSortKey;

/**
 * Field holder for SK (Sort Key) columns.
 * Fields are named SK_N where N is the sort key sequence number,
 * rather than COL_N used for normal DT columns.
 */
public class SKColumnFieldHolder extends ComponentFieldHolder {

    private final int sequenceNumber;

    public SKColumnFieldHolder(ViewColumn vc, ViewSortKey vsk) {
        super(vc);
        this.sequenceNumber = vsk.getSequenceNumber();
        setDetails(vsk, vc);
    }

    private void setDetails(ViewSortKey vsk, ViewColumn vc) {
        String skName = "SK_" + sequenceNumber;
        short len = vsk.getSkFieldLength();
        short dec = vsk.getSkDecimalCount();
        int precision = len * 2 - 1;

        switch (vsk.getSortKeyDataType()) {
            case ALPHANUMERIC:
            case ALPHA:
            case EDITED:
            case MASKED: {
                setAccessor("putString");
                setDefinition(String.format(
                        "private static final StringField %s = factory.getStringField(%d)", skName, len));
                break;
            }
            case PACKED:
            case BCD: {
                if (dec != 0) {
                    setAccessor("putBigDecimal");
                    setDefinition(String.format(
                            "private static final PackedDecimalAsBigDecimalField %s = factory.getPackedDecimalAsBigDecimalField(%d, %d, false)",
                            skName, len, dec));
                } else if (precision <= 9) {
                    setAccessor("putInt");
                    setDefinition(String.format(
                            "private static final PackedDecimalAsIntField %s = factory.getPackedDecimalAsIntField(%d, false)",
                            skName, len));
                } else if (precision <= 18) {
                    setAccessor("putLong");
                    setDefinition(String.format(
                            "private static final PackedDecimalAsLongField %s = factory.getPackedDecimalAsLongField(%d, false)",
                            skName, len));
                } else {
                    setAccessor("putBigInteger");
                    setDefinition(String.format(
                            "private static final PackedDecimalAsBigIntegerField %s = factory.getPackedDecimalAsBigIntegerField(%d, %d, false)",
                            skName, len, dec));
                }
                break;
            }
            case BINARY: {
                if (len <= 4) {
                    setAccessor("putInt");
                    setDefinition(String.format(
                            "private static final BinaryAsIntField %s = factory.getBinaryAsIntField(%d, false)",
                            skName, len));
                } else {
                    setAccessor("putLong");
                    setDefinition(String.format(
                            "private static final BinaryAsLongField %s = factory.getBinaryAsLongField(%d, false)",
                            skName, len));
                }
                break;
            }
            default: {
                setAccessor("putString");
                setDefinition(String.format(
                        "private static final StringField %s = factory.getStringField(%d)", skName, len));
                break;
            }
        }
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getAssignmentTarget() {
        return String.format("SK_%d.%s", sequenceNumber, accessor);
    }

    @Override
    public String getName() {
        return "SK_" + sequenceNumber;
    }
}
