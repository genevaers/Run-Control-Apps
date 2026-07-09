package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.ComponentNode;

public class ComponentFieldHolder {
    private ComponentNode comp;
    protected String accessor;
    private String definition;

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

}
