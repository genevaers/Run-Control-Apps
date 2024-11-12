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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.genevaers.genevaio.recordreader.RecordFileReaderWriter;
import org.genevaers.genevaio.recordreader.RecordFileWriter;
import org.genevaers.genevaio.vdpfile.record.VDPRecord;
import org.genevaers.repository.Repository;
import org.genevaers.repository.calculationstack.CalcStack;
import org.genevaers.repository.components.ControlRecord;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.genevaers.repository.components.OutputFile;
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
import org.genevaers.repository.components.enums.AccessMethod;
import org.genevaers.repository.components.enums.FieldDelimiter;
import org.genevaers.repository.components.enums.FileRecfm;
import org.genevaers.repository.components.enums.FileType;
import org.genevaers.repository.components.enums.RecordDelimiter;
import org.genevaers.repository.components.enums.TextDelimiter;
import org.genevaers.repository.components.enums.ViewType;
import org.genevaers.repository.jltviews.JoinViewsManager.JoinTargetEntry;

import com.google.common.flogger.FluentLogger;

public class VDPFileWriter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private File vdpFile;
	private RecordFileWriter VDPWriter;
	private int numrecords;
	private VDPManagementRecords vdpMgmtRecs;

	public VDPFileWriter() {
		// empty
	}

	// We should make an interface for a VDP Writer...
	// And then we have a bunch...
	// Dot file so we can draw the tree
	// We probably should have had the HMTL via this?
	public int writeVDPFrom(VDPManagementRecords vmrs) throws Exception {

		vdpMgmtRecs = vmrs;
		int numBytesWritten = 0;
		writeTheRecords();
		return numBytesWritten;
	}

	public void open(String name) {
		vdpFile = new File(name);
	}

	private void writeTheRecords() throws Exception {
		VDPWriter = RecordFileReaderWriter.getWriter();
		VDPWriter.writeRecordsTo(vdpFile);

		// We really need to walk the list of items to get the generation information
		// first
		// Another set of visitors?
		writeGenerationRecord();
		writeFormatViewsRecord();
		writeControlRecords();
		writePhysicalFileRecords();
		writeExitRecords();
		writeLogicalRecords();
		writeLRFields();
		writeLRIndexes();
		writeLookupPaths();
		writeLookupTargetSet();
		writeLookupGenMap();
		//I don't think this is ever used by MR95
		writeExtractOutputFile();
		writeExtractRecordFile();
		writeViews();
	}

	private void writeViews() {
		Iterator<ViewNode> vi = Repository.getViews().getIterator();
		while (vi.hasNext()) {
			writeView(vi.next());
		}
	}

	private void writeView(ViewNode view) {
	    logger.atInfo().log("------------------");
	    logger.atInfo().log("Write View %d", view.getID());
		writeViewDefinition(view.getViewDefinition());
		
		ViewManagementData vmd = vdpMgmtRecs.getViewManagmentData(view.getViewDefinition().getComponentId());
		// if(vmd.getFormatFile() == null) {
		// 	makeViewFormatRecord(view);  //This is a silly name for the view output record (basically its DDname)
		// }
		writeHeaders(view);
		writeFooters(view);
		writeTheOutputFile(view);
		writeFormatFilterLogic(view);
		writeFormatFilterStack(view);
		writeExtractTargetSet(vmd.getExtractTargetSet());
		writeViewColumns(view);
		writeViewSortKeys(view);
		writeViewSources(view);
		writeViewColumnSources(view);
	}

	private void writeHeaders(ViewNode view) {
		Iterator<ReportHeader> rhi = view.getHeadersIterator();
		short s = 1;
		while(rhi.hasNext()) {
			VDPHeader vh = new VDPHeader();
			if(vh.getTitleLength() == 0) {
				vh.setTitleText("");
			}
			vh.fillFromComponent(rhi.next());
			vh.setViewId(view.getID());
		    logger.atFine().log("Write View %d Format Header %s", view.getID(), s);
			vh.setSequenceNbr(s++);
			vh.fillTheWriteBuffer(VDPWriter);
			VDPWriter.writeAndClearTheRecord();
		}
	}

	private void writeFooters(ViewNode view) {
		Iterator<ReportFooter> rhi = view.getFootersIterator();
		short s = 1;
		while(rhi.hasNext()) {
			VDPFooter vh = new VDPFooter();
			vh.fillFromComponent(rhi.next());
			vh.setViewId(view.getID());
		    logger.atFine().log("Write View %d Format Header %s", view.getID(), s);
			vh.setSequenceNbr(s++);
			vh.fillTheWriteBuffer(VDPWriter);
			VDPWriter.writeAndClearTheRecord();
		}
	}

	private void makeViewFormatRecord(ViewNode view) {

			OutputFile vof = view.getOutputFile();
			vof.setComponentId(view.getID());
			String ddname = String.format("F%07d", view.getID());
			vof.setOutputDDName(ddname);
			vof.setName("Auto-generated Name for Extract Phase Output ");
			vof.setFileType(FileType.DISK);
	}

	private void writeTheOutputFile(ViewNode view) {
		//Output files are not required for all views
		VDPFormatFile ff = vdpMgmtRecs.getViewManagmentData(view.getID()).getFormatFile();
		if(ff == null) {
			ff = new VDPFormatFile();
			if(view.getOutputFile().getOutputDDName().isEmpty()) {
				makeViewFormatRecord(view);
			}
		    logger.atFine().log("Write View %d Output File", view.getID());
			ff.fillFromComponent(view.getOutputFile());
			ff.setViewId(view.getID());

			ff.setRecordType(VDPRecord.VDP_FORMAT_OUTPUT_FILE);
			ff.setViewId(view.getID());
			ff.setSequenceNbr((short) 1);
			ff.setRecordId(0);
			ff.setInputFileId(0);

			//Keep the writer happy
			ff.setServerId(1);
			ff.setDbmsRowFmtOptId(1);
			ff.setFieldDelimId(FieldDelimiter.INVALID);
			ff.setRecordDelimId(RecordDelimiter.INVALID);
			ff.setTextDelimId(TextDelimiter.INVALID);
			ff.setAccessMethodId(AccessMethod.SEQUENTIAL);
			ff.setAllocLrecl((short) 400);
			ff.setAllocRecfm(FileRecfm.FB);
			ff.setExpirationDate("00000000");
			ff.setCodesetId(2);
			ff.setEndianId(1);
			ff.setFieldDelimId(FieldDelimiter.FIXEDWIDTH);
			ff.setRecordDelimId(RecordDelimiter.CR);
			ff.setAllocDsorg(1);
			ff.setAllocVsamorg(1);
			ff.setAllocRecfm(FileRecfm.VB);
			ff.setAllocLrecl((short) 27994);
			ff.setAllocReleaseInd(true);
			ff.setControlRectypeId(1);
			ff.setVersNbrFldFmtId(1);
			ff.setRecCountFldFmtId(1);
			ff.setProcessInParallel(true);
		}

		ff.fillTheWriteBuffer(VDPWriter);
		VDPWriter.writeAndClearTheRecord();
	}

	private void writeFormatFilterLogic(ViewNode view) {
		String ffl = view.getFormatFilterLogic();
		if (ffl != null && ffl.length() > 0) {
			VDPFormatFilterLogic vffl = new VDPFormatFilterLogic();
            logger.atFine().log("Write View %d Format Filter\n%s", view.getID(), ffl);
			vffl.setRecordType(VDPRecord.VDP_FORMAT_FILTER_LOGIC);
			vffl.setViewId(view.getID());
			vffl.setSequenceNbr((short) 1);
			vffl.setRecordId(view.getID());
			vffl.setInputFileId(0);
			vffl.setLogicLength((short) ffl.length());
			vffl.setRecLen((short) (26 + ffl.length()));
			vffl.setLogic(ffl);
			vffl.fillTheWriteBuffer(VDPWriter);
			VDPWriter.setLengthFromPosition(); //Need this for the variable length records
			VDPWriter.writeAndClearTheRecord();
		}
	}

	private void writeFormatFilterStack(ViewNode view) {
		CalcStack ffcs = view.getFormatFilterCalcStack();
		if (ffcs != null) {
			VDPFormatFilterStack vffs = new VDPFormatFilterStack();
            logger.atFine().log("Write View %d Format Filter CalcStack\n", view.getID());

			vffs.setRecordType(VDPRecord.VDP_FORMAT_FILTER_STACK);
			vffs.setViewId(view.getID());
			vffs.setSequenceNbr((short) 1);
			vffs.setRecordId(view.getID());
			vffs.setInputFileId(0);

			for (int i = 0; i<16; i += 1) {
				vffs.getStackHeaderPrefix().add((byte)0);
			}
			vffs.setFunctionCode("CALC");
			for (int i = 0; i<40; i += 1) {
				vffs.getStackHeaderSuffix().add((byte)0);
			}

			ffcs.fill(vffs.getStack());
			vffs.setRecLen((short) (60 + ffcs.getStackLength()));
			vffs.setStackLength(ffcs.getStackLength());

			vffs.fillTheWriteBuffer(VDPWriter);
			VDPWriter.setLengthFromPosition(); //Need this for the variable length records
			VDPWriter.writeAndClearTheRecord();
		}
	}

	private void writeViewSources(ViewNode view) {
		Iterator<ViewSource> vsi = view.getViewSourceIterator();
		while (vsi.hasNext()) {
			writeViewSource(vsi.next());
		}
	}

	private void writeViewSource(ViewSource vs) {
		VDPViewSource vvs = new VDPViewSource();
        logger.atFine().log("Write View Source %d", vs.getSequenceNumber());
		vvs.fillFromComponent(vs);
		vvs.fillTheWriteBuffer(VDPWriter);
		VDPWriter.writeAndClearTheRecord();
		writeExtractFilter(vs);
		writeOutputLogic(vs);
	}

	private void writeOutputLogic(ViewSource vs) {
		if (vs.getExtractOutputLogic() != null && vs.getExtractOutputLogic().length() > 0) {
            logger.atFine().log("Output Logic\n%s", vs.getExtractOutputLogic());
			String eol = vs.getExtractOutputLogic();
			VDPExtractOutputLogic veol = new VDPExtractOutputLogic();

			veol.setRecordType(VDPRecord.VDP_OUTPUT_LOGIC);
			veol.setViewId(vs.getViewId());
			veol.setSequenceNbr((short) 0);
			veol.setRecordId(vs.getComponentId());
			veol.setInputFileId(vs.getComponentId());
			veol.setLogicLength((short) eol.length());
			veol.setRecLen((short) (26 + eol.length()));
			veol.setLogic(eol);
			veol.fillTheWriteBuffer(VDPWriter);
			VDPWriter.setLengthFromPosition(); //Need this for the variable length records
			VDPWriter.writeAndClearTheRecord();
		}
	}

	private void writeExtractFilter(ViewSource vs) {
		if (vs.getExtractFilter() != null && vs.getExtractFilter().length() > 0) {
			String ef = vs.getExtractFilter();
			VDPExtractFilter vef = new VDPExtractFilter();
            logger.atFine().log("Extract Filter\n%s", ef);

			vef.setRecordType(VDPRecord.VDP_EXTRACT_FILTER);
			vef.setViewId(vs.getViewId());
			vef.setSequenceNbr(vs.getSequenceNumber());
			vef.setRecordId(vs.getComponentId());
			vef.setInputFileId(vs.getComponentId());
			vef.setLogicLength((short) ef.length());
			vef.setRecLen((short) (26 + ef.length()));
			vef.setLogic(ef);
			vef.fillTheWriteBuffer(VDPWriter);
			VDPWriter.setLengthFromPosition(); //Need this for the variable length records
			VDPWriter.writeAndClearTheRecord();
		}
	}

	private void writeViewSortKeys(ViewNode view) {
		Iterator<ViewSortKey> vski = view.getSortKeyIterator();
		while (vski.hasNext()) {
			writeViewSortKey(vski.next(), view);
		}
	}

	private void writeViewSortKey(ViewSortKey vsk, ViewNode view) {
		VDPViewSortKey vvsk = new VDPViewSortKey();
        logger.atFine().log("Write sort key %d", vsk.getSequenceNumber());
		vvsk.setViewId(view.getID());
		vvsk.setColumnId(vsk.getColumnId());
		vvsk.setRecordId(vsk.getComponentId());
		vvsk.fillFromComponent(vsk);
		vvsk.fillTheWriteBuffer(VDPWriter);
		VDPWriter.writeAndClearTheRecord();
	}

	private void writeViewColumns(ViewNode view) {
		Iterator<ViewColumn> ci = view.getColumnIterator();
		while (ci.hasNext()) {
			writeViewColumn(ci.next(), view);
		}
	}

	private void writeViewColumn(ViewColumn col, ViewNode view) {
		VDPViewColumn vvc = new VDPViewColumn();
        logger.atFine().log("Write View %d column %d", col.getViewId(), col.getColumnNumber());
		
		vvc.fillFromComponent(col);
		vvc.fillTheWriteBuffer(VDPWriter);
		VDPWriter.writeAndClearTheRecord();
		writeColumnCalculation(col, view);
	}

	private void writeColumnCalculation(ViewColumn col, ViewNode view) {
		String ccl = col.getColumnCalculation();
		if (ccl != null && ccl.length() > 0) {
			if(view.getViewDefinition().getViewType() != ViewType.EXTRACT) {
				VDPColumnCalculationLogic vccl = new VDPColumnCalculationLogic();
				logger.atFine().log("Write View %d column calculation\n%s", col.getViewId(), ccl);
				vccl.setRecordType(VDPRecord.VDP_COLUMN_CALCULATION);
				vccl.setViewId(col.getViewId());
				vccl.setSequenceNbr((short) 1); // If there is more than 8K we ar in trouble
				vccl.setColumnId(col.getComponentId());
				vccl.setInputFileId(col.getComponentId());
				vccl.setLogicLength((short) ccl.length());
				vccl.setRecLen((short) (26 + ccl.length()));
				vccl.setLogic(ccl);
				vccl.fillTheWriteBuffer(VDPWriter);
				VDPWriter.setLengthFromPosition(); //Need this for the variable length records
				VDPWriter.writeAndClearTheRecord();

				writeColumnCalculationStack(col);
			} else {
				logger.atWarning().log("Ignoring column calculation %s for View %d Column %d since this is an extract view", ccl, view.getViewDefinition().getComponentId(), col.getColumnNumber());
			}
		}
	}

	private void writeColumnCalculationStack(ViewColumn col) {
		VDPColumnCalculationStack vccs = new VDPColumnCalculationStack();

		vccs.setRecordType(VDPRecord.VDP_COLUMN_CALCULATION_LT);
		vccs.setViewId(col.getViewId());
		vccs.setSequenceNbr((short) 1);
		vccs.setColumnId(col.getComponentId());
		vccs.setRecordId(col.getComponentId());
		vccs.setInputFileId(0);

		for (int i = 0; i<16; i += 1) {
			vccs.getStackHeaderPrefix().add((byte)0);
		}
		vccs.setFunctionCode("CALC");
		for (int i = 0; i<40; i += 1) {
			vccs.getStackHeaderSuffix().add((byte)0);
		}
		CalcStack colCalcStack = col.getColumnCalculationStack();
		colCalcStack.fill(vccs.getStack());
		vccs.setRecLen((short) (60 + colCalcStack.getStackLength()));
		vccs.setStackLength(colCalcStack.getStackLength());
        logger.atFine().log("Write column %d calc stack length %s", col.getViewId(), colCalcStack.getStackLength());

		vccs.fillTheWriteBuffer(VDPWriter);
		VDPWriter.setLengthFromPosition(); //Need this for the variable length records
		VDPWriter.writeAndClearTheRecord();
	}

	/**
	 * My initial approach was to write the ViewColumnSources when we wrote the
	 * column. This leads to great badness. Since the record types are out of order.
	 * When a subsequent read is done we get into a situation of having a view
	 * column source without a view source. Hence they are written on their own.
	 * 
	 * @param view
	 */
	private void writeViewColumnSources(ViewNode view) {
		Iterator<ViewColumn> ci = view.getColumnIterator();
		while (ci.hasNext()) {
			ViewColumn col = ci.next();
			Iterator<ViewColumnSource> vcsi = col.getIteratorForSourcesByNumber();
			while (vcsi.hasNext()) {
				writeViewColumnSource(col.getColumnNumber(), vcsi.next());
			}
		}
	}

	private void writeViewColumnSource(int colNumber, ViewColumnSource vcs) {
		VDPViewColumnSource vvcs = new VDPViewColumnSource();
        logger.atFine().log("Write column %d source", colNumber);
		vvcs.fillFromComponent(vcs);
		vvcs.fillTheWriteBuffer(VDPWriter);
		VDPWriter.writeAndClearTheRecord();
		writeColumnLogic(colNumber, vcs);
	}

	private void writeColumnLogic(int colNumber, ViewColumnSource vcs) {
		String cl = vcs.getLogicText();
		VDPColumnLogic vcl = new VDPColumnLogic();
        logger.atFine().log("Logic %s", cl);

		vcl.setRecordType(VDPRecord.VDP_COLUMN_LOGIC);
		vcl.setViewId(vcs.getViewId());
		vcl.setColumnId(colNumber);
		vcl.setSequenceNbr((short) 0);
		vcl.setRecordId(vcs.getComponentId());
		vcl.setInputFileId(vcs.getComponentId());
        if(cl.length() > 8162) {
			vcl.setLogicLength((short) 8162);
			vcl.setRecLen((short) (26 + 8162));
			vcl.setLogic(cl.substring(0, 8162));
            logger.atWarning().log("Truncating logic text record for view %s column %d, length was %d", vcs.getViewId(), vcs.getColumnNumber(), cl.length());
        } else {
			vcl.setLogicLength((short) cl.length());
			vcl.setRecLen((short) (26 + cl.length()));
			vcl.setLogic(cl);
        }
		vcl.fillTheWriteBuffer(VDPWriter);
		VDPWriter.setLengthFromPosition(); //Need this for the variable length records
		VDPWriter.writeAndClearTheRecord();
	}

	private void writeExtractTargetSet(VDPExtractTargetSet extractTargets) {
		if (extractTargets != null) {
            logger.atFine().log("Write View %d Extract Targets\n", extractTargets.getViewId());
			extractTargets.fillTheWriteBuffer(VDPWriter);
			VDPWriter.setLengthFromPosition(); //Need this for the variable length records
			VDPWriter.writeAndClearTheRecord();
		}
	}

		private void writeViewDefinition(ViewDefinition viewDefinition) {
		VDPViewDefinition vvd = new VDPViewDefinition();
		logger.atFine().log("Write View %d Definition", viewDefinition.getComponentId());
		fillTheErrorAndTruncFields(vvd);
		vvd.fillFromComponent(viewDefinition);
		vvd.setOutputColHdrLnsMax(viewDefinition.getOutputColHdrLnsMax());
		vvd.fillTheWriteBuffer(VDPWriter);
		//set some defaults
		VDPWriter.writeAndClearTheRecord();
	}

	private void fillTheErrorAndTruncFields(VDPViewDefinition vvd) {
		vvd.setFillErrorValue(fill('*'));
		vvd.setFillTruncationValue(fill('#'));
	}

	private String fill(char c) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<256; i++) {
			sb.append(c);
		}
		return sb.toString();
	}
	private void writeExtractRecordFile() {
		if (vdpMgmtRecs.getExtractRecordFile() != null) {
		    logger.atFine().log("Write Extract Record File");
			vdpMgmtRecs.fillExtractFileNumbersFrom(Repository.getExtractFileNubers());
			vdpMgmtRecs.getExtractRecordFile().fillTheWriteBuffer(VDPWriter);
			VDPWriter.setLengthFromPosition();
			VDPWriter.writeAndClearTheRecord();
		}
	}

	private void writeExtractOutputFile() {
		VDPExtractFile eof = vdpMgmtRecs.getExtractOutputFile();
		if(eof != null) {
		    logger.atFine().log("Write Extract Output File");
			eof.fillTheWriteBuffer(VDPWriter);
			VDPWriter.writeAndClearTheRecord();
		}
	}

	private void writeLookupTargetSet() {
		Iterator<JoinTargetEntry> jti = Repository.getJoinViews().getJoinTargetsIterator();
		VDPLookupPathTargetSet lpts = new VDPLookupPathTargetSet();
		lpts.setRecordId(650);
		lpts.setSequenceNbr((short)0);
		List<VDPLookupPathTargetEntry> refViews = lpts.getRefViews();
		while(jti.hasNext()) {
			JoinTargetEntry jt = jti.next();
			VDPLookupPathTargetEntry lpte = new VDPLookupPathTargetEntry();
			lpte.setGrefJoinid(jt.lfid);
			lpte.setRefFlag(jt.flag==1);
			lpte.setGrefEntry(jt.lfName);
			lpte.setGrefEntlen(jt.lfName.length());
			refViews.add(lpte);
		}
		if(refViews.size() > 0) {
			lpts.setRefCount(refViews.size());
			lpts.fillTheWriteBuffer(VDPWriter);
		} else if (vdpMgmtRecs.getLookupPathTargetSet() != null) {
			vdpMgmtRecs.getLookupPathTargetSet().fillTheWriteBuffer(VDPWriter);
		}
		logger.atFine().log("Write Lookup Target Set");
		VDPWriter.setLengthFromPosition(); //Need this for the variable length records
		VDPWriter.writeAndClearTheRecord();
	}

	private void writeLookupGenMap() {
		if (vdpMgmtRecs.getLookupPathGenerationMap() != null) {
			vdpMgmtRecs.getLookupPathGenerationMap().fillTheWriteBuffer(VDPWriter);
			logger.atFine().log("Write Lookup Gen Map");
			VDPWriter.setLengthFromPosition(); //Need this for the variable length records
			VDPWriter.writeAndClearTheRecord();
		}
	}

	private void writeLookupPaths() {
		Iterator<LookupPath> lpi = Repository.getLookups().getIterator();
		while (lpi.hasNext()) {
			LookupPath lp = lpi.next();
			Iterator<LookupPathStep> lpsi = lp.getStepIterator();
			while (lpsi.hasNext()) {
				LookupPathStep lps = lpsi.next();
				Iterator<LookupPathKey> lpki = lps.getKeyIterator();
				while (lpki.hasNext()) {
					VDPLookupPathKey vlpk = new VDPLookupPathKey();
					LookupPathKey lpk = lpki.next();
					lpk.setJoinName(lp.getName());
			        logger.atFine().log("Write Lookup Key:%d %d %d", lpk.getComponentId(), lpk.getStepNumber(), lpk.getKeyNumber());
					vlpk.fillFromComponent(lpk);
					vlpk.fillTheWriteBuffer(VDPWriter);
					VDPWriter.writeAndClearTheRecord();
				}
			}
		}
	}

	private void writeLRIndexes() {
		Iterator<LogicalRecord> lri = Repository.getLogicalRecords().getIterator();
		while(lri.hasNext()) {
			LogicalRecord lr = lri.next();
			Iterator<LRIndex> ii = lr.getIteratorForIndexBySeq();
			while (ii.hasNext()) {
				VDPLRIndex vi = new VDPLRIndex();
				LRIndex ndx = ii.next();
				logger.atFine().log("Write Index:%d %d %s", ndx.getComponentId(), ndx.getLrId(), ndx.getName());
				vi.fillFromComponent(ndx);
				vi.fillTheWriteBuffer(VDPWriter);
				VDPWriter.writeAndClearTheRecord();
			}
		}
	}

	private void writeLRFields() {
		Iterator<LRField> lrfi = Repository.getFields().getIterator();
		while (lrfi.hasNext()) {
			VDPLRField vlrf = new VDPLRField();
			LRField lrf = lrfi.next();
			logger.atFine().log("Write Field:%d %d %s", lrf.getComponentId(), lrf.getLrID(), lrf.getName());
			vlrf.fillFromComponent(lrf);
			vlrf.fillTheWriteBuffer(VDPWriter);
			VDPWriter.writeAndClearTheRecord();
		}
	}

	private void writeLogicalRecords() {
		Iterator<LogicalRecord> lri = Repository.getLogicalRecords().getIterator();
		while (lri.hasNext()) {
			VDPLogicalRecord vlr = new VDPLogicalRecord();
			LogicalRecord lr = lri.next();
			logger.atFine().log("Write LR:%d %s", lr.getComponentId(), lr.getName());
			vlr.fillFromComponent(lr);
			vlr.fillTheWriteBuffer(VDPWriter);
			VDPWriter.writeAndClearTheRecord();
		}
	}

	private void writeGenerationRecord() {
		logger.atFine().log("Write Generation");
		VDPGenerationRecord gen = vdpMgmtRecs.getViewGeneration();
		gen.setVersionInfo((short)13);
		gen.setLrFieldCount(Repository.getFields().size());
		gen.fillTheWriteBuffer(VDPWriter);
		VDPWriter.writeAndClearTheRecord();
	}

	private void writeFormatViewsRecord() {
		logger.atFine().log("Write Format Views");
		//Need to create a default empty record
		//VDP always has one in C++
		if (vdpMgmtRecs.getFormatViews() == null) {
			vdpMgmtRecs.makeFormatViewRecord();
		}
		vdpMgmtRecs.getFormatViews().fillTheWriteBuffer(VDPWriter);
		VDPWriter.setLengthFromPosition(); //Need this for the variable length records
		VDPWriter.writeAndClearTheRecord();
	}

	private void writeControlRecords() {
		Iterator<ControlRecord> cri = Repository.getControlRecords().getIterator();
		while (cri.hasNext()) {
			ControlRecord crc = (ControlRecord)cri.next();
			VDPControlRecord cr = new VDPControlRecord();
			logger.atFine().log("Write CR:%d", crc.getComponentId());
			cr.fillFromComponent(crc);
			cr.setEffectiveDate("00000000");
			cr.fillTheWriteBuffer(VDPWriter);
			VDPWriter.writeAndClearTheRecord();
		}
	}

	private void writePhysicalFileRecords() {
		// The VDP has an inflated or de normalised view of PFs.
		// So we need to write from the perspective of LFs
		Iterator<LogicalFile> lfi = Repository.getLogicalFiles().getIterator();
		while (lfi.hasNext()) {
			LogicalFile lf = lfi.next();
			Iterator<Entry<Integer, PhysicalFile>> pfi = lf.getPFSeqIterator();
			while (pfi.hasNext()) {
				Entry<Integer, PhysicalFile> pfe = pfi.next();
				PhysicalFile pf = pfe.getValue();
				pf.setLogicalFileId(lf.getID());
				pf.setLogicalFilename(lf.getName());
				VDPPhysicalFile vdppf = new VDPPhysicalFile();
				if(lf.isRequired() && pf.isRequired()) {
			        logger.atFine().log("Write PF:%d", pf.getComponentId());
					vdppf.fillFromComponent(pf);
					int si = pfe.getKey();
					vdppf.setSequenceNbr((short)si);
					vdppf.fillTheWriteBuffer(VDPWriter);
					VDPWriter.writeAndClearTheRecord();
				} else {
					logger.atFine().log("PF %s %d not required", pf.getName(), pf.getComponentId());
				}
			}
		}
	}

	private void writeExitRecords() {
		Iterator<UserExit> exi = Repository.getUserExits().getIterator();
		while (exi.hasNext()) {
			UserExit ex = exi.next();
			logger.atFine().log("Write Exit:%d", ex.getComponentId());
			VDPExit ue = new VDPExit();
			ue.fillFromComponent(ex);
			ue.fillTheWriteBuffer(VDPWriter);
			VDPWriter.writeAndClearTheRecord();
		}
	}

	public void close() {
		VDPWriter.close();
	}

	public int getNumRecordsWritten() {
		return VDPWriter.getNumRecordsWritten();
	}

}
