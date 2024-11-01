package org.genevaers.runcontrolgenerator.compilers;

import java.util.Iterator;

import org.genevaers.compilers.extract.astnodes.ASTFactory;
import org.genevaers.compilers.extract.astnodes.LFAstNode;
import org.genevaers.compilers.extract.astnodes.ViewSourceAstNode;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008
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


import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewDefinition;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.FileType;
import org.genevaers.repository.components.enums.OutputMedia;
import org.genevaers.repository.components.enums.ViewStatus;
import org.genevaers.repository.components.enums.ViewType;
import org.genevaers.repository.jltviews.JLTView;

public class RTHHeader extends REHHeader{

    private final String NAME ="Reference Title Header (RTH) data";
    private final String DDNAME = "REFRRTH";


    @Override
    public void addView(int viewNum) {
        
        ViewDefinition vd = new ViewDefinition();
        super.rehViewNum = viewNum;
        vd.setComponentId(viewNum);
        vd.setName(NAME);
        vd.setOutputMedia(OutputMedia.FILE);
        vd.setViewType(ViewType.EXTRACT);
        vd.setExtractSummarized(false);
        vd.setStatus(ViewStatus.ACTIVE);
        vd.setWriteExitParams("");
        vd.setFormatExitParams("");
        vn = Repository.getViewNodeMakeIfDoesNotExist(vd);

        makeHeaderLR(rehViewNum);

        addColumns();

//        makePF(viewNum);
    }

    @Override
    public ViewSourceAstNode addREHTree(LFAstNode lfNode, int ddnum) {
        //I think we just use the same lf as for the REF view
		//LFAstNode lfNode = (LFAstNode)ASTFactory.getNodeOfType(ASTFactory.Type.LF);
		//lfNode.setLogicalFile(repo.getLogicalFile(lfid));

        ViewNode vn = Repository.getViews().get(rehViewNum);
        ViewSourceAstNode vsnode = (ViewSourceAstNode) ASTFactory.getNodeOfType(ASTFactory.Type.VIEWSOURCE);
        ViewSource vs = vn.getViewSource(rthsourceNum++); 
        vs.setSourceLRID(0); //Reset to 0 for the REH
        vs.setOutputPFID(rehViewNum);
        vn.getOutputFile().setComponentId(rehViewNum);
        vn.getOutputFile().setName(Repository.getPhysicalFiles().get(rehViewNum).getName());
        vn.getOutputFile().setOutputDDName(Repository.getPhysicalFiles().get(rehViewNum).getOutputDDName());
        vn.getOutputFile().setFileType(FileType.DISK);
        vsnode.setViewSource(vs);
        lfNode.getLogicalFile().getPFIterator().next().setRequired(true); //There should be only one
        lfNode.getLogicalFile().setRequired(true);
        lfNode.addChildIfNotNull(vsnode);
        addViewColumnSourceNodes(vsnode);
        addWriteNode(vsnode, rehViewNum);

        return vsnode;
    }

    public void addViewSource(JLTView jv, int lfid) {
            // Add the data from the generation fields
        // We make a view that models its lr.
        ViewSource vs = new ViewSource();
        //What else do we care about for the ViewSource.
        //Probably needs a new id -> ask the repo to make it
        //It will know that the ids are - or use the view number. 
        //It will be unique and the will only be one source
        vs.setComponentId(vn.getID());
        vs.setSequenceNumber(rthsourceNum);
        vs.setSourceLFID(lfid);
        vs.setSourceLRID(hdrLR.getComponentId());
        vs.setViewId(vn.getID());
        vn.addViewSource(vs);

        Iterator<ViewColumn> vci = vn.getColumnIterator();
        addViewColumnSource(vs, vci.next(), Integer.toString(lfid));
        addViewColumnSource(vs, vci.next(), Integer.toString(jv.getLRid()));
        addRecordCount(vs, vci.next());
        addViewColumnSource(vs, vci.next(), Integer.toString(jv.getGenLrLength()));
        ViewColumnSource vcs = addViewColumnSource(vs, vci.next(), "0"); //Offest to Key is always 0
        vcs.setValueLength(2);
        addViewColumnSource(vs, vci.next(), Integer.toString(jv.getKeyLength())); 
        addViewColumnSource(vs, vci.next(), Integer.toString(jv.getDdNum())); 
        addViewColumnSource(vs, vci.next(), "0"); //always 0
        addViewColumnSource(vs, vci.next(), Integer.toString(jv.getEffDateCode())); 
        addViewColumnSource(vs, vci.next(), "0"); //always 0
        addViewColumnSource(vs, vci.next(), jv.isIndexText() ? "1" : "0"); 
        addViewColumnSource(vs, vci.next(), "Reserved  spaces"); //always 0
    }    
}
