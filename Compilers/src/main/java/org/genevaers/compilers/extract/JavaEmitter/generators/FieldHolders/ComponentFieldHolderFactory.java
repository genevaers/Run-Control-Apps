package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DataType;

public class ComponentFieldHolderFactory {


    public static FieldHolder getLRFieldHolder(LRField lrf) {
        return getComponentField(lrf.getName(), lrf, lrf.getDatatype(), lrf.getLength(), lrf.isSigned(), lrf.getNumDecimalPlaces());
    }

    public static ColumnFieldHolder getColumnFieldHolder(ViewColumn col) {
        return new ColumnFieldHolder(col);
    }

    public static ComponentFieldHolder getLookupLRFieldHolder(String lkfname, LRField lrf) {
        return getComponentField(lkfname, lrf, lrf.getDatatype(), lrf.getLength(), lrf.isSigned(), lrf.getNumDecimalPlaces());
    }

    private static FieldHolder getComponentField(String name, LRField fld, DataType dataType, short length, boolean signed, int numDecimals) {
        FieldHolder cfh = null;
        switch(dataType) {
            case ALPHA:
                break;
            case ALPHANUMERIC: {
                //To cater for redefines we will need to set the offset directly - or correct for redefines
                //Detect redefines as we iterate through the fields?
                cfh = new StringFieldHolder(fld);
                cfh.setAccessor("getString");
                cfh.setDefinition(String.format("private static final StringField %s = factory.getStringField(%d)", name, length));
                break;
            }
            case BCD:
                break;
            case BINARY:
                return new BinaryFieldHolder(fld);
            case BSORT:
                break;
            case CONSTDATE:
                break;
            case CONSTNUM:
                break;
            case CONSTSTRING:
                break;
            case EDITED: {
                cfh = new StringFieldHolder(fld);
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
                return new PackedFieldHolder(name, fld);
            }
            case PSORT:
                break;
            case ZONED:
                break;
            default: {
                cfh = new FieldHolder(fld);
                cfh.setAccessor("Default");
                cfh.setDefinition(String.format("//private static final TBD %s = factory.getStringField(%d)", name, length));
                return cfh;
            }
            
        }
        return cfh;
    }
    
}
