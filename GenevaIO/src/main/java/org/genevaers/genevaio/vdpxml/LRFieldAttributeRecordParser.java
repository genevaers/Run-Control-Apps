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
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;
import org.xml.sax.Attributes;

public class LRFieldAttributeRecordParser extends BaseParser {

	private LRField field;

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) {
		switch (name) {
			case "LRFIELDID":
				field = Repository.getFields().get(Integer.parseInt(text));
				field.setDateTimeFormat(DateCode.NONE);
				break;
			case "FLDFMTCD":
				field.setDatatype(DataType.fromdbcode(text.trim()));
				if (field.getDatatype() == DataType.ALPHANUMERIC) {
					field.setJustification(JustifyId.LEFT);
				} else {
					field.setJustification(JustifyId.RIGHT);
				}
				break;
			case "SIGNEDIND":
				field.setSigned(text.equals("1") ? true : false);
				break;
			case "MAXLEN":
				short s = (short) Integer.parseInt(text);
				field.setLength(s);
				break;
			case "DECIMALCNT":
				s = (short) Integer.parseInt(text);
				field.setNumDecimalPlaces(s);
				break;
			case "ROUNDING":
				s = (short) Integer.parseInt(text);
				field.setRounding(s);
				break;
			case "FLDCONTENTCD":
				field.setDateTimeFormat(DateCode.fromdbcode(text));
				break;
			case "JUSTIFYCD":
				break;
			case "INPUTMASK":
				field.setMask(text);
				break;
			default:
				break;
		}
	}
}
