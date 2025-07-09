package org.genevaers.genevaio.wbxml;

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
 * m_pSortKey->SetSortBreakFooterDisp(m_sortBreakInd {@literal &} 1 ?
 * AttrSortBreakFooterDispSamePage :
 * AttrSortBreakFooterDispNone);
 * 
 * // The next bit indicates whether or not to skip sort break logic
 * // 0 = No (perform sort break logic) [value 1]
 * // 1 = Yes (do not perform) [value 2]
 * m_pSortKey->SetSortBreakOption(m_sortBreakInd {@literal &} 2 ?
 * AttrSortBreakOptionNoBreak :
 * AttrSortBreakOptionBreak);
 * VDPSortBreakHeaderDisp tHeaderDisp = AttrSortBreakHeaderDispSamePage;
 * if (m_pageBreakInd == 1)
 * tHeaderDisp = AttrSortBreakHeaderDispNewPage;
 * else if (m_pageBreakInd == 2)
 * tHeaderDisp = AttrSortBreakHeaderDispNone;
 * 
 */
public class ViewSortKeyRecordParser extends RecordParser {

	private ViewSortKey vsk;

	private int currentViewId;

	private ViewNode currentViewNode;

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
	}

	public ViewSortKey getViewSortKey() {
		return vsk;
	}

	@Override
	public void parseRecord(XMLStreamReader reader) throws XMLStreamException {
		String part = reader.getName().getLocalPart();
		int n = reader.next();
		if (n == XMLEvent.CHARACTERS) {
			String text = reader.getText();
			switch (part) {
				case "VIEWSORTKEYID":
					vsk = new ViewSortKey();
					vsk.setViewSortKeyId(Integer.parseInt(text));
					setDefault(vsk);
					break;
				case "VIEWCOLUMNID":
					vsk.setComponentId(Integer.parseInt(text));
					vsk.setColumnId(Integer.parseInt(text));
					break;
				case "VIEWID":
					int viewId = Integer.parseInt(text.trim());
					if (viewId != currentViewId) {
						currentViewNode = Repository.getViews().get(viewId);
						currentViewId = viewId;
					}
					break;
				case "SORTKEYLABEL":
					vsk.setLabel(text.trim());
					break;
				case "KEYSEQNBR":
					short s = (short) Integer.parseInt(text);
					vsk.setSequenceNumber(s);
					if (Repository.isViewEnabled(currentViewId)) {
						currentViewNode.addViewSortKey(vsk);
					}
					break;
				case "SORTSEQCD":
					vsk.setSortorder(SortOrder.fromdbcode(text.trim()));
					break;
				case "SKFLDFMTCD":
					vsk.setSortKeyDataType(DataType.fromdbcode(text.trim()));
					break;
				case "SKSIGNED":
					vsk.setSortKeySigned(text.equals("1") ? true : false);
					break;
				case "SKSTARTPOS":
					s = (short) Integer.parseInt(text);
					vsk.setSkStartPosition(s);
					break;
				case "SKFLDLEN":
					s = (short) Integer.parseInt(text);
					vsk.setSkFieldLength(s);
					break;
				case "SKDECIMALCNT":
					s = (short) Integer.parseInt(text);
					vsk.setSktDecimalCount(s);
					break;
				case "SKFLDCONTENTCD":
						vsk.setSortKeyDateTimeFormat(DateCode.fromdbcode(text));
					break;
				case "SORTKEYDISPLAYCD":
						vsk.setSortDisplay(SortKeyDispOpt.fromdbcode(text.trim()));
					break;
				case "SORTBRKIND":
					int sbi = Integer.parseInt(text);
					int footerMask = 0x0001;
					int re = sbi & footerMask;
					if (re > 0) {
						vsk.setSortBreakFooterOption(SortBreakFooterOption.PRINT);
					} else {
						vsk.setSortBreakFooterOption(SortBreakFooterOption.NOPRINT);
					}
					int breakMask = 0x0002;
					re = sbi & breakMask;
					if (re > 0) {
						vsk.setPerformBreakLogic(PerformBreakLogic.NOBREAK);
					} else {
						vsk.setPerformBreakLogic(PerformBreakLogic.BREAK);
					}
					break;
				case "PAGEBRKIND":
					int pbi = Integer.parseInt(text);
					switch (pbi) {
						case 1:
							vsk.setSortBreakHeaderOption(SortBreakHeaderOption.NEWPAGE);
							break;
						case 2:
							vsk.setSortBreakHeaderOption(SortBreakHeaderOption.NONE);
							break;
						default:
							vsk.setSortBreakHeaderOption(SortBreakHeaderOption.SAMEPAGE);
							break;
					}
					break;
				case "SORTTITLELRFIELDID":
						vsk.setRtdLrFieldId(Integer.parseInt(text));
				default:
					break;
			}
		}

	}
}
