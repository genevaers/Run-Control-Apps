package org.genevaers.runcontrolgenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import com.google.common.flogger.FluentLogger;

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.FileType;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.ParmReader;

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

public class TestHelper {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	public static final String TEST_RESOURCES = "./src/test/resources";
	public static final String TEST_PARMNAME = "target/RCAPARM";
	public static final String TEST_PARM = "./src/test/resources/MR91ParmTest";
	public static final String TEST_DBVIEWS = "./src/test/resources/DBVIEWS";
	public static final String TEST_DB2PARM = "./src/test/resources/MR91ParmDB2";
	public static final String TEST_REPORTNAME = "./target/MR91Report";
	public static final String TEST_LOGNAME = "./target/MR91Log";
	public static final String TEST_RUNCONTROLS_RCGGEN = "../RunControls/RCGenerated/";
	public static final String TEST_VDP = "target/VDP";
	public static final String TEST_XLT = "target/XLT";
	public static final String TEST_JLT = "target/JLT";
	public static final String TEST_GEN_VDP = TEST_RUNCONTROLS_RCGGEN + "rcg.VDP";
	public static final String TEST_GEN_XLT = TEST_RUNCONTROLS_RCGGEN + "rcg.XLT";
	public static final String TEST_GEN_JLT = TEST_RUNCONTROLS_RCGGEN + "rcg.JLT";
	public static final String TEST_BASEFILE = "AllTypesBase[9953].xml";
    public static final String TEST_FILE = "mergeAssAll.xml";
    public static final String ALL_TYPES_TARGET = "AllTypesTarget[12156].xml";
    public static final String ARITH_FILE = "CMP_ARITH_PLUS.xml";
    public static final String ASSIGNMENTS = "JMR91Assignments[12075].xml";
    public static final String EVENT_ARTIH = "EVENT_ARITHMETIC_STEPS2[12094].xml";
    public static final String ORDER_SALES = "DEMO_Summary_Customer_Order_Sales_View[10702].xml";
    public static final String STATE_SALES = "DEMO_Summary_Customer_OrderByState_Sales_View[10714].xml";
    public static final String PROD_STATE = "DEMO_Summary_Product_Qty_Sold_By_State_View[10715].xml";
    public static final String DEMO_LOOKUPS = "DEMO_Extract_With_Lookups_VIEW[10689].xml";
    public static final String SKE_DT = "SKEandDT[11074].xml";
    public static final String COL_TOO_BIG = "ColTooBig.xml";
    public static final String FIELD_TOO_BIG = "FieldTooBig.xml";
    public static final String INCOMPATIBLE_DATES = "IncompatibleDates.xml";
    public static final String INCOMPATIBLE_LOOKUP_DATES = "IncompatibleLookupDates.xml";
    public static final String INCOMPATIBLE_FIELD_DATES = "IncompatibleFieldDates.xml";
    public static final String INCOMPATIBLE_LOOKUPFIELD_DATES = "IncompatibleLookupFieldDates.xml";
    public static final String ONE_COL = "OneCol.xml";
    public static final String ONE_COL_LOOKUP = "OneColLookup.xml";
    public static final String ONE_COL_WRITE_EXIT = "OneColWriteExit[12052].xml";
    public static final String SYM_LOOKUP = "SymLookupView[12045].xml";
    public static final String EFF_DATE_SYM_LOOKUP = "EffDateSymLookupView[12046].xml";
    public static final String LOOKUP_EXIT = "lookupExit[9829].xml";
    public static final String DTCDTE = "DTCDTE.xml";
    public static final String SELECTIF = "SelectifDTCDTE.xml";
    public static final String DEMO1 = "V0010689.xml";
    public static final String OLD_TEST_FILE = "oldpass2.xml";
    public static final String FORMAT_COMPILE = "FormatCompileTest[12087].xml";
    public static final String WBCOMPILER_TEST = "WBCompilerTest.xml";
    public static final String GIO_WBXMLFILES = "../GenevaIO/src/test/resources/";
    public static final String ALL_ARITH = "../../../../PETestFramework/xml/AllArith.xml";
	public static final String CFAX_TEST = "CFAXTest[12051].xml";
	public static final String CFXA_TEST = "CFXATest[12051].xml";
	public static final String CONCAT = "ConcatNew[12150].xml";
	public static final String RIGHT = "Right.xml";
	public static final String LEFT = "Left.xml";
	public static final String SUBSTR = "Substr.xml";
	
