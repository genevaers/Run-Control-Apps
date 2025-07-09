package org.genevaers.genevaio.vdpxml;

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

public class ViewExtractParser extends BaseParser {

	private ViewNode viewNode;

	public ViewExtractParser() {
		sectionName = "Extract";
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) throws XMLStreamException {
		switch (name.toUpperCase()) {
			case "FILENUMBER":
				short efn = (short) Integer.parseInt(text.trim());
				viewNode.getViewDefinition().setExtractFileNumber(efn);
				break;
			case "ENABLEAGGREGATION":
				viewNode.getViewDefinition().setExtractSummarized(text.equals("0") ? false : true);
				break;
			case "AGGREGATIONRECORDS":
				viewNode.getViewDefinition().setMaxExtractSummaryRecords(Integer.parseInt(text.trim()));
				break;

			case "EXTRACTCOLUMNS":
				logger.atFine().log("Parsing Extract Columns");
				ViewColumnRecordParser vcp = new ViewColumnRecordParser();
				vcp.setViewNode(viewNode);
				vcp.parse(reader);
				logger.atFine().log("Parsing View Columns completed");
				break;
			default:
				break;
		}
	}

	public void setViewNode(ViewNode vn) {
		this.viewNode = vn;
	}

}
