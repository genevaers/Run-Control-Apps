package org.genevaers.genevaio.report;

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
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.genevaers.genevaio.fieldnodes.ComparisonState;
import org.genevaers.genevaio.fieldnodes.FieldNodeBase;
import org.genevaers.genevaio.fieldnodes.MetadataNode;
import org.genevaers.genevaio.fieldnodes.NumericFieldNode;
import org.genevaers.genevaio.fieldnodes.RecordNode;
import org.genevaers.genevaio.fieldnodes.StringFieldNode;
import org.genevaers.genevaio.fieldnodes.FieldNodeBase.FieldNodeType;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.UserExit;
import com.google.common.flogger.FluentLogger;

public class VDPTextWriter extends TextRecordWriter {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	
	private Map<Integer, ViewDetails> viewDetailsById = new TreeMap<>();
	private Map<Integer, LookupDetails> lookupDetailsById = new TreeMap<>();
	private Map<Integer, LrDetails> lrDetailsById = new TreeMap<>();
	private Set<Integer> ignoredLrs = new HashSet<>();

	private boolean cppCompare;
	private boolean compareMode;

	public VDPTextWriter() {
		setIgnores();
	}

	@Override
	public void writeDetails( MetadataNode recordsRoot, Writer fw, String generated) throws IOException {
		checkIfOldIsCpp(recordsRoot);
		addCppIgnores();
		setIgnores();
		writeHeader(generated, fw);
		writeSummary(recordsRoot,fw);
		writeViewSummaries(recordsRoot, fw);
		writeLookupSummaries(recordsRoot, fw);
		writeLRSummaries(recordsRoot, fw);
		if(compareMode == false) {
			writeExitSummaries(recordsRoot, fw);
			writeLfSummaries(recordsRoot, fw);
			writePfSummaries(recordsRoot, fw);
		}
		writeContent(recordsRoot,fw);
		writeComparisonSummary(recordsRoot, fw);
	}
	
	private void addCppIgnores() {
		ignoreTheseDiffs.put("Generation_lrCount", true); 
		ignoreTheseDiffs.put("Generation_inputFileCount", true); 
		ignoreTheseDiffs.put("Generation_lrFieldCount", true); 
		ignoreTheseDiffs.put("Generation_lrIndexFieldCount", true); 
		ignoreTheseDiffs.put("Generation_joinStepCount", true); 
		ignoreTheseDiffs.put("Generation_vdpByteCount", true); 
		ignoreTheseDiffs.put("Generation_recordCount", true); 
		ignoreTheseDiffs.put("View_Definition_outputPageSizeMax", true); 
		ignoreTheseDiffs.put("View_Definition_outputLineSizeMax", true); 
		ignoreTheseDiffs.put("Sources_columnId", true); 
		ignoreTheseDiffs.put("Lookup_Paths_valueLength", true); 
		ignoreTheseDiffs.put("Lookup_Paths_joinName", true); 
		ignoreTheseDiffs.put("Physical_Files_ddnameOutput", true); 
		ignoreTheseDiffs.put("Physical_Files_ddnameInput", true); 
		ignoreTheseDiffs.put("Physical_Files_minimumLength", true); 
		ignoreTheseDiffs.put("View_Output_File_textDelimId", true); 
		ignoreTheseDiffs.put("Column_Calculation_inputFileId", true); 
		ignoreTheseDiffs.put("Column_Calculation_sequenceNbr", true); 
		ignoreTheseDiffs.put("Column_Calculation_Stack_recordId", true); 
		ignoreTheseDiffs.put("Format_Filter_Stack_recordId", true); 
		ignoreTheseDiffs.put("Column_Sources_viewSrcLrId", true); 
		ignoreTheseDiffs.put("Sort_Keys_descOrdinalPosition", true); 
	}

	private void checkIfOldIsCpp(MetadataNode recordsRoot) {
		compareMode = recordsRoot.getName().equals("Compare");
		if(compareMode) {
			FieldNodeBase desc = recordsRoot.getChildrenByName("Generation").getChildrenByName("1_0_1").getChildrenByName("description");
			cppCompare = ((StringFieldNode)desc).getDiffValue().contains("GVBMR91");
		}
	}

	private void writeComparisonSummary(MetadataNode recordsRoot, Writer fw) throws IOException {
		if(compareMode) {
			fw.write("\n\nComparison Results\n==================\n\n");
			fw.write(String.format("%-20s: %s\n", "Comparison mode", cppCompare ? "CPP" : "JAVA"));			
			fw.write(String.format("%-20s: %7d\n\n\n", "Number of diffs", numDiffs));
		}
	}

