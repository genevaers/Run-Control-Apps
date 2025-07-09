package org.genevaers.genevaio.vdpxml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

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

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.PerformBreakLogic;
import org.genevaers.repository.components.enums.SortKeyDispOpt;
import org.genevaers.repository.components.enums.SubtotalType;

public class ViewColumnRecordParser extends BaseParser {

	private ViewColumn vc;

	private boolean formalDetails = false;

	private ViewNode viewNode;

	private ViewSortKey vsk;
	boolean sortkey = false;
	boolean sortkeyTitle = false;

	public ViewColumnRecordParser() {
		sectionName = "ExtractColumns";
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) {
		switch (name.toUpperCase()) {
			case "EXTRACTCOLUMN":
				componentID = Integer.parseInt(attributes.get("ID"));
				int seqNum = Integer.parseInt(attributes.get("seq"));
				logger.atFine().log("ExtractColumn " + seqNum);
				vc = new ViewColumn();
				vc.setComponentId(componentID);
				vc.setName("");
				vc.setFieldName("");
				vc.setDetailPrefix("");
				vc.setSubtotalMask("");
				vc.setDateCode(DateCode.NONE);
				vc.setSubtotalType(SubtotalType.NONE);
				vc.setHeaderJustifyId(JustifyId.CENTER);
				vc.setViewId(viewNode.getID());
				vc.setColumnNumber(seqNum);
				vc.setExtractAreaPosition((short) 1);
				vc.setStartPosition((short) 1);
				viewNode.addViewColumn(vc);
				sortkey = false;
				sortkeyTitle = false;
				break;
			case "NAME":
				vc.setName(text);
				formalDetails = true;
				break;
			case "AREA":
				vc.setExtractArea(ExtractArea.fromdbcode(text.trim()));
				if (vc.getExtractArea() == ExtractArea.SORTKEY) {
					vsk = new ViewSortKey();
					//vsk.setComponentId(vc.getComponentId());
					//vsk.setViewSortKeyId(vc.getComponentId());
					vsk.setColumnId(vc.getComponentId());
					vsk.setSortKeyDataType(vc.getDataType());
					vsk.setSkFieldLength(vc.getFieldLength());
					vsk.setSkStartPosition(vc.getExtractAreaPosition());
					setDefault(vsk);
					viewNode.addViewSortKey(vsk);
					sortkey = true;
				}
				break;
			case "DATATYPE":
				if (sortkey) {
					if (sortkeyTitle) {
						vsk.setSktDataType(DataType.fromdbcode(text.trim()));
					} else {
						vsk.setSortKeyDataType(DataType.fromdbcode(text.trim()));
					}
				} else {
					vc.setDataType(DataType.fromdbcode(text.trim()));
					if (vc.getDataType() == DataType.ALPHANUMERIC) {
						vc.setJustifyId(JustifyId.LEFT);
					} else {
						vc.setJustifyId(JustifyId.RIGHT);
					}
				}
				break;
			case "SIGNEDDATA":
				vc.setSigned(text.equals("1") ? true : false);
				break;
			case "POSITION":
				short s = (short) Integer.parseInt(text.trim());
				if (sortkey) {
					vsk.setSkStartPosition(s);
				}
				vc.setExtractAreaPosition(s);
				
				break;
			case "LENGTH":
				s = (short) Integer.parseInt(text.trim());
				if (sortkey) {
					if (sortkeyTitle) {
						vsk.setSktFieldLength(s);
					} else {
						vsk.setSkFieldLength(s);
					}
				} else {
					vc.setFieldLength(s);
				}
				break;
			case "ORDINAL":
				s = (short) Integer.parseInt(text.trim());
				vc.setOrdinalPosition(s);
				break;
			case "ORDINALPOSITION":
				break;
			case "DECIMALPLACES":
				s = (short) Integer.parseInt(text.trim());
				vc.setDecimalCount(s);
				break;
			case "SCALEFACTOR":
				s = (short) Integer.parseInt(text);
				vc.setRounding(s);
				break;
			case "DATEFORMAT":
				vc.setDateCode(DateCode.fromdbcode(text.trim()));
				break;
			case "ALIGNMENT":
				vc.setJustifyId(JustifyId.fromdbcode(text.trim()));
				break;
			case "SORTTITLEKEY":
				logger.atFine().log("Column %s has sort key title Found");
				vsk.setRtdLrFieldId(Integer.parseInt(attributes.get("ID")));
				sortkeyTitle = true;
				break;
			default:
				break;
		}
	}

	public void setViewNode(ViewNode viewNode) {
		this.viewNode = viewNode;
	}

	private void setDefault(ViewSortKey vsk) {
		vsk.setDescDateCode(DateCode.NONE);
		vsk.setDescDataType(DataType.ALPHANUMERIC);
		vsk.setDescJustifyId(JustifyId.LEFT);
		vsk.setLabel("");
		vsk.setSkJustifyId(JustifyId.LEFT);
		vsk.setSktDateCode(DateCode.NONE);
		vsk.setSktDataType(DataType.ALPHANUMERIC);
		vsk.setSktJustifyId(JustifyId.LEFT);
		vsk.setSortKeyDateTimeFormat(DateCode.NONE);
		vsk.setSortDisplay(SortKeyDispOpt.CATEGORIZE);
		vsk.setPerformBreakLogic(PerformBreakLogic.NOBREAK);
	}

}
