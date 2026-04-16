package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ExtractFilterAST;

public class ExtractFilterGenerator extends ExtractRecordGenerator {

    private ExtractFilterAST filter;

    public ExtractFilterGenerator(ExtractFilterAST filter) {
        this.filter = filter;
    }

    @Override
    public void generateCode() {
        filterRecs.add("/* " + filter.getLogicText() + "\n */");        
        JoinGenerator.generateFilterCode();
        generateFromChildNodes(filter);
    }
}
