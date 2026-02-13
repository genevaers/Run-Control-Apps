package org.genevaers.extractgenerator.codegenerators;

public class EndScope {
    EndScopeType type;
    int targetRow;

    public EndScope(EndScopeType type, int targetRow) {
        this.type = type;
        this.targetRow = targetRow;
    }

    public EndScopeType getType() {
        return type;
    }

    public int getTargetRow() {
        return targetRow;
    }
}