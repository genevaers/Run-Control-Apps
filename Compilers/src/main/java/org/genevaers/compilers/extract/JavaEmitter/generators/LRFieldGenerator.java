package org.genevaers.compilers.extract.JavaEmitter.generators;

import java.util.Map;

import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.ComponentFieldHolder;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.repository.components.LRField;

public class LRFieldGenerator extends ExtractRecordGenerator {

    private FieldReferenceAST fieldnode;

    public LRFieldGenerator(FieldReferenceAST fieldnode) {
        this.fieldnode = fieldnode;
    }

    @Override
    public void generateCode() {
    }
    
     @Override
     public String getCode(ExtractBaseAST node) {
        FieldReferenceAST fn = (FieldReferenceAST) node;
        LRField fld = fn.getRef();
        //This will be dependent on the type of the field, for now we will assume all fields are strings and use the String
        switch (fld.getDatatype()) {
            case ALPHA:
                break;
            case ALPHANUMERIC:
                break;
            case BCD:
                break;
            case BINARY:
                break;
            case BSORT:
                break;
            case CONSTDATE:
                break;
            case CONSTNUM:
                break;
            case CONSTSTRING:
                break;
            case EDITED:
                break;
            case FLOAT:
                break;
            case GENEVANUMBER:
                break;
            case INVALID:
                break;
            case MASKED:
                break;
            case PACKED:
                //PRICE.getBigDecimal(src).compareTo(BigDecimal_100)
                //if bigDecimal
                if(sourceFieldHolders.get(fld.getName()).getAccessor().startsWith("Big")) {
                    return String.format("%s.get%s(src).compareTo", fld.getName(),  sourceFieldHolders.get(fld.getName()).getAccessor());
                } else {
                    return String.format("%s.get%s(src)", fld.getName(),  sourceFieldHolders.get(fld.getName()).getAccessor());
                }
                //return fld.getName() + ".get" + sourceFieldHolders.get(fld.getName()).getAccessor() +"()";
            case PSORT:
                break;
            case ZONED:
                break;
            default:
                break;

        }
        return fld.getName();
     }
}
