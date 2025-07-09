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
import org.genevaers.repository.components.ControlRecord;
import org.xml.sax.Attributes;

public class CRRecordParser extends BaseParser {

	private ControlRecord cr;

	public CRRecordParser() {
		sectionName = "ControlRecords";
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) {
		switch (name.toUpperCase()) {
			case "CONTROLRECORD":
				componentID = Integer.parseInt(attributes.get("ID"));
				break;
			case "NAME":
				cr = new ControlRecord();
				cr.setComponentId(componentID);
				cr.setName(text);
				Repository.getControlRecords().add(cr, componentID, text);
				break;
			case "MONTHFIRSTPERIOD":
				short val = (short) Integer.parseInt(text);
				cr.setFirstFiscalMonth(val);
				break;
			case "MINPERIOD":
				val = (short) Integer.parseInt(text);
				cr.setBeginningPeriod(val);
				break;
			case "MAXPERIOD":
				val = (short) Integer.parseInt(text);
				cr.setEndingPeriod(val);
				break;
			case "COMMENTS":
				cr.setDescription(text);
				break;
			case "CREATEDTIMESTAMP":
				created = text;
				break;
			case "MODIFIEDTIMESTAMP":
				lastMod = text;
				break;
			default:
				break;
		}
	}
}
