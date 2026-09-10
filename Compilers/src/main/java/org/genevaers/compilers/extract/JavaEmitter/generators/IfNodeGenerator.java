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
        ExtractBaseAST pred  = (ExtractBaseAST) node.getChild(0);
        ExtractBaseAST then  = (ExtractBaseAST) node.getChild(1);
        ExtractRecordGenerator predGen  = getcodeGenerator(pred);
        ExtractRecordGenerator thenGen  = getcodeGenerator(then);
        ExtractRecordGenerator elseGen  = null;
        ExtractBaseAST elseNode = null;
        if(node.getNumberOfChildren() == 3) {
            elseNode = (ExtractBaseAST) node.getChild(2);
            elseGen  = getcodeGenerator(elseNode);
        }
        return buildIfFormatString(predGen, pred, thenGen, then, elseGen, elseNode);
     }

     private String getIfFormatString() {
        return buildIfFormatString(predicateGen, predicate, thenBodyGenerator, thenBody, elseBodyGenerator, elseBody);
     }

     private String buildIfFormatString(ExtractRecordGenerator predGen, ExtractBaseAST pred,
                                        ExtractRecordGenerator thenGen, ExtractBaseAST then,
                                        ExtractRecordGenerator elseGen, ExtractBaseAST elseNode) {
        if(elseGen == null) {
            // No ELSE clause — emit a warning comment so the intent is visible in generated code
            String ifFormat = "        if(%s) {\n    %s\n        } else {\n            // WARNING: no ELSE clause in source logic\n        }";
            return String.format(ifFormat, predGen.getCode(pred), thenGen.getCode(then));
        }
        String ifFormat = "        if(%s) {\n    %s\n        } else {\n    %s\n        }";
        return String.format(ifFormat, predGen.getCode(pred), thenGen.getCode(then), elseGen.getCode(elseNode));
    }
}
