package org.genevaers.runcontrolgenerator.workbenchinterface;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023.
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
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.antlr.v4.runtime.tree.ParseTree;
import org.genevaers.compilers.extract.BuildGenevaASTVisitor;
import org.genevaers.compilers.extract.astnodes.ExtractAST2Dot;
import org.genevaers.compilers.format.astnodes.FormatBaseAST;
import org.genevaers.genevaio.dbreader.DBReaderBase;
import org.genevaers.genevaio.dbreader.DatabaseConnectionParams;
import org.genevaers.genevaio.dbreader.LazyDBReader;
import org.genevaers.genevaio.dbreader.WBConnection;
import org.genevaers.genevaio.ltfile.LTLogger;
import org.genevaers.genevaio.ltfile.LogicTable;
import org.genevaers.grammar.GenevaERSParser.GoalContext;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewDefinition;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.ColumnSourceType;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.ViewType;
import org.genevaers.repository.data.CompilerMessage;
import org.genevaers.repository.data.CompilerMessageSource;
import org.genevaers.repository.data.LookupRef;
import org.genevaers.repository.data.ViewLogicDependency;
import org.genevaers.runcontrolgenerator.compilers.ExtractPhaseCompiler;

import com.google.common.flogger.FluentLogger;


public abstract class WorkbenchCompiler implements SyntaxChecker, DependencyAnalyser {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private static DatabaseConnectionParams params = new DatabaseConnectionParams();
	protected WBCompilerType type;
	private GoalContext tree;
	protected ParseErrorListener errorListener;
	private static LazyDBReader dataProvider = new LazyDBReader();;
	private int envId;
	private ViewType viewType;
	private ColumnData columnData;
	private static ViewNode currentView;
	protected static ViewColumnSource currentViewColumnSource;
	protected static ViewSource currentViewSource;
	private static int environmentID;
	private static int sourceLR;
	private static int sourceLF;
	private static int currentColumnNumber;
	private static Set<Integer> columnRefs = new HashSet<Integer>();


	//Common setup functions
    public static void setSQLConnection(Connection c) {
		WBConnection wbc = new WBConnection();
		wbc.setSQLConnection(c);
		dataProvider.setDatabaseConnection(wbc);
		dataProvider.setParams(params);
		BuildGenevaASTVisitor.setDataProvider(dataProvider);
    }

	public static void setSchema(String schema) {
		params.setSchema(schema);
	}

    public void setViewDetails(int environmentId, int viewNum, ViewType type) {
        envId = environmentId;
		int viewID = viewNum;
		viewType = type;
    }

	public static void setEnvironment(int i) {
		environmentID = i;
		params.setEnvironmentID(Integer.toString(i));
		dataProvider.setEnvironmentID(i);
	}

    public static void setSourceLRID(int i) {
        sourceLR = i;
		dataProvider.loadLR(environmentID, sourceLR);
    }

    public static void setSourceLFID(int id) {
        sourceLF = id;
    }

	public static void addView(ViewData vd) {
    	ViewDefinition vdef = new ViewDefinition();
        vdef.setComponentId(vd.getId());
        vdef.setName(vd.getName());
        vdef.setViewType(ViewType.values()[vd.getTypeValue()]);
        currentView = Repository.getViewNodeMakeIfDoesNotExist(vdef);
		currentView.setFormatFilterLogic(vd.getFormatFilter());
		DBReaderBase.addViewId(vd.getId());
	}

    public static void addColumn(ColumnData ci) {
    	ViewColumn vc = new ViewColumn();
        vc.setColumnNumber(ci.getColumnNumber());
        vc.setComponentId(ci.getColumnId());
        vc.setDataType(DataType.values()[ci.getDataTypeValue()]);
        vc.setDateCode(DateCode.values()[ci.getDateCodeValue()]);
        vc.setDecimalCount((short)ci.getNumDecimalPlaces());
        vc.setExtractArea(ExtractArea.values()[ci.getExtractAreaValue()]);
		if(vc.getExtractArea() == ExtractArea.SORTKEY) {
			addSortkeyToRepo(ci);
		}
		if(vc.getExtractArea() == ExtractArea.AREACALC) {
        	vc.setExtractAreaPosition((short)1); //A legacy thing
		} else {
        	vc.setExtractAreaPosition((short)ci.getExtractAreaPosition());
		}
        vc.setFieldLength((short)ci.getLength());
        vc.setFieldName(ci.getName());
        vc.setHeaderJustifyId(JustifyId.values()[ci.getAlignment()]);
        vc.setRounding((short)ci.getRounding());
        vc.setSigned(ci.isSigned());
        vc.setStartPosition((short)ci.getStartPosition());
        vc.setViewId(ci.getViewID());
		vc.setColumnCalculation(ci.getColumnCalculation());
        currentView.addViewColumn(vc);
		FormatBaseAST.setCurrentColumnNumber(vc.getColumnNumber());
		currentColumnNumber = vc.getColumnNumber();
    }

