package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.StringAtomAST;

public class StringAtomGenerator extends ExtractRecordGenerator {

    private StringAtomAST fieldnode;

    public StringAtomGenerator(StringAtomAST fieldnode) {
        this.fieldnode = fieldnode;
    }

    @Override
    public void generateCode() {
    }
    
     @Override
     public String getCode(ExtractBaseAST node) {
        StringAtomAST sa = (StringAtomAST) node;
        return String.format("\"%s\"", sa.getValue());
     }
}
