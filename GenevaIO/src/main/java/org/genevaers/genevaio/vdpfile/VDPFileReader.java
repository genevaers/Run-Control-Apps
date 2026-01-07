package org.genevaers.genevaio.vdpfile;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.StackSize;

import org.genevaers.genevaio.fieldnodes.MetadataNode;
import org.genevaers.genevaio.fieldnodes.RecordNode;
import org.genevaers.genevaio.fieldnodes.RootTypeFactory;
import org.genevaers.genevaio.fieldnodes.ViewFieldNode;
import org.genevaers.genevaio.recordreader.FileRecord;
import org.genevaers.genevaio.recordreader.RecordFileReader;
import org.genevaers.genevaio.recordreader.RecordFileReaderWriter;
import org.genevaers.genevaio.vdpfile.record.VDPRecord;
import org.genevaers.repository.Repository;
import org.genevaers.repository.calculationstack.CalcStack;
import org.genevaers.repository.components.ControlRecord;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.ReportFooter;
import org.genevaers.repository.components.ReportHeader;
import org.genevaers.repository.components.UserExit;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewDefinition;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.utilities.GersConfigration;

public class VDPFileReader{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private boolean writeCSV;
	private MetadataNode recordsRoot;
	private boolean compare;
	
	private File vdpFile;
	private RecordFileReader rr;
	private int numrecords;

	private ViewNode currentView;

	private VDPFileRecordReader recordReader = new VDPFileRecordReader();

	private Path filePath;

	private Set<String> csvHeaderWritten = new HashSet<String>();

	private VDPManagementRecords vdpManagementRecords = new VDPManagementRecords();

	private ViewManagementData currentVMD;
	private int currentViewID;

	private short currentType;

	private RecordNode currentTypeRoot;

	private ViewFieldNode currentViewNode;
	
	public VDPFileReader() {}

	public void addToRepsitory() {
		try {
			readVDP();
		} catch (Exception e) {
			logger.atSevere().log("VDP File read failed:\n%s", e.getMessage());
			logger.atSevere().withCause(e).withStackTrace(StackSize.FULL);
		}
	}

	public void setRecordsRoot(MetadataNode recordsRoot2) {
		this.recordsRoot = recordsRoot2;
	}

	public void setCompare(boolean comp) {
		this.compare = comp;
	}

	public void open(Path vdpPath, String name) {
			vdpFile = vdpPath.toFile();
	}
	
	private void readVDP() throws Exception {
		rr = RecordFileReaderWriter.getReader();
		rr.readRecordsFrom(vdpFile);
		FileRecord rec = rr.readRecord();
		while (rr.isAtFileEnd() == false) {
			numrecords++;
			addVDPRecordToRepo(rec);
			rec.bytes.clear();
			rec = rr.readRecord();
		}
		//addVDPRecordToRepo(rec);
		logger.atInfo().log("Read %d VDP records", numrecords);
		rr.close();
	}

