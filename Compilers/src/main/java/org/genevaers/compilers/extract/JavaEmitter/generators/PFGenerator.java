package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.PFAstNode;

public class PFGenerator extends ExtractRecordGenerator {

    private PFAstNode pfnode;

    public PFGenerator(PFAstNode pfnode) {
        this.pfnode = pfnode;
    }

    @Override
    public void generateCode() {
        filterRecs.add("/* PF " + pfnode.getName() + " */");
     }
    
}
