package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;

public class ExtractBaseGenerator extends ExtractRecordGenerator {
    
    private ExtractBaseAST erb;

    public ExtractBaseGenerator(ExtractBaseAST node) {
        this.erb = node;
    }

    @Override
    public void generateCode() {
        generateFromChildNodes((ExtractBaseAST) erb);
     }
}
