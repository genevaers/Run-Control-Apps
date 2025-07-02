package org.genevaers.genevaio.vdpxml;

import java.util.HashMap;
import java.util.Map;

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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.PerformBreakLogic;
import org.genevaers.repository.components.enums.SortBreakFooterOption;
import org.genevaers.repository.components.enums.SortBreakHeaderOption;
import org.genevaers.repository.components.enums.SortKeyDispOpt;
import org.genevaers.repository.components.enums.SortOrder;
import org.xml.sax.Attributes;

/**
 * We need to understand how the fields of the XML record map into the in memory
 * component
 * and then into the VDP record.
 * 
 * SortBreakFooterOption
 * SortBreakHeaderOption
 * 
 * The low order bit is the sort break footer option
 * // 0 = Don't display a footer line at all [value 1]
 * // 1 = Print footer line on Same Page [value 2]
 * m_pSortKey->SetSortBreakFooterDisp(m_sortBreakInd &amp; 1 ?
 * AttrSortBreakFooterDispSamePage :
 * AttrSortBreakFooterDispNone);
 * 
 * // The next bit indicates whether or not to skip sort break logic
 * // 0 = No (perform sort break logic) [value 1]
 * // 1 = Yes (do not perform) [value 2]
 * m_pSortKey->SetSortBreakOption(m_sortBreakInd &amp; 2 ?
 * AttrSortBreakOptionNoBreak :
 * AttrSortBreakOptionBreak);
 * VDPSortBreakHeaderDisp tHeaderDisp = AttrSortBreakHeaderDispSamePage;
 * if (m_pageBreakInd == 1)
 * tHeaderDisp = AttrSortBreakHeaderDispNewPage;
 * else if (m_pageBreakInd == 2)
 * tHeaderDisp = AttrSortBreakHeaderDispNone;
 * 
 */
public class ViewSortKeyRecordParser extends BaseParser {

	private ViewSortKey vsk;
	private int seqNum;
	private ViewNode viewNode;

	public ViewSortKeyRecordParser() {
		sectionName = "Sort";
	}


	public ViewSortKey getViewSortKey() {
		return vsk;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) {
				logger.atFine().log("name %s text %s", name, text);
		short s;
		switch (name.toUpperCase()) {
			case "SORTCOLUMN":
				componentID = Integer.parseInt(attributes.get("ID"));
				seqNum = Integer.parseInt(attributes.get("seq"));
				break;
			case "COLUMNREF":
				int colID = Integer.parseInt(attributes.get("ID"));
				vsk = viewNode.getViewSortKeyFromColumnId(colID);	
				vsk.setComponentId(componentID);
				vsk.setViewSortKeyId(componentID);
				vsk.setSequenceNumber((short) seqNum);
				viewNode.addViewSortKeyBySeq(vsk);;
				break;
			case "SORTKEYLABEL":
				vsk.setLabel(text.trim());
				break;
			case "ORDER":
				vsk.setSortorder(SortOrder.fromdbcode(text.trim()));
				break;
			case "BREAK":
				if (text.equals("1")) {
					vsk.setPerformBreakLogic(PerformBreakLogic.BREAK);
				} else {
					vsk.setPerformBreakLogic(PerformBreakLogic.NOBREAK);
				}
				break;
			case "HEADER":
				if(text.equals("NEWP")) {
				logger.atFine().log("Fixed new page");
					vsk.setSortBreakHeaderOption(SortBreakHeaderOption.NEWPAGE);
				} else {
					vsk.setSortBreakHeaderOption(SortBreakHeaderOption.fromdbcode(text));
				}
				break;
			case "FOOTER":
				if (text.equals("1"))
					vsk.setSortBreakFooterOption(SortBreakFooterOption.PRINT);
				else
					vsk.setSortBreakFooterOption(SortBreakFooterOption.NOPRINT);
				break;
			case "PREFIX":
				vsk.setLabel(text);
				break;
			case "HARDCOPY":
				vsk.setSortDisplay(SortKeyDispOpt.fromdbcode(text));
				break;

			case "DATATYPE":
				vsk.setSortKeyDataType(DataType.fromdbcode(text.trim()));
				break;
			default:
				break;
		}
	}

	public void setViewNode(ViewNode viewNode) {
		this.viewNode = viewNode;
	}
}
