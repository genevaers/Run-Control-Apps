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
import org.genevaers.repository.components.LookupPath;

/**
 * This class will parse a Join Record element into a
 * LookupPathTransfer object.
 */
public class LookupRecordParser extends RecordParser {

	private LookupPath lookup;

	@Override
	public void parseRecord(XMLStreamReader reader) throws XMLStreamException {
		String part = reader.getName().getLocalPart();
		int n = reader.next();
		if (n == XMLEvent.CHARACTERS) {
			String text = reader.getText();
			switch (part) {
				case "LOOKUPID":
					lookup = new LookupPath();
					componentID = Integer.parseInt(text);
					lookup.setID(componentID);
					lookup.setStatus(1);
					break;
				case "NAME":
					componentName = text;
					lookup.setName(componentName);
					Repository.getLookups().add(lookup, componentID, text);
					break;
				case "DESTLRLFASSOCID":
					lookup.setDestLrLfid(Integer.parseInt(text));
					break;
				// TODO we should probably do something if not valid
				// Can this happen?
				// fieldValue = parseField("VALIDIND", record);
				// trans.setValidInd(DataUtilities.intToBoolean(fieldToInteger("VALIDIND",fieldValue)));
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
}