	private void addVDPRecordToRepo(FileRecord rec) throws Exception{
		VDPFileObject vdpObject = null;
		rec.dump();
		int viewID = rec.bytes.getInt(2);
		short recType = rec.bytes.getShort(14);
		logger.atFine().log("RecType %d", recType);
		rec.bytes.rewind();
		switch(recType) {
		case VDPRecord.VDP_GENERATION:
			vdpObject = makeAndStoreGenerationRecord(recordReader, rec);
			break;
		case VDPRecord.VDP_FORMAT_VIEWS:
			vdpObject = makeAndStoreFormatViewsRecord(recordReader, rec);
			break;
		case VDPRecord.VDP_CONTROL:
			vdpObject = makeAndStoreControlRecord(recordReader, rec);
			break;
		case VDPRecord.VDP_PHYSICAL_FILE:
			vdpObject = makeAndStorePhysicalFileRecord(recordReader, rec);
			break;
		case VDPRecord.VDP_EXIT:
			vdpObject = makeAndStoreUserExitRoutine(recordReader, rec);
			break;
		case VDPRecord.VDP_LOGICAL_RECORD:
			vdpObject = makeAndStoreLogicalRecord(recordReader, rec);
			break;
		case VDPRecord.VDP_LRFIELD:
			vdpObject = makeAndStoreLRFieldRecord(recordReader, rec);
			break;
		case VDPRecord.VDP_INDEX:
			vdpObject = makeAndStoreIndexRecord(recordReader, rec);
			break;
		case VDPRecord.VDP_LOOKUP:
		case VDPRecord.VDP_LOOKUP_OLD:
			vdpObject = makeAndStoreLookupPathRecord(recordReader, rec);
			break;
		case VDPRecord.VDP_LOOKUP_TARGET_SET:
			vdpObject = makeAndStoreLookupTargetSet(recordReader, rec);
			break;
		case VDPRecord.VDP_LOOKUP_GENMAP:
			vdpObject = makeAndStoreLookupGenerationMap(recordReader, rec);
			break;
		case VDPRecord.VDP_EXTRACT_OUTPUT_FILE:
			vdpObject = makeAndStoreExtractOutputFile(recordReader, rec);
			break;
		case VDPRecord.VDP_EXTRACT_RECORD_FILE:
			vdpObject = makeAndStoreExtractRecordFile(recordReader, rec);
			break;
		case VDPRecord.VDP_VIEW:
			vdpObject = makeAndStoreView(recordReader, rec);
			break;
		case VDPRecord.VDP_FORMAT_OUTPUT_FILE:
			vdpObject = makeAndStoreFormatOutputFile(recordReader, rec);
			break;
		case VDPRecord.VDP_EXTRACT_TARGET_SET:
			vdpObject = makeAndStoreExtractTargetSet(recordReader, rec);
			break;
		case VDPRecord.VDP_COLUMN:
			vdpObject = makeAndStoreViewColumn(recordReader, rec);
			break;
		case VDPRecord.VDP_SORT_KEY:
			vdpObject = makeAndStoreViewSortKey(recordReader, rec);
			break;
		case VDPRecord.VDP_VIEW_SOURCE:
			vdpObject = makeAndStoreViewSource(recordReader, rec);
			break;
		case VDPRecord.VDP_EXTRACT_FILTER:
			vdpObject = makeAndStoreExtractFilter(recordReader, rec);
			break;
		case VDPRecord.VDP_COLUMN_SOURCE:
			vdpObject = makeAndStoreViewColumnSource(recordReader, rec);
			break;
		case VDPRecord.VDP_COLUMN_LOGIC:
			vdpObject = makeAndStoreViewColumnLogicText(recordReader, rec);
			break;
		case VDPRecord.VDP_OUTPUT_LOGIC:
			vdpObject = makeAndStoreViewOutputLogic(recordReader, rec);
			break;
		case VDPRecord.VDP_FORMAT_FILTER_LOGIC:
			vdpObject = makeAndStoreFormatFilterLogic(recordReader, rec);
			break;
		case VDPRecord.VDP_COLUMN_CALCULATION:
			vdpObject = makeAndStoreColumnCalculation(recordReader, rec);
			break;
		case VDPRecord.VDP_FORMAT_FILTER_STACK:
			vdpObject = makeAndStoreFormatStack(recordReader, rec);
			break;
		case VDPRecord.VDP_COLUMN_CALCULATION_LT:
			vdpObject = makeAndStoreColumnCalculationStack(recordReader, rec);			
			break;
		case VDPRecord.VDP_HEADER:
			vdpObject = makeAndStoreReportHeader(recordReader, rec);			
			break;
		case VDPRecord.VDP_FOOTER:
			vdpObject = makeAndStoreReportFooter(recordReader, rec);			
			break;

		case VDPRecord.VDP_FORMAT_FILTER_OLD:
		case VDPRecord.VDP_COLUMN_CALCULATION_OLD:
		case VDPRecord.VDP_EXTRACT_FILTER_OLD:
		case VDPRecord.VDP_COLUMN_LOGIC_OLD:
		case VDPRecord.VDP_COLUMN_LOGIC_LT:
		default:
			logger.atWarning().log("Rec Num " + numrecords + " Ignoring type" + recType);
		}
		buildTreeIfNeeded(recType,vdpObject, viewID);
	}

	private void buildTreeIfNeeded(short recType, VDPFileObject vdpObject, int viewID) {
		if(recordsRoot != null) {
			if(currentType != recType) {
				currentType = recType;
				if(viewID > 0) {
					currentViewNode = checkAndGetViewNode(viewID);
					currentTypeRoot = RootTypeFactory.getRecordNodeForType(recType);
					currentTypeRoot = (RecordNode) currentViewNode.add(currentTypeRoot, compare);
				} else {
					currentTypeRoot = RootTypeFactory.getRecordNodeForType(recType);
					currentTypeRoot = (RecordNode) recordsRoot.add(currentTypeRoot, compare);
				}
			}
			vdpObject.addRecordNodes(currentTypeRoot, compare);
		}
	}

