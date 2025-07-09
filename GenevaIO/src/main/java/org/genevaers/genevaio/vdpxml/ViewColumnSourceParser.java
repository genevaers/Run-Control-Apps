package org.genevaers.genevaio.vdpxml;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;

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

import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.ColumnSourceType;
import org.xml.sax.Attributes;

public class ViewColumnSourceParser extends BaseParser {

	private ViewColumnSource vcs;
	private int colID;
	private ViewNode viewNode;
		private ViewSource viewSource;
	
		public ViewColumnSourceParser() {
			sectionName = "ColumnAssignments";
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
				case "COLUMNASSIGNMENT":
					componentID = Integer.parseInt(attributes.get("ID"));
					break;
				case "COLUMNREF":
					vcs = new ViewColumnSource();
					colID = Integer.parseInt(attributes.get("ID"));
					vcs.setComponentId(componentID);
					vcs.setColumnID(colID);
					vcs.setViewId(viewNode.getID());
					vcs.setViewSourceId(viewSource.getComponentId());
					viewNode.addViewColumnSource(vcs);
					break;
				case "SOURCETYPE":
					vcs.setSourceType(ColumnSourceType.values()[Integer.parseInt(text.trim())]);
					break;
				case "FIELDREF":
					vcs.setViewSrcLrFieldId(Integer.parseInt(attributes.get("ID")));
					break;
				case "LOOKUPREF":
					vcs.setSrcJoinId((Integer.parseInt(attributes.get("ID"))));
					break;
				case "VALUE":
					vcs.setValueLength(text.length());
					vcs.setSrcValue(text);
					break;
				case "TITLEFIELDREF":
					vcs.setSortTitleFieldId(Integer.parseInt(attributes.get("ID")));
					break;
				case "TITLELOOKUPREF":
					vcs.setSortTitleLookupId(Integer.parseInt(attributes.get("ID")));
					break;
				case "LOGIC":
					// have to append by getting what is there first
					String logic = vcs.getLogicText();
					logic += text;
					vcs.setLogicText(logic);
					break;
				default:
					break;
			}
		}
	
		public int getColID() {
			return colID;
		}
	
		public ViewColumnSource getVcs() {
			return vcs;
		}
	
		public void setViewNode(ViewNode viewNode) {
			this.viewNode = viewNode;
		}
	
		public void setViewSource(ViewSource vs) {
			this.viewSource = vs;
	}
}
