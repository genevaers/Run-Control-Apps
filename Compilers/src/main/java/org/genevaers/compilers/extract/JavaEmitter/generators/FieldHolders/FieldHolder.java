package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.ComponentNode;
import org.genevaers.repository.components.LRField;

public class FieldHolder extends ComponentFieldHolder{

    protected LRField field;
    protected String accessor;

    public FieldHolder(LRField f) {
        super(f);
        field = f;
    }

    public LRField getField() {
        return field;
    }

    public void setAccessor(String a) {
        accessor = a;
    }

    public String getAccessor() {
        return accessor;
    }

    @Override
    public String getName() {
        return field.getName();
    }
    
    @Override
    public String getValueFrom(String src) {
        return getName() + ".get" + accessor + "(" + src + ")";
    }
}