	private ViewFieldNode checkAndGetViewNode(int viewID) {
		if(currentViewID != viewID) {
			currentViewID = viewID;
			currentViewNode = new ViewFieldNode();
			currentViewNode.setName("View" + viewID);
			currentViewNode.setTypeNumber(1000);
			currentViewNode = (ViewFieldNode) recordsRoot.add(currentViewNode, compare);
		}
		return currentViewNode;
	}

	private VDPFileObject makeAndStoreReportHeader(VDPFileRecordReader recordReader, FileRecord rec) throws Exception {
		VDPHeader vh =  new VDPHeader();
		vh.readRecord(recordReader, rec);
		ReportHeader rh = new ReportHeader();
		if(vh.getJustification() == null) {
			vh.setJustification(JustifyId.NONE);
		}
		vh.populateComponent(rh);
		currentView.addReportHeader(rh);
		return vh;
	}

	private VDPFileObject makeAndStoreReportFooter(VDPFileRecordReader recordReader, FileRecord rec) throws Exception {
		VDPFooter vf =  new VDPFooter();
		vf.readRecord(recordReader, rec);
		ReportFooter rf = new ReportFooter();
		if(vf.getJustification() == null) {
			vf.setJustification(JustifyId.NONE);
		}
		vf.populateComponent(rf);
		currentView.addReportFooter(rf);
		return vf;
	}

	private VDPFileObject makeAndStoreColumnCalculationStack(VDPFileRecordReader recordReader2, FileRecord rec) throws Exception {
		VDPColumnCalculationStack ccs = new VDPColumnCalculationStack();
		ccs.readRecord(recordReader, rec);
		ByteBuffer bb = ByteBuffer.allocate(ccs.getStack().size());
		for(int i=0; i<ccs.getStack().size(); i++ ) {
			bb.put(ccs.getStack().get(i));
		}
		ViewColumn col = currentView.getColumnByID(ccs.getColumnId());
		CalcStack cs = new CalcStack(bb, 0, 0);
		cs.buildEntriesArrayFromTheBuffer();
		col.setColumnCalculationStack(cs);
		return ccs;
	}

	private VDPFileObject makeAndStoreFormatStack(VDPFileRecordReader recordReader, FileRecord rec) throws Exception {
		VDPFormatFilterStack ffs = new VDPFormatFilterStack();
		ffs.readRecord(recordReader, rec);
		ByteBuffer bb = ByteBuffer.allocate(ffs.getStack().size());
		for(int i=0; i<ffs.getStack().size(); i++ ) {
			bb.put(ffs.getStack().get(i));
		}
		CalcStack cs = new CalcStack(bb, 0, 0);
		cs.buildEntriesArrayFromTheBuffer();
		currentView.setCalcStack(cs);
		return ffs;
	}

	private VDPColumnCalculationLogic makeAndStoreColumnCalculation(VDPFileRecordReader recordReader, FileRecord rec) throws Exception {
		VDPColumnCalculationLogic ccr = new VDPColumnCalculationLogic();
		ccr.readRecord(recordReader, rec);
		//Should we hold onto the current view as a convenience/optimisation?
		//Should that be done in the repository?
		//Should we find the view each time (do that within the repository?)
		ViewColumn col = currentView.getColumnByID(ccr.getColumnId());
		col.setColumnCalculation(ccr.getLogic());
		return ccr;
	}

	private VDPFormatFilterLogic makeAndStoreFormatFilterLogic(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		VDPFormatFilterLogic ffr = new VDPFormatFilterLogic();
		ffr.readRecord(recordReader, rec);
		currentView.setFormatFilterLogic(ffr.getLogic());
		return ffr;
	}

	private VDPExtractOutputLogic makeAndStoreViewOutputLogic(VDPFileRecordReader recordReader, FileRecord rec
			) throws Exception {
		VDPExtractOutputLogic eol = new VDPExtractOutputLogic();
		 eol.readRecord(recordReader, rec);
		ViewSource vs = currentView.getViewSourceById(eol.getRecordId());
		vs.setExtractOutputLogic(eol.getLogic());
		return eol;
	}

