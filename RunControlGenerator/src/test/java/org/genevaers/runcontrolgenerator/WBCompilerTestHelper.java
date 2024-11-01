package org.genevaers.runcontrolgenerator;

import java.io.File;
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

public class WBCompilerTestHelper {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	public static final String TEST_RESOURCES = "./src/test/resources";
	public static final String TEST_PARMNAME = "./src/test/resources/MR91ParmTest";
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


	public static final String MR91DOT = "mr91.dot";
	public static final String MR91JOINDOT = "mr91Joins.dot";
	public static final String MR91ORIGDOT = "mr91orig.dot";
	public static final String MR91FORMATDOT = "mr91format.dot";


	private static Path resoucesPath = Paths.get(TEST_RESOURCES);
	private static Path targetPath = Paths.get("target");

	private static int vcscompid = 1;

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
		File xlt = new File(TEST_XLT);
		xlt.delete();
		File vdp = new File(TEST_VDP);
		vdp.delete();
		File jlt = new File(TEST_JLT);
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

	public static void addTestLogicalRecord() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'addTestLogicalRecord'");
	}
}
