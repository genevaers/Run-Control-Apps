package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.NumAtomAST;

public class NumAtomGenerator extends ExtractRecordGenerator {

    public NumAtomGenerator(NumAtomAST node) {
    }

    @Override
    public void generateCode() {
    }

    @Override
    public String getCode(ExtractBaseAST node) {
        NumAtomAST na = (NumAtomAST) node;
        // A pre-declared typed constant takes priority (set by ExprComparisonGenerator).
        // The constant was already declared with the correct type for the context.
        if (constName != null) {
            return getConstName();
        }
        // Always return the raw literal string. The caller (CalculationGenerator,
        // ExprComparisonGenerator etc.) has the context to decide how to wrap it —
        // e.g. BigDecimal.valueOf(long), new BigDecimal("3.14"), BigInteger.valueOf(long).
        return na.getValueString();
    }
}