	private void writeHeader(String generated, Writer fw) throws IOException {
		fw.write(String.format("VDP Report: %s\n\n", generated));
	}

	private void writePfSummaries(MetadataNode recordsRoot, Writer fw) throws IOException {
		fw.write("\nPhysical Files\n");
		fw.write("==================\n");
		fw.write(String.format("%7s %-48s %-7s %8s %8s %7s %-48s\n", "ID", "Name", "Type", "InputDD", "OutputDD", "ExitID", "Parm"));
		fw.write(StringUtils.repeat('-', 64)+"\n");
		Iterator<PhysicalFile> pfi = Repository.getPhysicalFiles().getIterator();
		while (pfi.hasNext()) {
			PhysicalFile pf = pfi.next();
			fw.write(String.format("%7s %-48s %-7s %8s %8s %7d %-48s\n",pf.getComponentId(), pf.getName(), pf.getFileType(), pf.getInputDDName(), pf.getOutputDDName(), pf.getReadExitID(), pf.getReadExitIDParm()));
		}
	}

	private void writeLfSummaries(MetadataNode recordsRoot, Writer fw) throws IOException {
		fw.write("\nLogical Files\n");
		fw.write("==================\n");
		addStateColumIfCompareMode(fw);
		fw.write(String.format("%7s %-48s %-7s\n", "ID", "Name", "Num PFs"));
		fw.write(StringUtils.repeat('-', 64)+"\n");
		Iterator<LogicalFile> lfi = Repository.getLogicalFiles().getIterator();
		while (lfi.hasNext()) {
			LogicalFile lf = lfi.next();
			//addStateValueIfCompareMode(fw, lrd.state);
			fw.write(String.format("%7s %-48s %-7d\n",lf.getID(), lf.getName(), lf.getNumberOfPFs()));
		}
	}

	private void writeExitSummaries(MetadataNode recordsRoot, Writer fw) throws IOException {
		fw.write("\nUser Exit Routines\n");
		fw.write("==================\n");
		if(Repository.getUserExits().size() > 0) {
			fw.write(String.format("%7s %-48s %-6s %-9s %8s %10s \n", "ID", "Name", "Type", "Optimized", "Language", "Executable"));
			fw.write(StringUtils.repeat('-', 93)+"\n");
			Iterator<UserExit> ei = Repository.getUserExits().getIterator();
			while (ei.hasNext()) {
				UserExit e = ei.next();
				fw.write(String.format("%7s %-48s %-6s %-9s %8s %10s \n",e.getComponentId(), e.getName(), e.getExitType().toString(), e.isOptimizable(), e.getProgramType(), e.getExecutable()));
			}
		} else {
			fw.write("<none>\n");
		}
	}

	private void writeLRSummaries(MetadataNode recordsRoot, Writer fw) throws IOException {
		Iterator<LrDetails> lrds = lrDetailsById.values().iterator();
		fw.write("\nLogical Record Summaries\n");
		fw.write("========================\n");
		addStateColumIfCompareMode(fw);
		fw.write(String.format("%7s %-48s %-6s %-9s %7s %7s %-48s\n", "ID", "Name", "Length", "NumFields", "KeyLen", "ExitId", "Parms"));
		fw.write(StringUtils.repeat('-', 95)+"\n");
		while (lrds.hasNext()) {
			LrDetails lrd = lrds.next();
			addStateValueIfCompareMode(fw, lrd.state);
			fw.write(String.format("%7d %-48s %6s %9s %7d %7d %-48s\n", lrd.id, lrd.name, lrd.length, lrd.numberOfFields, lrd.keyLen, lrd.lookupExitId, lrd.exitParms));
		}
	}

	private void addStateValueIfCompareMode(Writer fw, ComparisonState state) throws IOException {
		if(compareMode ) {
			if(state == ComparisonState.ORIGINAL && cppCompare) {
				fw.write(String.format("%-7s ", "RCGOnly"));
				//find the lr and propagate the state

			} else {
				fw.write(String.format("%-7s ", getRecordState(state)));
			}
		}
	}

	private void addStateColumIfCompareMode(Writer fw) throws IOException {
		if(cppCompare) {
			fw.write(String.format("%-7s ","State"));
		}
	}

