package org.genevaers.genevaio.vdpxml;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.genevaers.repository.Repository;

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
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.ColumnSourceType;
import org.xml.sax.Attributes;

public class ViewColumnSourceParser extends BaseParser {

	private ViewColumnSource vcs;
	private int viewID;
	private int viewSourceID;
	private int colID;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		switch (qName.toUpperCase()) {
			case "COLUMNREF":
				vcs = new ViewColumnSource();
				vcs.setComponentId(componentID);
				colID = Integer.parseInt(attributes.getValue("ID"));
				vcs.setColumnID(colID);
				vcs.setViewId(viewID);
				vcs.setViewSourceId(viewSourceID);
				Repository.getViews().get(viewID).addViewColumnSource(vcs);
				break;
			case "FIELDREF":
				vcs.setViewSrcLrFieldId(Integer.parseInt(attributes.getValue("ID")));
				break;
			case "LOOKUPREF":
				vcs.setSrcJoinId((Integer.parseInt(attributes.getValue("ID"))));
				break;
			default:
				break;
		}
	}		
	
	@Override
	public void addElement(String name, String text) {
		switch (name.toUpperCase()) {
			case "LOGIC":
				//have to append by getting what is there first
				String logic = vcs.getLogicText();
				logic += text;
				vcs.setLogicText(logic);
				break;
			case "VIEWSOURCEID":
				vcs.setViewSourceId(Integer.parseInt(text));
				break;
			case "VIEWCOLUMNID":
				vcs.setColumnID(Integer.parseInt(text));
				break;
			case "SOURCETYPE":
				vcs.setSourceType(ColumnSourceType.values()[Integer.parseInt(text.trim())]);
				break;
			case "VALUE":
				vcs.setSrcValue(text);
				vcs.setValueLength(text.length());
				break;
			case "LOOKUPID":
				vcs.setSrcJoinId(Integer.parseInt(text));
				break;
			case "LRFIELDID":
				vcs.setViewSrcLrFieldId(Integer.parseInt(text));
				break;
			case "EXTRACTCALCLOGIC":
				vcs.setLogicText(removeBRLineEndings(text));
				break;
			case "SORTTITLELOOKUPID":
				vcs.setSortTitleLookupId(Integer.parseInt(text));
				break;
			case "SORTTITLELRFIELDID":
				vcs.setSortTitleFieldId(Integer.parseInt(text));
				break;
			default:
				break;
		}
	}
	public void setViewId(int currentViewID) {
		viewID = currentViewID;
	}
	public void setViewSourceId(int vsid) {
		viewSourceID = vsid;
	}

	public int getColID() {
		return colID;
	}

	public ViewColumnSource getVcs() {
		return vcs;
	}
}