	private VDPColumnLogic makeAndStoreViewColumnLogicText(VDPFileRecordReader recordReader, FileRecord rec
			) throws Exception {
		VDPColumnLogic vcl = new VDPColumnLogic();
		vcl.readRecord(recordReader, rec);
		ViewColumn vc = currentView.getColumnNumber(vcl.getColumnId());
		ViewColumnSource vcs = vc.findFromSourcesByID(vcl.getRecordId());
		vcs.setLogicText(vcl.getLogic());
		return vcl;
	}

	private VDPViewColumnSource makeAndStoreViewColumnSource(VDPFileRecordReader recordReader, FileRecord rec
			) throws Exception {
		VDPViewColumnSource vvcs = new VDPViewColumnSource();
		vvcs.readRecord(recordReader, rec);
		ViewColumnSource vcs = new ViewColumnSource();
		vvcs.populateComponent(vcs);
		currentView.addViewColumnSource(vcs);
		return vvcs;
	}

	private VDPExtractFilter makeAndStoreExtractFilter(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		VDPExtractFilter ef = new VDPExtractFilter();
		ef.readRecord(recordReader, rec);
		ViewSource vs = currentView.getViewSourceById(ef.getRecordId());
		vs.setExtractFilter(ef.getLogic());
		return ef;
	}

	private VDPViewSource makeAndStoreViewSource(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		VDPViewSource vsr = new VDPViewSource();
		vsr.readRecord(recordReader, rec);
		ViewSource vs = new ViewSource();
		vsr.populateComponent(vs);
		currentView.addViewSource(vs);
		return vsr;
	}

	private VDPViewSortKey makeAndStoreViewSortKey(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		VDPViewSortKey vvsk = new VDPViewSortKey();
		vvsk.readRecord(recordReader, rec);
		ViewSortKey vsk = new ViewSortKey();
		vvsk.populateComponent(vsk);
		currentView.addViewSortKey(vsk);
		return vvsk;
	}

	private VDPViewColumn makeAndStoreViewColumn(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		VDPViewColumn vvc = new VDPViewColumn();
		vvc.readRecord(recordReader, rec);
		ViewColumn vc = new ViewColumn();
		vvc.populateComponent(vc);
		currentView.addViewColumn(vc);
		return vvc;
	}

	private VDPExtractTargetSet makeAndStoreExtractTargetSet(VDPFileRecordReader recordReader, FileRecord rec
			) throws Exception {
		VDPExtractTargetSet ets = new VDPExtractTargetSet();
		ets.readRecord(recordReader, rec);
		currentVMD.setExtractTargetSet(ets);
		return ets;
	}

	private VDPFormatFile makeAndStoreFormatOutputFile(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		VDPFormatFile vffr = new VDPFormatFile();
		vffr.readRecord(recordReader, rec);
		currentVMD.setFormatFile(vffr);
		return vffr;
	}

	private VDPViewDefinition makeAndStoreView(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		VDPViewDefinition vvd = new VDPViewDefinition();
		vvd.readRecord(recordReader, rec);
		ViewDefinition vd = new ViewDefinition();
		vvd.populateComponent(vd);
		currentView = Repository.getViewNodeMakeIfDoesNotExist(vd);
		currentVMD = vdpManagementRecords.getViewManagmentData(currentView.getID());
		return vvd;
	}

	private VDPExtractRecordFile makeAndStoreExtractRecordFile(VDPFileRecordReader recordReader, FileRecord rec
			) throws Exception {
		VDPExtractRecordFile ver = new VDPExtractRecordFile();
		ver.readRecord(recordReader, rec);
		vdpManagementRecords.setExtractRecordFile(ver);
		return ver;
	}

	private VDPExtractFile makeAndStoreExtractOutputFile(VDPFileRecordReader recordReader, FileRecord rec
			) throws Exception {
		VDPExtractFile vef = new VDPExtractFile();
		vef.readRecord(recordReader, rec);
		vdpManagementRecords.setExtractOutpuFile(vef);
		return vef;
	}

	private VDPLookupGenMap makeAndStoreLookupGenerationMap(VDPFileRecordReader recordReader, FileRecord rec
			) throws Exception {
		VDPLookupGenMap lgm = new VDPLookupGenMap();
		lgm.readRecord(recordReader, rec);
		vdpManagementRecords.setLookupPathGenerationMap(lgm);
		return lgm;
	}

