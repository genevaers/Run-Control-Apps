package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.genevaers.compilers.extract.astnodes.ExprComparisonAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;

public class ExprComparisonGenerator extends ExtractRecordGenerator {

    private ExprComparisonAST exprComp;
    private ExtractBaseAST lhs;
    private ExtractBaseAST rhs;
    private ExtractRecordGenerator lhscg;
    private ExtractRecordGenerator rhscg;

    public ExprComparisonGenerator(ExprComparisonAST node) {
        this.exprComp = node;
    }

    @Override
    public void generateCode() {
     }

     @Override
     public String getCode(ExtractBaseAST node) {
        lhs = (ExtractBaseAST) node.getChild(0);
        rhs = (ExtractBaseAST) node.getChild(1);

        //We can make use of the emitters of the AST? WE need a parallel set for Java?
        //Or we look ahead for JOINS etc.
        //Or we lookahead from the view sourcs for JOINS we need.
        //Build a map of Joins and the Java code required for them.
        //Or do we already know what they are?

        lhscg = getcodeGenerator(lhs);
        rhscg = getcodeGenerator(rhs);
        //Make the format string dependent on the operator and types.
        return getComparisonFormatString();
     }

     private String getComparisonFormatString() {
        String lhsFormat = "%s";
        String opFormat = "";
        String rhsFormat = "%s";
        if(lhs.getType() == Type.LOOKUPFIELDREF) {
            lhsFormat = "joinBuffer != null && %s";
        }
        boolean stringComparison = false;
        switch(exprComp.getOp()) {
            case "=":
                opFormat = ".equals(";
                stringComparison = true;
                break;
            case "!=":
                opFormat = "!=";
                break;
            case ">":
                opFormat = ">";
                break;
            case "<":
                opFormat = "<";
                break;
            case ">=":
                opFormat = ">=";
                break;
            case "<=":
                opFormat = "<=";
                break;
            default:
                throw new RuntimeException("Unknown operator in expression comparison: " + exprComp.getOp());
        }
        if(stringComparison) {
            return String.format("%s%s%s) ", String.format(lhsFormat, lhscg.getCode(lhs)), opFormat, String.format(rhsFormat, rhscg.getCode(rhs)));
        } else {
            return String.format("%s%s%s", lhsFormat, opFormat, rhsFormat);
        }
        
     }
    
}
