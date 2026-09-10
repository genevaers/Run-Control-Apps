package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.ComponentFieldHolder;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.compilers.extract.astnodes.StringComparisonAST;

/**
 * Generates Java boolean expressions for string-function comparisons:
 *   CONTAINS   → lhs.contains(rhs)
 *   STARTS_WITH → lhs.startsWith(rhs)
 *   ENDS_WITH  → lhs.endsWith(rhs)
 *   =          → lhs.equals(rhs)          (negated with !)
 *   !=         → !lhs.equals(rhs)
 */
public class StringComparisonGenerator extends ExtractRecordGenerator {

    public StringComparisonGenerator(StringComparisonAST node) {
        // node stored for future use if needed
    }

    @Override
    public void generateCode() {
        // String comparisons appear as predicates inside IF/SELECTIF nodes;
        // their code is generated via getCode(), not pushed directly to columnRecs.
    }

    @Override
    public String getCode(ExtractBaseAST node) {
        StringComparisonAST sc = (StringComparisonAST) node;
        ExtractBaseAST lhs = (ExtractBaseAST) node.getChild(0);
        ExtractBaseAST rhs = (ExtractBaseAST) node.getChild(1);

        String lhsExpr = getStringExpr(lhs, "src");
        String rhsExpr = getStringExpr(rhs, "src");

        String op = sc.getOp();
        switch (op) {
            case "CONTAINS":
                return String.format("%s.contains(%s)", lhsExpr, rhsExpr);
            case "STARTS_WITH":
                return String.format("%s.startsWith(%s)", lhsExpr, rhsExpr);
            case "ENDS_WITH":
                return String.format("%s.endsWith(%s)", lhsExpr, rhsExpr);
            case "=":
                return String.format("%s.equals(%s)", lhsExpr, rhsExpr);
            case "!=":
                return String.format("!%s.equals(%s)", lhsExpr, rhsExpr);
            default:
                return String.format("/* Unsupported string op '%s' */ false", op);
        }
    }

    /**
     * Returns a Java expression that yields a String value for the given AST node.
     * Handles LR fields, lookup field references, and string literals.
     */
    private String getStringExpr(ExtractBaseAST node, String srcBuffer) {
        switch (node.getType()) {
            case LRFIELD: {
                FieldReferenceAST fr = (FieldReferenceAST) node;
                ComponentFieldHolder cfh = sourceFieldHolders.get(fr.getRef().getName());
                if (cfh != null) {
                    return cfh.getValueFrom(srcBuffer);
                }
                return String.format("/* unknown field %s */ null", fr.getRef().getName());
            }
            case LOOKUPFIELDREF: {
                LookupFieldRefAST lfr = (LookupFieldRefAST) node;
                String joinBuf = "joinBuffer" + lfr.getNewJoinId();
                String fieldName = lfr.getLookup().getName() + "_" + lfr.getRef().getName();
                ComponentFieldHolder cfh = lookupFieldHolders.get(fieldName);
                if (cfh != null) {
                    return String.format("%s != null ? %s : \"\"", joinBuf, cfh.getValueFrom(joinBuf));
                }
                return String.format("%s != null ? %s.getString(%s) : \"\"", joinBuf, fieldName, joinBuf);
            }
            case STRINGATOM: {
                ExtractRecordGenerator cg = getcodeGenerator(node);
                return cg.getCode(node);
            }
            default:
                return String.format("/* unhandled type %s */ null", node.getType());
        }
    }
}
