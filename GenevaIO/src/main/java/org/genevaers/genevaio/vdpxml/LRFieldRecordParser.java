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

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;

/**
 * This class will parse a LRField Record element into a
 * LRFieldTransfer object.
 */
public class LRFieldRecordParser extends BaseParser {

	private LRField lrField;
	private int currentLrId;

	public LRFieldRecordParser() {
		sectionName = "Fields";
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) {
		switch (name.toUpperCase()) {
			case "FIELD":
			componentID = Integer.parseInt(attributes.get("ID"));
			break;
			case "NAME":
				lrField = new LRField();
				lrField.setComponentId(componentID);
				lrField.setLrID(currentLrId);
				lrField.setName(text.trim());
				lrField.setOrdinalPosition((short)1);
				lrField.setDateTimeFormat(DateCode.NONE);
				lrField.setStartPosition((short)1); //default since VDP XML will not supply
				Repository.addLRField(lrField);
				break;
			case "DATABASECOLUMN":
				lrField.setDbColName(text.trim());
				break;
			case "POSITION":
				short s = (short) Integer.parseInt(text);
				lrField.setStartPosition(s);
				break;
			case "ORDINAL":
				s = (short) Integer.parseInt(text);
				lrField.setOrdinalPosition(s);
				break;
			case "ORDINALOFFSET":
				lrField.setOrdinalOffset((short) Integer.parseInt(text));
			case "DATATYPE":
				lrField.setDatatype(DataType.fromdbcode(text.trim()));
				if (lrField.getDatatype() == DataType.ALPHANUMERIC) {
					lrField.setJustification(JustifyId.LEFT);
				} else {
					lrField.setJustification(JustifyId.RIGHT);
				}
				break;
			case "SIGNEDDATA":
				lrField.setSigned(text.equals("1") ? true : false);
				break;
			case "LENGTH":
				lrField.setLength((short) Integer.parseInt(text));
				break;
			case "DECIMALPLACES":
				s = (short) Integer.parseInt(text);
				lrField.setNumDecimalPlaces(s);
				break;
			case "SCALEFACTOR":
				s = (short) Integer.parseInt(text);
				lrField.setRounding(s);
				break;
			case "DATEFORMAT":
				lrField.setDateTimeFormat(DateCode.fromdbcode(text));
				break;
			case "JUSTIFYCD":
				break;
			default:
				break;
		}
	}

	public void setCurrentLrId(int currentLrId) {
		this.currentLrId = currentLrId;
	}

}
