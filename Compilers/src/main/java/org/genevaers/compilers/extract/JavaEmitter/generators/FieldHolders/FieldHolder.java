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
    public String getAssignmentSource(int len) {
        // Unsupported type — return empty so callers can detect and emit a comment.
        return "";
    }

    @Override
    public String getValueFrom(String src) {
        // If the accessor is already a full method name (e.g. "getString") use it
        // directly; otherwise prepend "get" for bare type names (e.g. "BigDecimal").
        String method = accessor.startsWith("get") ? accessor : "get" + accessor;
        return getName() + "." + method + "(" + src + ")";
    }
}
