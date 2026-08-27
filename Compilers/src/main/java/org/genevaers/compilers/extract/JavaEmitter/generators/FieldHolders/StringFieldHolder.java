package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.LRField;

public class StringFieldHolder extends FieldHolder {

    public StringFieldHolder(LRField c) {
        super(c);
    }

    @Override
    public String getAssignmentSource(int len) {
        return String.format("%s.getString(src)",field.getName());
    }
}
