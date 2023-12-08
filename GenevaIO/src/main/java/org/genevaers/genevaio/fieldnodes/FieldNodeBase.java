package org.genevaers.genevaio.fieldnodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FieldNodeBase {
    public enum FieldNodeType {
        RECORD("Record"), 
        STRINGFIELD("String"),
        NUMBERFIELD("Number"), 
        METADATA("Metadata"), 
        VIEW("View"), 
        ROOT("Root"), 
        RECORDPART("RecordPart"), 
        NOCOMPONENT("No Component"), 
        FUNCCODE("FunctionCode")
         ;

        private String name;
        private FieldNodeType(String n) {
            name = n;
        }
        @Override
        public String toString() {
            return name;
        }

    }

    private String name;
    protected ComparisonState state;
    private FieldNodeBase parent;
    protected FieldNodeType type;
    private Map<String, FieldNodeBase> childrenByName = new TreeMap<>();
    private List<FieldNodeBase> children = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ComparisonState getState() {
        return state;
    }

    public void setState(ComparisonState state) {
        this.state = state;
    }

    public FieldNodeBase getParent() {
        return parent;
    }

    public void setParent(FieldNodeBase parent) {
        this.parent = parent;
    }

    public FieldNodeType getFieldNodeType() {
        return type;
    }

    public List<FieldNodeBase> getChildren() {
        return children;
    }

    public void setChildren(List<FieldNodeBase> children) {
        this.children = children;
    }

    public FieldNodeBase add(FieldNodeBase rn, boolean compare) {
        FieldNodeBase useThisOne;
        if(compare) {
            useThisOne = compareChildNode(rn);
        } else {
            useThisOne = childrenByName.computeIfAbsent(rn.getName(), cname -> addNewChild(rn));
        }
        return useThisOne;
    }

    private FieldNodeBase compareChildNode(FieldNodeBase rn) {
        FieldNodeBase comparedNode = childrenByName.computeIfPresent(rn.getName(), (cname, cmpNode) -> comparedChild(cmpNode,rn));
        if(comparedNode == null) {
            comparedNode = childrenByName.computeIfAbsent(rn.getName(), cname -> addComparedChild(rn));
        }
        return comparedNode;
    }

    private FieldNodeBase comparedChild(FieldNodeBase cmpNode, FieldNodeBase rn) {
        if(cmpNode.compareTo(rn)) {
            cmpNode.setState(ComparisonState.INSTANCE);
        } else {
            cmpNode.setState(ComparisonState.DIFF);
            cmpNode.getParent().setState(ComparisonState.DIFF);
        }
        return cmpNode;
    }

    private FieldNodeBase addComparedChild(FieldNodeBase cmpNode) {
        cmpNode.setParent(this);
        children.add(cmpNode);
        cmpNode.setState(ComparisonState.NEW);
        return cmpNode;
    }

    private FieldNodeBase addNewChild(FieldNodeBase rn) {
        rn.setParent(this);
        children.add(rn);
        return rn;
    }

    public boolean compareTo(FieldNodeBase rn) {
        state = ComparisonState.INSTANCE;
        return true;
    }
}
