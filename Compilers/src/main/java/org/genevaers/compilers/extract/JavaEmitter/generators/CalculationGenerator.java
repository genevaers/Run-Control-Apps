package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.ComponentFieldHolder;
import org.genevaers.compilers.extract.astnodes.ASTFactory;
import org.genevaers.compilers.extract.astnodes.CalculationAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;

/**
 * Generates a Java arithmetic expression from a CALCULATION AST node.
 *
 * A CALCULATION node has exactly two children:
 *   child 0 - SETTER  (wraps the LHS / initial operand)
 *   child 1 - ADDITION | SUBTRACTION | MULTIPLICATION | DIVISION
 *             (wraps the RHS operand)
 *
 * Nested CALCULATION nodes (e.g. a * b + c) produce nested getCode() calls
 * that compose naturally into a single inline Java expression.
 *
 * Type rules:
 *   - If either operand is BigDecimal  → use BigDecimal method chain (.add, .subtract, …)
 *   - Else if either operand is BigInteger → use BigInteger method chain
 *   - Otherwise                          → plain Java infix  (+, -, *, /)
 *
 * Plain-typed operands are wrapped via wrapForCompareTo() when the other side
 * requires a method-chain type.
 */
public class CalculationGenerator extends ExtractRecordGenerator {

    public CalculationGenerator(CalculationAST node) {
        // node retained for future use (e.g. accumulator name)
    }

    @Override
    public void generateCode() {
        // generation is driven through getCode()
    }

    @Override
    public String getCode(ExtractBaseAST node) {
        // child 0 is the SETTER — its single child is the LHS operand
        ExtractBaseAST setterNode = (ExtractBaseAST) node.getChild(0);
        ExtractBaseAST lhsOperand = (ExtractBaseAST) setterNode.getChild(0);

        // child 1 is the arithmetic operator node — its single child is the RHS operand
        ExtractBaseAST opNode     = (ExtractBaseAST) node.getChild(1);
        ExtractBaseAST rhsOperand = (ExtractBaseAST) opNode.getChild(0);

        String lhsExpr = operandExpr(lhsOperand);
        String rhsExpr = operandExpr(rhsOperand);

        ComponentFieldHolder lhsFH = fieldHolder(lhsOperand);
        ComponentFieldHolder rhsFH = fieldHolder(rhsOperand);

        // Determine the dominant type (BigDecimal beats BigInteger beats primitives)
        boolean lhsBig = lhsFH != null && lhsFH.useCompareTo();
        boolean rhsBig = rhsFH != null && rhsFH.useCompareTo();

        // Find which holder drives the method-chain type (prefer BigDecimal)
        ComponentFieldHolder dominantFH = pickDominant(lhsFH, rhsFH);

        if (dominantFH != null) {
            // At least one side needs method-chain arithmetic
            if (lhsBig && !rhsBig) {
                rhsExpr = wrapForCompareTo(lhsFH, rhsExpr);
            } else if (!lhsBig && rhsBig) {
                lhsExpr = wrapForCompareTo(rhsFH, lhsExpr);
            }
            // both big: both expressions are already the right type, no wrap needed
            return String.format("%s.%s(%s)", lhsExpr, methodName(opNode), rhsExpr);
        } else {
            // both primitives (int / long) — plain infix
            return String.format("%s %s %s", lhsExpr, infixOp(opNode), rhsExpr);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Get the Java value expression for an operand node. */
    private String operandExpr(ExtractBaseAST operand) {
        ExtractRecordGenerator gen = getcodeGenerator(operand);
        return gen.getCode(operand);
    }

    /**
     * Return the ComponentFieldHolder for an operand, or null if the operand
     * has no holder (e.g. it is a numeric literal or a nested CALCULATION).
     */
    private ComponentFieldHolder fieldHolder(ExtractBaseAST operand) {
        if (operand.getType() == ASTFactory.Type.LRFIELD) {
            FieldReferenceAST fr = (FieldReferenceAST) operand;
            return sourceFieldHolders.get(fr.getRef().getName());
        }
        // CALCULATION, NUMATOM, LOOKUPFIELDREF etc. — treated as primitive for
        // promotion purposes unless we can inspect them further in the future.
        return null;
    }

    /**
     * If either holder is BigDecimal or BigInteger, return the dominant one.
     * BigDecimal wins over BigInteger (to avoid loss of precision).
     */
    private ComponentFieldHolder pickDominant(ComponentFieldHolder a, ComponentFieldHolder b) {
        if (a != null && "BigDecimal".equals(a.getAccessor())) return a;
        if (b != null && "BigDecimal".equals(b.getAccessor())) return b;
        if (a != null && "BigInteger".equals(a.getAccessor())) return a;
        if (b != null && "BigInteger".equals(b.getAccessor())) return b;
        return null;
    }

    /** Map an operator AST node type to a BigDecimal/BigInteger method name. */
    private String methodName(ExtractBaseAST opNode) {
        switch (opNode.getType()) {
            case ADDITION:       return "add";
            case SUBTRACTION:    return "subtract";
            case MULTIPLICATION: return "multiply";
            case DIVISION:       return "divide";
            default: throw new RuntimeException("Unknown arithmetic operator: " + opNode.getType());
        }
    }

    /** Map an operator AST node type to a Java infix operator symbol. */
    private String infixOp(ExtractBaseAST opNode) {
        switch (opNode.getType()) {
            case ADDITION:       return "+";
            case SUBTRACTION:    return "-";
            case MULTIPLICATION: return "*";
            case DIVISION:       return "/";
            default: throw new RuntimeException("Unknown arithmetic operator: " + opNode.getType());
        }
    }
}
