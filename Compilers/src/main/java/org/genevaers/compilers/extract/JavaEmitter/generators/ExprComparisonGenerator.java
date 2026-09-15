package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.ComponentFieldHolder;
import org.genevaers.compilers.extract.astnodes.ASTFactory;
import org.genevaers.compilers.extract.astnodes.ExprComparisonAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
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

        sb.append(getComparisonFormatString());
        return sb.toString();
     }

     // -------------------------------------------------------------------------
     // Field holder resolution — handles source fields AND lookup fields
     // -------------------------------------------------------------------------

     /**
      * Return the ComponentFieldHolder for a leaf operand node, or null if the
      * node type has no holder (e.g. a numeric literal).
      */
     private ComponentFieldHolder fieldHolder(ExtractBaseAST operand) {
         if (operand.getType() == ASTFactory.Type.LRFIELD) {
             FieldReferenceAST fr = (FieldReferenceAST) operand;
             return sourceFieldHolders.get(fr.getRef().getName());
         }
         if (operand.getType() == ASTFactory.Type.LOOKUPFIELDREF) {
             LookupFieldRefAST lfr = (LookupFieldRefAST) operand;
             String holderKey = lfr.getLookup().getName() + "_" + lfr.getRef().getName();
             return lookupFieldHolders.get(holderKey);
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

     /**
      * Recursively find the dominant field holder for an operand, descending into
      * CALCULATION nodes so that e.g. "{LK.A} + {LK.B}" still resolves a type.
      */
     private ComponentFieldHolder findFieldHolder(ExtractBaseAST node) {
         if (node.getType() == ASTFactory.Type.LRFIELD ||
             node.getType() == ASTFactory.Type.LOOKUPFIELDREF) {
             return fieldHolder(node);
         } else if (node.getType() == ASTFactory.Type.CALCULATION) {
             ExtractBaseAST setterNode = (ExtractBaseAST) node.getChild(0);
             ExtractBaseAST lhsOp = (ExtractBaseAST) setterNode.getChild(0);
             ExtractBaseAST opNode = (ExtractBaseAST) node.getChild(1);
             ExtractBaseAST rhsOp = (ExtractBaseAST) opNode.getChild(0);
             return pickDominant(findFieldHolder(lhsOp), findFieldHolder(rhsOp));
         }
         return null;
     }

     // -------------------------------------------------------------------------
     // Null-guard helpers for lookup fields
     // -------------------------------------------------------------------------

     /**
      * If the operand is (or contains) a LOOKUPFIELDREF, return the
      * "joinBuffer<id> != null" guard prefix, otherwise null.
      */
     private String lookupNullGuard(ExtractBaseAST operand) {
         if (operand.getType() == ASTFactory.Type.LOOKUPFIELDREF) {
             LookupFieldRefAST lfr = (LookupFieldRefAST) operand;
             return "joinBuffer" + lfr.getNewJoinId() + " != null && ";
         }
         if (operand.getType() == ASTFactory.Type.CALCULATION) {
             return lookupNullGuardFromCalc(operand);
         }
         return null;
     }

     /** Walk a CALCULATION tree and collect any LOOKUPFIELDREF null guard. */
     private String lookupNullGuardFromCalc(ExtractBaseAST calcNode) {
         ExtractBaseAST setterNode = (ExtractBaseAST) calcNode.getChild(0);
         ExtractBaseAST lhsOp = (ExtractBaseAST) setterNode.getChild(0);
         ExtractBaseAST opNode = (ExtractBaseAST) calcNode.getChild(1);
         ExtractBaseAST rhsOp = (ExtractBaseAST) opNode.getChild(0);
         String lhsGuard = lookupNullGuard(lhsOp);
         String rhsGuard = lookupNullGuard(rhsOp);
         if (lhsGuard != null && rhsGuard != null) {
             // de-duplicate if both reference the same buffer
             if (lhsGuard.equals(rhsGuard)) return lhsGuard;
             return lhsGuard + rhsGuard;
         }
         return lhsGuard != null ? lhsGuard : rhsGuard;
     }

     // -------------------------------------------------------------------------
     // Constant declaration
     // -------------------------------------------------------------------------

     private String getConstDeclaration(ExtractBaseAST t, ExtractBaseAST otherside, ExtractRecordGenerator cg) {
         if (t.getType() == ASTFactory.Type.NUMATOM) {
             ComponentFieldHolder cfh = findFieldHolder(otherside);
             if (cfh == null) {
                 return null;
             }
             String othertype = cfh.getAccessor();
             NumAtomAST na = (NumAtomAST) t;
             if (cfh.useCompareTo()) {
                 // Build a safe constant name: replace '.' and '-' so it is a valid Java identifier
                 String safeValue = na.getValueString().replace("-", "neg").replace(".", "_");
                 String constName = String.format("%s_%s", othertype, safeValue);
                 if (na.isFloatingPoint()) {
                     constantDeclarations.computeIfAbsent(constName, s -> String.format("final %s %s = new %s(\"%s\");",
                             othertype, constName, othertype, na.getValueString()));
                 } else {
                     constantDeclarations.computeIfAbsent(constName, s -> String.format("final %s %s = %s.valueOf(%s);",
                             othertype, constName, othertype, na.getValueString()));
                 }
                 cg.addConstName(constName);
             }
         }
         return null;
     }

     // -------------------------------------------------------------------------
     // Comparison expression builder
     // -------------------------------------------------------------------------

      private String getComparisonFormatString() {
        String opFormat = "";
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

        // Build the null-guard prefix for any lookup field on either side, and
        // record those buffer names so nested generators skip redundant checks.
        String lhsGuard = lookupNullGuard(lhs);
        String rhsGuard = lookupNullGuard(rhs);
        String nullGuard = "";
        if (lhsGuard != null) {
            nullGuard += lhsGuard;
            recordGuardedBuffers(lhsGuard);
        }
        if (rhsGuard != null && !rhsGuard.equals(lhsGuard)) {
            nullGuard += rhsGuard;
            recordGuardedBuffers(rhsGuard);
        }

        String lhsExpr = lhscg.getCode(lhs);
        String rhsExpr = rhscg.getCode(rhs);

        if (stringComparison) {
            return nullGuard + String.format("%s%s%s) ", lhsExpr, opFormat, rhsExpr);
        }

        ComponentFieldHolder lhsfh = findFieldHolder(lhs);
        ComponentFieldHolder rhsfh = findFieldHolder(rhs);
        boolean lhsUseCompare = lhsfh != null && lhsfh.useCompareTo();
        boolean rhsUseCompare = rhsfh != null && rhsfh.useCompareTo();

        ComponentFieldHolder dominantFH = pickDominant(lhsfh, rhsfh);

        if (lhsUseCompare) {
            if (!rhsUseCompare && rhscg.getConstName() == null) {
                // RHS is a primitive — wrap up to the LHS type
                rhsExpr = wrapForCompareTo(lhsfh, rhsExpr);
            } else if (rhsUseCompare && lhsfh != dominantFH) {
                // Both sides are "big" but different types — LHS is non-dominant, wrap it up
                lhsExpr = wrapForCompareTo(dominantFH, lhsExpr);
            } else if (rhsUseCompare && rhsfh != dominantFH) {
                // Both sides are "big" but different types — RHS is non-dominant, wrap it up
                rhsExpr = wrapForCompareTo(dominantFH, rhsExpr);
            }
            return nullGuard + String.format("%s.compareTo(%s) %s 0", lhsExpr, rhsExpr, opFormat);
        } else if (rhsUseCompare) {
            if (lhscg.getConstName() == null) {
                // LHS is a primitive — wrap up to the RHS type
                lhsExpr = wrapForCompareTo(rhsfh, lhsExpr);
            }
            return nullGuard + String.format("%s.compareTo(%s) %s 0", rhsExpr, lhsExpr, flipOperator(opFormat));
        } else {
            return nullGuard + String.format("%s %s %s", lhsExpr, opFormat, rhsExpr);
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
