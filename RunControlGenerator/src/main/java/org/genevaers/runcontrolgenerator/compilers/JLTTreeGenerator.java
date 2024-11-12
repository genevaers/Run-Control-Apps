package org.genevaers.runcontrolgenerator.compilers;

import java.util.ArrayList;

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


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.compilers.extract.astnodes.ASTFactory;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.LFAstNode;
import org.genevaers.compilers.extract.astnodes.RecordCountAST;
import org.genevaers.compilers.extract.astnodes.ViewSourceAstNode;
import org.genevaers.compilers.extract.emitters.LogicTableEmitter;
import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.LookupType;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.enums.AccessMethod;
import org.genevaers.repository.components.enums.DbmsRowFmtOptId;
import org.genevaers.repository.components.enums.FieldDelimiter;
import org.genevaers.repository.components.enums.FileRecfm;
import org.genevaers.repository.components.enums.FileType;
import org.genevaers.repository.components.enums.RecordDelimiter;
import org.genevaers.repository.components.enums.TextDelimiter;
import org.genevaers.repository.jltviews.JLTView;
import org.genevaers.repository.jltviews.JLTViewMap;
import org.genevaers.repository.jltviews.ReferenceJoin;

public class JLTTreeGenerator {

    private JoinViewGenerator jvGenerator = new JoinViewGenerator();
    private LogicTableEmitter jltEmitter;
    private REHHeader rehHeader;
    private RTHHeader rthHeader;
    private ExtractBaseAST joinsRoot;
    private List<JLTView> headerEntries = new ArrayList<>();
    private int joinNumber = 1;
    int headerViewNumber;

    public JLTTreeGenerator(LogicTableEmitter ltEmitter) {
        jltEmitter = ltEmitter;
    }

    public ExtractBaseAST buildJoinViews() {
        joinsRoot = (ExtractBaseAST) ASTFactory.getNodeOfType(ASTFactory.Type.ERSJOINSROOT);
        buildReferenceJoins();
        LtFactoryHolder.getLtFunctionCodeFactory().setAccumNumber(0); //Set this back to zero for XLT
        return joinsRoot;
    }

    private RTHHeader addRTHView(int rthViewNumber) {
        makeOutputPF(rthViewNumber, "REFRRTH", "gvbrcg generated REFRRTH");
        rthHeader = new RTHHeader();
        rthHeader.setLogicTableEmitter(jltEmitter);
        rthHeader.addView(rthViewNumber);
        return rthHeader;
    }

    private void buildReferenceJoins() {
        Map<Integer, JLTViewMap<ReferenceJoin>> refDataset = Repository.getJoinViews().getReferenceDataSet();

        Iterator<Entry<Integer, JLTViewMap<ReferenceJoin>>> refi = refDataset.entrySet().iterator();
        while (refi.hasNext()) {
            Entry<Integer, JLTViewMap<ReferenceJoin>> refEntry = refi.next();
            buildJoinsForLF(refEntry);
        }
    }

    private void buildJoinsForLF(Entry<Integer, JLTViewMap<ReferenceJoin>> refEntry) {
        LFAstNode lfNode = jvGenerator.makeLFNode(refEntry.getKey());
        joinsRoot.addChildIfNotNull(lfNode);
        addJoinViewsToLfNode(lfNode, refEntry.getValue());
    }

    private void addJoinViewsToLfNode(LFAstNode lfNode, JLTViewMap<ReferenceJoin> refJoins) {

        Iterator<ReferenceJoin> ri = refJoins.getIterator();
        ViewSourceAstNode currentViewNode = null;
        while(ri.hasNext()) {
            ReferenceJoin refJoin = ri.next();
            refJoin.setDDNumber(joinNumber);
            currentViewNode = jvGenerator.addViewToLFNode(refJoin, lfNode, joinNumber);
            headerEntries.add(refJoin);
            LookupType lkt = refJoin.getLookupType();
            Repository.getJoinViews().addJoinTarget((byte)(lkt == LookupType.SKT ? 2 : 1), lfNode.getLogicalFile().getID(), lfNode.getName());
            joinNumber++;
        }
        jvGenerator.addRecordCountIncrement(currentViewNode);
        jvGenerator.addEndofSet(currentViewNode);
        addHeaderEntriesToTree(lfNode);
        headerEntries.clear();
    }