	private VDPLookupPathTargetSet makeAndStoreLookupTargetSet(VDPFileRecordReader recordReader, FileRecord rec
			) throws Exception {
		VDPLookupPathTargetSet vlpts = new VDPLookupPathTargetSet();
		vlpts.readRecord(recordReader, rec);	
		vdpManagementRecords.setLookupPathTargetSet(vlpts);
		return vlpts;
	}

	private VDPLookupPathKey makeAndStoreLookupPathRecord(VDPFileRecordReader recordReader, FileRecord rec
			) throws Exception {
		VDPLookupPathKey vlpk = new VDPLookupPathKey();
		vlpk.readRecord(recordReader, rec);
		LookupPathKey lpk = new LookupPathKey();
		vlpk.populateComponent(lpk);
		Repository.addLookupPathKey(lpk);
		return vlpk;
	}

	private VDPLRIndex makeAndStoreIndexRecord(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		VDPLRIndex vndx = new VDPLRIndex();
		vndx.readRecord(recordReader, rec);
		LRIndex lri = new LRIndex();
		vndx.populateComponent(lri);
		Repository.addLRIndex(lri);
		return vndx;
	}

	private VDPLRField makeAndStoreLRFieldRecord(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		VDPLRField vlrf = new VDPLRField();
		vlrf.readRecord(recordReader, rec);
		LRField lrf = new LRField();
		vlrf.populateComponent(lrf);
		Repository.addLRField(lrf);
		return vlrf;
	}

	private VDPLogicalRecord makeAndStoreLogicalRecord(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		VDPLogicalRecord vlr = new VDPLogicalRecord();
		vlr.readRecord(recordReader, rec);
		LogicalRecord lr = new LogicalRecord();
		vlr.populateComponent(lr);
		Repository.addLogicalRecord(lr);
		return vlr;
	}

	private VDPExit makeAndStoreUserExitRoutine(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		VDPExit ve = new VDPExit();
		ve.readRecord(recordReader, rec);
		UserExit ue = new UserExit();
		ve.populateComponent(ue);
		Repository.getUserExits().add(ue, ue.getComponentId(), ue.getName());
		return ve;
	}

	private VDPPhysicalFile makeAndStorePhysicalFileRecord(VDPFileRecordReader recordReader, FileRecord rec
			) throws Exception {
		VDPPhysicalFile vpf = new VDPPhysicalFile();
		vpf.readRecord(recordReader, rec);
		PhysicalFile pf = new PhysicalFile();
		vpf.populateComponent(pf);
		Repository.addPhysicalFile(pf);
		return vpf;
	}

	private VDPControlRecord makeAndStoreControlRecord(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		VDPControlRecord vcr = new VDPControlRecord();
		vcr.readRecord(recordReader, rec);
		ControlRecord cr = new ControlRecord();
		vcr.populateComponent(cr);
		Repository.getControlRecords().add(cr, cr.getComponentId(), cr.getName());
		return vcr;
	}

	private VDPFormatViews makeAndStoreFormatViewsRecord(VDPFileRecordReader recordReader, FileRecord rec
			) throws Exception {
		VDPFormatViews vfviews = new VDPFormatViews();
		vfviews.readRecord(recordReader, rec);
		vdpManagementRecords.setFormatViews(vfviews);
		return vfviews;
	}

	private VDPGenerationRecord makeAndStoreGenerationRecord(VDPFileRecordReader recordReader, FileRecord rec )
			throws Exception {
		//Look ahead to get the text format
		if(rec.bytes.get(39) == 1) {
			VDPFileRecordReader.setASCIIText();
		} else {
			VDPFileRecordReader.setEBCDICText();
		}
		logger.atInfo().log("Text is %s", VDPFileRecordReader.isASCIItext() ? "ASCII" : "EBCDIC");
		VDPGenerationRecord vgen = new VDPGenerationRecord();
		vgen.readRecord(recordReader, rec);
		vdpManagementRecords.setViewGeneration(vgen);
		logger.atInfo().log("gen from %s", vgen.getDescription());
		return vgen;
	}

	public int numRecordsRead() {
		return numrecords;
	}
	
	public void close() throws IOException {
		rr.close();		
	}

	public boolean isWriteCSV() {
		return writeCSV;
	}

	public void setWriteCSV(boolean writeCSV) {
		this.writeCSV = writeCSV;
	}

	public VDPManagementRecords getViewManagementRecords() {
		return vdpManagementRecords;
	}

}
