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
import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LogicalRecord;
import org.xml.sax.Attributes;

/**
 * This class will parse a LR-Index Record element into a
 * LRIndexTransfer object.
 */
public class LRIndexRecordParser extends BaseParser {

	private int effStartFld;
	private int effEndFld;
	private int currentLrId;
	private int seqNumber;
	private int fieldID;
	private String ndxName;

	public LRIndexRecordParser() {
		sectionName = "Index";
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		switch (qName.toUpperCase()) {
			default:
				break;
		}
	}		

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) {
		switch (name.toUpperCase()) {
			case "NAME":
				ndxName = text;
				break;
			case "XID":
				break;
			case "INDEXFIELDREF":
				fieldID = Integer.parseInt(attributes.get("ID"));
				seqNumber = Integer.parseInt(attributes.get("seq"));
				LRIndex xndx = new LRIndex();
				xndx.setComponentId(componentID);
				xndx.setFieldID(fieldID);
				xndx.setEffectiveDateEnd(false);
				xndx.setEffectiveDateStart(false);
				xndx.setLrId(currentLrId);
				xndx.setKeyNumber((short)seqNumber);
				xndx.setName(ndxName);
				//Repository.addLRIndex(pndx);
				LogicalRecord ndxLR = Repository.getLogicalRecords().get(currentLrId);
				ndxLR.addToIndexBySeq(xndx);
				break;
			case "EFFDATESTARTFLDID":
				effStartFld = Integer.parseInt(text);
				if (effStartFld > 0) {
					LRIndex ndx = new LRIndex();
					ndx.setComponentId(componentID);
					ndx.setFieldID(effStartFld);
					ndx.setLrId(currentLrId);
					ndx.setEffectiveDateEnd(false);
					ndx.setEffectiveDateStart(true);
					ndx.setName("Start Eff Date");
					effdateStarts.put(componentID, ndx);
				}
				break;
			case "EFFDATEENDFLDID":
				effEndFld = Integer.parseInt(text);
				if (effEndFld > 0) {
					LRIndex ndx = new LRIndex();
					ndx.setComponentId(componentID);
					ndx.setFieldID(effEndFld);
					ndx.setLrId(currentLrId);
					ndx.setEffectiveDateEnd(true);
					ndx.setEffectiveDateStart(false);
					ndx.setName("End Eff Date");
					effdateEnds.put(componentID, ndx);
				}
				break;
			default:
				break;
		}
	}

	public void setCurrentLrId(int currentLrId) {
		this.currentLrId = currentLrId;
	}

	public void setSeqNumber(int seqNumber) {
		this.seqNumber = seqNumber;
	}

}
