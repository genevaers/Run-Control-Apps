package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.StatementList;

public class StatementListGenerator extends ExtractRecordGenerator {

    private StatementList statementList;

    public StatementListGenerator(StatementList statementList) {
        this.statementList = statementList;
    }
    
    @Override
    public void generateCode() {
        generateFromChildNodes(statementList);
    }

    @Override
    public String getCode(ExtractBaseAST node) {
        return getcodeGenerator((ExtractBaseAST) statementList.getChild(0)).getCode((ExtractBaseAST) statementList.getChild(0));
    }
    
}
