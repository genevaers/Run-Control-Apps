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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.genevaers.genevaio.fieldnodes.ComparisonState;
import org.genevaers.genevaio.fieldnodes.FieldNodeBase;
import org.genevaers.genevaio.fieldnodes.MetadataNode;
import org.genevaers.genevaio.fieldnodes.StringFieldNode;
import org.genevaers.genevaio.fieldnodes.FieldNodeBase.FieldNodeType;
import org.genevaers.genevaio.fieldnodes.FunctionCodeNode;
import com.google.common.flogger.FluentLogger;

public class LogicTableTextWriter extends TextRecordWriter {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private Map<String, String> accumulatorNamesMap = new TreeMap<>();
	boolean jlt;
	
	public LogicTableTextWriter() {
		setIgnores();
	}

	@Override
	public void writeDetails( MetadataNode recordsRoot, Writer fw, String generated) throws IOException {
		addCppIgnores();
		addJLTIgnores(recordsRoot);
		writeHeader(generated, fw);
		writeContent(recordsRoot,fw);
		writeComparisonSummary(recordsRoot, fw);
	}
	
	private void addJLTIgnores(MetadataNode recordsRoot) {
		FieldNodeBase gen = recordsRoot.getChildrenByName("LT_0");
		if(((StringFieldNode)gen.getChildrenByName("extract")).getValue().equals("False")) {
			jlt = true;
			ignoreTheseDiffs.put("viewId", true); 
			ignoreTheseDiffs.put("columnId", true); 
			ignoreTheseDiffs.put("ordinalPosition", true); 
			ignoreTheseDiffs.put("DTA_signedInd", true); 
			ignoreTheseDiffs.put("DTE_signedInd", true); 
			ignoreTheseDiffs.put("DTE_fieldId", true); 
			ignoreTheseDiffs.put("DTE_fieldFormat", true); 
			ignoreTheseDiffs.put("DTE_fieldContentId", true); 
			ignoreTheseDiffs.put("DTE_startPosition", true); 
			ignoreTheseDiffs.put("DTA_logfileId", true); 
		}
	}

	private void addCppIgnores() {
		ignoreTheseDiffs.put("accumulatorName", true); 
		ignoreTheseDiffs.put("tableName", true); 
		ignoreTheseDiffs.put("ADDA_value", true); 
		ignoreTheseDiffs.put("DIVA_value", true); 
		ignoreTheseDiffs.put("MULA_value", true); 
		ignoreTheseDiffs.put("SUBA_value", true); 
		ignoreTheseDiffs.put("SETA_value", true); 
		ignoreTheseDiffs.put("CFAA_value", true); 
		ignoreTheseDiffs.put("REEX_viewId", true); 
		ignoreTheseDiffs.put("LKLR_gotoRow1", true); 
		ignoreTheseDiffs.put("LKLR_gotoRow2", true); 
		ignoreTheseDiffs.put("KSLK_fileId", true); 
		ignoreTheseDiffs.put("CTC_logfileId", true); 
	}

	private void writeComparisonSummary(MetadataNode recordsRoot, Writer fw) throws IOException {
		if(recordsRoot.getName().equals("Compare")) {
			fw.write("\n\nComparison Results\n==================\n\n");
			fw.write(String.format("%-20s: %7d\n\n\n", "Number of diffs", numDiffs));
		}
	}

	private void writeHeader(String generated, Writer fw) throws IOException {
		fw.write(String.format("LT Report: %s\n\n", generated));
	}


	private void writeContent(MetadataNode recordsRoot, Writer fw) throws IOException {
		fw.write(String.format("\nRecord Level Reports\n"));
		fw.write(String.format("====================\n"));
        Iterator<FieldNodeBase> fi = recordsRoot.getChildren().iterator();
        while (fi.hasNext()) {
            FieldNodeBase n = (FieldNodeBase) fi.next();
			preCheckAndChangeRowState(n);
			writeRecord(n, fw);
        }
	}


	private void writeRecord(FieldNodeBase r, Writer fw) throws IOException {
		fw.write("    Record:\n");
		Iterator<FieldNodeBase> fi = r.getChildren().iterator();
		while (fi.hasNext()) {
			FieldNodeBase n = (FieldNodeBase) fi.next();
			if(n.getFieldNodeType() == FieldNodeType.RECORDPART){
				writeArg(n, fw);
			} else {
				writeField(n, fw, false);
			}
		}
	}

	private void writeArg(FieldNodeBase r, Writer fw) throws IOException {
		fw.write("    Arg:\n");
		Iterator<FieldNodeBase> fi = r.getChildren().iterator();
		while (fi.hasNext()) {
			FieldNodeBase n = (FieldNodeBase) fi.next();
			if(n.getFieldNodeType() == FieldNodeType.RECORDPART){
				writeRecord(n, fw);
			} else {
				writeField(n, fw, false);
			}
		}
	}

