package org.genevaers.genevaio.vdpxml;

import java.util.ArrayList;
import java.util.Iterator;

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
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;
import org.xml.sax.Attributes;

/**
 * This class will parse a Join Record element into a
 * LookupPathTransfer object.
 */
public class LookupRecordParser extends BaseParser {

	private LookupPath lookup;
	private boolean source = true;
	private int stepNumber;
	private LookupPathStep lookupStep;
	private int seqNum;
	private LookupPathKey lookupKey;
	private int targLrId;
	private LogicalRecord targetLR;
	private int srcLrId;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		switch (qName.toUpperCase()) {
			case "STEP":
				stepNumber = Integer.parseInt(attributes.getValue("Number"));
				source = true;
				break;
			case "SOURCE":
				source = true;
				break;
			case "LOGICALRECORDREF":
				if(source) {
					lookupStep = new LookupPathStep();
					lookupStep.setStepNum(stepNumber);
					srcLrId = Integer.parseInt(attributes.getValue("ID"));
					lookupStep.setSourceLRid(srcLrId);
					lookup.addStep(lookupStep);
				} else {
					targLrId = Integer.parseInt(attributes.getValue("ID"));
					lookupStep.setTargetLRid(targLrId);
					lookup.setTargetLRid(targLrId);
				}
				break;
			case "KEYFIELD":
				seqNum = Integer.parseInt(attributes.getValue("seq"));
				lookupKey = new LookupPathKey();
				lookupKey.setStepNumber(stepNumber);
				lookupKey.setComponentId(lookup.getID());
				lookupKey.setDateTimeFormat(DateCode.NONE);
				lookupKey.setJustification(JustifyId.LEFT);
				lookupKey.setKeyNumber((short)seqNum);
				lookupStep.addKey(lookupKey);
				break;
			case "FIELDREF":
				lookupKey.setFieldId(Integer.parseInt(attributes.getValue("ID")));
				break;
			case "LOGICALFILEREF":
				if(source == false) {
					int lfid = Integer.parseInt(attributes.getValue("ID"));
					lookupStep.setTargetLFid(lfid);
					Iterator<LookupPathKey> ki = lookupStep.getKeyIterator();
					while (ki.hasNext()) {
						LookupPathKey k = ki.next();
						k.setTargetlfid(lfid);
						k.setTargetLrId(targLrId);
						k.setSourceLrId(srcLrId);
					}
				}
				break;
			case "TARGET":
				source = false;
				break;
			case "EXITREF":
				targetLR = Repository.getLogicalRecords().get(targLrId);
				targetLR.setLookupExitID(Integer.parseInt(attributes.getValue("ID")));
				break;
			default:
				break;
		}
	}		

	@Override
	public void addElement(String name, String text) {
		switch (name.toUpperCase()) {
			case "NAME":
				lookup = new LookupPath();
				lookup.setID(componentID);
				componentName = text;
				lookup.setName(componentName);
				Repository.getLookups().add(lookup, componentID, text);
				break;
			case "DATATYPE":
				lookupKey.setDatatype(DataType.fromdbcode(text.trim()));
				break;
			case "SIGNEDDATA":
				lookupKey.setSigned(text.equals("1") ? true : false);
				break;
			case "LENGTH":
				short s = (short) Integer.parseInt(text);
				lookupKey.setFieldLength(s);
				if(lookupKey.getFieldId() == 0) {
					lookupKey.setValueLength(s);
				}
				break;
			case "DECIMALPLACES":
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
				break;
			case "PARAMETER":
				targetLR.setLookupExitParams(text);
				break;
			case "CREATEDTIMESTAMP":
				created = text;
				break;
			case "LASTMODTIMESTAMP":
				lastMod = text;
				break;
			default:
				break;
		}
	}

}
