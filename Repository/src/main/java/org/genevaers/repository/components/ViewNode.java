package org.genevaers.repository.components;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

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
import java.util.TreeMap;

import org.genevaers.repository.calculationstack.CalcStack;
import org.genevaers.repository.components.enums.AccessMethod;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.components.enums.FieldDelimiter;
import org.genevaers.repository.components.enums.FileRecfm;
import org.genevaers.repository.components.enums.FileType;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.RecordDelimiter;
import org.genevaers.repository.components.enums.TextDelimiter;
import org.genevaers.repository.components.enums.ViewType;

public class ViewNode extends ComponentNode{

	public ViewDefinition viewDef = new ViewDefinition();

	private Map<Integer, ViewColumn> columns = new TreeMap<Integer, ViewColumn>();
	private Map<Integer, ViewColumn> columnsByID = new TreeMap<Integer, ViewColumn>();
	private Map<Short, ViewSortKey> sortKeys = new TreeMap<Short, ViewSortKey>();
	private Map<Integer, ViewSortKey> sortKeysByColumnID = new TreeMap<Integer, ViewSortKey>();
	private Map<Short, ViewSource> viewSources = new TreeMap<Short, ViewSource>();
	private Map<Integer, ViewSource> viewSourcesByID = new TreeMap<Integer, ViewSource>();
	private List<ReportHeader> reportHeaders = new ArrayList<>();
	private List<ReportFooter> reportFooters = new ArrayList<>();
	
	//Used when parsing VDP XML. That data arrives in a different order
	private Map<Integer, ViewColumnSource> vcsByComponentID = new TreeMap<>();


	//A VDP view always has a 1600 record - its output file.
	//We need to capture that in some way
	//Add an output file as a component?

	private OutputFile outputFile = new OutputFile();

	private String formatFilterLogic;

	private CalcStack calcStack;

	public int getNumberOfColumns() {
		return columns.size();
	}

	public void addViewColumn(ViewColumn vc) {
		columns.put(vc.getColumnNumber(), vc);
		if(vc.getComponentId() > getMaxColumnID())
			setMaxColumnID(vc.getComponentId());
		columnsByID.put(vc.getComponentId(), vc);
	}

	public ViewColumn getColumnNumber(int c) {
		return columns.get(c);
	}

	public int getNumberOfSortKeys() {
		return sortKeys.size();
	}

	public void addViewSortKey(ViewSortKey vsk) {
		sortKeys.put(vsk.getSequenceNumber(), vsk);
		sortKeysByColumnID.put(vsk.getColumnId(), vsk);
	}

	/*
	 * Should we get these by record id or by key number Will need to map them to
	 * match
	 */
	public ViewSortKey getViewSortKey(short id) {
		return sortKeys.get(id);
	}

	public ViewSortKey getViewSortKeyFromColumnId(int id) {
		return sortKeysByColumnID.get(id);
	}
	

	public int getNumberOfViewSources() {
		return viewSources.size();
	}

	public ViewSource getViewSource(short s) {
		return viewSources.get(s);
	}

	public void addViewSource(ViewSource vs) {
		viewSources.put(vs.getSequenceNumber(), vs);
		viewSourcesByID.put(vs.getComponentId(), vs);
	}

	public ViewSource getViewSourceById(int recordID) {
		return viewSourcesByID.get(recordID);
	}

	public ViewColumn getColumnByID(int recordID) {
		return columnsByID.get(recordID);
	}

	// @Override
	// public void accept(VDPNodeVisitor vdpNodeVisitor) {
	// 	vdpNodeVisitor.visit(this);
	// }

	public Iterator<ViewSource> getViewSourceIterator() {
		return viewSourcesByID.values().iterator();
	}

	public Iterator<ViewColumn> getColumnIterator() {
		return columns.values().iterator();
	}

	public Iterator<ViewSortKey> getSortKeyIterator() {
		return sortKeys.values().iterator();
	}

	public void setFormatFilterLogic(String ffl) {
		formatFilterLogic = ffl;
	}

	public boolean hasFormatFilterLogic() {
		return formatFilterLogic != null;
	}

