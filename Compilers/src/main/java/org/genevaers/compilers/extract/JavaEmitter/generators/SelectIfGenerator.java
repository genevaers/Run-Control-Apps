package org.genevaers.compilers.extract.JavaEmitter.generators;


import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.SelectIfAST;

public class SelectIfGenerator extends ExtractRecordGenerator {
    
    private SelectIfAST selectIfAST;

    public SelectIfGenerator(SelectIfAST selectIfAST) {
        this.selectIfAST = selectIfAST;
    }   

    public void generateCode() {
        String ifFormat = "        if(%s) {\n            columnLogic(src, target, outWriter, numrecords);\n        }";
        filterRecs.add(String.format(ifFormat, getPredicate()));


    }

    private String getPredicate() {
        StringBuilder sb = new StringBuilder();
        ExtractBaseAST c1 = (ExtractBaseAST)selectIfAST.getChild(0);
        //We have been getting the nodes to add records to the records but here we want the code itself.

        return getCode(c1);
    }
}
