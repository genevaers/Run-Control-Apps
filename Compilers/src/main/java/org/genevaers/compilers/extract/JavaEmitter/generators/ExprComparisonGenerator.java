package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.genevaers.repository.components.enums.DataType;

import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.ComponentFieldHolder;
import org.genevaers.compilers.extract.astnodes.ASTFactory;
import org.genevaers.compilers.extract.astnodes.ExprComparisonAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.NumAtomAST;

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

     private ComponentFieldHolder fieldHolder(ExtractBaseAST operand) {
         if (operand.getType() == ASTFactory.Type.LRFIELD) {
             FieldReferenceAST fr = (FieldReferenceAST) operand;
             return sourceFieldHolders.get(fr.getRef().getName());
         }
         return null;
     }

     private ComponentFieldHolder pickDominant(ComponentFieldHolder a, ComponentFieldHolder b) {
         if (a != null && "BigDecimal".equals(a.getAccessor())) return a;
         if (b != null && "BigDecimal".equals(b.getAccessor())) return b;
         if (a != null && "BigInteger".equals(a.getAccessor())) return a;
         if (b != null && "BigInteger".equals(b.getAccessor())) return b;
         return null;
     }

     private ComponentFieldHolder findFieldHolder(ExtractBaseAST node) {
         if (node.getType() == ASTFactory.Type.LRFIELD) {
             FieldReferenceAST fr = (FieldReferenceAST) node;
             return sourceFieldHolders.get(fr.getRef().getName());
         } else if (node.getType() == ASTFactory.Type.CALCULATION) {
             ExtractBaseAST setterNode = (ExtractBaseAST) node.getChild(0);
             ExtractBaseAST lhsOp = (ExtractBaseAST) setterNode.getChild(0);
             ExtractBaseAST opNode = (ExtractBaseAST) node.getChild(1);
             ExtractBaseAST rhsOp = (ExtractBaseAST) opNode.getChild(0);
             return pickDominant(findFieldHolder(lhsOp), findFieldHolder(rhsOp));
         }
         return null;
     }

     private String getConstDeclaration(ExtractBaseAST t, ExtractBaseAST otherside, ExtractRecordGenerator cg) {
         String decl = null;

         if (t.getType() == ASTFactory.Type.STRINGATOM) {

         } else if (t.getType() == ASTFactory.Type.NUMATOM) {
             ComponentFieldHolder cfh = findFieldHolder(otherside);
             if (cfh == null) {
                 return decl;
             }
             String othertype = cfh.getAccessor();
             NumAtomAST na = (NumAtomAST) t;
             if (cfh.useCompareTo()) {
                 // Build a safe constant name: replace '.' and '-' so it is a valid Java identifier
                 String safeValue = na.getValueString().replace("-", "neg").replace(".", "_");
                 String constName = String.format("%s_%s", othertype, safeValue);
                 if (na.isFloatingPoint()) {
                     // Use string-constructor form to preserve exact decimal value
                     constantDeclarations.computeIfAbsent(constName, s -> String.format("final %s %s = new %s(\"%s\");",
                             othertype, constName, othertype, na.getValueString()));
                 } else {
                     // Integer value — valueOf(long) is efficient and cache-friendly
                     constantDeclarations.computeIfAbsent(constName, s -> String.format("final %s %s = %s.valueOf(%s);",
                             othertype, constName, othertype, na.getValueString()));
                 }
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
            ComponentFieldHolder lhsfh = findFieldHolder(lhs);
            ComponentFieldHolder rhsfh = findFieldHolder(rhs);
            boolean lhsUseCompare = lhsfh != null && lhsfh.useCompareTo();
            boolean rhsUseCompare = rhsfh != null && rhsfh.useCompareTo();

            String lhsExpr = lhscg.getCode(lhs);
            String rhsExpr = rhscg.getCode(rhs);

            if (lhsUseCompare) {
                if (!rhsUseCompare && rhscg.getConstName() == null) {
                    rhsExpr = wrapForCompareTo(lhsfh, rhsExpr);
                }
                return String.format("%s.compareTo(%s) %s 0", lhsExpr, rhsExpr, opFormat);
            } else if (rhsUseCompare) {
                if (lhscg.getConstName() == null) {
                    lhsExpr = wrapForCompareTo(rhsfh, lhsExpr);
                }
                return String.format("%s.compareTo(%s) %s 0", rhsExpr, lhsExpr, flipOperator(opFormat));
            } else {
                return String.format("%s %s %s", lhsExpr, opFormat, rhsExpr);
            }
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
