package org.genevaers.genevaio.vdpxml;

import java.util.ArrayList;

import org.genevaers.repository.Repository;

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

import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;
import org.xml.sax.Attributes;

public class LookupSourceKeyParser extends BaseParser {
	private LookupPathKey lookupKey;
	private int lrlfAssocid;
	private int currentLookupId = 0;
	private LookupPath currenLookupPath;

	private int currentStepId;

	private LookupPathStep currentStep;

	private int seqNum;

	private int lookupStepId;
	private ArrayList<LookupPathKey> currentKeyList;
	private int lrfieldid;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		switch (qName.toUpperCase()) {
			case "FIELDREF":
				lookupKey.setFieldId(Integer.parseInt(attributes.getValue("ID")));
				break;
			default:
				break;
		}
	}		
	
	@Override
	public void addElement(String name, String text) {
		switch (name.toUpperCase()) {
			case "DATATYPE":
				lookupKey.setDatatype(DataType.fromdbcode(text.trim()));
				break;
			case "SIGNED":
				lookupKey.setSigned(text.equals("1") ? true : false);
				break;
			case "LENGTH":
				short s = (short) Integer.parseInt(text);
				lookupKey.setFieldLength(s);
				break;
			case "DECIMALCNT":
				s = (short) Integer.parseInt(text);
				lookupKey.setDecimalCount(s);
				break;
			case "FLDCONTENTCD":
				lookupKey.setDateTimeFormat(DateCode.fromdbcode(text));
				break;
			case "ROUNDING":
				s = (short) Integer.parseInt(text);
				lookupKey.setRounding(s);
				break;
			case "JUSTIFYCD":
				lookupKey.setJustification(JustifyId.fromdbcode(text));
				break;
			case "MASK":
				lookupKey.setMask(text);
				break;
			case "SYMBOLICNAME":
				lookupKey.setSymbolicName(text);
				break;
			case "SYMBOLICDEFAULT":
			case "VALUE":
			case "CONSTANT":
				lookupKey.setValue(text);
			default:
				break;
		}
	}

	public void setSequencNumber(int seq) {
		seqNum = seq;
	}

    public void setLookupID(int lkid) {
		currenLookupPath = Repository.getLookups().get(lkid);
		lookupKey = new LookupPathKey();
		lookupKey.setComponentId(lkid);
		lookupKey.setDateTimeFormat(DateCode.NONE);
		lookupKey.setJustification(JustifyId.NONE);
		lookupKey.setKeyNumber((short)seqNum);
		currentKeyList = new ArrayList<LookupPathKey>();
		currentStepId = componentID;
//		lookupStepKeys.put(componentID, currentKeyList);
		currentKeyList.add(lookupKey);
	}

	public void setFieldId(int int1) {
	}
}