	private String getRecordState(ComparisonState state) {
		switch (state) {
			case CHANGED:
				break;
			case DIFF:
				return "***";
			case IGNORED:
				break;
			case INSTANCE:
				break;
			case NEW:
				return "Old    ";
			case ORIGINAL:
				return "New    ";
			case RECIGNORE:
				return "RCGOnly";
			case CPPONLY:
				return "CPPOnly";
			case MAPPED:
				return "Mapped ";
			default:
				break;
		}
		return "";
	}

	private void writeLookupSummaries(MetadataNode recordsRoot, Writer fw) throws IOException {
		Iterator<LookupDetails> lkds = lookupDetailsById.values().iterator();
		fw.write("\nLookup Path Summaries\n");
		fw.write("=====================\n");
		fw.write(String.format("%7s %-48s %-5s %7s %7s %7s\n", "ID", "Name", "Steps", "SrcLR", "TrgLR", "TrgLF"));
		fw.write(StringUtils.repeat('-', 86)+"\n");
		while (lkds.hasNext()) {
			LookupDetails lkd = lkds.next();
			fw.write(String.format("%7d %-48s %5s %7d %7d %7d\n", lkd.id, lkd.name, lkd.numberOfSteps, lkd.sourceLR, lkd.targetLR, lkd.targetLF));
		}
	}

	private void writeViewSummaries(MetadataNode recordsRoot, Writer fw) throws IOException {
		Iterator<ViewDetails> vds = viewDetailsById.values().iterator();
		fw.write("View Summaries\n");
		fw.write("==============\n");
		addStateColumIfCompareMode(fw);
		fw.write(String.format("%7s %-48s %-8s %-4s %s\n", "ID", "Name", "Type", "Cols", "Sources"));
		fw.write(StringUtils.repeat('-', 78)+"\n");
		while (vds.hasNext()) {
			ViewDetails vd = vds.next();
			addStateValueIfCompareMode(fw, vd.state);
			fw.write(String.format("%7d %-48s %-8s %4d %7d\n", vd.id, vd.name, vd.viewType, vd.numberOfColumns, vd.numberOfSources));
		}
	}

	private void writeSummary(MetadataNode recordsRoot, Writer fw) throws IOException {
        Iterator<FieldNodeBase> fi = recordsRoot.getChildren().iterator();

		if(compareMode) {
			fw.write("Comparison Summary\n==================\n\n");
			fw.write("Source1: " + recordsRoot.getSource1() + "\n");
			fw.write("Source2: " + recordsRoot.getSource2() + "\n");
			fw.write("Differences: Source1 -> Source2 \n\n");
		} else {
			fw.write("Summary\n=======\n\n");
		}
		fw.write(String.format("%-22s: %7s\n", "Component", "Count"));
		fw.write(StringUtils.repeat('=', 31)+"\n");
		int numViews = 0;
        while (fi.hasNext()) {
            FieldNodeBase n = (FieldNodeBase) fi.next();
			if(n.getFieldNodeType() != FieldNodeType.VIEW) {
				fw.write(String.format("%-22s: %7d\n", n.getName(), n.getChildren().size()));
				if(n.getName().equals("Lookup_Paths")) {
					collectLookupDetails(n, fw);
				} else if(n.getName().equals("Logical_Records")) {
					collectLrDetails(n, fw, recordsRoot);
				} else if(n.getName().equals("Logical_Files")) {
					collectLfDetails(n, fw);
				} else if(n.getName().equals("LR_Fields")) {
					//collectLrFieldDetails(n, fw);
				}

			} else {
				numViews++;
				collectViewDetails(n, fw);
			}
		}
		fw.write(String.format("%-22s: %7d\n\n\n", "Views", numViews));
	}

	private void collectLfDetails(FieldNodeBase n, Writer fw) {
        Iterator<FieldNodeBase> fi = n.getChildren().iterator();
		int currentID = 0 ;
		LrDetails lrds = null;
        while (fi.hasNext()) {
            FieldNodeBase lr = (FieldNodeBase) fi.next();
			int id = ((NumericFieldNode)(lr.getChildrenByName("recordId"))).getValue();
			if(id != currentID) {
				lrds = new LrDetails();
				lrds.id = id;
				lrds.state = lr.getState();
				if(cppCompare && lrds.state == ComparisonState.DIFF && lrds.id > 900000) {
					lr.getChildrenByName("lrName").setState(ComparisonState.INSTANCE);
					lrds.state = ComparisonState.INSTANCE;
				}
				lrds.name = ((StringFieldNode)(lr.getChildrenByName("lrName"))).getValue();
				lrDetailsById.put(lrds.id, lrds);
				lrds.numberOfFields = Repository.getLogicalRecords().get(id).getValuesOfFieldsByID().size();
				lrds.keyLen = Repository.getLrKeyLen(id);
				lrds.lookupExitId = ((NumericFieldNode)(lr.getChildrenByName("exitPgmId"))).getValue(); 
				lrds.exitParms = ((StringFieldNode)(lr.getChildrenByName("exitStartup"))).getValue();
			}
		}
	}


