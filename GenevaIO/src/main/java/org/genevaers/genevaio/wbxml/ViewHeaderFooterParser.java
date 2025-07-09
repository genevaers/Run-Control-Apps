package org.genevaers.genevaio.wbxml;

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
import org.genevaers.repository.components.ReportFooter;
import org.genevaers.repository.components.ReportHeader;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.ReportFunction;

public class ViewHeaderFooterParser extends RecordParser {

	private int id;
	private String functonCode;
	private String justify;
	private int row;
	private int col;
	private int length;
	private String itemText;

	private int viewid;

	@Override
	public void parseRecord(XMLStreamReader reader) throws XMLStreamException {
		String part = reader.getName().getLocalPart();
		int n = reader.next();
		if (n == XMLEvent.CHARACTERS) {
			String text = reader.getText();
			switch (part) {
				case "HEADERFOOTERID":
					id = Integer.parseInt(text);
					itemText = "";
					break;
				case "STDFUNCCD":
					functonCode = text.trim();
					break;
				case "VIEWID":
					viewid = Integer.parseInt(text);
					break;
				case "JUSTIFYCD":
					justify = text.trim();
					break;
				case "ROWNUMBER":
					row = Integer.parseInt(text);
					break;
				case "COLNUMBER":
					col = Integer.parseInt(text);
					break;
				case "LENGTH":
					length = Integer.parseInt(text);
					break;
				case "ITEMTEXT":
					itemText = text;
					break;
				case "HEADERFOOTERIND":
					if((Repository.isViewEnabled(viewid))) {
						if (text.equals("1")) {
							addHeaderToRepository();
						} else {
							addFooterToRepository();
						}
					}
					break;
				default:
					break;
			}
		}

	}

	private void addFooterToRepository() {
		ReportFooter rf = new ReportFooter();
		rf.setComponentId(id);
		if (functonCode.length() > 0) {
			rf.setFunction(ReportFunction.fromdbcode(functonCode));
		} else {
			rf.setFunction(ReportFunction.INVALID);
		}
		if (justify.length() > 0) {
			rf.setJustification(JustifyId.fromdbcode(justify));
		} else {
			rf.setJustification(JustifyId.NONE);
		}
		rf.setColumn((short) col);
		rf.setRow((short) row);
		rf.setFooterLength((short) length);
		rf.setText(itemText != null ? itemText : "");
		Repository.getViews().get(viewid).addReportFooter(rf);
	}

	private void addHeaderToRepository() {
		ReportHeader rh = new ReportHeader();
		rh.setComponentId(id);
		if (functonCode.length() > 0) {
			rh.setFunction(Repository.getReportFunctionValue(ReportFunction.fromdbcode(functonCode)));
		} else {
			rh.setFunction(0);
		}
		if (justify.length() > 0) {
			rh.setJustification(JustifyId.fromdbcode(justify));
		} else {
			rh.setJustification(JustifyId.NONE);
		}
		rh.setColumn((short) col);
		rh.setRow((short) row);
		rh.setTitleLength((short) length);
		if(itemText == null) {
			rh.setText("");
		} else {
			rh.setText(itemText);
		}
		Repository.getViews().get(viewid).addReportHeader(rh);
	}
}
