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
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.SubtotalType;

public class ViewColumnRecordParser extends RecordParser {

	private ViewColumn vc;

	private int currentViewId = 0;

	private ViewNode currentViewNode;

	@Override
	public void parseRecord(XMLStreamReader reader) throws XMLStreamException {
		String part = reader.getName().getLocalPart();
		int n = reader.next();
		if (n == XMLEvent.CHARACTERS) {
			String text = reader.getText();
			switch (part) {
				case "VIEWCOLUMNID":
					vc = new ViewColumn();
					vc.setComponentId(Integer.parseInt(text.trim()));
					vc.setName("");
					vc.setFieldName("");
					vc.setDetailPrefix("");
					vc.setSubtotalMask("");
					vc.setDateCode(DateCode.NONE);
					vc.setSubtotalType(SubtotalType.NONE);
					vc.setHeaderJustifyId(JustifyId.CENTER);
					break;
				case "VIEWID":
					int viewId = Integer.parseInt(text.trim());
					if (viewId != currentViewId) {
						currentViewNode = Repository.getViews().get(viewId);
						currentViewId = viewId;
					}
					vc.setViewId(viewId);
					break;
				case "COLUMNNUMBER":
					vc.setColumnNumber(Integer.parseInt(text.trim()));
					if(Repository.isViewEnabled(currentViewId)) {
						currentViewNode.addViewColumn(vc);
					}
					vc.setName("Column Number " + text.trim());
					break;
				case "FLDFMTCD":
					vc.setDataType(DataType.fromdbcode(text.trim()));
					if(vc.getDataType() == DataType.ALPHANUMERIC) {
						vc.setJustifyId(JustifyId.LEFT);
					} else {
						vc.setJustifyId(JustifyId.RIGHT);
					}
					break;
				case "SIGNEDIND":
					vc.setSigned(text.equals("1") ? true : false);
					break;
				case "STARTPOSITION":
					short s = (short) Integer.parseInt(text.trim());
					vc.setStartPosition(s);
					break;
				case "MAXLEN":
					s = (short) Integer.parseInt(text.trim());
					vc.setFieldLength(s);
					break;
				case "ORDINALPOSITION":
					s = (short) Integer.parseInt(text.trim());
					vc.setOrdinalPosition(s);
					break;
				case "DECIMALCNT":
					s = (short) Integer.parseInt(text.trim());
					vc.setDecimalCount(s);
					break;
				case "ROUNDING":
					s = (short) Integer.parseInt(text);
					vc.setRounding(s);
					break;
				case "FLDCONTENTCD":
					vc.setDateCode(DateCode.fromdbcode(text.trim()));
					break;
				case "JUSTIFYCD":
					vc.setJustifyId(JustifyId.fromdbcode(text.trim()));
					break;
				case "DEFAULTVAL":
					vc.setDefaultValue(""); //not used
					break;
				case "VISIBLE":
					vc.setHidden(text.equals("1") ? false : true);
					break;
				case "SUBTOTALTYPECD":
					vc.setSubtotalType(SubtotalType.fromdbcode(text.trim()));
					break;
				case "SPACESBEFORECOLUMN":
					s = (short) Integer.parseInt(text.trim());
					vc.setSpacesBeforeColumn(s);
					break;
				case "EXTRACTAREACD":
					vc.setExtractArea(ExtractArea.fromdbcode(text.trim()));
					break;
				case "EXTRAREAPOSITION":
					s = (short) Integer.parseInt(text);
					vc.setExtractAreaPosition(s);
					break;
				case "SUBTLABEL":
					vc.setSubtotalPrefix(text);
					break;
				case "RPTMASK":
					vc.setReportMask(text);
					break;
				case "HDRJUSTIFYCD":
					vc.setHeaderJustifyId(JustifyId.fromdbcode(text));
					break;
				case "HDRLINE1":
					vc.setHeaderLine1(text);
					break;
				case "HDRLINE2":
					vc.setHeaderLine2(text);
					break;
				case "HDRLINE3":
					vc.setHeaderLine3(text);
					break;
				case "FORMATCALCLOGIC":
					vc.setColumnCalculation(removeBRLineEndings(text));
					break;
				default:
					break;
			}
		}
	}
}
