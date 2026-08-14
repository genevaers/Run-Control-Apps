package org.genevaers.compilers.extract.JavaEmitter.generators;


import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.SelectIfAST;

public class SelectIfGenerator extends ExtractRecordGenerator {
    
    private SelectIfAST selectIfAST;

    public SelectIfGenerator(SelectIfAST selectIfAST) {
        this.selectIfAST = selectIfAST;
    }   

    public void generateCode() {
        //need to look for constant declaraions
        //Probably in a more general way to
        //Before columns we can get them too
        //So look at whole tree for the view source and add them to a constants declarations list
        //Like the fields and columns 
        //Build them at the time we search for lookups
        //problem will be how to get the type - need to know what the other side is?
        //So add them to the consts declarations as we find them?

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