	@Override
	public void setIgnores() {
		ignoreTheseDiffs.put("GEN_fileId", true); 
		ignoreTheseDiffs.put("GEN_dateDd", true); 
		ignoreTheseDiffs.put("GEN_timeHh", true); 
		ignoreTheseDiffs.put("GEN_timeMm", true); 
		ignoreTheseDiffs.put("GEN_timeSs", true); 
		ignoreTheseDiffs.put("GEN_timeTt", true); 
		ignoreTheseDiffs.put("GEN_desc", true); 
		ignoreTheseDiffs.put("GEN_padding", true); 
		ignoreTheseDiffs.put("GEN_padding", true); 
		ignoreTheseDiffs.put("HD_fileId", true); 
		ignoreTheseDiffs.put("HD_time", true); 
		ignoreTheseDiffs.put("HD_date", true); 
		ignoreTheseDiffs.put("NV_littleEndian", true); 
		ignoreTheseDiffs.put("NV_fileId", true); 
		ignoreTheseDiffs.put("sourceSeqNbr", true); 
		ignoreTheseDiffs.put("ordinalPosition", true); 
		ignoreTheseDiffs.put("GOTO_viewId", true); 
		ignoreTheseDiffs.put("DTA_logfileId", true); 
		ignoreTheseDiffs.put("DTC_lrId", true); 
		ignoreTheseDiffs.put("DTC_logfileId", true); 
		ignoreTheseDiffs.put("JOIN_fieldId", true); 
		ignoreTheseDiffs.put("JOIN_fieldFormat", true); 
		ignoreTheseDiffs.put("JOIN_startPosition", true); 
		ignoreTheseDiffs.put("JOIN_justifyId", true); 
		ignoreTheseDiffs.put("JOIN_value", true); 
		ignoreTheseDiffs.put("LKE_ordinalPosition", true); 
		ignoreTheseDiffs.put("SETC_viewId_lRecordCount", true); 
		ignoreTheseDiffs.put("RENX_viewId", true); 
		ignoreTheseDiffs.put("ADDX_columnId", true); 
		ignoreTheseDiffs.put("DIVX_columnId", true); 
		ignoreTheseDiffs.put("MULX_columnId", true); 
		ignoreTheseDiffs.put("SUBX_columnId", true); 
		ignoreTheseDiffs.put("SETX_columnId", true); 
		ignoreTheseDiffs.put("LKDC_justifyId", true); 
		ignoreTheseDiffs.put("LKDE_justifyId", true); 
		ignoreTheseDiffs.put("LKLR_justifyId", true); 
		ignoreTheseDiffs.put("LKLR_fieldId", true); 
		ignoreTheseDiffs.put("LKLR_fieldFormat", true); 
		ignoreTheseDiffs.put("LKLR_startPosition", true); 
		ignoreTheseDiffs.put("LKLR_value", true); 
		ignoreTheseDiffs.put("LKS_logfileId", true); 
		ignoreTheseDiffs.put("FNCC_logfileId", true); 
		ignoreTheseDiffs.put("FNCC_justifyId", true); 
		ignoreTheseDiffs.put("CTC_fieldLength", true); 
		ignoreTheseDiffs.put("CTC_lrId", true); 
		ignoreTheseDiffs.put("SKC_lrId", true); 
		ignoreTheseDiffs.put("SKC_logfileId", true); 
		ignoreTheseDiffs.put("CTA_fieldLength", true); 
		ignoreTheseDiffs.put("DTX_columnId", true); 
		ignoreTheseDiffs.put("CFLX_columnId", true); 
		ignoreTheseDiffs.put("CFEX_columnId", true); 
		ignoreTheseDiffs.put("CFAX_columnId", true); 
		ignoreTheseDiffs.put("CFPX_columnId", true); 
		ignoreTheseDiffs.put("CFXA_columnId", true); 
		ignoreTheseDiffs.put("CFXE_columnId", true); 
		ignoreTheseDiffs.put("CFXL_columnId", true); 
		ignoreTheseDiffs.put("CFXP_columnId", true); 
		ignoreTheseDiffs.put("SFXE_columnId", true); 
		ignoreTheseDiffs.put("SFXL_columnId", true); 
		ignoreTheseDiffs.put("SFXP_columnId", true); 
		ignoreTheseDiffs.put("SFEX_columnId", true); 
		ignoreTheseDiffs.put("SFLX_columnId", true); 
		ignoreTheseDiffs.put("SFPX_columnId", true); 
		ignoreTheseDiffs.put("EN_fileId", true); 
	}


