package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.enums.DataType;

import java.util.Map;
import java.util.function.Function;

import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.ComponentFieldHolder;
import org.genevaers.compilers.extract.astnodes.ASTFactory;
import org.genevaers.compilers.extract.astnodes.ExprComparisonAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.NumAtomAST;
import org.genevaers.compilers.extract.astnodes.TypedASTNode;

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
        StringBuilder sb = new StringBuilder();
        lhs = (ExtractBaseAST) node.getChild(0);
        rhs = (ExtractBaseAST) node.getChild(1);
        //do we need a map of emitter types like the LT emitter?
        //Do we need to predeclare a constant?
        Type lhsType = lhs.getType();
        Type rhsType = rhs.getType();
        lhscg = getcodeGenerator(lhs);
        rhscg = getcodeGenerator(rhs);
        String lhsConstDeclaration = getConstDeclaration(lhs, rhs, lhscg);
        String rhsConstDeclaration = getConstDeclaration(rhs, lhs, rhscg);
            
        if(lhsConstDeclaration != null) {
            sb.append(lhsConstDeclaration);
        }

        if(rhsConstDeclaration != null) {
            sb.append(rhsConstDeclaration);
        }

        //We can make use of the emitters of the AST? WE need a parallel set for Java?
        //Or we look ahead for JOINS etc.
        //Or we lookahead from the view sourcs for JOINS we need.
        //Build a map of Joins and the Java code required for them.
        //Or do we already know what they are?

        //Make the format string dependent on the operator and types.
        sb.append(getComparisonFormatString());
        return sb.toString();
     }

     private String getConstDeclaration(ExtractBaseAST t, ExtractBaseAST otherside, ExtractRecordGenerator cg) {
         String decl = null;

         if (t.getType() == ASTFactory.Type.STRINGATOM) {

         } else if (t.getType() == ASTFactory.Type.NUMATOM) {
             DataType dt = DataType.INVALID;
             String name = null;
             // declaration type dependenent on other side type
             switch (otherside.getType()) {
                 case LRFIELD:
                     FieldReferenceAST lrfr = (FieldReferenceAST) otherside;
                     dt = lrfr.getRef().getDatatype();
                     name = lrfr.getRef().getName();
                     break;
                 case LOOKUPFIELDREF:
                     break;
                 default:
                     break;
             }
             ComponentFieldHolder cfh = sourceFieldHolders.get(name);
             String othertype = cfh.getAccessor();
             // final BigDecimal MIN_BALANCE = new BigDecimal("100.00");
             NumAtomAST na = (NumAtomAST) t;
             if (cfh.useCompareTo()) {
                 String constName = String.format("%s_%d", othertype, na.getValue());
                 constantDeclarations.computeIfAbsent(constName, s -> String.format("final %s %s = new %s(\"%d\");",
                         othertype, constName, othertype, na.getValue()));
                 cg.addConstName(constName);
             }
         }
         return decl;
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
            if(lhs.getType() == ASTFactory.Type.LRFIELD) {
                FieldReferenceAST lfr = (FieldReferenceAST)lhs;
                ComponentFieldHolder lhsfh = sourceFieldHolders.get(lfr.getRef().getName());

                if(lhsfh.useCompareTo()) {
                    // LHS is BigDecimal/BigInteger — use lhs.compareTo(rhs) OP 0
                    // RHS must be wrapped to match LHS type if it does not already useCompareTo
                    String rhsExpr = rhscg.getCode(rhs);
                    if(rhs.getType() == ASTFactory.Type.LRFIELD) {
                        FieldReferenceAST rfr = (FieldReferenceAST)rhs;
                        ComponentFieldHolder rhsfh = sourceFieldHolders.get(rfr.getRef().getName());
                        if(!rhsfh.useCompareTo()) {
                            rhsExpr = wrapForCompareTo(lhsfh, rhsfh.getValueFrom("src"));
                        }
                    }
                    return String.format("%s.compareTo(%s) %s 0", lhsfh.getValueFrom("src"), rhsExpr, opFormat);
                } else if(rhs.getType() == ASTFactory.Type.LRFIELD) {
                    FieldReferenceAST rfr = (FieldReferenceAST)rhs;
                    ComponentFieldHolder rhsfh = sourceFieldHolders.get(rfr.getRef().getName());
                    if(rhsfh.useCompareTo()) {
                        // Only RHS is BigDecimal/BigInteger — flip: rhs.compareTo(lhs) FLIPPED_OP 0
                        // LHS must be wrapped to match RHS type
                        String lhsExpr = wrapForCompareTo(rhsfh, lhsfh.getValueFrom("src"));
                        return String.format("%s.compareTo(%s) %s 0", rhsfh.getValueFrom("src"), lhsExpr, flipOperator(opFormat));
                    }
                }

                // Neither side needs compareTo — plain infix
                return String.format("%s %s %s", lhsfh.getValueFrom("src"), opFormat, rhscg.getCode(rhs));
            }
            return "Bad Comparison";
        }
        
     }

     private String wrapForCompareTo(ComponentFieldHolder comparingHolder, String valueExpr) {
         switch(comparingHolder.getAccessor()) {
             case "BigDecimal":  return String.format("new BigDecimal(String.valueOf(%s))", valueExpr);
             case "BigInteger":  return String.format("BigInteger.valueOf(%s)", valueExpr);
             default:            return valueExpr;
         }
     }

     private String flipOperator(String op) {
         switch(op) {
             case ">":  return "<";
             case "<":  return ">";
             case ">=": return "<=";
             case "<=": return ">=";
             default:   return op;
         }
     }
    
}
