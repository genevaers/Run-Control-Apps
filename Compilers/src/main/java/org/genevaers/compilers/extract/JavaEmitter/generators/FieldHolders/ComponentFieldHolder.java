package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.ComponentNode;

public class ComponentFieldHolder {
    private ComponentNode comp;
    protected String accessor;
    private String definition;
    protected String name;

    public ComponentFieldHolder(ComponentNode c) {
        comp = c;
        accessor = "TBD";
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDefinition() {
        return definition;
    }

    public String getAccessor() {
        return accessor;
    }

    public void setAccessor(String accessor) {
        this.accessor = accessor;
    }

    public ComponentNode getComp() {
        return comp;
    }

    public String getAssignmentSource(int s) {
        return "default src";
    }

    public String getValueFrom(String src) {
        // If the accessor is already a full method name (e.g. "getString", "getBigInteger")
        // use it directly; otherwise prepend "get" for bare type names (e.g. "BigDecimal").
        String method = accessor.startsWith("get") ? accessor : "get" + accessor;
        return getName() + "." + method + "(" + src + ")";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean useCompareTo() {
       return false;
    }

}
