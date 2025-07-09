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
import org.genevaers.repository.components.ReportFooter;
import org.genevaers.repository.components.ReportHeader;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.ReportFunction;

public class ViewHeaderFooterParser extends BaseParser {

	private int id;
	private String functonCode;
	private String justify;
	private int row;
	private int col;
	private int length;
	private String itemText;

	private int viewid;
	private ViewNode viewNode;
	private ReportHeader rh;

	public ViewHeaderFooterParser() {
		sectionName = "Headers";
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) {
		switch (name.toUpperCase()) {
			case "HEADER":
				id = Integer.parseInt(attributes.get("ID"));
				rh = new ReportHeader();
				rh.setComponentId(id);
				rh.setJustification(JustifyId.NONE);
				viewNode.addReportHeader(rh);
				break;
			case "FUNCTION":
				functonCode = text.trim();
				if("DATE".equalsIgnoreCase(functonCode)){
					functonCode = "PDATE";
				}else if ("TIME".equalsIgnoreCase(functonCode)) {
					functonCode = "PTIME";
				}
				rh.setFunction(Repository.getReportFunctionValue(ReportFunction.fromdbcode(functonCode)));
				break;
			case "ALIGNMENT":
				justify = text.trim();
				rh.setJustification(JustifyId.fromdbcode(justify));
				break;
			case "POSITION":
				row = Integer.parseInt(text);
				rh.setColumn((short) row);
				break;
			case "LINE":
				col = Integer.parseInt(text);
				rh.setRow((short) col);
				break;
			case "LENGTH":
				length = Integer.parseInt(text);
				break;
			case "TEXT":
				itemText = text;
				rh.setText(itemText);
				rh.setTitleLength((short)text.length());
				break;
			case "DATEFORMAT":
			itemText = text;
			//rh.setd(itemText);
			break;
			default:
				break;
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
		viewNode.addReportFooter(rf);
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
		if (itemText == null) {
			rh.setText("");
		} else {
			rh.setText(itemText);
		}
		viewNode.addReportHeader(rh);
	}

	public void setViewNode(ViewNode viewNode) {
		this.viewNode = viewNode;
	}
}