	//Derving the SK from the column isn't quite going to work
	//The key values can be overridden in the workbench
	//Workbench should supply SKData
	private static void addSortkeyToRepo(ColumnData ci) {
		ViewSortKey vsk = new ViewSortKey();
		vsk.setColumnId(ci.getColumnId());
        vsk.setComponentId(ci.getColumnId());
        vsk.setSortKeyDataType(DataType.values()[ci.getDataTypeValue()]);
        vsk.setSortKeyDateTimeFormat(DateCode.values()[ci.getDateCodeValue()]);
        vsk.setSkDecimalCount((short)ci.getNumDecimalPlaces());
        vsk.setSkFieldLength((short)ci.getLength());
        vsk.setSkRounding((short)ci.getRounding());
        vsk.setSortKeySigned(ci.isSigned());
        vsk.setSkStartPosition((short)ci.getStartPosition());
        vsk.setViewSortKeyId(ci.getColumnId());
		currentView.addViewSortKey(vsk);
	}

	public static void addViewSource(ViewSourceData vsd) {
    	currentViewSource = new ViewSource();
        currentViewSource.setComponentId(vsd.getId());
        currentViewSource.setViewId(vsd.getViewID());
        currentViewSource.setExtractFilter(vsd.getExtractFilter());
		currentViewSource.setExtractOutputLogic(vsd.getOutputLogic());
        currentViewSource.setSequenceNumber((short)vsd.getSequenceNumber());
        currentViewSource.setSourceLRID(vsd.getSourceLrId());
        currentView.addViewSource(currentViewSource);
		FormatBaseAST.setCurrentView(currentView.getID());
		Repository.getDependencyCache().setCurrentParentId(currentViewSource.getComponentId());
	}

	public static void addViewColumnSource(ViewColumnSourceData vcsd) {
    	currentViewColumnSource = new ViewColumnSource();
        currentViewColumnSource.setColumnID(vcsd.getColumnId());
        currentViewColumnSource.setColumnNumber(vcsd.getColumnNumber());
        currentViewColumnSource.setComponentId(vcsd.getColumnId());
        currentViewColumnSource.setLogicText(vcsd.getLogicText());
        currentViewColumnSource.setSequenceNumber((short)vcsd.getSequenceNumber());
        currentViewColumnSource.setSourceType(ColumnSourceType.LOGICTEXT);
        currentViewColumnSource.setViewSourceId(vcsd.getViewSourceId());
        currentViewColumnSource.setViewId(vcsd.getViewID());
        currentViewColumnSource.setViewSrcLrId(vcsd.getViewSourceLrId());
		currentViewSource.addToColumnSourcesByNumber(currentViewColumnSource);
        currentView.addViewColumnSource(currentViewColumnSource);
		Repository.getDependencyCache().setCurrentParentId(currentViewColumnSource.getComponentId());
	}

	public void run() {
		buildAST();
		buildLogicTablesAndPerformWholeViewChecks();
	}

	public void validate() {
		buildAST();
		ExtractPhaseCompiler.buildTheJoinLogicTable();
		ExtractPhaseCompiler.buildTheExtractLogicTable();
	}

	public abstract void buildAST();

	public static void buildLogicTablesAndPerformWholeViewChecks() {
		ExtractPhaseCompiler.buildTheJoinLogicTable();
		ExtractPhaseCompiler.buildTheExtractLogicTable();
		ExtractPhaseCompiler.wholeViewChecks();
	}

	public static LogicTable getXlt() {
		return ExtractPhaseCompiler.getExtractLogicTable();
	}

	public static String getLogicTableLog() {
		return LTLogger.logRecords(getXlt());
	}

	public static boolean hasErrors() {
		return Repository.getCompilerErrors().size() > 0;
	}

	public static List<CompilerMessage> getErrorMessages() {
		return Repository.getCompilerErrors();
	}

	public static List<String> getErrors() {
		List<String> errs = new ArrayList<String>();
		Iterator<CompilerMessage> ei = Repository.getCompilerErrors().iterator();
		while(ei.hasNext()) {
			CompilerMessage e = ei.next();
			errs.add(e.getDetail());
		}
		return errs;
	}

	public static boolean hasWarnings() {
		return Repository.getWarnings().size() > 0;
	}

	public static List<String> getWarnings() {
		List<String> warns = new ArrayList<String>();
		Iterator<CompilerMessage> wi = Repository.getWarnings().iterator();
		while(wi.hasNext()) {
			CompilerMessage w = wi.next();
			warns.add(w.getDetail());
		}
		return warns;
	}

	public static List<CompilerMessage> getWarningMessages() {
		return Repository.getWarnings();
	}