    public static final String MERGESS = "mergeAssAll.xml";


	public static final String MR91DOT = "mr91.dot";
	public static final String MR91JOINDOT = "mr91Joins.dot";
	public static final String MR91ORIGDOT = "mr91orig.dot";
	public static final String MR91FORMATDOT = "mr91format.dot";

    public static final String FORMAT_AND_COMPILE = "FormatCompileANDTest[12087].xml";

    public static final String FORMAT_OR_COMPILE = "FormatCompileORTest[12087].xml";

    public static final String SKT_LOOKUP = "SKTandExtractRef[9977].xml";

	private static Path resoucesPath = Paths.get(TEST_RESOURCES);
	private static Path targetPath = Paths.get("target/test");

	private static Path wbxmliPath = targetPath.resolve("WBXMLI");

	private static int vcscompid = 1;

	public static void writeToParm(String parms) {
        GersConfigration.setCurrentWorkingDirectory(TestHelper.targetPath.toString());
		writeStringToFile(parms, GersConfigration.getParmFileName());
	}

	public static void writeToIds(String parms) {
		writeStringToFile(parms, TEST_DBVIEWS);
	}

	public static void writeStringToFile(String data, String name) {
		try (FileWriter fw = new FileWriter((new File(name)))){
			fw.write(data);
		} catch (IOException e) {
			logger.atSevere().log("Unable to write to %s:\n%s", name, e.getMessage());
		}
	}

	public static String getTestParmName() {
		return TEST_PARMNAME;
	}

	public static File getReport() {
		return new File(GersConfigration.getReportFileName());
	}

	public static File getLog() {
		return new File(GersConfigration.getLogFileName());
	}

	public static File getXLT() {
		return new File(GersConfigration.getXLTFileName());
	}

	public static File getJLT() {
		return new File(GersConfigration.getXLTFileName());
	}

	public static void setupWithBaseView() {
		try {
			GersConfigration.setCurrentWorkingDirectory(TestHelper.targetPath.toString());
			wbxmliPath.toFile().mkdirs();
			org.apache.commons.io.FileUtils.cleanDirectory(wbxmliPath.toFile());
			File base = resoucesPath.resolve(TEST_BASEFILE).toFile();
			org.apache.commons.io.FileUtils.copyFileToDirectory(base, wbxmliPath.toFile());
			writeToParm(GersConfigration.INPUT_TYPE + "=WBXML\n"
					+ GersConfigration.WB_XML_FILES_SOURCE + "=" + wbxmliPath.toString() + "\n");
			ParmReader pr = new ParmReader();
			pr.populateConfigFrom(GersConfigration.getParmFileName());
		} catch (IOException e) {
			logger.atSevere().log("setupWithBaseView failed %s", e.getMessage());
		}
	}

	public static void setupWithOneColumnView() {
		try {
			wbxmliPath.toFile().mkdirs();
			org.apache.commons.io.FileUtils.cleanDirectory(wbxmliPath.toFile());
			File base = resoucesPath.resolve(ONE_COL).toFile();
			org.apache.commons.io.FileUtils.copyFileToDirectory(base, wbxmliPath.toFile());
			writeToParm(GersConfigration.INPUT_TYPE + "=WBXML\n"
					+ GersConfigration.WB_XML_FILES_SOURCE + "=" + wbxmliPath.toString() + "\n");
			File gendir = new File(TEST_RUNCONTROLS_RCGGEN);
			gendir.mkdirs();
		} catch (IOException e) {
			logger.atSevere().log("setupWithOneColumnView failed %s", e.getMessage());
		}
	}

	public static void setupWithSelectView() {
		try {
			wbxmliPath.toFile().mkdirs();
			org.apache.commons.io.FileUtils.cleanDirectory(wbxmliPath.toFile());
			File base = resoucesPath.resolve(SELECTIF).toFile();
			org.apache.commons.io.FileUtils.copyFileToDirectory(base, wbxmliPath.toFile());
			writeToParm(GersConfigration.INPUT_TYPE + "=WBXML\n"
					+ GersConfigration.WB_XML_FILES_SOURCE + "=" + wbxmliPath.toString() + "\n");
			File gendir = new File(TEST_RUNCONTROLS_RCGGEN);
			gendir.mkdirs();
		} catch (IOException e) {
			logger.atSevere().log("setupWithSelectView failed %s", e.getMessage());
		}
	}

