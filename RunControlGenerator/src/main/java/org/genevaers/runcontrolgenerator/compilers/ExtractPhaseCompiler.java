package org.genevaers.runcontrolgenerator.compilers;

/*
 * Copyright Contributors to the GenevaERS Project.
								SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation
								2008
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


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.compilers.extract.BuildGenevaASTVisitor;
import org.genevaers.compilers.extract.ExtractColumnCompiler;
import org.genevaers.compilers.extract.ExtractFilterCompiler;
import org.genevaers.compilers.extract.ExtractOutputCompiler;
import org.genevaers.compilers.extract.astnodes.ASTFactory;
import org.genevaers.compilers.extract.astnodes.ExtractAST2Dot;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.ExtractFilterAST;
import org.genevaers.compilers.extract.astnodes.ExtractOutputAST;
import org.genevaers.compilers.extract.astnodes.LFAstNode;
import org.genevaers.compilers.extract.astnodes.PFAstNode;
import org.genevaers.compilers.extract.astnodes.ViewColumnSourceAstNode;
import org.genevaers.compilers.extract.astnodes.ViewSourceAstNode;
import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.genevaers.compilers.extract.emitters.LogicTableEmitter;
import org.genevaers.genevaio.dataprovider.RepoDataProvider;
import org.genevaers.genevaio.ltfile.LogicTable;
import org.genevaers.genevaio.yamlreader.LazyYAMLReader;
import org.genevaers.genevaio.yamlreader.YAMLizer;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.runcontrolgenerator.singlepassoptimiser.LogicGroup;
import org.genevaers.runcontrolgenerator.singlepassoptimiser.ViewSourceWrapper;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.Status;
import org.genevaers.visualisation.GraphVizRunner;

import com.google.common.flogger.FluentLogger;

public class ExtractPhaseCompiler {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private static List<LogicGroup> logicGroups;   
	private static ExtractBaseAST extractRoot;
	private static ExtractBaseAST joinsRoot;
	private static LogicTableEmitter xltEmitter = new LogicTableEmitter();
	private static LogicTableEmitter jltEmitter = new LogicTableEmitter();
	private static ViewSourceAstNode vsnode;
	private static Status status = Status.OK;
	private static JLTTreeGenerator jltgen;
	
	public ExtractPhaseCompiler(List<LogicGroup> lgs) {
		logicGroups = lgs;
	}

	public static void reset() {
		xltEmitter = new LogicTableEmitter();
		jltEmitter = new LogicTableEmitter();
		extractRoot = (ExtractBaseAST) ASTFactory.getNodeOfType(ASTFactory.Type.ERSROOT);
		ExtractBaseAST.clearLastColumnWithAWrite();
		vsnode = null;
		status =  Status.OK;
	}

	public static Status run(List<LogicGroup> lgs) {
		logicGroups = lgs;
		buildTheAST();
		if(Repository.getCompilerErrors().size() == 0) {
			//once the AST is built we now have enough data
			//to generate the JLT
			//This has to be done before building the XLT
			//since the lookup numbers are changed when we generate the JLT
			buildTheJoinLogicTable();
			buildTheExtractLogicTable();
			wholeViewChecks();
		}
		return Repository.getCompilerErrors().size() > 0 ? Status.ERROR : Status.OK;
	}

	public static void buildTheAST() {
		extractRoot = (ExtractBaseAST) ASTFactory.getNodeOfType(ASTFactory.Type.ERSROOT);
		if(GersConfigration.getInputType().equals("YAML")) {
			YAMLizer.setEnvironmentName(GersConfigration.getParm(GersConfigration.ENVIRONMENT_ID));
			LazyYAMLReader lyr = new LazyYAMLReader();
			lyr.setEnvironmentName(GersConfigration.getParm(GersConfigration.ENVIRONMENT_ID));
			BuildGenevaASTVisitor.setDataProvider(lyr);
		} else {
			BuildGenevaASTVisitor.setDataProvider(new RepoDataProvider());
		}
		Iterator<LogicGroup> lgi = logicGroups.iterator();
		while(lgi.hasNext()) {
			LogicGroup lg = lgi.next();
			addNodesFromLogicGroup(lg);
		}
		Repository.saveNumberOfExtractViews();
		writeXLTDotIfEnabled();
	}

	public static void buildViewSourceAST(ViewSource vs) {
		vsnode = buildVSNodeAndAddToRoot(vs);
		ExtractFilterAST ef = (ExtractFilterAST) ASTFactory.getNodeOfType(ASTFactory.Type.EXTRFILTER);
		vsnode.addChildIfNotNull(ef);
		ExtractFilterCompiler efc = new ExtractFilterCompiler();
		efc.setViewSource(vsnode.getViewSource());
		try {
			efc.processLogicAndAddNodes(ef);
		} catch (IOException e) {
			logger.atSevere().log("buildViewSourceAST %s", e.getMessage());
		}
	}

	public static void buildViewColumnSourceAST(ViewColumnSource vcs) {
		if(vsnode == null) {
			buildVSNodeAndAddToRoot(Repository.getViews().get(vcs.getViewId()).getViewSource(vcs.getSequenceNumber()));
		}
		ViewColumnSourceAstNode vcsn = (ViewColumnSourceAstNode) ASTFactory.getNodeOfType(ASTFactory.Type.VIEWCOLUMNSOURCE);
		vcsn.setViewColumnSource(vcs);
		vsnode.addChildIfNotNull(vcsn);
		compileColumn(vcsn);		
		vcsn.checkAssigned();
		writeXLTDotIfEnabled();
	}

	public static void buildViewOutputAST(ViewSource vs) {
		if(vsnode == null) {
			buildVSNodeAndAddToRoot(Repository.getViews().get(vs.getViewId()).getViewSource(vs.getSequenceNumber()));
		}
		ExtractOutputAST eo = (ExtractOutputAST) ASTFactory.getNodeOfType(ASTFactory.Type.EXTRACTOUTPUT);
		vsnode.addChildIfNotNull(eo);
		ExtractOutputCompiler eoc = new ExtractOutputCompiler();
		eoc.setViewSource(vsnode.getViewSource());
		try {
			eoc.processLogicAndAddNodes(eo);
		} catch (IOException e) {
			logger.atSevere().log("buildViewSourceAST %s", e.getMessage());
		}
	}

	private static ViewSourceAstNode buildVSNodeAndAddToRoot(ViewSource vs) {
		vsnode = (ViewSourceAstNode) ASTFactory.getNodeOfType(ASTFactory.Type.VIEWSOURCE);
		vsnode.setViewSource(Repository.getViews().get(vs.getViewId()).getViewSource(vs.getSequenceNumber()));
		extractRoot.addChildIfNotNull(vsnode);
		return vsnode;
	}


	private static void writeXLTDotIfEnabled() {
		if(GersConfigration.isXltDotEnabled()) {
			ExtractAST2Dot.setFilter(GersConfigration.getViewDots().length() >0 || GersConfigration.getColumnDots().length()>0);
			ExtractAST2Dot.setViews(GersConfigration.getViewDots().split(","));
			ExtractAST2Dot.setCols(GersConfigration.getColumnDots().split(","));
			ExtractAST2Dot.write(extractRoot, Paths.get("target/XLT.dot"));
		}
	}

	private static void addNodesFromLogicGroup(LogicGroup lg) {
		lg.getLfID();
		LFAstNode lfNode = (LFAstNode)ASTFactory.getNodeOfType(ASTFactory.Type.LF);
		lfNode.setLogicalFile(lg.getLogicalFile());
		if(GersConfigration.isPFDotEnabled()) {
			addPFNodes(lfNode);
		} else {
			extractRoot.addChildIfNotNull(lfNode);
		}
		addViewSourceNodesToLFNode(lfNode, lg);
	}

	private static void addViewSourceNodesToLFNode(LFAstNode lfNode, LogicGroup lg) {
		Iterator<ViewSourceWrapper> vsi = lg.getSortedViewSources().iterator();
		while(vsi.hasNext()) {
			ViewSourceWrapper vsw = vsi.next();
			ViewSourceAstNode vsnode = (ViewSourceAstNode) ASTFactory.getNodeOfType(ASTFactory.Type.VIEWSOURCE);
			ViewSource vs = vsw.getViewSource(); 
			logger.atInfo().log("Compile view %d source %d", vs.getViewId(), vs.getSequenceNumber());
			vsnode.setViewSource(vs);
			lfNode.addChildIfNotNull(vsnode);
			ExtractBaseAST.clearLastColumnWithAWrite();
			addViewSourceNodes(vsnode);
			addViewColumnSourceNodes(vsnode);
		}
	}

	private static void addViewSourceNodes(ViewSourceAstNode vsnode) {
		ExtractFilterAST ef = (ExtractFilterAST) ASTFactory.getNodeOfType(ASTFactory.Type.EXTRFILTER);
		vsnode.addChildIfNotNull(ef);
		ExtractFilterCompiler efc = new ExtractFilterCompiler();
		efc.setViewSource(vsnode.getViewSource());
		try {
			efc.processLogicAndAddNodes(ef);
		} catch (IOException e) {
			logger.atSevere().log("addViewSourceNodes %s", e.getMessage());
		}
	}

	private static void addViewColumnSourceNodes(ViewSourceAstNode vsnode) {
		Iterator<ViewColumnSource> vcsi = vsnode.getViewSource().getIteratorForColumnSourcesByNumber();
		while(vcsi.hasNext()) {
			ViewColumnSource vcs = vcsi.next();
			ViewColumnSourceAstNode vcsn = (ViewColumnSourceAstNode) ASTFactory.getNodeOfType(ASTFactory.Type.VIEWCOLUMNSOURCE);
			vcsn.setViewColumnSource(vcs);
			vsnode.addChildIfNotNull(vcsn);
			compileColumn(vcsn);
			resolveSortKeyTitleLookups(vcs);
			vcsn.checkAssigned();
		}
		compileExtractOutputLogic(vsnode);
	}

	private static void resolveSortKeyTitleLookups(ViewColumnSource vcs) {
		if(vcs.getSortTitleLookupId() > 0) {
			logger.atInfo().log("Need Sort Title lookup id %d", vcs.getSortTitleLookupId());
			LookupPath lkup = Repository.getLookups().get(vcs.getSortTitleLookupId());
			LRField f = Repository.getFields().get(vcs.getSortTitleFieldId());
			Repository.getJoinViews().addSortKeyJLTViewField(lkup, f);
		}
	}

	private static void compileExtractOutputLogic(ViewSourceAstNode vsnode) {
		boolean runEOC = true;
		if(GersConfigration.getInputType().equalsIgnoreCase("VDPXML")) {
			if(noWriteStatementMissing(vsnode)) {
				runEOC = false; //There must have been a write in the last column so don't generate write
			} else {
				runEOC = true; //need a write statement
			}
		}

		//What if the extract output logic does not contain a write?
		if(runEOC) {
			ExtractOutputAST eo = (ExtractOutputAST) ASTFactory.getNodeOfType(ASTFactory.Type.EXTRACTOUTPUT);
			vsnode.addChildIfNotNull(eo);
			ExtractOutputCompiler eoc = new ExtractOutputCompiler();
			eoc.setViewSource(vsnode.getViewSource());
			try {
				eoc.processLogicAndAddNodes(eo);
			} catch (IOException e) {
				logger.atSevere().log("compileExtractOutputLogic %s", e.getMessage());
			}	
		}
	}

	private static boolean noWriteStatementMissing(ViewSourceAstNode vsnode) {
		return (ExtractBaseAST.getLastColumnWithAWrite(vsnode.getViewSource()) == vsnode.getNumberOfColumns());
	}

	private static void compileColumn(ViewColumnSourceAstNode vcsn) {
		logger.atFine().log("Compiling column %d", vcsn.getViewColumnSource().getColumnNumber());
		ExtractColumnCompiler ecc = new ExtractColumnCompiler();
		try {
			ecc.processLogicAndAddNodes(vcsn);
			if(Repository.newErrorsDetected()) {
//				logger.atSevere().log("%d Errors detected. Logic Table will not be written.", Repository.getCompilerErrors().size());
				status = Status.ERROR;
			}
		} catch (IOException e) {
			logger.atSevere().log("noWriteStatementMissing %s", e.getMessage());
		}	
	}

	public static ExtractBaseAST getXltRoot() {
		return extractRoot;
	}

	public static void addPFNodes(LFAstNode lfNode) {
		Iterator<PhysicalFile> pfi = lfNode.getLogicalFile().getPFIterator();
		while(pfi.hasNext()) {
			PFAstNode pfn = (PFAstNode)ASTFactory.getNodeOfType(ASTFactory.Type.PF);
			pfn.setPhysicalFile(pfi.next());
			pfn.addChildIfNotNull(lfNode);
			extractRoot.addChildIfNotNull(pfn);
		}
	}

	public static void buildTheJoinLogicTable() {
		//To do this use a JLT generator
		//Which knows how to take the Repo data and make the beast
		//All of the required Join data should have been accumulated
		//Let's see what we have
		jltgen = new JLTTreeGenerator(jltEmitter);
		ExtractBaseAST.setLogicTableEmitter(jltEmitter);
		joinsRoot = jltgen.buildJoinViews();
		Repository.getJoinViews().logdata();
		writeJltDotIfEnabled();
		jltgen.emit();
	}

	public static REHHeader getRehHeader() {
		return jltgen != null ? jltgen.getRehHeader() : null;
	}

	public static RTHHeader getRthHeader() {
		return jltgen != null ? jltgen.getRthHeader() : null;
	}

	private static void writeJltDotIfEnabled() {
		if(GersConfigration.isJltDotEnabled()) {
        	ExtractAST2Dot.write(joinsRoot, Paths.get("target/JLT.dot"));
		}
	}

	public static void buildTheExtractLogicTable() {
		if(Repository.newErrorsDetected()) {
			logger.atSevere().log("%d Errors detected. Logic Table will not be written.", Repository.getCompilerErrors().size());
			status = Status.ERROR;
		} else {
			ExtractBaseAST.setLogicTableEmitter(xltEmitter);
			((EmittableASTNode)extractRoot).emit();
		}
	}

    public static void wholeViewChecks() {
		checkWriteStatements(extractRoot);
		lookForNoAssignments();
	}

	private static void lookForNoAssignments() {
		//Start at the root find the VCS node and see if any have not assigned
		List<ExtractBaseAST> vcsNodes = extractRoot.getChildNodesOfType(Type.VIEWCOLUMNSOURCE);
		Iterator<ExtractBaseAST> vcsi = vcsNodes.iterator();
		while (vcsi.hasNext()) {
			ViewColumnSourceAstNode vcs = (ViewColumnSourceAstNode) vcsi.next();
			if(vcs.isAssignedTo()) {

			} else {
				ViewSourceAstNode vs = (ViewSourceAstNode)vcs.getParent();
				Repository.addWarningMessage(ExtractBaseAST.makeCompilerMessage("Column not assigned"));              				
			}
		}
	}

	private static void checkWriteStatements(ExtractBaseAST root) {
		ViewSourceAstNode localvsnode = (vsnode == null ? (ViewSourceAstNode) root.getChildIterator().next().getChildIterator().next() : vsnode);
		if(noWriteStatementMissing(localvsnode)) {
			logger.atInfo().log("LT built for view %s", localvsnode.getViewSource().getViewId());
		} else {
			Repository.addErrorMessage(ExtractBaseAST.makeCompilerMessage("No write statements were found"));              			
		}
	}

	public static LogicTable getExtractLogicTable() {
        return xltEmitter.getLogicTable();
    }

    public static LogicTable getJoinLogicTable() {
        return jltEmitter.getLogicTable();
    }

	public static void dotTo(Path dotfile) {
		ExtractAST2Dot.write(extractRoot, dotfile);
		//Maybe WB Compiler should be above the RCG and RCA
		//So it can use them
		GraphVizRunner gr = new GraphVizRunner();
		gr.processDot(dotfile.toFile());
	}

}