	private void collectLrDetails(FieldNodeBase n, Writer fw, MetadataNode recordsRoot) {
        Iterator<FieldNodeBase> fi = n.getChildren().iterator();
		int currentID = 0 ;
		LrDetails lrds = null;
        while (fi.hasNext()) {
            FieldNodeBase lr = (FieldNodeBase) fi.next();
			int id = ((NumericFieldNode)(lr.getChildrenByName("RecordId"))).getValue();
			if(id != currentID) {
				lrds = new LrDetails();
				lrds.id = id;
				lrds.state = lr.getState();
				if(cppCompare && (lrds.state == ComparisonState.DIFF || lrds.state == ComparisonState.ORIGINAL) && lrds.id > 900000) {
					lr.getChildrenByName("Name").setState(ComparisonState.RECIGNORE);
					lrds.state = ComparisonState.RECIGNORE;
					ignoredLrs.add(id);
				}
				if(lrds.state == ComparisonState.ORIGINAL && cppCompare) {
					lr.getChildrenByName("Name").setState(ComparisonState.RECIGNORE);
					lrds.state = ComparisonState.RECIGNORE;
					ignoredLrs.add(id);
				}
				lrds.name = ((StringFieldNode)(lr.getChildrenByName("Name"))).getValue();
				lrDetailsById.put(lrds.id, lrds);
				lrds.numberOfFields = Repository.getLogicalRecords().get(id).getValuesOfFieldsByID().size();
				lrds.length = Repository.getLRLength(id);
				lrds.keyLen = Repository.getLrKeyLen(id);
				lrds.lookupExitId = ((NumericFieldNode)(lr.getChildrenByName("Lookup Exit ID"))).getValue(); 
				lrds.exitParms = ((StringFieldNode)(lr.getChildrenByName("Lookup Exit Parameters"))).getValue();
			}
		}
	}

	private void propagate(FieldNodeBase rec, ComparisonState newState) {
		Iterator<FieldNodeBase> ri = rec.getChildren().iterator();
		while (ri.hasNext()) {
			FieldNodeBase child = ri.next();
			child.setState(newState);
			if(child.getChildren().size() > 0) {
				propagate(child, newState);
			}
		}
	}

	private void collectLookupDetails(FieldNodeBase n, Writer fw) {
        Iterator<FieldNodeBase> fi = n.getChildren().iterator();
		int currentID = 0 ;
		int stepNum = 0;
		LookupDetails lkds = null;
        while (fi.hasNext()) {
            FieldNodeBase lk = (FieldNodeBase) fi.next();
			int id = ((NumericFieldNode)(lk.getChildrenByName("RecordId"))).getValue();
			if(id != currentID) {
				lkds = new LookupDetails();
				lkds.id = id;
				lkds.name = ((StringFieldNode)(lk.getChildrenByName("JoinName"))).getValue();
				lookupDetailsById.put(lkds.id, lkds);
				lkds.sourceLR = ((NumericFieldNode)(lk.getChildrenByName("SourceLrId"))).getValue();
				currentID = id;
			}
			lkds.numberOfSteps = ((NumericFieldNode)(lk.getChildrenByName("Sequence Number"))).getValue();
			lkds.targetLF = ((NumericFieldNode)(lk.getChildrenByName("Input File ID"))).getValue();
			lkds.targetLR = ((NumericFieldNode)(lk.getChildrenByName("TargetLrId"))).getValue();
		}
	}

	private void collectViewDetails(FieldNodeBase v, Writer fw) throws IOException {
        Iterator<FieldNodeBase> fi = v.getChildren().iterator();
		ViewDetails vds = new ViewDetails();
		vds.state = v.getState();
        while (fi.hasNext()) {
            FieldNodeBase n = (FieldNodeBase) fi.next();
			if(n.getName().equals("View_Definition")) {
				FieldNodeBase rec = n.getChildren().get(0);
				vds.name = ((StringFieldNode)(rec.getChildrenByName("Name"))).getValue();
				vds.id = ((NumericFieldNode)(rec.getChildrenByName("RecordId"))).getValue();
				vds.viewType = ((StringFieldNode)(rec.getChildrenByName("ViewType"))).getValue();
			} else if(n.getName().equals("Columns")) {
				vds.numberOfColumns = n.getChildren().size();
			} else if(n.getName().equals("Sources")) {
				vds.numberOfSources = n.getChildren().size();
			}		
		}
		viewDetailsById.put(vds.id, vds);
	}

