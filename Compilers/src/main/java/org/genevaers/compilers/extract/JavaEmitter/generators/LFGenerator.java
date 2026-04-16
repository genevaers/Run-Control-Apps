package org.genevaers.compilers.extract.JavaEmitter.generators;

import java.util.stream.Collectors;

import org.genevaers.compilers.extract.astnodes.LFAstNode;

public class LFGenerator extends ExtractRecordGenerator {

    private LFAstNode lfnode;

    public LFGenerator(LFAstNode lfnode) {
        this.lfnode = lfnode;
    }

    @Override
    public void generateCode() {
        inputDDnames = lfnode.getLogicalFile().getPFs().stream().map(pf -> pf.getInputDDName()).collect(Collectors.toList());
        filterRecs.add("/* LF " + lfnode.getName() + " */");
        generateFromChildNodes(lfnode);
     }
    
}