	public String getFormatFilterLogic() {
		return formatFilterLogic;
	}

	public String getName() {
		return viewDef.getName();
	}

	public int getID() {
		return viewDef.getComponentId();
	}

	public String getIDStr() {
		return String.format("%7d", viewDef.getComponentId());
	}

	public boolean isFormat() {
		return (viewDef.getViewType() == ViewType.SUMMARY || viewDef.getViewType() == ViewType.DETAIL);
	}

	public void setDefinition(ViewDefinition vd) {
		viewDef = vd;
	}

	public ViewDefinition getViewDefinition() {
		return viewDef;
	}

	public void addViewColumnSource(ViewColumnSource vcs) {
		ViewSource vs = getViewSourceById(vcs.getViewSourceId());
		vcs.setSequenceNumber(vs.getSequenceNumber());
		ViewColumn vc = getColumnByID(vcs.getColumnID());
		if(vc != null) { //VDP XML give data in a different order
			vcs.setColumnNumber(vc.getColumnNumber());
			vcs.setViewSrcLrId(vs.getSourceLRID());

			//It has no view sources
			vc.addToSourcesByID(vcs);
			vc.addToSourcesByNumber(vcs);
			vs.addToColumnSourcesByNumber(vcs);
		} else {
			vcsByComponentID.put(vcs.getComponentId(), vcs);
		}
	}

	public void fixupVDPXMLColumns() {
		Iterator<ViewColumnSource> vcsi = vcsByComponentID.values().iterator();
		while (vcsi.hasNext()) {
			ViewColumnSource vcs = vcsi.next();
			ViewSource vs = getViewSourceById(vcs.getViewSourceId());
			ViewColumn vc = getColumnByID(vcs.getColumnID());
			if(vc != null) { //VDP XML give data in a different order
				vcs.setColumnNumber(vc.getColumnNumber());
				vcs.setViewSrcLrId(vs.getSourceLRID());
	
				//It has no view sources
				vc.addToSourcesByID(vcs);
				vc.addToSourcesByNumber(vcs);
				vs.addToColumnSourcesByNumber(vcs);
			} else {
				System.out.println("Fixup failed\n");
			}
		}
	}

	public void setOutputFileFrom(PhysicalFile pf) {
		outputFile.setName(pf.getName());
		outputFile.setOutputDDName(pf.getOutputDDName());
		outputFile.setFileType(pf.getFileType());
	}

    public void setDefaultOutputFile() {
		String ddName = String.format("F%07d", viewDef.getComponentId());
		outputFile.setOutputDDName(ddName);
		outputFile.setName("Auto Generated PF " + ddName );
		outputFile.setFileType(FileType.DISK);
		outputFile.setAccessMethod(AccessMethod.SEQUENTIAL);
		outputFile.setRecfm(FileRecfm.FB);
		outputFile.setLrecl((short)27994);
		outputFile.setFieldDelimiter(FieldDelimiter.INVALID);
		outputFile.setRecordDelimiter(RecordDelimiter.INVALID);
		outputFile.setTextDelimiter(TextDelimiter.DOUBLEQUOTE);
		outputFile.setLogicalFilename("Auto Generated LF " + ddName );
    }

	public OutputFile getOutputFile() {
		return outputFile;
	}

    public boolean hasSelectFilter() {
		//There can be many sources... 
		//Do any of them have a select filter?
		boolean found = false;
		Iterator<ViewSource> vsi = viewSources.values().iterator();
		while(vsi.hasNext()) {
			ViewSource vs = vsi.next();
			if(vs.getExtractFilter().length() > 0) {
				found = true;
			}
		}
        return found;
    }

	public ViewColumn makeJLTColumn(String name, int num) {
        int colID = ComponentNode.getMaxColumnID() + 1;
        ViewColumn vc = new ViewColumn();
		vc.setComponentId(colID);
		vc.setName(name);
		vc.setColumnNumber(num);
		vc.setOrdinalPosition((short)num);;
		vc.setViewId(viewDef.getComponentId());
		vc.setJustifyId(JustifyId.LEFT);
		vc.setHeaderJustifyId(JustifyId.LEFT);
		vc.setHeaderLine1("");
		addViewColumn(vc);
		return vc;
	}

