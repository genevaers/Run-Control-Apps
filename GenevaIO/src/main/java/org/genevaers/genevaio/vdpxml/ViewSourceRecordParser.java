package org.genevaers.genevaio.vdpxml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

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

import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.xml.sax.Attributes;

public class ViewSourceRecordParser extends BaseParser {

	private ViewSource vs;

	private int sequenceNumber;
	private ViewNode viewNode;

	public ViewSourceRecordParser() {
		sectionName = "DataSources";
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) throws XMLStreamException {
		switch (name.toUpperCase()) {
			case "DATASOURCE":
				sequenceNumber = Integer.parseInt(attributes.get("seq"));
				componentID = Integer.parseInt(attributes.get("ID"));
				break;
			case "LOGICALFILEREF":
				vs = new ViewSource();
				vs.setComponentId(componentID);
				vs.setSourceLFID(Integer.parseInt(attributes.get("ID")));
				vs.setViewId(viewNode.getID());
				vs.setSequenceNumber((short) sequenceNumber);
				viewNode.addViewSource(vs);
				break;
			case "LOGICALRECORDREF":
				vs.setSourceLRID(Integer.parseInt(attributes.get("ID")));
				break;
			case "FILTER":
				vs.setExtractFilter(vs.getExtractFilter() + text);
				vs.setSequenceNumber((short) sequenceNumber);
				break;
			case "COLUMNASSIGNMENTS":
				logger.atFine().log("Parsing View Column Sources");
				ViewColumnSourceParser vsp = new ViewColumnSourceParser();
				vsp.setViewNode(viewNode);
				vsp.setViewSource(vs);
				vsp.parse(reader);
				logger.atFine().log("Parsing View Column Sources completed");
				break;
			default:
				break;
		}
	}

	public void setViewNode(ViewNode vn) {
		this.viewNode = vn;
	}
}