	public static void setupWithView(String xml) {
		try {
			deleteOutputFiles();
			GersConfigration.setCurrentWorkingDirectory(TestHelper.targetPath.toString());
			wbxmliPath.toFile().mkdirs();
			org.apache.commons.io.FileUtils.cleanDirectory(wbxmliPath.toFile());
			File base = resoucesPath.resolve(xml).toFile();
			org.apache.commons.io.FileUtils.copyFileToDirectory(base, wbxmliPath.toFile());
			writeToParm(GersConfigration.INPUT_TYPE + "=WBXML\n"
					+ GersConfigration.WB_XML_FILES_SOURCE + "=" + wbxmliPath.toString() + "\n"
					+ GersConfigration.DOT_JLT + "=Y" + "\n");
			ParmReader pr = new ParmReader();
			pr.populateConfigFrom(GersConfigration.getParmFileName());
		} catch (IOException e) {
			logger.atSevere().log("setupWithView failed %s", e.getMessage());
		}
	}

	public static void setupWithViewCompileOnlyAndDot(String xml, String views, String columnFilter) {
		try {
			deleteOutputFiles();
			wbxmliPath.toFile().mkdirs();
			org.apache.commons.io.FileUtils.cleanDirectory(wbxmliPath.toFile());
			File base = resoucesPath.resolve(xml).toFile();
			org.apache.commons.io.FileUtils.copyFileToDirectory(base, wbxmliPath.toFile());
			writeToParm(GersConfigration.INPUT_TYPE + "=WBXML\n" 
					+ GersConfigration.WB_XML_FILES_SOURCE + "=" + wbxmliPath.toString() + "\n" 
					+ GersConfigration.DOT_XLT + "=Y" + "\n" 
					+ GersConfigration.DOT_JLT + "=Y" + "\n" 
					+ GersConfigration.VIEW_DOTS + "=" + views + "\n" 
					+ GersConfigration.COLUMN_DOTS + "=" + columnFilter + "\n");
			File gendir = new File(TEST_RUNCONTROLS_RCGGEN);
			gendir.mkdirs();
		} catch (IOException e) {
			logger.atSevere().log("setupWithViewCompileOnlyAndDot failed %s", e.getMessage());
		}
	}

    public static void addNoneOverlappingSource() {
		//we will need the view.
		ViewNode v = Repository.getViews().getIterator().next();
		//Use the same LR
		//Make a new PF 
		LogicalFile lf = makeLF("NoneOverLapLF");
		makePFAddedToLF("NoneOverLapPF" + lf.getID(), lf.getID());
		makeAndAddViewSourceToView(v, lf);
    }

	private static void makeAndAddViewSourceToView(ViewNode v, LogicalFile lf) {
		int numVSs =v.getNumberOfViewSources();
		ViewSource existingVS = v.getViewSource((short)(numVSs));
		ViewSource newVS = new ViewSource();
		newVS.setComponentId(existingVS.getComponentId()+100);
		newVS.setSequenceNumber((short)(v.getNumberOfViewSources() + 1));
		newVS.setSourceLFID(lf.getID());
		newVS.setSourceLRID(existingVS.getSourceLRID());
		//Should the view source have the component?
		newVS.setExtractFilter("Filter me good");
		newVS.setExtractOutputLogic("Write something somewhere");
		newVS.setViewId(v.getID());
		addDefaultViewColumnSources(v, newVS);
		v.addViewSource(newVS);
	}

	private static void addDefaultViewColumnSources(ViewNode v, ViewSource newVS) {
		Iterator<ViewColumn> colIt = v.getColumnIterator();
		while(colIt.hasNext()) {
			ViewColumn col = colIt.next();
			ViewColumnSource vcs = new ViewColumnSource();
			vcs.setComponentId(vcscompid++);
			vcs.setColumnNumber(col.getColumnNumber());
			vcs.setSequenceNumber((short)(col.getValuesOfSourcesByNumber().size()+1));
			vcs.setLogicText("Column = " + col.getColumnNumber());
			col.addToSourcesByID(vcs);
			col.addToSourcesByNumber(vcs);
			newVS.addToColumnSourcesByNumber(vcs);
		}
	}

	private static PhysicalFile makePFAddedToLF(String name, int lfid) {
		PhysicalFile pf = new PhysicalFile();
		pf.setName(name);
		pf.setComponentId(Repository.getMaxPFID().getAsInt() + 1);
		pf.setFileType(FileType.DISK);
		pf.setLogicalFileId(lfid);
		Repository.addPhysicalFile(pf);
		return pf;
	}

