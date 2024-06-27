package org.genevaers.compilers.base;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** 
 * This class represents a generic AST node.
 * And provides functions for management thereof.
 * Proably should be an interface?
 * It should know nothing about specific Geneva components
 */
public abstract class ASTBase {
    protected List<ASTBase> children = new ArrayList<>();
    protected ASTBase parent;
    private int charPositionInLine;
    private int lineNumber;


    public Iterator<ASTBase> getChildIterator() {
        return children.iterator();
    }

    public void addChildIfNotNull(ASTBase child) {
        if (child != null) {
            children.add(child);
            child.setParent(this);
        }
    }

    public int getNumberOfChildren() {
        return children.size();
    }

    public ASTBase getFirstLeafNode() {
        ASTBase leaf = null;
        Iterator<ASTBase> ci = children.iterator();
        if(ci.hasNext()) {
            leaf = ci.next();
            leaf = leaf.getFirstLeafNodeOf(leaf);
        }
        return leaf;
    }

    private ASTBase getFirstLeafNodeOf(ASTBase root) {
        ASTBase leaf = root;
        Iterator<ASTBase> ci = root.getChildIterator();
        if(ci.hasNext()) {
            leaf = ci.next();
            if(leaf != null) {
                leaf = leaf.getFirstLeafNodeOf(leaf);
            } else {
                leaf = root;
            }
        }
        return leaf;
    }

    public void setParent(ASTBase parent) {
        this.parent = parent;
    }

    public ASTBase getParent() {
        return parent;
    }

    public void setCharPostionInLine(int charPositionInLine) {
        this.charPositionInLine = charPositionInLine;
    }

    public void setLineNumber(int line) {
        lineNumber = line;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public ASTBase getChild(int num) {
        if(num <= children.size() - 1) {
            return children.get(num);
        } else {
            return null;
        }
    }
    
}
