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
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.PhysicalFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class will parse a LogicalFile Record element into a
 * LogicalFileTransfer object.
 */
public class LogicalFileRecordParser extends BaseParser {

	private LogicalFile lf;
	private PhysicalFile pf;

	public LogicalFileRecordParser() {
		sectionName = "LogicalFiles";
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		// switch (qName.toUpperCase()) {
		// 	case "PARTITIONREF":
		// 		logger.atFine().log("Logical Files");
		// 		String a0 = attributes.get("seq");
		// 		int pfid = Integer.parseInt(attributes.get("ID"));
		// 		pf = Repository.getPhysicalFiles().get(pfid);
		// 		lf.addPF(pf);
		// 		pf.setLogicalFilename(lf.getName());
		// 		break;
		// 	default:
		// 		break;
		// }
	}		

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) {
		switch (name.toUpperCase()) {
			case "LOGICALFILE":
				componentID = Integer.parseInt(attributes.get("ID"));
				break;
			case "NAME":
				lf = new LogicalFile();
				lf.setID(componentID);
				lf.setName(text);
				Repository.getLogicalFiles().add(lf, componentID, text);
				break;
			case "PARTITIONREF":
				logger.atFine().log("Logical Files");
				String a0 = attributes.get("seq");
				int pfid = Integer.parseInt(attributes.get("ID"));
				pf = Repository.getPhysicalFiles().get(pfid);
				lf.addPF(pf);
				pf.setLogicalFilename(lf.getName());
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