	public boolean isExtractSummarized() {
		return viewDef.isExtractSummarized();
	}

    public CalcStack getFormatFilterCalcStack() {
        return calcStack;
    }

    public CalcStack makeCalcStack() {
		ByteBuffer buffer = ByteBuffer.allocate(8192);
        calcStack = new CalcStack(buffer, 0, 0);
        return calcStack;
    }

	public void setCalcStack(CalcStack calcStack) {
		this.calcStack = calcStack;
	}

	public int getNumCalcStackEntries() {
		//Also need to iterate the columns to see what is there
		int stackEntries = 0;
		for(ViewColumn vc : columns.values()) {
			CalcStack cs = vc.getColumnCalculationStack();
			if(cs != null) {
				stackEntries += cs.getNumEntries();
			}
		}
		if(calcStack != null) {
			stackEntries += calcStack.getNumEntries();
		}
		return stackEntries;
	}

    public int getNumberOfDTColumns() {
		int numDTcolumns = 0;
		Iterator<ViewColumn> ci = columns.values().iterator();
		while(ci.hasNext()) {
			ViewColumn vc = ci.next();
			if(vc.getExtractArea() == ExtractArea.AREADATA) {
				numDTcolumns ++;
			}
		}
		return numDTcolumns;
    }

	public Collection<ViewColumn> getColumns() {
		return columns.values();
	}

    public void addReportHeader(ReportHeader rh) {
		reportHeaders.add(rh);
    }

	public boolean hasReportHeaders() {
		return !reportHeaders.isEmpty();
	}

	public Iterator<ReportHeader> getHeadersIterator() {
		return reportHeaders.iterator();
	}

    public void addReportFooter(ReportFooter rh) {
		reportFooters.add(rh);
    }

	public boolean hasReportFooters() {
		return !reportFooters.isEmpty();
	}

	public Iterator<ReportFooter> getFootersIterator() {
		return reportFooters.iterator();
	}

	public void fixupMaxHeaderLines() {
		int maxNumHeaderLines = 0;
		Iterator<ViewColumn> ci = columns.values().iterator();
		while(ci.hasNext()) {
			ViewColumn col = ci.next();
			if (maxNumHeaderLines < 3 && col.isHidden() == false) {
				if (col.getHeaderLine3().length() > 0) {
					maxNumHeaderLines = 3;
				} 
				else if (col.getHeaderLine2().length() > 0 && maxNumHeaderLines < 2) {
					maxNumHeaderLines = 2;
				}
				else if (col.getHeaderLine1().length() > 0 && maxNumHeaderLines < 1) {
					maxNumHeaderLines = 1;
				}
			}
		}
		viewDef.setOutputColHdrLnsMax((short)maxNumHeaderLines);
	}

	public String getOutputPhaseName() {
		return "Soon";
	}

	public String getPhase() {
		return isFormat() ? "Format" : "Extract";
	}

	public String getOutputFormat() {
		switch(viewDef.getOutputMedia()) {
			case HARDCOPY:
				return "Report";
			case CSV:
				return "Delimited";
			case FILE:
			case EXCEL:
			case EXECINFO:
			case EXECINFOCSV:
			case INVALID:
			case LOTUS123:
			case ONLINE:
			default:
				return "Fixed-Length";
			
		}
	}

	public String getERAon() {
		String era = " ";
		if(isFormat()) {
			era = isExtractSummarized() ? "Y" : "N";
		}
		return era;
	}

	public String getERAsize() {
		String buffSize = " ";
		if(isExtractSummarized()) {
			buffSize = String.format("%,d", viewDef.getMaxExtractSummaryRecords());
		}
		return buffSize;
	}

	public String getFRAon() {
		String fra = " ";
		if( isFormat()) {
			fra = viewDef.getViewType() == ViewType.SUMMARY ? "Y" : "N";
		}
		return fra;
	}
}
