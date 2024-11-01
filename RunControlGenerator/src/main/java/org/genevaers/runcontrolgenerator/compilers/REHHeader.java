package org.genevaers.runcontrolgenerator.compilers;

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


import java.util.Iterator;

import org.genevaers.compilers.extract.astnodes.ASTFactory;
import org.genevaers.compilers.extract.astnodes.ColumnAST;
import org.genevaers.compilers.extract.astnodes.ColumnAssignmentASTNode;
import org.genevaers.compilers.extract.astnodes.EndOfSetASTNode;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.LFAstNode;
import org.genevaers.compilers.extract.astnodes.NumAtomAST;
import org.genevaers.compilers.extract.astnodes.RecordCountAST;
import org.genevaers.compilers.extract.astnodes.StringAtomAST;
import org.genevaers.compilers.extract.astnodes.ViewColumnSourceAstNode;
import org.genevaers.compilers.extract.astnodes.ViewSourceAstNode;
import org.genevaers.compilers.extract.astnodes.WriteASTNode;
import org.genevaers.compilers.extract.emitters.LogicTableEmitter;
import org.genevaers.repository.RepoHelper;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ComponentNode;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewDefinition;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.AccessMethod;
import org.genevaers.repository.components.enums.ColumnSourceType;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DbmsRowFmtOptId;
import org.genevaers.repository.components.enums.FieldDelimiter;
import org.genevaers.repository.components.enums.FileRecfm;
import org.genevaers.repository.components.enums.FileType;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.OutputMedia;
import org.genevaers.repository.components.enums.RecordDelimiter;
import org.genevaers.repository.components.enums.TextDelimiter;
import org.genevaers.repository.components.enums.ViewStatus;
import org.genevaers.repository.components.enums.ViewType;
import org.genevaers.repository.jltviews.JLTView;

public class REHHeader {

    private final String NAME ="Reference Extract Header (REH) data";
    private final String DDNAME = "REFRREH";

    private LogicTableEmitter jltEmitter;
    protected ViewNode vn;
    protected int rehViewNum;
    private short startPos;
    protected LogicalRecord hdrLR;
    private RecordCountAST recCountAccum;
    private static short rehsourceNum = 1;
    protected static short rthsourceNum = 1;

    public void setLogicTableEmitter(LogicTableEmitter ltEmitter) {
        //jltEmitter = ltEmitter;
    }

    public void setRepository(Repository r) {

	}