	protected void preCheckAndChangeRowState(FieldNodeBase r) {
		boolean updateRowState = true;
		for (FieldNodeBase n : r.getChildren()) {
			if (n.getFieldNodeType() == FieldNodeType.RECORDPART) {
				preCheckAndChangeRowState(n);
			} else {
				if (n.getState() == ComparisonState.DIFF) {
					if(jlt) {
						if (ignoreTheseDiffs.get(getDiffKey(n)) != null) {
							n.setState(ComparisonState.IGNORED);
						} else if (n.getFieldNodeType() == FieldNodeType.STRINGFIELD
								&& ((StringFieldNode) n).getDiffValue().startsWith("<Reserve")) {
							n.setState(ComparisonState.IGNORED);
						} else {
							updateRowState = false;
						}
					} else {
						if (n.getParent().getFieldNodeType() == FieldNodeType.FUNCCODE) {
							System.out.println(((FunctionCodeNode)n.getParent()).getFunctionCode());
						}
						System.out.println(getDiffKey(n));
						if (ignoreTheseDiffs.get(getDiffKey(n)) != null) {
							n.setState(ComparisonState.IGNORED);
						} else if (n.getParent().getFieldNodeType() == FieldNodeType.FUNCCODE) {
							mapAccumulatorNames(n);
							if( ((FunctionCodeNode)n.getParent()).getFunctionCode().equals("CFAC")) {
								if( ((StringFieldNode)n).getDiffValue().endsWith(".00")) {
									n.setState(ComparisonState.IGNORED);
								}		
							} else if (((FunctionCodeNode)n.getParent()).getFunctionCode().equals("CFCC")) {
								n.setState(ComparisonState.IGNORED);
							}
						} else if (n.getParent().getFieldNodeType() == FieldNodeType.RECORDPART) {
							ignoreTrailingZeros(n);
						} else if (jlt == false && n.getFieldNodeType() == FieldNodeType.STRINGFIELD
								&& ((StringFieldNode) n).getDiffValue().startsWith("0000")) {
							n.setState(ComparisonState.IGNORED);
						} else {
							updateRowState = false;
						}
					}
				}
			}
		}
		if (updateRowState) {
			r.setState(ComparisonState.INSTANCE);
		}
	}

	private void ignoreTrailingZeros(FieldNodeBase n) {
		String fc = ((FunctionCodeNode)n.getParent().getParent()).getFunctionCode();
		if(n.getFieldNodeType() == FieldNodeType.STRINGFIELD) {
			StringFieldNode sfn = (StringFieldNode)n;
			switch(fc) {
				case "CFAC":
					//really want to look for numerical equivalence
					if(sfn.getDiffValue().endsWith("0")) {
						n.setState(ComparisonState.IGNORED);
					}
				break;
				case "CFEC":
					//really want to look for numerical equivalence
					if(sfn.getDiffValue().endsWith("0")) {
						n.setState(ComparisonState.IGNORED);
					}
				break;
				case "LKDC":
					if(sfn.getDiffValue().endsWith("0")) {
						n.setState(ComparisonState.IGNORED);
					}
				break;
				case "CTC":
				if(sfn.getDiffValue().endsWith("000")) {
					n.setState(ComparisonState.IGNORED);
				}
				break;
			}
		}
	}

	private void mapAccumulatorNames(FieldNodeBase n) {
		String fc = ((FunctionCodeNode)n.getParent()).getFunctionCode();
		if(n.getFieldNodeType() == FieldNodeType.STRINGFIELD) {
			StringFieldNode sfn = (StringFieldNode)n;
			if(fc.equals("DIMN")) {
				//Save the mapping
				accumulatorNamesMap.put( sfn.getValue(), sfn.getDiffValue());
				n.setState(ComparisonState.MAPPED);
			}
			switch(fc) {
				case "DTA":
				case "SETA":
				case "SETE":
				case "SETL":
				case "SETX":
				case "ADDA":
				case "ADDX":
				case "DIVA":
				case "MULA":
				case "SUBA":
				case "SUBX":
				case "ADDC":
				case "DIVC":
				case "DIVX":
				case "MULC":
				case "MULX":
				case "SUBC":
					String accName = accumulatorNamesMap.get(sfn.getValue());
					if(accName != null && accName.equals(sfn.getDiffValue())) {
						n.setState(ComparisonState.MAPPED);
					}
				break;
			}
		}
	}

	@Override
	protected String getDiffKey(FieldNodeBase n) {
		if(n.getName().equals("sourceSeqNbr") || n.getName().equals("ordinalPosition") 
			|| n.getName().equals("tableName") || n.getName().equals("accumulatorName")) {
			return n.getName();
		} else {
			if(n.getParent().getFieldNodeType() == FieldNodeType.RECORDPART) {
				if(jlt && (n.getName().equals("ordinalPosition"))) {
					return n.getName();
				} else {
					return ((FunctionCodeNode)n.getParent().getParent()).getFunctionCode() + "_" + n.getName();
				}
			} else {
				if( ((FunctionCodeNode)n.getParent()).getFunctionCode().equals("SETC") ){
					FieldNodeBase setcacc = (((FunctionCodeNode)n.getParent()).getChildrenByName("tableName"));
					return ((FunctionCodeNode)n.getParent()).getFunctionCode() + "_" + n.getName() + "_" + ((StringFieldNode)setcacc).getValue();
				} else if(jlt && 
				(n.getName().equals("viewId") || n.getName().equals("columnId") || n.getName().equals("ordinalPosition"))) {
					return n.getName();
				} else {
					return ((FunctionCodeNode)n.getParent()).getFunctionCode() + "_" + n.getName();
				}
			}
		}
	}
}
