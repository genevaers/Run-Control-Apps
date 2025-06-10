package org.genevaers.genevaio.vdpxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

	private LookupStepParser stepParser;

	public LookupRecordParser() {
		sectionName = "Lookups";
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) throws XMLStreamException {
		switch (name.toUpperCase()) {
			case "LOOKUP":
			componentID = Integer.parseInt(attributes.get("ID"));
			break;
			case "NAME":
				lookup = new LookupPath();
				lookup.setID(componentID);
				lookup.setStatus(1);
				componentName = text;
				lookup.setName(componentName);
				Repository.getLookups().add(lookup, componentID, text);
				break;
			case "STEPS":
				logger.atFine().log("Steps");
				LookupStepParser lksp = new LookupStepParser();
				lksp.setLookupID(componentID);
				lksp.setLookup(lookup);
				lksp.parse(reader);
				logger.atFine().log("Steps parsing completed for LR " + lookup.getName());
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