	private static PhysicalFile makePF(String name) {
		PhysicalFile pf = new PhysicalFile();
		pf.setName(name);
		pf.setComponentId(Repository.getMaxPFID().getAsInt() + 1);
		pf.setFileType(FileType.DISK);
		return pf;
	}

	private static LogicalFile makeLF(String name) {
		LogicalFile lf = new LogicalFile();
		lf.setName(name);
		//Want a new unique id
		lf.setID(Repository.getMaxFileID().getAsInt()+1);
		Repository.getLogicalFiles().add(lf, lf.getID(), lf.getName());
		return lf;
	}

	public static void addOverlappingSource() {
		//we will need the view.
		ViewNode v = Repository.getViews().getIterator().next();
		//Use the same LR
		//Make a new PF 
		LogicalFile lf = makeLF("OverlapLF");
		ViewSource vs = v.getViewSource((short)1);
		//Probably need a function to add a collection of PFs
		lf.addPF(Repository.getLogicalFiles().get(vs.getSourceLFID()).getPFIterator().next());
		makeAndAddViewSourceToView(v, lf);
	}

	public static void addSharedPFToViewSources() {
		PhysicalFile pf = makePF("SharedPF");
		ViewNode v = Repository.getViews().getIterator().next();
		//Add the PF to the LF of each view source
		Iterator<ViewSource> vsi = v.getViewSourceIterator();
		while(vsi.hasNext()) {
			ViewSource vs = vsi.next();
			LogicalFile lf = Repository.getLogicalFiles().get(vs.getSourceLFID());
			pf.setLogicalFileId(lf.getID());
			Repository.addPhysicalFile(pf);
		}
	}

	public static void addSharedPFToViewSourcesStartAt(int start) {
		PhysicalFile pf = makePF("SharedPF");
		ViewNode v = Repository.getViews().getIterator().next();
		//Add the PF to the LF of each view source
		Iterator<ViewSource> vsi = v.getViewSourceIterator();
		while(vsi.hasNext()) {
			ViewSource vs = vsi.next();
			if(vs.getSequenceNumber() >= start) {
				LogicalFile lf = Repository.getLogicalFiles().get(vs.getSourceLFID());
				pf.setLogicalFileId(lf.getID());
				Repository.addPhysicalFile(pf);
			}
		}
	}

	public static Path getMR91dotPath() {
		return targetPath.resolve(MR91DOT);
	}

	public static Path getMR91JoinsdotPath() {
		return targetPath.resolve(MR91JOINDOT);
	}

	public static Path getMR91origdotPath() {
		return targetPath.resolve(MR91ORIGDOT);
	}

	public static Path getMR91FormatdotPath() {
		return targetPath.resolve(MR91FORMATDOT);
	}

	public static Path getResoucesPath() {
		return resoucesPath;
	}

	public static void setColumn1Logic(int viewNum, String logic) {
		ViewColumn vc = Repository.getViews().get(viewNum).getColumnNumber(1);
		vc.findFromSourcesByNumber((short)1).setLogicText(logic);
	}

	public static void setColumnNLogic(int viewNum, String logic, int c) {
		ViewColumn vc = Repository.getViews().get(viewNum).getColumnNumber(c);
		vc.findFromSourcesByNumber((short)1).setLogicText(logic);
	}


    public static void setOutputLogic(int viewNum, String logic) {
		ViewSource vs = Repository.getViews().get(viewNum).getViewSource((short)1);
		vs.setExtractOutputLogic(logic);
    }

	public static void deleteOutputFiles() {
		File xlt = new File(GersConfigration.getXLTFileName());
		xlt.delete();
		File vdp = new File(GersConfigration.getVDPFileName());
		vdp.delete();
		File jlt = new File(GersConfigration.getJLTFileName());
		jlt.delete();
	}

	public static void setExtractFilter(int viewNum, String logic) {
		ViewSource vs = Repository.getViews().get(viewNum).getViewSource((short)1);
		vs.setExtractFilter(logic);
	}

	public static void setFormatFilter(int viewNum, String logic) {
		Repository.getViews().get(viewNum).setFormatFilterLogic(logic);
	}

    public static void setColumnCalculation(int viewNum, int colNum, String logic) {
		Repository.getViews().get(viewNum).getColumnNumber(colNum).setColumnCalculation(logic);
    }
}
