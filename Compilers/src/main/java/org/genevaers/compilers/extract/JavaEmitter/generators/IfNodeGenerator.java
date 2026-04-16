package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.IfAST;

public class IfNodeGenerator extends ExtractRecordGenerator {
    private IfAST ifNode;
    private ExtractRecordGenerator predicateGen;
    private ExtractRecordGenerator thenBodyGenerator;
    private ExtractRecordGenerator elseBodyGenerator;
    private ExtractBaseAST predicate;
    private ExtractBaseAST thenBody;
    private ExtractBaseAST elseBody;

    public IfNodeGenerator(IfAST node) {
        this.ifNode = node;
    }

    @Override
    public void generateCode() {
        predicate = (ExtractBaseAST) ifNode.getChild(0);
        //predicate.emit();
        predicateGen = getcodeGenerator(predicate);

        thenBody = (ExtractBaseAST) ifNode.getChild(1);
        thenBodyGenerator = getcodeGenerator(thenBody);
        //thenBody.emit();
        if(ifNode.getNumberOfChildren() == 3) {
            elseBody = (ExtractBaseAST) ifNode.getChild(2);
            elseBodyGenerator = getcodeGenerator(elseBody);
            //e//lseBody.emit();
        }
        columnRecs.add(getIfFormatString());
    }

     @Override
     public String getCode(ExtractBaseAST node) {
        return "IF Code TBD";
     }

     private String getIfFormatString() {
        String ifFormat = "        if(%s) {\n    %s\n        } else {\n    %s\n        }";
        return String.format(ifFormat, predicateGen.getCode(predicate), thenBodyGenerator.getCode(thenBody), elseBodyGenerator.getCode(elseBody));
    }
}
