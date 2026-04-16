package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.BooleanAndAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;

public class BooleanAndGenerator extends ExtractRecordGenerator {

    private BooleanAndAST band;

    public BooleanAndGenerator(BooleanAndAST node) {
        this.band = node;
    }

    @Override
    public void generateCode() {
        filterRecs.add("&&");
     }

     @Override
     public String getCode(ExtractBaseAST node) {
        ExtractBaseAST lhs = (ExtractBaseAST) node.getChild(0);
        ExtractBaseAST rhs = (ExtractBaseAST) node.getChild(1);
        ExtractRecordGenerator lhscg = getcodeGenerator(lhs);
        ExtractRecordGenerator rhscg = getcodeGenerator(rhs);
        return String.format("%s && %s", lhscg.getCode(lhs), rhscg.getCode(rhs));
     }   
}
