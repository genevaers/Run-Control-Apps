package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;

/**
 * Generates code for the EXTRACTOUTPUT AST node.
 *
 * EXTRACTOUTPUT is a wrapper placed at the end of each ViewSource that
 * contains the WRITE node(s).  It delegates to its child generators,
 * which will be WriteStatementGenerator instances.
 */
public class ExtractOutputGenerator extends ExtractRecordGenerator {

    private final ExtractBaseAST node;

    public ExtractOutputGenerator(ExtractBaseAST node) {
        this.node = node;
    }

    @Override
    public void generateCode() {
        generateFromChildNodes(node);
    }

    @Override
    public String getCode(ExtractBaseAST node) {
        // EXTRACTOUTPUT is a statement-level node; its code is added to
        // columnRecs via generateCode(), not returned as an inline expression.
        generateFromChildNodes(node);
        return "";
    }
}