	@Override
	public ParseTree getParseTree() {
		return tree;
	}

	@Override
	public boolean hasSyntaxErrors() {
		return errorListener.getErrors().size() > 0;
	}

	@Override
	public List<String> getSyntaxErrors() {
		return errorListener.getErrors();
	}

	@Override
	public int getNumberOfSyntaxWarningss() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Stream<Integer> getFieldIDs() {
		return Repository.getDependencyCache().getFieldIDs();
	}

	public Stream<LookupRef> getLookupsStream() {
		return Repository.getDependencyCache().getLookupsStream();
	}

	public Set<Integer> getExitIDs() {
		return Repository.getDependencyCache().getExitIDs();
	}

	public Stream<Integer> getLFPFAssocIDs() {
		return Repository.getDependencyCache().getLFPFAssocIDs();
	}

	public void clearErrors() {
	}

	public static void reset() {
		Repository.clearAndInitialise();
        ExtractPhaseCompiler.reset();
        Repository.setGenerationTime(Calendar.getInstance().getTime());
		currentViewColumnSource = null;
		currentViewSource = null;
		currentView = null;
		currentColumnNumber = 0;
		columnRefs.clear();
		DBReaderBase.clearViewIds();
	}

	public static String getDependenciesAsString() {
		Iterator<ViewLogicDependency> depsi = Repository.getDependencyCache().getDependenciesStream().iterator();
		String deps = "Dependencies\n";
		while (depsi.hasNext()) {
			ViewLogicDependency dep = depsi.next();
			switch (dep.getLogicTextType()) {
				case EXTRACT_RECORD_FILTER:
					deps += "Filter ";
					break;
				case EXTRACT_COLUMN_ASSIGNMENT:
				deps += "Assignment ";
				break;
				case EXTRACT_RECORD_OUTPUT:
				deps += "Output ";
					break;
				case FORMAT_COLUMN_CALCULATION:
					break;
				case FORMAT_RECORD_FILTER:
					break;
				case INVALID:
					break;
				default:
					break;
			
			}
			deps += dep.getParentId();
			if(dep.getFileAssociationId() != null) {
				deps += " File assoc " + dep.getFileAssociationId();
			} else if(dep.getLookupPathId() != null) {
				deps += " Lookup " + dep.getLookupPathId() + " Field " + dep.getLrFieldId();
			} else if(dep.getLrFieldId() != null) {
				deps += " Field " + dep.getLrFieldId();
			} else if(dep.getUserExitRoutineId() != null) {
				deps += " Exit " + dep.getUserExitRoutineId();
			}
			deps += "\n";
		}
		return deps;
	}

	public static Stream<ViewLogicDependency> getDependenciesStream() {
		return Repository.getDependencyCache().getDependenciesStream();
	}

	public static Stream<CompilerMessage> getErrorMessageStream() {
		return Repository.getCompilerErrors().stream();
	}

	public static Stream<CompilerMessage> getWarningMessageStream() {
		return Repository.getWarnings().stream();
	}


	@Override
	public Map<Integer, List<Integer>> getLookupIDs() {
		throw new UnsupportedOperationException("Unimplemented method 'getLookupIDs'");
	}

