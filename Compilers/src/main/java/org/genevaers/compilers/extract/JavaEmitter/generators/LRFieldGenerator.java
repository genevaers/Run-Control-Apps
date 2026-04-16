package org.genevaers.compilers.extract.JavaEmitter.generators;

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
        return String.format("new String(src, %d , %d)", fld.getStartPosition()-1, fld.getLength());
     }
}
