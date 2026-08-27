package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.ViewColumn;

public class ColumnFieldHolder extends ComponentFieldHolder {

    private ViewColumn viewcolumn;

    public ColumnFieldHolder(ViewColumn c) {
        super(c);
        setDetails(c);
        viewcolumn = c;
    }

    //Maybe the getAccessor here switches when needed
    //And get whatever

    private void setDetails(ViewColumn c) {
        switch(c.getDataType()) {
            case ALPHA:
                break;
            case ALPHANUMERIC: {
                //To cater for redefines we will need to set the offset directly - or correct for redefines
                //Detect redefines as we iterate through the fields?
                setAccessor("getString");
                setDefinition(String.format("private static final StringField %s = factory.getStringField(%d)", "COL_" + c.getColumnNumber(), c.getFieldLength()));
                break;
            }
            case BCD:
                break;
            case BINARY:
                //return addBinaryField(name, node, length, signed);
            case BSORT:
                break;
            case CONSTDATE:
                break;
            case CONSTNUM:
                break;
            case CONSTSTRING:
                break;
            case EDITED: {
                setAccessor("getString");
                setDefinition(String.format("private static final StringField %s = factory.getStringField(%d)", "COL_" + c.getColumnNumber(), c.getFieldLength()));
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
                //return getPackedField(name, node, length, signed, numDecimals);
            }
            case PSORT:
                break;
            case ZONED:
                break;
            default: {
                setAccessor("Default");
                setDefinition(String.format("//private static final TBD %s = factory.getStringField(%d)", "COL_" + c.getColumnNumber(), c.getFieldLength()));
                break;
            }
            
        }
    }

    public String getAssignmentTarget() {
        return String.format("COL_%d.putString" , viewcolumn.getColumnNumber());
    }

    @Override
    public String getName() {
        return "COL_" + viewcolumn.getColumnNumber();
    }

}
