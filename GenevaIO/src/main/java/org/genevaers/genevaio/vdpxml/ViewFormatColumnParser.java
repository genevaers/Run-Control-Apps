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
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.SubtotalType;

public class ViewFormatColumnParser extends BaseParser {

	private ViewColumn vc;

	private ViewNode viewNode;

	public ViewFormatColumnParser() {
		sectionName = "FormatColumns";
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) {
		switch (name.toUpperCase()) {
			case "FORMATCOLUMN":
				componentID = Integer.parseInt(attributes.get("ID"));
				int seqNum = Integer.parseInt(attributes.get("seq"));
				logger.atFine().log("Format Column " + seqNum);
				vc = viewNode.getColumnByID(componentID);
				break;
			case "NAME":
				vc.setName(text);
				break;
			case "DATATYPE":
				vc.setDataType(DataType.fromdbcode(text.trim()));
				if (vc.getDataType() == DataType.ALPHANUMERIC) {
					vc.setJustifyId(JustifyId.LEFT);
				} else {
					vc.setJustifyId(JustifyId.RIGHT);
				}
				break;
			case "SIGNEDDATA":
				vc.setSigned(text.equals("1") ? true : false);
				break;
			case "POSITION":
				short s = (short) Integer.parseInt(text.trim());
				vc.setStartPosition(s);
				break;
			case "LENGTH":
				s = (short) Integer.parseInt(text.trim());
				vc.setFieldLength(s);
				break;
			case "ORDINAL":
				s = (short) Integer.parseInt(text.trim());
				vc.setOrdinalPosition(s);
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
			case "DEFAULTVALUE":
				vc.setDefaultValue(text);
				break;
			case "HIDDEN":
				vc.setHidden(text.equals("1") ? true : false);
				break;
			case "SUBTOTALTYPECD":
				vc.setSubtotalType(SubtotalType.fromdbcode(text.trim()));
				break;
			case "SPACESBEFORECOLUMN":
				s = (short) Integer.parseInt(text.trim());
				vc.setSpacesBeforeColumn(s);
				break;
			case "AGGREGATIONPREFIX":
				vc.setSubtotalPrefix(text);
				break;
			case "MASK":
				vc.setReportMask(text);
				break;
			case "HEADERALIGNMENT":
				vc.setHeaderJustifyId(JustifyId.fromdbcode(text));
				break;
			case "HEADERLINE1":
				vc.setHeaderLine1(text);
				break;
			case "HEADERLINE2":
				vc.setHeaderLine2(text);
				break;
			case "HEADERLINE3":
				vc.setHeaderLine3(text);
				break;
			case "AGGREGATION":
				vc.setSubtotalType(SubtotalType.fromdbcode(text));
				break;
			case "CALCULATION":
				vc.setColumnCalculation(vc.getColumnCalculation() + text);
				//vc.setColumnCalculation(removeBRLineEndings(text));
				break;
			default:
				break;
		}
	}

	public void setViewNode(ViewNode viewNode) {
		this.viewNode = viewNode;
	}

}
