package org.genevaers.genevaio.wbxml;

import java.util.ArrayList;

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

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;

public class LookupSourceKeyRecordParser extends RecordParser {
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
	public void parseRecord(XMLStreamReader reader) throws XMLStreamException {
		String part = reader.getName().getLocalPart();
		int n = reader.next();
		if (n == XMLEvent.CHARACTERS) {
			String text = reader.getText();
			switch (part) {
				case "LOOKUPSTEPID":
					lookupStepId = Integer.parseInt(text);
					lrfieldid = 0;
					lrlfAssocid = 0;
					break;
				// TODO backfill this?
				// lookupKey.setStepNumber(stepNumber);
				case "KEYSEQNBR":
					seqNum = Integer.parseInt(text);
					break;
				// This will be the type of key
				// field/const/sym - not sure we use that
				// parseField("FLDTYPE", record);
				// trans.setSourceFieldType(intToEnum(fieldToInteger("FLDTYPE",fieldValue)));
				case "LRFIELDID":
					lrfieldid = Integer.parseInt(text);
					break;
				case "LRLFASSOCID":
					lrlfAssocid = Integer.parseInt(text);
					LKStepKeyAssocs assocs = RecordParserData.lookupKeyDestAssocs.computeIfAbsent(lookupStepId, k -> new LKStepKeyAssocs());
					assocs.add(seqNum, lrlfAssocid);
					break;
				// lookupKey.set
				case "LOOKUPID":
					int lkid = Integer.parseInt(text);
					//Ah there be dragons here
					//The order of the WB XML has the lookup steps
					//After the source keys
					//so if we lookup the current step it won't be there
					//We need to make the lookup keys and juggle them later
					//keep them in a base level map
					//keyed by lookup and then step?
					//or we make the step now...
					//but fill it in later?
					lookupKey = new LookupPathKey();
					lookupKey.setComponentId(lkid);
					lookupKey.setDateTimeFormat(DateCode.NONE);
					lookupKey.setJustification(JustifyId.NONE);
					lookupKey.setFieldId(lrfieldid);
					lookupKey.setKeyNumber((short)seqNum);
					lookupKey.setOrdinalPosition((short)seqNum);
					if(lookupStepId != currentStepId) {
						currentKeyList = new ArrayList<LookupPathKey>();
						currentStepId = lookupStepId;
						lookupStepKeys.put(lookupStepId, currentKeyList);
					}
					currentKeyList.add(lookupKey);
					break;

				// fieldValue = parseField(, record);
				// trans.setSourceJoinId(fieldToInteger("LOOKUPID", fieldValue));
				case "VALUEFMTCD":
					lookupKey.setDatatype(DataType.fromdbcode(text.trim()));
					break;
				case "SIGNED":
					lookupKey.setSigned(text.equals("1") ? true : false);
					break;
				case "VALUELEN":
					short s = (short) Integer.parseInt(text);
					lookupKey.setFieldLength(s);
					if(lrfieldid == 0) {
						lookupKey.setValueLength(s);
					}
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
				case "VALUE":
					lookupKey.setValue(text);
					lookupKey.setValueLength(text.length());
				default:
					break;
			}
		}
	}
}