	private void writeComponentSummary(MetadataNode recordsRoot, Writer fw) throws IOException {
        Iterator<FieldNodeBase> fi = recordsRoot.getChildren().iterator();
		fw.write("Summary\n=======\n\n");
		fw.write(String.format("%-20s: %7s\n", "Component", "Count"));
		fw.write(StringUtils.repeat('=', 29)+"\n");
		int numViews = 0;
        while (fi.hasNext()) {
            FieldNodeBase n = (FieldNodeBase) fi.next();
			if(n.getFieldNodeType() != FieldNodeType.VIEW) {
				fw.write(String.format("%-20s: %7d\n", n.getName(), n.getChildren().size()));
			} else {
				numViews++;
			}
		}
		fw.write(String.format("%-20s: %7d\n\n\n", "Views", numViews));
	}


	private void writeContent(MetadataNode recordsRoot, Writer fw) throws IOException {
		fw.write(String.format("\nRecord Level Reports\n"));
		fw.write(String.format("====================\n"));
        Iterator<FieldNodeBase> fi = recordsRoot.getChildren().iterator();
        while (fi.hasNext()) {
            FieldNodeBase n = (FieldNodeBase) fi.next();
            writeComponents(n, fw);
            //writeFields(child, n, fw);
        }
	}
	private void writeComponents(FieldNodeBase c, Writer fw) throws IOException {
			if(c.getFieldNodeType() == FieldNodeType.VIEW) {
				writeView(c, fw);
			} else {
				writeComponent(c, fw);
		}
	}

	private void writeComponent(FieldNodeBase c, Writer fw) throws IOException {
		int recordType = ((NumericFieldNode)c.getChildren().get(0).getChildrenByName("Record Type")).getValue();
		int recordId = ((NumericFieldNode)c.getChildren().get(0).getChildrenByName("RecordId")).getValue();
		fw.write(String.format("\n~%s (%d)\n",c.getName(), recordType));
		if(recordType == 300 && ignoredLrs.contains(recordId)) {
			propagate(c, ComparisonState.IGNORED);
		}
		writeComponentEntries(c, fw);
	}
	

	private void writeComponentEntries(FieldNodeBase c, Writer fw) throws IOException {

		Iterator<FieldNodeBase> fi = c.getChildren().iterator();
		while (fi.hasNext()) {
			FieldNodeBase n = (FieldNodeBase) fi.next();
			preCheckAndChangeRowState(n);
			writeRecord(n, fw);
		}
	}

	
    private void writeView(FieldNodeBase c, Writer fw) throws IOException {
		fw.write("\n~View "+ c.getName().substring(4) + "\n");		
		Iterator<FieldNodeBase> fi = c.getChildren().iterator();
		while (fi.hasNext()) {
			FieldNodeBase n = (FieldNodeBase) fi.next();
			fw.write(String.format("\n    ~*%s (%s)\n",n.getName().replace('_', ' '), ((NumericFieldNode)n.getChildren().get(0).getChildrenByName("Record Type")).getValue()));
			writeComponentEntries(n, fw);
		}
	}

	private void writeRecord(FieldNodeBase r, Writer fw) throws IOException {
		int recordType = ((NumericFieldNode)r.getChildrenByName("Record Type")).getValue();
		int recordId = ((NumericFieldNode)r.getChildrenByName("RecordId")).getValue();
		if(recordType == 300 && ignoredLrs.contains(recordId)) {
			r.setState(ComparisonState.RECIGNORE);
		}
		if(recordType == 400) {
			int lrid = ((NumericFieldNode)r.getChildrenByName("ID")).getValue();
		 	if(ignoredLrs.contains(lrid)) {
				r.setState(ComparisonState.RECIGNORE);
			}
		}
		if(recordType == 1650 || recordType == 651 || recordType == 800) {
			r.setState(ComparisonState.CPPONLY);
		}
		if(cppCompare && recordType == 3000 && recordId > 900000) {
			r.setState(ComparisonState.RECIGNORE);
		}


		fw.write("    Record:\n");
		Iterator<FieldNodeBase> fi = r.getChildren().iterator();
		while (fi.hasNext()) {
			FieldNodeBase n = (FieldNodeBase) fi.next();
			if(n.getParent().getState() == ComparisonState.RECIGNORE) {
				n.setState(ComparisonState.RECIGNORE);
			}
			if(n.getParent().getState() == ComparisonState.CPPONLY) {
				n.setState(ComparisonState.CPPONLY);
			}
			writeField(n, fw, compareMode);
		}
	}

