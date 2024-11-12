package org.genevaers.compilers.extract.astnodes;

import java.util.ArrayList;
import java.util.HashMap;

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


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.compilers.extract.emitters.LogicTableEmitter;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.data.CompilerMessage;
import org.genevaers.repository.data.CompilerMessageSource;

/**
 * The class represents a generic Extract AST Node.
 * Forces extended child nodes to implement an emit function
 * to populate the ltEmitter and its underlying logic table..
 * 
 * We should probably be have an interface 
 * then those nodes that do not emit eg. NumAtom do no need empty functions.
 *  
 */
public abstract class ExtractBaseAST extends ASTBase{

    protected static LogicTableEmitter ltEmitter;
    protected int endOfLogic = 0;

    protected static int currentAccumNumber = 0;
    protected static short currentColumnNumber = 0;
    protected Set<String> accumNames = new HashSet<>();
    protected static ViewSource currentViewSource;
    protected static ViewColumn currentViewColumn;
    protected static ViewColumnSource currentViewColumnSource;
    protected static int lastColumnWithAWrite = 0;

    protected static Map<ViewSource, Integer> lastWriteColumnMap = new HashMap<>();

    protected ASTFactory.Type type = null;
    private boolean negative = false;

    protected static CompilerMessageSource currentMessageSource;


    ExtractBaseAST() {
        type = ASTFactory.Type.EBASE;
    }

    // We should do this via an emittable interface
    // since not all AST notes are emittable?
    //public abstract void emit();

    protected void emitChildNodes() {
        Iterator<ASTBase> ci = children.iterator();
        while(ci.hasNext()) {
            ASTBase n = ci.next();
            if(n instanceof EmittableASTNode) {
                EmittableASTNode c = (EmittableASTNode)n; 
                c.emit();
            }
        }
    }

    public static void setLogicTableEmitter(LogicTableEmitter ltEmitter) {
        ExtractBaseAST.ltEmitter = ltEmitter;
    }

    public void resolveGotosTop() {
        resolveGotos(null, null, null, null);
    }    
    
    public void setEndOfLogic(int endOfLogic) {
        this.endOfLogic = endOfLogic;
    }

    public Integer getEndOfLogic() {
        return endOfLogic;
    }

    public void resolveGotos(Integer comparisonT, Integer comparisonF, Integer joinT, Integer joinfF) {
        Iterator<ASTBase> ci = children.iterator();
        while(ci.hasNext()) {
            ExtractBaseAST c = (ExtractBaseAST) ci.next();
            c.resolveGotos(null, null, null, null);
        }
    }

    public static LogicTableEmitter getLtEmitter() {
        return ltEmitter;
    }

    public static short getCurrentColumnNumber() {
        return (short) (currentViewColumnSource != null ? currentViewColumnSource.getColumnNumber() : 0);
    }

    public static void setCurrentColumnNumber(short currentColumnNumber) {
        ExtractBaseAST.currentColumnNumber = currentColumnNumber;
    }

    public static void setCurrentAccumNumber(int currentAccumNumber) {
        ExtractBaseAST.currentAccumNumber = currentAccumNumber;
    }

    public ASTFactory.Type getType() {
        return type;
    }  
    
    public ExtractBaseAST getFirstNodeOfType(ASTFactory.Type t) {
        ExtractBaseAST leaf = null;
        Iterator<ASTBase> ci = children.iterator();
        while(leaf == null && ci.hasNext()) {
            leaf = (ExtractBaseAST) ci.next();
            if(leaf.getType() == t) {
                return leaf;
            } else {
                leaf = leaf.getFirstNodeOfType(t);
            }
        }
        return leaf;
    }

    public List<ExtractBaseAST> getChildNodesOfType(ASTFactory.Type t) {
        List<ExtractBaseAST> nodes = new ArrayList<>();
        ExtractBaseAST leaf = null;
        Iterator<ASTBase> ci = children.iterator();
        while(leaf == null && ci.hasNext()) {
            leaf = (ExtractBaseAST) ci.next();
            if(leaf.getType() == t) {
                nodes.add(leaf);
            }
            nodes = leaf.addNodesOfType(t, nodes);
        }
        return nodes;
    }

    public List<ExtractBaseAST> addNodesOfType(ASTFactory.Type t, List<ExtractBaseAST> nodes) {
        ExtractBaseAST leaf = null;
        Iterator<ASTBase> ci = children.iterator();
        while(ci.hasNext()) {
            leaf = (ExtractBaseAST) ci.next();
            if(leaf.getType() == t) {
                nodes.add(leaf);
            }
            nodes = leaf.addNodesOfType(t, nodes);
        }
        return nodes;
    }


    public void setNegative() {
        negative = true;
    }

    public boolean isNegative() {
        return negative;
    }

    public void addError(String message) {
        ErrorAST err = (ErrorAST) ASTFactory.getNodeOfType(ASTFactory.Type.ERRORS);
        err.setError(String.format("line:%d offset:%d %s", getLineNumber(), getCharPositionInLine(), message));
        addChildIfNotNull(err);
    }

    public void addWarning(String message) {
        WarningAST warn = (WarningAST) ASTFactory.getNodeOfType(ASTFactory.Type.WARNING);
        warn.setWarning(String.format("line:%d offset:%d %s", getLineNumber(), getCharPositionInLine(), message));
        addChildIfNotNull(warn);
    }

    public static ViewSource getCurrentViewSource() {
        return currentViewSource;
    }

    public static ViewColumn getCurrentViewColumn() {
        return currentViewColumn;
    }


    public static int getLastColumnWithAWrite(ViewSource vs) {
        Integer colNum = lastWriteColumnMap.get(vs);
        return colNum == null ? 0 : colNum;
    }

    public static void setLastColumnWithAWrite() {
        if(currentViewColumnSource != null) { //can be when there are no columns
            lastWriteColumnMap.put(currentViewSource, currentViewColumnSource.getColumnNumber());
        }
    }

    public static void clearLastColumnWithAWrite() {
        ExtractBaseAST.lastColumnWithAWrite = 0;
    }

    public static int getCurrentSourceOrColumnNumber() {
        return currentViewColumnSource != null ? currentViewColumnSource.getColumnNumber() : currentViewSource.getSequenceNumber();
    }

    public static CompilerMessageSource getCompilerMessageSource() {
        return currentMessageSource;
    }

    public static int getCurrentViewNumber() {
        return currentViewSource.getViewId();
    }

    public static CompilerMessage makeCompilerMessage(String detail) {
        return new CompilerMessage(getCurrentViewNumber(), 
        getCompilerMessageSource(), 
        getCurrentViewSource().getSourceLRID(), 
        getCurrentViewSource().getSourceLFID(), 
        getCurrentSourceOrColumnNumber(), detail);
    }

    public static void setCurrentMessageSource(CompilerMessageSource currentMessageSource) {
        ExtractBaseAST.currentMessageSource = currentMessageSource;
    }

    public static void init() {
        currentViewColumn = null;
    }

}