	public static String getVersion() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		String ver = "";
		try (InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
			properties.load(resourceStream);
			ver = "RCG" + ": " + properties.getProperty("build.version");
		} catch (IOException e) {
			logger.atSevere().log("Cannot opem applicaiton.properties %s", e.getMessage());
		}
		return ver;
	}

	public static void setDataProvider(LazyDBReader dp) {
		dataProvider = dp;
	}

	public static void addViewPropertiesErrorMessage(String msg) {
		Repository.addErrorMessage(new CompilerMessage(environmentID, CompilerMessageSource.VIEW_PROPS, sourceLR, sourceLF, currentColumnNumber, msg));
	}

	public static void addViewPropertiesWarningMessage(String msg) {
		Repository.addWarningMessage(new CompilerMessage(environmentID, CompilerMessageSource.VIEW_PROPS, sourceLR, sourceLF, currentColumnNumber, msg));
	}

	public static void addFormatCalculationErrorMessage(String msg) {
		Repository.addErrorMessage(new CompilerMessage(environmentID, CompilerMessageSource.COLUMN_CALC, sourceLR, sourceLF, currentColumnNumber, msg));
	}

	public static void addFormatFilterErrorMessage(String msg) {
		Repository.addErrorMessage(new CompilerMessage(environmentID, CompilerMessageSource.FORMAT_FILTER, sourceLR, sourceLF, 0, msg));
	}

	public static void addColumnAssignmentErrorMessage(String msg) {
		Repository.addErrorMessage(new CompilerMessage(environmentID, CompilerMessageSource.COLUMN, sourceLR, sourceLF, currentColumnNumber, msg));
	}

	public static int getCurrentColumnNumber() {
		return currentColumnNumber;
	}

	public static void setCurrentColumnNumber(int currentColumnNumber) {
		WorkbenchCompiler.currentColumnNumber = currentColumnNumber;
		FormatBaseAST.setCurrentColumnNumber(currentColumnNumber);
		currentViewColumnSource = currentViewSource.findFromColumnSourcesByNumber(currentColumnNumber);
	}

	public static void clearNewErrorsDetected() {
		Repository.clearNewErrorsDetected();
	}

	public static boolean newErrorsDetected() {
		return Repository.newErrorsDetected();
	}

	public static boolean hasNoNewErrors() {
		return Repository.newErrorsDetected() == false;
	}

	public static void dotTo(Path dir) {
		ExtractPhaseCompiler.dotTo(dir);
	}

	public static void setDotFilter() {
		ExtractAST2Dot.setFilter(true);
	}

	public static void setDotViews(String views) {
		ExtractAST2Dot.setViews(views.split(","));
	}

	public static void setDotCols(String cols) {
		ExtractAST2Dot.setCols(cols.split(","));
	}

	public static void setCurrentViewSource(ViewSource vs) {
		currentViewSource = vs;
	}

	public static String checkSyntaxFormatFilter(int viewId, String logic) {
		ViewNode vn = Repository.getViews().get(viewId);
		vn.setFormatFilterLogic(logic);
		WBFormatFilterCompiler ffc = new WBFormatFilterCompiler();
		return ffc.generateCalcStack(viewId);
	}

	public static String checkSyntaxFormatCalc(int viewId, int colnum, String logic) {
		ViewColumn vc = Repository.getViews().get(viewId).getColumnNumber(colnum);
		vc.setColumnCalculation(logic);
		WBFormatCalculationCompiler fcc = new WBFormatCalculationCompiler();
		return fcc.generateCalcStack(viewId, colnum);
	}

	public static String checkSyntaxExtractFilter(int viewId, int srcnum, String logic) {
		currentView = Repository.getViews().get(viewId);
		currentViewSource = currentView.getViewSource((short)srcnum);
		currentViewSource.setExtractFilter(logic);
		WBExtractFilterCompiler efc = new WBExtractFilterCompiler();
		efc.validate();
		return getLogicTableLog();
	}

	public static String checkSyntaxExtractAssign(int viewId, int srcnum, int colnum, String logic) {
		currentView = Repository.getViews().get(viewId);
		currentViewSource = currentView.getViewSource((short)srcnum);
		currentViewColumnSource = currentViewSource.findFromColumnSourcesByNumber(colnum);
		currentViewColumnSource.setLogicText(logic);
		WBExtractColumnCompiler efc = new WBExtractColumnCompiler();
		efc.validate();
		return getLogicTableLog();
	}

	public static String checkSyntaxExtractOutput(int viewId, int srcnum, String logic) {
		currentView = Repository.getViews().get(viewId);
		currentViewSource = currentView.getViewSource((short)srcnum);
		currentViewSource.setExtractOutputLogic(logic);
		WBExtractOutputCompiler efc = new WBExtractOutputCompiler();
		efc.validate();
		return getLogicTableLog();
	}

	public static String compileFormatFilter(int viewId) {
		WBFormatFilterCompiler ffc = new WBFormatFilterCompiler();
		String cs = ffc.generateCalcStack(viewId);
		columnRefs.addAll(ffc.getColumnRefs());
		return cs;
	}

	public static String compileFormatCalc(int viewId, int colnum) {
		WBFormatCalculationCompiler fcc = new WBFormatCalculationCompiler();
		String cs = fcc.generateCalcStack(viewId, colnum);
		columnRefs.addAll(fcc.getColumnRefs());
		return cs;
	}

	public static void compileExtractFilter(int viewId, int srcnum) {
		currentView = Repository.getViews().get(viewId);
		currentViewSource = currentView.getViewSource((short)srcnum);
		WBExtractFilterCompiler efc = new WBExtractFilterCompiler();
		efc.buildAST();
	}

	public static void compileExtractAssign(int viewId, int srcnum, int colnum) {
		currentView = Repository.getViews().get(viewId);
		currentViewSource = currentView.getViewSource((short)srcnum);
		currentViewColumnSource = currentViewSource.findFromColumnSourcesByNumber(colnum);
		WBExtractColumnCompiler efc = new WBExtractColumnCompiler();
		efc.buildAST();
	}

	public static void compileExtractOutput(int viewId, int srcnum) {
		currentView = Repository.getViews().get(viewId);
		currentViewSource = currentView.getViewSource((short)srcnum);
		WBExtractOutputCompiler efc = new WBExtractOutputCompiler();
		efc.buildAST();
	}

    public static Set<Integer> getColumnRefs() {
		return columnRefs;
    }
}
