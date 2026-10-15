package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ViewColumnSourceAstNode;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.enums.ExtractArea;

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

        String comment = "\n/* Column " + vcs.getViewColumnSource().getColumnNumber() + "\n"
                + vcs.getViewColumnSource().getLogicText() + " */";

        ViewNode view = Repository.getViews().get(vcs.getViewColumnSource().getViewId());
        ViewColumn col = view.getColumnByID(vcs.getViewColumnSource().getColumnID());
        if (col.getExtractArea() == ExtractArea.SORTKEY) {
            ViewSortKey vsk = view.getViewSortKeyFromColumnId(col.getComponentId());
            if (vsk != null) {
                skColumnRecs.put((int) vsk.getSequenceNumber(), comment);
            }
        } else {
            columnRecs.add(comment);
        }

        generateFromChildNodes(vcs);
    }

}
