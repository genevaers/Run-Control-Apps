package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.LRField;

public class FieldHolder {

    protected LRField field;
    protected String accessor;

    public LRField getField() {
        return field;
    }

    public void setAccessor(String a) {
        accessor = a;
    }

    public String getAccessor() {
        return accessor;
    }
}
