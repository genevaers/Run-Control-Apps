package org.genevaers.compilers.extract.astnodes;

import java.util.Iterator;

import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSortKey;

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


import com.google.common.flogger.FluentLogger;

public class ViewColumnSourceAstNode extends ExtractBaseAST implements EmittableASTNode {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private ViewColumnSource vcs;

    ViewColumnSourceAstNode() {
        type = ASTFactory.Type.VIEWCOLUMNSOURCE;
    }

    public void setViewColumnSource(ViewColumnSource vcs) {
        this.vcs = vcs;
        currentViewColumnSource = vcs;
        currentViewColumn = Repository.getViews().get(vcs.getViewId()).getColumnByID(vcs.getColumnID());
    }

    public ViewColumnSource getViewColumnSource() {
        return vcs;
    }

    @Override
    public void emit() {
        ltEmitter.setSuffixSeqNbr((short)vcs.getColumnNumber());
        accumulateAreaLengths();
        //This will be a stepping stone.

        //At the moment there can only be a ColumnAssignment Node below us.
        //Note in C++ the ColumnAssignment node is derived from an AssignNode -> FormattedASTNode -> ExtractNode

        //So let the children emit what they have..

        //Current compiler has the column node as a child of the assignment.
        //This means the assignment node as the column details without us needing to pass it down.
        //We should do that too..
        emitChildNodes();
        if(vcs.getSortTitleLookupId() > 0) {
            emitSortTitleLookup(vcs.getSortTitleFieldId());
            LookupPath sktLookup = Repository.getLookups().get(vcs.getSortTitleLookupId());
            LogicalRecord sktLR = Repository.getLogicalRecords().get(sktLookup.getTargetLRID());
            Iterator<LRIndex> ii = sktLR.getIteratorForIndexBySeq();
            short keyLen = 0;
            while(ii.hasNext()) {
                LRIndex ndx = ii.next();
                keyLen += Repository.getFields().get(ndx.getFieldID()).getLength();
            }
            ((ViewSourceAstNode)getParent()).getAreaValues().addSkTLen(keyLen);
        }
     }

     private void accumulateAreaLengths() {
        ViewColumn vc = Repository.getViews().get(vcs.getViewId()).getColumnByID(vcs.getColumnID());
        switch (vc.getExtractArea()) {
             case SORTKEY:
                 ViewSortKey sk = Repository.getViews().get(vc.getViewId()).getViewSortKeyFromColumnId(vc.getComponentId());
                 ((ViewSourceAstNode) getParent()).getAreaValues().addSkLen(sk.getSkFieldLength());
                 break;
             case SORTKEYTITLE:
                 break;
             case AREADATA:
                 ((ViewSourceAstNode) getParent()).getAreaValues().addDtLen(vc.getFieldLength());
                 break;
             case AREACALC:
                 ((ViewSourceAstNode) getParent()).getAreaValues().addCtLen((short) 1);
                 break;
             default:
                 break;
         }
     }

     private void emitSortTitleLookup(int i) {
    }

}
