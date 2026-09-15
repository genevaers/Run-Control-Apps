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
        switch(dataType) {
            case ALPHANUMERIC: {
                FieldHolder cfh = new StringFieldHolder(fld);
                cfh.setAccessor("getString");
                cfh.setDefinition(String.format("private static final StringField %s = factory.getStringField(%d)", name, length));
                return cfh;
            }
            case EDITED: {
                FieldHolder cfh = new StringFieldHolder(fld);
                cfh.setAccessor("getString");
                cfh.setDefinition(String.format("private static final StringField %s = factory.getStringField(%d); //For Edited Numeric", name, length));
                return cfh;
            }
            case BCD:
                return new BCDFieldHolder(name, fld);
            case BINARY:
                return new BinaryFieldHolder(fld);
            case PACKED:
                return new PackedFieldHolder(name, fld);
            case ZONED:
                return new ZonedFieldHolder(name, fld);
            default: {
                // Unsupported or unrecognised data type — return a sentinel holder that
                // emits a comment in the generated source so the problem is visible.
                FieldHolder cfh = new FieldHolder(fld);
                cfh.setAccessor("getString");
                cfh.setDefinition(String.format(
                    "// WARNING: field %s has unsupported data type %s — skipped", name, dataType));
                return cfh;
            }
        }
    }
    
}
