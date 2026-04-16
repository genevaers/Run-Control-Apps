package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ViewColumnSourceAstNode;

public class ViewColumnSourceGenerator extends ExtractRecordGenerator {

    private ViewColumnSourceAstNode vcs;

    public ViewColumnSourceGenerator(ViewColumnSourceAstNode vcsAST) {
        this.vcs = vcsAST;
    };

    @Override
    public void generateCode() {
        if (vcs.getViewColumnSource().getColumnNumber() == 1) {
            JoinGenerator.generateColumnLogicCode();
        }

        columnRecs.add("\n/* Column " + vcs.getViewColumnSource().getColumnNumber() + "\n"
                + vcs.getViewColumnSource().getLogicText() + " */");
        generateFromChildNodes(vcs);
        
    }

}