	protected  void preCheckAndChangeRowState(FieldNodeBase r) {
		boolean updateRowState = true;
		for( FieldNodeBase n : r.getChildren()) {
			if(n.getFieldNodeType() == FieldNodeType.RECORDPART) {
				preCheckAndChangeRowState(n);
			} else {
				if(n.getState() == ComparisonState.DIFF) {
					if(ignoreTheseDiffs.get(getDiffKey(n)) != null) {
						n.setState(ComparisonState.IGNORED);
					} else if(n.getParent().getParent().getName().equals("Physical_Files")) {
						RecordNode rec =  (RecordNode) n.getParent();
						StringFieldNode ft =  (StringFieldNode) rec.getChildrenByName("allocFileType");
						if(!ft.equals("DATAB") && n.getName().startsWith("dbms")) {
							n.setState(ComparisonState.IGNORED);
						}
						if(!ft.equals("PIPE") && n.getName().equals("textDelimId")) {
							n.setState(ComparisonState.IGNORED);
						}
						if(!ft.equals("DISK") && n.getName().equals("allocLrecl")) {
							n.setState(ComparisonState.IGNORED);
						}
					} else if(n.getParent().getParent().getName().equals("Columns")) {
						RecordNode rec =  (RecordNode) n.getParent();
						NumericFieldNode v =  (NumericFieldNode) rec.getChildrenByName("viewId");
						if(v.getValue() > 900000  && n.getName().equals("signedInd")) {
							n.setState(ComparisonState.IGNORED);
						}
					} else {
						updateRowState = false;
					}
				}
			}
		}
		if(updateRowState) {
			r.setState(ComparisonState.INSTANCE);
		}
	}
	@Override
	protected String getDiffKey(FieldNodeBase n) {
		return n.getParent().getParent().getName() + "_" + n.getName();
	}

	@Override
	public void setIgnores() {
		//Hide diffs we don't care about via map
		ignoreTheseDiffs.put("Generation_runDate", true); 
		ignoreTheseDiffs.put("Generation_date", true); 
		ignoreTheseDiffs.put("Generation_description", true); 
		ignoreTheseDiffs.put("Generation_time", true); 
		ignoreTheseDiffs.put("Control_Records_description", true); 
		ignoreTheseDiffs.put("Physical_Files_columnId", true); 
		ignoreTheseDiffs.put("Physical_Files_name", true); 
		ignoreTheseDiffs.put("Physical_Files_lfName", true); 
		ignoreTheseDiffs.put("Logical_Records_lrName", true); 
		ignoreTheseDiffs.put("LR_Fields_recordId", true); 
		ignoreTheseDiffs.put("LR_Fields_ordinalPosition", true); 
		ignoreTheseDiffs.put("LR_Fields_mask", true); 
		ignoreTheseDiffs.put("Lookup_Paths_columnId", true); 
		ignoreTheseDiffs.put("LR_Indexes_columnId", true); 
		ignoreTheseDiffs.put("LR_Indexes_lrIndexName", true); 
		ignoreTheseDiffs.put("View_Definition_viewName", true); 
		ignoreTheseDiffs.put("View_Definition_ownerUser", true); 
		ignoreTheseDiffs.put("View_Definition_defaultOutputFileId", true); 
		ignoreTheseDiffs.put("View_Definition_columnId", true); 
		ignoreTheseDiffs.put("View_Output_File_name", true); 
		ignoreTheseDiffs.put("View_Output_File_recordDelimId", true); 
		ignoreTheseDiffs.put("View_Output_File_allocRecfm", true); 
		ignoreTheseDiffs.put("View_Output_File_allocLrecl", true); 
		ignoreTheseDiffs.put("View_Output_File_lfName", true); 
		ignoreTheseDiffs.put("View_Output_File_ddnameOutput", true); 
		ignoreTheseDiffs.put("Columns_columnName", true); 
		ignoreTheseDiffs.put("Columns_fieldName", true); 
		ignoreTheseDiffs.put("Column_Sources_sequenceNbr", true); 
	}
}
