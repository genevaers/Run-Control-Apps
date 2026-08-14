package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.NumAtomAST;

public class NumAtomGenerator extends ExtractRecordGenerator {

    private NumAtomAST fieldnode;

    public NumAtomGenerator(NumAtomAST node) {
        this.fieldnode = node;
    }

    @Override
    public void generateCode() {
    }
    
     @Override
     public String getCode(ExtractBaseAST node) {
        NumAtomAST sa = (NumAtomAST) node;
        //return String.format("%s", sa.getValue());
        //Const name should have been setup
        if(constName != null ) {
            return getConstName();
        } else {
            return sa.getValueString();
        }
     }
}
