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
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.enums.LrStatus;

/**
 * This class will parse a LogicalRecord Record element into a
 * LogicalRecordTransfer object.
 */
public class LRRecordParser extends BaseParser {

	private LogicalRecord lr;
	private LRIndexRecordParser ip;
	private LRFieldRecordParser fp;
	
		public LRRecordParser() {
			sectionName = "LogicalRecords";
		}
	
		@Override
		public void addElement(String name, String text, Map<String, String> attributes)  throws XMLStreamException {
			switch (name.toUpperCase()) {
				case "LOGICALRECORD":
				componentID = Integer.parseInt(attributes.get("ID"));
				break;
				case "NAME":
					lr = new LogicalRecord();
					lr.setComponentId(componentID);
					componentName = text;
					lr.setName(componentName);
					Repository.addLogicalRecord(lr);
					lr.setLookupExitID(0);
					break;
				case "LRSTATUS":
					lr.setStatus(LrStatus.fromdbcode(text));
					break;
				case "LOOKUPEXITID":
					lr.setLookupExitID(Integer.parseInt(text));
					break;
				case "LOOKUPEXITSTARTUP":
					lr.setLookupExitParams(text);
					break;
				case "INDEX":
					logger.atFine().log("Index");
					ip  = new LRIndexRecordParser();
					int ndxid = Integer.parseInt(attributes.get("ID"));
					ip.setComponentID(ndxid);
					lr.setPrimaryKey(ndxid);
					ip.setCurrentLrId(lr.getComponentId());
					ip.parse(reader);
					logger.atFine().log("Index parsing completed for LR " + lr.getName());
				break;
				case "FIELDS":
					logger.atFine().log("Fields");
					fp  = new LRFieldRecordParser();
					fp.setCurrentLrId(lr.getComponentId());
					fp.parse(reader);
					logger.atFine().log("Fields parsing completed for LR " + lr.getName());
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
