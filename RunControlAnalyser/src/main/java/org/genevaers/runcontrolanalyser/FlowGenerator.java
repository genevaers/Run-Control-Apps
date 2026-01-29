package org.genevaers.runcontrolanalyser;

import java.io.FileWriter;

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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.genevaers.genevaio.dots.FlowDotFile;
import org.genevaers.genevaio.dots.JoinDotWriter;
import org.genevaers.genevaio.dots.ViewDotFile;
import org.genevaers.genevaio.dots.ViewLinks;
import org.genevaers.genevaio.ltfile.ArgHelper;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTable;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.genevaio.ltfile.LogicTableF2;
import org.genevaers.genevaio.ltfile.LogicTableNV;
import org.genevaers.genevaio.ltfile.LogicTableRE;
import org.genevaers.genevaio.vdpfile.VDPManagementRecords;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.UserExit;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.enums.ColumnSourceType;
import org.genevaers.repository.components.enums.LtRecordType;

import com.google.common.flogger.FluentLogger;


public class FlowGenerator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private LogicTable xlt;
	private String currentView;
	private LogicalRecord srcLR;
	
	private Map<Integer, ViewDotFile>view2File = new HashMap<Integer, ViewDotFile>();
	private Map<Integer, LogicalFile> sourceLFs = new HashMap<Integer, LogicalFile>();
	private ViewDotFile currentVdf;
	private Path cwd;
	private short currentViewSource;
	private UserExit currentReadExit = null;
	private String currentJoinNumber;
	private List<String> detailedJoinIDs = null;
	private FlowDotFile fdf;
	private VDPManagementRecords vmrs;

	private ViewNode currentViewNode;
	
	public FlowGenerator(VDPManagementRecords vmrs, LogicTable x) {

		xlt = x;
		this.vmrs = vmrs;
	}

	public void setLogicTable(LogicTable lt) {
		xlt = lt;
	}
	
	public void writeDotFilesFromLT(String ltName) throws IOException {
		logger.atInfo().log("writeDotFilesFromLT to %s", ltName);
		fdf = new FlowDotFile();
		fdf.setName(ltName);
	    Iterator<LTRecord> xi = xlt.getIterator();
		whilestuff(xi);
	}

	private void whilestuff(Iterator<LTRecord> xi) throws IOException {
		while (xi.hasNext()) {
			LTRecord xtr = xi.next();
			if (isRead(xtr)) {
				getInputFileInfo(xtr);
			} else if (isNewView(xtr)) {
				generateNewViewDot(xtr);
			} else if (isEndOfLT(xtr)) {
				writeViewDotFiles();
				writeViewCalcStackFiles();
				writeFlowDotFile();
			} else {
				buildViewFromXLTRecord(xtr);
			}
		}
	}

	private void writeViewCalcStackFiles() {
		Iterator<ViewNode> vi = Repository.getViews().getIterator();
		while(vi.hasNext()){
			ViewNode v = vi.next();
			writeFormatFilterIfNeeded(v);
			writeColumnClackStacksIfNeeded(v);
		}
	}

	private void writeColumnClackStacksIfNeeded(ViewNode v) {
		Iterator<ViewColumn> ci = v.getColumnIterator();
		while(ci.hasNext()) {
			writeColumnCalcStack(ci.next());
		}
	}

	private void writeColumnCalcStack(ViewColumn col) {
		if(col.getColumnCalculation() != null && col.getColumnCalculation().length() > 0) {
			Path vcsPath = cwd.resolve("views").resolve(String.format("VCS%07d_%03d.txt", col.getViewId(), col.getColumnNumber()));
			try (FileWriter fw = new FileWriter(vcsPath.toFile())) {
				fw.write(col.getColumnCalculation() + "\n\n");
				fw.write(col.getColumnCalculationStack().toString());
			} catch (IOException e) {
				logger.atSevere().log("Cannot write calculation stack\n%s", e.getMessage());
			}
		}
	}

	private void writeFormatFilterIfNeeded(ViewNode v) {
		if(v.hasFormatFilterLogic()) {
			Path vfPath = cwd.resolve("views").resolve(String.format("VFF%07d.txt", v.getID()));
			try (FileWriter fw = new FileWriter(vfPath.toFile())) {
				fw.write(v.getFormatFilterLogic() + "\n\n");
				fw.write(v.getFormatFilterCalcStack() != null ? v.getFormatFilterCalcStack().toString() : "");
			} catch (IOException e) {
				logger.atSevere().log("Cannot write format filter\n%s", e.getMessage());
			}
		}
	}

	public void setCurrentWorkingDirectory(Path p) {
		cwd = p;
	}

	public void setDetailJoins(String joinsFilter) {
		if(joinsFilter != null) {
			if(joinsFilter.length() > 0) {
				detailedJoinIDs = Arrays.asList(joinsFilter.split(","));
			} else {
				detailedJoinIDs = new ArrayList<String>(); //Empty list means all
			}
		} else {
			System.out.println("No Joins Filter");
		}
	}


	private void addFieldLink(int src, int trg, boolean SK){
		currentVdf.getLinks().addFieldLink(currentViewSource, src, trg, SK);
	}

	private ViewDotFile  getOrMakeViewDotFile(LTRecord xtr) throws IOException {
		LogicTableNV nvr = (LogicTableNV) xtr;
		ViewNode view = Repository.getViews().get(nvr.getViewId());
		//View sections may be repeated in different LFs
		fdf.addView(view.getName(), nvr.getViewId());
		ViewDotFile vdf = view2File.get(nvr.getViewId());
		if(vdf == null) {
			Path viewsp = cwd.resolve("views");
			if(viewsp.toFile().exists() ==  false) {
				viewsp.toFile().mkdir();
			}			
			vdf =  new ViewDotFile(nvr.getViewId());
			vdf.open(viewsp); 
			view2File.put(nvr.getViewId(), vdf);
			vdf.writeHeader(view.getName());
		}
		return vdf;
	}

	private void getInputFileInfo(LTRecord xtr) throws IOException {
		LogicTableRE read = (LogicTableRE)xtr;
		LogicalFile lf = Repository.getLogicalFiles().get(xtr.getFileId());
		if(lf == null) {
			System.out.println("bang");
		} else {
			fdf.addLogicalFile(lf);
		}
		//What type NX EX TK
		if(xtr.getFunctionCode().endsWith("NX")) {
			currentReadExit = null;
		} else if(xtr.getFunctionCode().endsWith("EX")) {
			currentReadExit = Repository.getUserExits().get(read.getReadExitId());
		} else if(xtr.getFunctionCode().endsWith("TK")) {
			currentReadExit = null;
			
		}
		
	}

	private boolean isRead(LTRecord xtr) {
		return xtr.getFunctionCode().startsWith("RE");
	}

	private void writeFlowDotFile() throws IOException {
		fdf.open(cwd);
		fdf.write();
		fdf.close();
	}

	void writeViewDotFiles() throws IOException {
		logger.atFine().log("writeViewDotFiles");
		for(ViewDotFile v : view2File.values()) {
			v.write();
		}
	}

	public void clearView2fileMap() throws IOException {
		view2File.clear();
	}

	private boolean isEndOfLT(LTRecord xtr) {
		return xtr.getFunctionCode().equals("EN");
	}

	private void generateNewViewDot(LTRecord xtr) throws IOException {
		logger.atInfo().log("Make New View Dot for view %d", xtr.getViewId());
		currentVdf = getOrMakeViewDotFile(xtr);
		addEventSetTo(currentVdf, xtr);
	}

	private void addEventSetTo(ViewDotFile currentVdf2, LTRecord xtr) {
		LogicTableNV nv = (LogicTableNV)xtr;
		currentViewNode = Repository.getViews().get(nv.getViewId());
		currentView = currentViewNode.getName();
		
		LogicalFile srclf = Repository.getLogicalFiles().get(nv.getFileId());
		sourceLFs.put(nv.getFileId(), srclf);
		srcLR = Repository.getLogicalRecords().get(nv.getSourceLrId());
		currentViewSource = nv.getSourceSeqNbr();
		currentVdf.addEventSet(currentViewSource, srclf, srcLR);
		
		fdf.addLF2ViewLink(srclf, currentViewNode);
}

	private boolean isNewView(LTRecord xtr) {
		return xtr.getRecordType() == LtRecordType.NV;
	}

	private void buildViewFromXLTRecord(LTRecord xtr)
			throws IOException {
		// Wonder if these would be better in a ViewDotWriter class
		//TODO Find and treat the GEN record differently
		if (xtr.getSuffixSeqNbr() == 0) {
			addToExtractFilter(xtr);
		}
		if (fieldAssignment(xtr)) {
			buildAssignmentLink(xtr);
		} else if (calculationAssignment(xtr)) {
			buildCalcAssignmentLink(xtr);
		} else if (fieldComparison(xtr)) {
			buildComparisonAssignment(xtr);
		} else if (joinFunction(xtr)) {
			addJoinToCurrentView(xtr);
			// The join does not have the REFR LR
			// Get that from the xxL function codes
			// And build up the clusters of the REFRs
			// We can also accumulate the links from the REFRs to the columns
		} else if (lookupKey(xtr)) {
			addLookupKey(currentView, xtr);
		} else if (xtr.getFunctionCode().startsWith("WR")) {
			fdf.addWriteTarget(xtr, vmrs);
		} else if (xtr.getFunctionCode().equalsIgnoreCase("ES")) {
			if (currentVdf.isOpen()) {
				// write the view source
				// Write the lookup refs... or maybe this is an end thing
				// Same lookup shared by many sources?

				// Maybe we should draw each source diagram as its own - that is
				// what we did for the
				// original version... avoids the many lines heading to the
				// column
				// So an ES will trigger a write...
			}
		}
	}

	private void addLookupKey(String currentView2, LTRecord xtr) {
		if(xtr.getFunctionCode().charAt(2) == 'E') {
			LogicTableF2 lke = (LogicTableF2)xtr;
			addFieldLookupKeyLink(lke.getArg2().getFieldId());				
		}		
	}

	private void addFieldLookupKeyLink(int lrfieldID) {
		if(isJoinDetailed()) {
			currentVdf.getLinks().addFieldLookupKeyLink(currentViewSource, lrfieldID, currentJoinNumber);
		}
	}

	private boolean lookupKey(LTRecord xtr) {
		return xtr.getFunctionCode().startsWith("LK");
	}

	private boolean joinFunction(LTRecord xtr) {
		boolean retval = false; 
		if (xtr.getFunctionCode().startsWith("JO")) {
			retval = true;
		} else if (xtr.getFunctionCode().startsWith("LK")) {
			if(xtr.getFunctionCode().endsWith("LR")) {
				LogicTableF1 f1 = (LogicTableF1)xtr;
				if(f1.getArg().getOrdinalPosition() == 1) {
					retval = true;
				}
			}
		}
		return retval;
	}

	private void buildComparisonAssignment(LTRecord xtr) {
		boolean e1 = (xtr.getFunctionCode().charAt(2) == 'E');
		boolean e2 = (xtr.getFunctionCode().charAt(3) == 'E');
		boolean l1 = (xtr.getFunctionCode().charAt(2) == 'L');
		boolean l2 = (xtr.getFunctionCode().charAt(3) == 'L');
		if(e1) {
			if(xtr.getRecordType() ==  LtRecordType.F1){
				LogicTableF1 f1 = (LogicTableF1)xtr;
				addFieldLink(f1.getArg().getFieldId(), f1.getSuffixSeqNbr(), false);
			} else if(xtr.getRecordType() ==  LtRecordType.F2){
				LogicTableF2 f2 = (LogicTableF2)xtr;
				addFieldLink(f2.getArg1().getFieldId(), f2.getSuffixSeqNbr(), false);
			}
		}
		if(e2) {
			if(xtr.getRecordType() ==  LtRecordType.F1){
				LogicTableF1 f1 = (LogicTableF1)xtr;
				addFieldLink(f1.getArg().getFieldId(), f1.getSuffixSeqNbr(), false);
			} else if(xtr.getRecordType() ==  LtRecordType.F2){
				LogicTableF2 f2 = (LogicTableF2)xtr;
				addFieldLink(f2.getArg1().getFieldId(), f2.getSuffixSeqNbr(), false);
			}
		}
		if(l1) {
			if(xtr.getRecordType() ==  LtRecordType.F1){
				LogicTableF1 f1 = (LogicTableF1)xtr;
				LRField refField = Repository.getFields().get(f1.getArg().getFieldId());
				if(refField == null) {
					logger.atWarning().log("Missing LRField id %d referenced in comparison", f1.getArg().getFieldId());
				} else {
					int  parentLRid = refField.getLrID();
					addParentREFRLR(parentLRid);
					addRefFieldLink(f1.getArg().getFieldId(), f1.getColumnId(), xtr.getFunctionCode().startsWith("SK"));
				}
			} else if(xtr.getRecordType() ==  LtRecordType.F2){
				LogicTableF2 f2 = (LogicTableF2)xtr;
				int  parentLRid = Repository.getFields().get(f2.getArg1().getFieldId()).getComponentId();
				addParentREFRLR(parentLRid);
				addRefFieldLink(f2.getArg1().getFieldId(), f2.getColumnId(), xtr.getFunctionCode().startsWith("SK"));				
			}
		}
		if(l2) {
			if(xtr.getRecordType() ==  LtRecordType.F1){
				LogicTableF1 f1 = (LogicTableF1)xtr;
				LRField refField = Repository.getFields().get(f1.getArg().getFieldId());
				if(refField == null) {
					logger.atWarning().log("Missing LRField id %d referenced in comparison", f1.getArg().getFieldId());
				} else {
					int  parentLRid = refField.getLrID();
					addParentREFRLR(parentLRid);
					addRefFieldLink(f1.getArg().getFieldId(), f1.getColumnId(), xtr.getFunctionCode().startsWith("SK"));
				}
			} else if(xtr.getRecordType() ==  LtRecordType.F2){
				LogicTableF2 f2 = (LogicTableF2)xtr;
				int  parentLRid = Repository.getFields().get(f2.getArg1().getFieldId()).getComponentId();
				addParentREFRLR(parentLRid);
				addRefFieldLink(f2.getArg1().getFieldId(), f2.getColumnId(), xtr.getFunctionCode().startsWith("SK"));				
			}
		}
	}

	private boolean fieldComparison(LTRecord xtr) {
		return xtr.getFunctionCode().startsWith("CF");
	}

	private void buildCalcAssignmentLink(LTRecord xtr) {
		if(xtr.getRecordType() ==  LtRecordType.F1){
			LogicTableF1 f1 = (LogicTableF1)xtr;
			addFieldLink(f1.getArg().getFieldId(), f1.getColumnId(), false);
		}
	}

	private boolean calculationAssignment(LTRecord xtr) {
		return xtr.getFunctionCode().startsWith("CT");
	}

	private void buildAssignmentLink(LTRecord xtr) {
		if(xtr.getFunctionCode().endsWith("E")) {
			//arg1 will be the source lrfield
			//arg2 will be the column details
			//Sort after a walk
			LogicTableF2 dte = (LogicTableF2)xtr;
			addFieldLink(dte.getArg1().getFieldId(), dte.getColumnId(), xtr.getFunctionCode().startsWith("SK"));
		}
		else if(xtr.getFunctionCode().endsWith("L")) {
			LogicTableF2 dtl = (LogicTableF2)xtr;
			LRField refField = Repository.getFields().get(dtl.getArg1().getFieldId());
			if(refField == null) {
				logger.atWarning().log("Missing LRField id %d referenced in assignment", dtl.getArg1().getFieldId());
			} else {
				int  parentLRid = refField.getLrID();
				addParentREFRLR(parentLRid);
				addRefFieldLink(dtl.getArg1().getFieldId(), dtl.getColumnId(), xtr.getFunctionCode().startsWith("SK"));
			}			
		}
		else if(xtr.getFunctionCode().endsWith("C")) {
			LogicTableF1 dtc = (LogicTableF1)xtr;
			ViewColumn vc = currentViewNode.getColumnNumber(dtc.getSuffixSeqNbr());
			if(vc.findFromSourcesByNumber(currentViewSource) == null) {
				ViewColumnSource vcs = new ViewColumnSource();
				vcs.setSrcValue(dtc.getArg().getValue().getString());
				vcs.setValueLength(dtc.getArg().getValue().getString().length());
				vcs.setSourceType(ColumnSourceType.CONSTANT);
				vcs.setSequenceNumber(currentViewSource);
				vc.addToSourcesByNumber(vcs);
			}
			//addConstantLink(dtc.getColumnId(), dtc.getColumnId(), xltr.getFunctionCode()equalsIgnoreCase("SK"));
		}
	}

	private void addRefFieldLink(int lrfieldID, int trg,	boolean SK) {
		currentVdf.getLinks().addRefFieldLink(currentViewSource, currentJoinNumber, lrfieldID, trg, SK, isJoinDetailed());
	}

	private boolean isJoinDetailed() {
		if( detailedJoinIDs != null ) {
			return detailedJoinIDs.size() == 0  //not null and empty means all
				|| detailedJoinIDs.contains(currentJoinNumber) ; 
		} else {
			return false;
		}
	}

	private boolean fieldAssignment(LTRecord xtr) {
		return xtr.getFunctionCode().startsWith("DT") 
			|| xtr.getFunctionCode().startsWith("SK");
	}

	private void addToExtractFilter(LTRecord xtr) {
		// TODO Auto-generated method stub
		
	}

	private void addParentREFRLR(int  parentLRid) {
		LogicalRecord lr = Repository.getLogicalRecords().get(parentLRid);
		if(lr == null) {
			System.out.println("Oh dear");
		}
		//There should be one or something has gone wrong
		currentVdf.setRefLR(currentJoinNumber, lr, currentViewSource);
		JoinDotWriter jdw = currentVdf.getJoinDotWriter(currentJoinNumber, currentViewSource);
		jdw.setRefLR(Repository.getLogicalRecords().get(parentLRid));
}

	private void addJoinToCurrentView( LTRecord xtr) throws IOException {
		LogicTableF1 join = (LogicTableF1)xtr;
		currentJoinNumber = join.getArg().getValue().getString();
		JoinDotWriter jdw = currentVdf.addJoin(join, isJoinDetailed(), currentViewSource);
		LogicalRecord lr = Repository.getLogicalRecords().get(join.getArg().getLrId());
		jdw.setRefLR(lr);
		logger.atInfo().log("Add Join %s to View %d Target LRID %d ",currentJoinNumber ,xtr.getViewId(), join.getArg().getLrId());
	}

}