    private void addHeaderEntriesToTree(LFAstNode lfNode) {
        Iterator<JLTView> hei = headerEntries.iterator();
        ViewSourceAstNode vsnode = null;
        while(hei.hasNext()) {
            JLTView he = hei.next();
            if(vsnode != null) {
                jvGenerator.addEndofSet(vsnode);
            }
            REHHeader hdrView = makeHeaderViewIfNeeded(he);
            hdrView.addViewSource(he, lfNode.getLogicalFile().getID());
            RecordCountAST rc = (RecordCountAST) ASTFactory.getNodeOfType(ASTFactory.Type.RECORD_COUNT);
            rc.setNotEmittable();
            hdrView.setRecordCountAccumulator(rc);
            vsnode = hdrView.addREHTree(lfNode, he.getDdNum());
        }
    }


    private REHHeader makeHeaderViewIfNeeded(JLTView he) {
        if(he.getLookupType() == LookupType.NORMAL) {
            if(rehHeader == null) 
                return addREHView(Repository.getJoinViews().getHeaderViewNumber());
            else
                return rehHeader;
        } else {
            if(rthHeader == null)
                return addRTHView(Repository.getJoinViews().getHeaderViewNumber());
            else 
                return rthHeader;
        }
    }

    // Take the repo infomation and build the JLT
    public void emit() {
        if (joinsRoot != null) {
            ((EmittableASTNode)joinsRoot).emit();
        }
    }

    private REHHeader addREHView(int viewNum) {
        makeOutputPF(viewNum, "REFRREH", "gvbrcg generated REFRREH");
        rehHeader = new REHHeader();
        rehHeader.setLogicTableEmitter(jltEmitter);
        rehHeader.addView(viewNum);
        return rehHeader;
    }

    private void makeOutputPF(int viewNum, String ddname, String name) {
        PhysicalFile hdrPf = new PhysicalFile();
        hdrPf.setComponentId(viewNum);
        hdrPf.setOutputDDName(ddname);
        hdrPf.setName(name);
        hdrPf.setFileType(FileType.DISK);
        hdrPf.setLogicalFileId(viewNum);
        hdrPf.setLogicalFilename(name + "_LF");
        hdrPf.setExtractDDName("");
        hdrPf.setDataSetName("");
        hdrPf.setDatabase("");
        hdrPf.setDatabaseTable("");
        hdrPf.setSqlText("");
        hdrPf.setDatabaseConnection("");
        hdrPf.setReadExitIDParm("");
        hdrPf.setDatabaseRowFormat(DbmsRowFmtOptId.NONE);
        hdrPf.setInputDDName("");
        hdrPf.setFieldDelimiter(FieldDelimiter.FIXEDWIDTH);
        hdrPf.setRecordDelimiter(RecordDelimiter.VARIABLE_EXCLUSIVE);
        hdrPf.setTextDelimiter(TextDelimiter.INVALID);
        hdrPf.setAccessMethod(AccessMethod.SEQUENTIAL);
        hdrPf.setRecfm(FileRecfm.VB);
        hdrPf.setLrecl((short)4144);
        hdrPf.setDatabaseRowFormat(DbmsRowFmtOptId.SQL);
        Repository.getPhysicalFiles().add(hdrPf, viewNum);
        LogicalFile lf = new LogicalFile();
        lf.setID(viewNum);
        lf.setName(name);
        lf.addPF(hdrPf);
        Repository.getLogicalFiles().add(lf, viewNum, name);
    }

}