    public void addView(int viewNum) {
        ViewDefinition vd = new ViewDefinition();
        rehViewNum = viewNum;
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

    protected void makePF(int viewNum) {
        PhysicalFile rehPF = new PhysicalFile();
        rehPF.setComponentId(viewNum);
        rehPF.setFileType(FileType.DISK);
        rehPF.setLogicalFileId(viewNum);
        rehPF.setName(NAME + "_PF");
        rehPF.setLogicalFilename(NAME + "_LF");
        rehPF.setExtractDDName(DDNAME);
        rehPF.setDataSetName("");
        rehPF.setOutputDDName(DDNAME);
        rehPF.setFileType(FileType.DISK);
        rehPF.setDatabase("");
        rehPF.setDatabaseTable("");
        rehPF.setSqlText("");
        rehPF.setDatabaseConnection("");
        rehPF.setReadExitIDParm("");
        rehPF.setDatabaseRowFormat(DbmsRowFmtOptId.NONE);
        rehPF.setInputDDName("");
        rehPF.setFieldDelimiter(FieldDelimiter.INVALID);
        rehPF.setRecordDelimiter(RecordDelimiter.FIXED);
        rehPF.setTextDelimiter(TextDelimiter.INVALID);
        rehPF.setAccessMethod(AccessMethod.SEQUENTIAL);
        rehPF.setRecfm(FileRecfm.FB);
        Repository.getPhysicalFiles().add(rehPF, rehPF.getComponentId(), rehPF.getName());
        LogicalFile lf = new LogicalFile();
        lf.setID(viewNum);
        lf.setName(NAME + "_LF");
        lf.addPF(rehPF);
        Repository.getLogicalFiles().add(lf, viewNum, NAME +"_LF");
    }

    protected void makeHeaderLR(int rehLRNum) {
        hdrLR = Repository.makeLR("Ref Header LR", rehLRNum);
        startPos = 1;
        short dwordLen = 4;
        short wordLen = 2;
        short byteLen = 1;

        makeHeaderField("File ID", dwordLen);
        makeHeaderField("LR ID", dwordLen);
        LRField rc = makeHeaderField("Record Count", dwordLen);
        rc.setSigned(false);
        makeHeaderField("Record Length", wordLen);
        makeHeaderField("Offest to Key", wordLen);
        makeHeaderField("Key Length", wordLen);
        makeHeaderField("Data File Number", wordLen);
        makeHeaderField("Build Ref File", byteLen);
        makeHeaderField("Effective Dates", byteLen);
        makeHeaderField("Range Present", byteLen);
        makeHeaderField("Text Data Flag", byteLen);
        LRField res = makeHeaderField("Reserved", byteLen);
        res.setDatatype(DataType.ALPHANUMERIC);
        res.setLength((short)16);    
    }

    protected void addColumns() {
        Iterator<LRField> fldIt = hdrLR.getIteratorForFieldsByID();
        int columnNumber = 1;
        while(fldIt.hasNext()) {
            LRField fld = fldIt.next();
            ViewColumn vc = new ViewColumn();
            vc.setComponentId(columnNumber);
            vc.setViewId(rehViewNum);
            vc.setName(fld.getName());
            vc.setOrdinalPosition((short)columnNumber);
            vc.setColumnNumber(columnNumber++);
            RepoHelper.setViewColumnFromLRField(vc, fld);
            vc.setJustifyId(JustifyId.LEFT);
            vn.addViewColumn(vc);
        }
    }



    public ViewSourceAstNode addREHTree(LFAstNode lfNode, int ddnum) {
        //I think we just use the same lf as for the REF view
		//LFAstNode lfNode = (LFAstNode)ASTFactory.getNodeOfType(ASTFactory.Type.LF);
		//lfNode.setLogicalFile(repo.getLogicalFile(lfid));

        ViewNode vn = Repository.getViews().get(rehViewNum);
        ViewSourceAstNode vsnode = (ViewSourceAstNode) ASTFactory.getNodeOfType(ASTFactory.Type.VIEWSOURCE);
        ViewSource vs = vn.getViewSource(rehsourceNum++); 
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

    private void addEndofSet(ViewSourceAstNode vsnode) {
        EndOfSetASTNode eos = (EndOfSetASTNode) ASTFactory.getNodeOfType(ASTFactory.Type.EOS);
        eos.setLfID(vsnode.getViewSource().getSourceLFID());
        vsnode.addChildIfNotNull(eos);
    }

    protected void addWriteNode(ViewSourceAstNode vsnode, int fileNum) {
        WriteASTNode wrNode = (WriteASTNode) ASTFactory.getNodeOfType(ASTFactory.Type.WRITE);
        wrNode.setPhysicalFile(Repository.getPhysicalFiles().get(fileNum));
        wrNode.setViewSource(vsnode.getViewSource());
        vsnode.addChildIfNotNull(wrNode);
    }

    protected void addViewColumnSourceNodes(ViewSourceAstNode vsnode) {
        //Iterate through the columns and 
		Iterator<ViewColumnSource> vcsi = vsnode.getViewSource().getIteratorForColumnSourcesByNumber();
		while(vcsi.hasNext()) {
			ViewColumnSource vcs = vcsi.next();
			ViewColumnSourceAstNode vcsn = (ViewColumnSourceAstNode) ASTFactory.getNodeOfType(ASTFactory.Type.VIEWCOLUMNSOURCE);
			vcsn.setViewColumnSource(vcs);
			vsnode.addChildIfNotNull(vcsn);
			vcsn.addChildIfNotNull(addColumnAssignment(vcs));
		}
    }

    public ExtractBaseAST addColumnAssignment(ViewColumnSource vcs) { 
        ColumnAssignmentASTNode casnode = (ColumnAssignmentASTNode) ASTFactory.getNodeOfType(ASTFactory.Type.COLUMNASSIGNMENT);
        ViewNode view = Repository.getViews().get(vcs.getViewId());
        ColumnAST colNode = (ColumnAST)ASTFactory.getColumnNode(view.getColumnNumber(vcs.getColumnNumber()));
        if(colNode.getViewColumn().getName().equals("Record Count")) {
            addRecordCountAccumulator(casnode);
        } else if(colNode.getViewColumn().getName().equals("Reserved")) {
            addStringConstant(casnode, vcs);
        } else {
            addConstant(casnode, vcs);
        }
        casnode.addChildIfNotNull(colNode);
        return casnode;
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
        vs.setSequenceNumber(rehsourceNum);
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

    protected void addRecordCount(ViewSource vs, ViewColumn vc) {
        ViewColumnSource vcs = new ViewColumnSource();
        vcs.setColumnID(vc.getComponentId());
        vcs.setColumnNumber(vc.getColumnNumber());
        vcs.setComponentId(vc.getComponentId());
        vcs.setSequenceNumber((short)1);
        vcs.setSourceType(ColumnSourceType.NONE);
        vcs.setViewId(vn.getID());
        vcs.setViewSourceId(vs.getComponentId());
        vcs.setViewSrcLrId(hdrLR.getComponentId());
        vcs.setLogicText(vc.getName());
        vcs.setSrcValue("");
        vs.addToColumnSourcesByNumber(vcs);
    }

    protected ViewColumnSource addViewColumnSource(ViewSource vs, ViewColumn vc, String value) {
        ViewColumnSource vcs = new ViewColumnSource();
        vcs.setColumnID(vc.getComponentId());
        vcs.setColumnNumber(vc.getColumnNumber());
        vcs.setComponentId(vc.getComponentId());
        vcs.setSequenceNumber((short)1);
        vcs.setSourceType(ColumnSourceType.CONSTANT);
        vcs.setViewId(vn.getID());
        vcs.setViewSourceId(vs.getComponentId());
        vcs.setViewSrcLrId(hdrLR.getComponentId());
        vcs.setLogicText(vc.getName());
        vcs.setSrcValue(value);
        vcs.setValueLength(value.length());
        vs.addToColumnSourcesByNumber(vcs);
        return vcs;
    }

    private LRField makeHeaderField(String name, short len) {
        LRField hdrFld = Repository.makeNewField(hdrLR);
        hdrFld.setName(name);
        RepoHelper.setField(hdrFld, DataType.BINARY, startPos, len);
        hdrFld.setSigned(false);
        hdrLR.addToFieldsByID(hdrFld);
        hdrLR.addToFieldsByName(hdrFld);
        startPos += len;
        return hdrFld;
    }

    private void addConstant(ColumnAssignmentASTNode casnode, ViewColumnSource vcs) {
        NumAtomAST constNode = (NumAtomAST) ASTFactory.getNodeOfType(ASTFactory.Type.NUMATOM);
        constNode.setValue(vcs.getSrcValue()); 
        casnode.addChildIfNotNull(constNode);
        vcs.setSourceType(ColumnSourceType.CONSTANT);
    }
    
    private void addStringConstant(ColumnAssignmentASTNode casnode, ViewColumnSource vcs) {
        StringAtomAST constNode = (StringAtomAST) ASTFactory.getNodeOfType(ASTFactory.Type.STRINGATOM);
        constNode.setValue(vcs.getSrcValue()); 
        casnode.addChildIfNotNull(constNode);
        vcs.setSourceType(ColumnSourceType.CONSTANT);
    }
    
    private void addRecordCountAccumulator(ColumnAssignmentASTNode casnode) {
        casnode.addChildIfNotNull(recCountAccum);
    }

    public void setRecordCountAccumulator(RecordCountAST recCountAccum2) {
        recCountAccum = recCountAccum2;
    }

}
