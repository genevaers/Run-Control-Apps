package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.ComponentNode;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DataType;

public class ComponentFieldHolderFactory {

    public static ComponentFieldHolder getComponentField(String name, ComponentNode node, DataType dataType, short length, boolean signed, int numDecimals) {
        ComponentFieldHolder cfh = null;
        switch(dataType) {
            case ALPHA:
                break;
            case ALPHANUMERIC: {
                //To cater for redefines we will need to set the offset directly - or correct for redefines
                //Detect redefines as we iterate through the fields?
                cfh = new ComponentFieldHolder(node);
                cfh.setAccessor("getString");
                cfh.setDefinition(String.format("private static final StringField %s = factory.getStringField(%d)", name, length));
                break;
            }
            case BCD:
                break;
            case BINARY:
                return addBinaryField(name, node, length, signed);
            case BSORT:
                break;
            case CONSTDATE:
                break;
            case CONSTNUM:
                break;
            case CONSTSTRING:
                break;
            case EDITED: {
                cfh = new ComponentFieldHolder(node);
                cfh.setAccessor("getString");
                cfh.setDefinition(String.format("private static final StringField %s = factory.getStringField(%d); //For Edited Numeric", name, length));
                break;
            }
            case FLOAT:
                break;
            case GENEVANUMBER:
                break;
            case INVALID:
                break;
            case MASKED:
                break;
            case PACKED: {
                return getPackedField(name, node, length, signed, numDecimals);
            }
            case PSORT:
                break;
            case ZONED:
                break;
            default: {
                cfh = new ComponentFieldHolder(node);
                cfh.setAccessor("Default");
                cfh.setDefinition(String.format("//private static final TBD %s = factory.getStringField(%d)", name, length));
                return cfh;
            }
            
        }
        return cfh;
    }
    

    public static ComponentFieldHolder getLRFieldHolder(LRField lrf) {
        return getComponentField(lrf.getName(), lrf, lrf.getDatatype(), lrf.getLength(), lrf.isSigned(), lrf.getNumDecimalPlaces());
    }

    public static ComponentFieldHolder getColumnFieldHolder(ViewColumn col) {
        return getComponentField("COL_" + col.getColumnNumber(), col, col.getDataType(), col.getFieldLength(), col.isSigned(), col.getDecimalCount());
    }

    private static ComponentFieldHolder addBinaryField(String name, ComponentNode node, short length, boolean signed) {
        ComponentFieldHolder cfh = new ComponentFieldHolder(node);
        if ((length <= 0 || length >= 4) && (length != 4 || !signed)) {
            if (length <= 8) {
                cfh.setAccessor("Long");
                cfh.setDefinition(String.format("private static final BinaryAsLongField %s = factory.getBinaryAsLongField(%d, %b)", name, length, signed));
            } else if (length > 8) {
                cfh.setAccessor("BigInteger");
                cfh.setDefinition(String.format("private static final BinaryAsBigIntegerField %s = factory.getBinaryAsBigIntegerField(%d, %b)", name, length, signed));
            } else {
                cfh.setAccessor("illegal length");
            }
        } else {
                cfh.setAccessor("Int");
                cfh.setDefinition(String.format("private static final BinaryAsIntField %s = factory.getBinaryAsIntField(%d, %b)", name, length, signed));
        }
        return cfh;
    }

    private static ComponentFieldHolder getPackedField(String name, ComponentNode node, short length, boolean signed, int numDecimals) {
        // private static final PackedDecimalAsIntField AdmissionDate =
        // factory.getPackedDecimalAsIntField(7, true);
        // Different lengths mean use different converters
        int precision = length * 2 - 1;
        ComponentFieldHolder fh = new ComponentFieldHolder(node);
        if (numDecimals > 0) {
            fh.setAccessor("BigDecimal");
            fh.setDefinition(String.format("private static final PackedDecimalAsBigDecimalField %s = factory.getPackedDecimalAsBigDecimalField(%d, %d, %b);", name, length, numDecimals, signed));
        } else if (numDecimals < 0) {
            fh.setAccessor("BigDecimal");
            fh.setDefinition(String.format("private static final PackedDecimalAsBigDecimalField %s = factory.getPackedDecimalAsBigDecimalField(%d, %d, %b);", name, length, numDecimals, signed));
        } else if (precision <= 9) {
            fh.setAccessor("Int");
            fh.setDefinition(String.format("private static final PackedDecimalAsIntField %s = factory.getPackedDecimalAsIntField(%d, %b);", name, length, signed));
        } else if (precision <= 18) {
            fh.setAccessor("Long");
            fh.setDefinition(String.format("private static final PackedDecimalAsLongField %s = factory.getPackedDecimalAsLongField(%d, %b);", name, length, signed));
        } else if (precision <= 31) {
            fh.setAccessor("BigInteger");
            fh.setDefinition(String.format("private static final PackedDecimalAsBigIntegerField %s = factory.getPackedDecimalAsBigIntegerField(%d, %d, %b);", name, length, numDecimals, signed));
        } else {
            fh.setAccessor("length too long");
        }
        return fh;
    }


    public static ComponentFieldHolder getLookupLRFieldHolder(String lkfname, LRField lrf) {
        return getComponentField(lkfname, lrf, lrf.getDatatype(), lrf.getLength(), lrf.isSigned(), lrf.getNumDecimalPlaces());
    }

}
